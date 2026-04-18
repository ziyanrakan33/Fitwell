package fitwell.control;

import fitwell.db.Db;
import fitwell.integration.ImageExtractionService;
import fitwell.integration.NotificationGateway;
import fitwell.integration.SwiftFitGateway;
import fitwell.repo.AttendanceRepository;
import fitwell.repo.ClassEquipmentAssignmentRepository;
import fitwell.repo.EmergencyAlertRepository;
import fitwell.repo.EquipmentAssignmentRepository;
import fitwell.repo.EquipmentInspectionRepository;
import fitwell.repo.EquipmentRepository;
import fitwell.repo.PlanClassRepository;
import fitwell.repo.PlanMemberRepository;
import fitwell.repo.RegistrationRepository;
import fitwell.repo.TraineeRepository;
import fitwell.repo.TrainingClassRepository;
import fitwell.repo.TrainingClassRuntimeStateRepository;
import fitwell.repo.TrainingPlanRepository;

public class FitWellServiceRegistry {
    private static final FitWellServiceRegistry INSTANCE = new FitWellServiceRegistry();

    private final TrainingClassRepository trainingClassRepository = new TrainingClassRepository();
    private final TrainingClassRuntimeStateRepository trainingClassRuntimeStateRepository = new TrainingClassRuntimeStateRepository();
    private final RegistrationRepository registrationRepository = new RegistrationRepository();
    private final TraineeRepository traineeRepository = new TraineeRepository();
    private final EquipmentRepository equipmentRepository = new EquipmentRepository();
    private final TrainingPlanRepository trainingPlanRepository = new TrainingPlanRepository();
    private final PlanMemberRepository planMemberRepository = new PlanMemberRepository();
    private final AttendanceRepository attendanceRepository = new AttendanceRepository();
    private final EmergencyAlertRepository emergencyAlertRepository = new EmergencyAlertRepository();
    private final EquipmentInspectionRepository equipmentInspectionRepository = new EquipmentInspectionRepository();
    private final EquipmentAssignmentRepository equipmentAssignmentRepository = new EquipmentAssignmentRepository(() -> {
        try {
            return Db.getConnection();
        } catch (Exception ex) {
            throw new java.sql.SQLException(ex);
        }
    });
    private final ClassEquipmentAssignmentRepository classEquipmentAssignmentRepository = new ClassEquipmentAssignmentRepository();
    private final PlanClassRepository planClassRepository = new PlanClassRepository();

    private final NotificationGateway notificationGateway = new NotificationGateway();
    private final ImageExtractionService imageExtractionService = new ImageExtractionService();
    private final SwiftFitGateway swiftFitGateway = new SwiftFitGateway();

    private final TrainingClassService trainingClassService =
            new TrainingClassService(trainingClassRepository, trainingClassRuntimeStateRepository);
    private final TraineeProfileService traineeProfileService = new TraineeProfileService(traineeRepository);
    private final TrainingPlanService trainingPlanService = createTrainingPlanService();

    private TrainingPlanService createTrainingPlanService() {
        TrainingPlanService svc = new TrainingPlanService(trainingPlanRepository, planMemberRepository);
        svc.setPlanClassRepository(planClassRepository);
        return svc;
    }
    private final AttendanceService attendanceService =
            new AttendanceService(registrationRepository, attendanceRepository, traineeRepository, trainingClassService);
    private final EmergencyAlertService emergencyAlertService = createEmergencyAlertService();

    private EmergencyAlertService createEmergencyAlertService() {
        EmergencyAlertService svc = new EmergencyAlertService(emergencyAlertRepository, trainingClassService, notificationGateway);
        svc.setRuntimeStateRepository(trainingClassRuntimeStateRepository);
        return svc;
    }
    private final TrainingClassQueryService trainingClassQueryService =
            new TrainingClassQueryService(trainingClassService, trainingPlanService, attendanceService, emergencyAlertService);
    private final EquipmentReviewService equipmentReviewService = createEquipmentReviewService();

    private EquipmentReviewService createEquipmentReviewService() {
        EquipmentReviewService svc = new EquipmentReviewService(equipmentRepository, notificationGateway);
        svc.setEquipmentInspectionRepository(equipmentInspectionRepository);
        return svc;
    }
    private final EquipmentImportService equipmentImportService =
            new EquipmentImportService(equipmentRepository, imageExtractionService, notificationGateway, swiftFitGateway);
    private final LowAttendanceReportService lowAttendanceReportService =
            new LowAttendanceReportService(attendanceService);
    private final InventoryReportController inventoryReportController =
            new InventoryReportController(equipmentRepository, classEquipmentAssignmentRepository, trainingClassService, swiftFitGateway);
    private final InspectionWorkflowService inspectionWorkflowService = createInspectionWorkflowService();

    private InspectionWorkflowService createInspectionWorkflowService() {
        InspectionWorkflowService svc = new InspectionWorkflowService(
                trainingClassService, equipmentAssignmentRepository,
                equipmentInspectionRepository, equipmentReviewService);
        svc.setNotificationGateway(notificationGateway);
        svc.setClassEquipmentAssignmentRepository(classEquipmentAssignmentRepository);
        return svc;
    }
    private final EquipmentAssignmentController equipmentAssignmentController = new EquipmentAssignmentController(
            equipmentAssignmentRepository,
            new EquipmentAvailabilityService(
                    equipmentAssignmentRepository,
                    classId -> {
                        var trainingClass = trainingClassService.findById(classId);
                        if (trainingClass == null) {
                            return null;
                        }
                        return new EquipmentAvailabilityService.ClassTime(
                                trainingClass.getClassId(),
                                trainingClass.getStartTime(),
                                trainingClass.getEndTime()
                        );
                    }),
            serial -> {
                var equipment = equipmentRepository.findBySerial(serial);
                if (equipment == null) {
                    return null;
                }
                EquipmentAssignmentController.EquipmentInfo info = new EquipmentAssignmentController.EquipmentInfo();
                info.serial = equipment.getSerialNumber();
                info.name = equipment.getName();
                info.qty = equipment.getQuantity();
                info.status = equipment.getStatus().name();
                return info;
            }
    );

    private FitWellServiceRegistry() {
    }

    public static FitWellServiceRegistry getInstance() {
        return INSTANCE;
    }

    private final EquipmentManagementController equipmentManagementController = new EquipmentManagementController(equipmentRepository, imageExtractionService, notificationGateway);
    public TrainingClassService trainingClassService() { return trainingClassService; }
    public TraineeProfileService traineeProfileService() { return traineeProfileService; }
    public TrainingPlanService trainingPlanService() { return trainingPlanService; }
    public AttendanceService attendanceService() { return attendanceService; }
    public EmergencyAlertService emergencyAlertService() { return emergencyAlertService; }
    public TrainingClassQueryService trainingClassQueryService() { return trainingClassQueryService; }
    public EquipmentReviewService equipmentReviewService() { return equipmentReviewService; }
    public EquipmentManagementController equipmentManagementController() { return equipmentManagementController; }
    public EquipmentImportService equipmentImportService() { return equipmentImportService; }
    public LowAttendanceReportService lowAttendanceReportService() { return lowAttendanceReportService; }
    public InventoryReportController inventoryReportController() { return inventoryReportController; }
    public InspectionWorkflowService inspectionWorkflowService() { return inspectionWorkflowService; }
    public EquipmentAssignmentController equipmentAssignmentController() { return equipmentAssignmentController; }
    public RegistrationRepository registrationRepository() { return registrationRepository; }
    public TraineeRepository traineeRepository() { return traineeRepository; }
    public TrainingClassRepository trainingClassRepository() { return trainingClassRepository; }
    public EquipmentRepository equipmentRepository() { return equipmentRepository; }
    public PlanClassRepository planClassRepository() { return planClassRepository; }
    public EquipmentInspectionRepository equipmentInspectionRepository() { return equipmentInspectionRepository; }
    public ClassEquipmentAssignmentRepository classEquipmentAssignmentRepository() { return classEquipmentAssignmentRepository; }
}
