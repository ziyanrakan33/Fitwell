package fitwell.control;

import fitwell.persistence.db.Db;
import fitwell.integration.ImageExtractionService;
import fitwell.integration.NotificationGateway;
import fitwell.integration.SwiftFitGateway;
import fitwell.persistence.api.AttendanceRepository;
import fitwell.persistence.api.ClassEquipmentAssignmentRepository;
import fitwell.persistence.api.EmergencyAlertRepository;
import fitwell.persistence.api.EquipmentAssignmentRepository;
import fitwell.persistence.api.EquipmentInspectionRepository;
import fitwell.persistence.api.EquipmentRepository;
import fitwell.persistence.api.PlanClassRepository;
import fitwell.persistence.api.PlanMemberRepository;
import fitwell.persistence.api.RegistrationRepository;
import fitwell.persistence.api.TraineeRepository;
import fitwell.persistence.api.TrainingClassRepository;
import fitwell.persistence.api.TrainingClassRuntimeStateRepository;
import fitwell.persistence.api.TrainingPlanRepository;
import fitwell.persistence.jdbc.InMemoryAttendanceRepository;
import fitwell.persistence.jdbc.InMemoryEmergencyAlertRepository;
import fitwell.persistence.jdbc.InMemoryEquipmentInspectionRepository;
import fitwell.persistence.jdbc.InMemoryPlanClassRepository;
import fitwell.persistence.jdbc.InMemoryPlanMemberRepository;
import fitwell.persistence.jdbc.InMemoryTrainingClassRuntimeStateRepository;
import fitwell.persistence.jdbc.JdbcClassEquipmentAssignmentRepository;
import fitwell.persistence.jdbc.JdbcEquipmentAssignmentRepository;
import fitwell.persistence.jdbc.JdbcEquipmentRepository;
import fitwell.persistence.jdbc.JdbcRegistrationRepository;
import fitwell.persistence.jdbc.JdbcTraineeRepository;
import fitwell.persistence.jdbc.JdbcTrainingClassRepository;
import fitwell.persistence.jdbc.JdbcTrainingPlanRepository;
import fitwell.service.auth.AuthenticationService;
import fitwell.service.attendance.AttendanceService;
import fitwell.service.attendance.LowAttendanceReportService;
import fitwell.service.emergency.EmergencyAlertService;
import fitwell.service.equipment.EquipmentAvailabilityService;
import fitwell.service.equipment.EquipmentImportService;
import fitwell.service.equipment.EquipmentReviewService;
import fitwell.service.equipment.InspectionWorkflowService;
import fitwell.service.training.TraineeProfileService;
import fitwell.service.training.TrainingClassQueryService;
import fitwell.service.training.TrainingClassService;
import fitwell.service.training.TrainingPlanService;
import fitwell.controller.EquipmentAssignmentController;
import fitwell.controller.EquipmentAvailabilityController;
import fitwell.controller.EquipmentManagementController;
import fitwell.controller.InventoryReportController;

public class FitWellServiceRegistry {
    private static final FitWellServiceRegistry INSTANCE = new FitWellServiceRegistry();

    private final TrainingClassRepository trainingClassRepository = new JdbcTrainingClassRepository();
    private final TrainingClassRuntimeStateRepository trainingClassRuntimeStateRepository = new InMemoryTrainingClassRuntimeStateRepository();
    private final RegistrationRepository registrationRepository = new JdbcRegistrationRepository();
    private final TraineeRepository traineeRepository = new JdbcTraineeRepository();
    private final EquipmentRepository equipmentRepository = new JdbcEquipmentRepository();
    private final TrainingPlanRepository trainingPlanRepository = new JdbcTrainingPlanRepository();
    private final PlanMemberRepository planMemberRepository = new InMemoryPlanMemberRepository();
    private final AttendanceRepository attendanceRepository = new InMemoryAttendanceRepository();
    private final EmergencyAlertRepository emergencyAlertRepository = new InMemoryEmergencyAlertRepository();
    private final EquipmentInspectionRepository equipmentInspectionRepository = new InMemoryEquipmentInspectionRepository();
    private final EquipmentAssignmentRepository equipmentAssignmentRepository = new JdbcEquipmentAssignmentRepository(() -> {
        try {
            return Db.getConnection();
        } catch (Exception ex) {
            throw new java.sql.SQLException(ex);
        }
    });
    private final ClassEquipmentAssignmentRepository classEquipmentAssignmentRepository = new JdbcClassEquipmentAssignmentRepository();
    private final PlanClassRepository planClassRepository = new InMemoryPlanClassRepository();

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
