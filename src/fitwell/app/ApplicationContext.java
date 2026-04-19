package fitwell.app;

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
import fitwell.controller.EquipmentManagementController;
import fitwell.controller.InventoryReportController;
import fitwell.controller.RegistrationController;

public class ApplicationContext {

    // repositories
    private final TrainingClassRepository trainingClassRepository;
    private final TrainingClassRuntimeStateRepository trainingClassRuntimeStateRepository;
    private final RegistrationRepository registrationRepository;
    private final TraineeRepository traineeRepository;
    private final EquipmentRepository equipmentRepository;
    private final TrainingPlanRepository trainingPlanRepository;
    private final PlanMemberRepository planMemberRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;
    private final EquipmentInspectionRepository equipmentInspectionRepository;
    private final EquipmentAssignmentRepository equipmentAssignmentRepository;
    private final ClassEquipmentAssignmentRepository classEquipmentAssignmentRepository;
    private final PlanClassRepository planClassRepository;

    // integrations
    private final NotificationGateway notificationGateway;
    private final ImageExtractionService imageExtractionService;
    private final SwiftFitGateway swiftFitGateway;

    // services
    private final AuthenticationService authenticationService;
    private final TrainingClassService trainingClassService;
    private final TraineeProfileService traineeProfileService;
    private final TrainingPlanService trainingPlanService;
    private final AttendanceService attendanceService;
    private final EmergencyAlertService emergencyAlertService;
    private final TrainingClassQueryService trainingClassQueryService;
    private final EquipmentReviewService equipmentReviewService;
    private final EquipmentImportService equipmentImportService;
    private final LowAttendanceReportService lowAttendanceReportService;
    private final InspectionWorkflowService inspectionWorkflowService;

    // controllers
    private final InventoryReportController inventoryReportController;
    private final EquipmentAssignmentController equipmentAssignmentController;
    private final EquipmentManagementController equipmentManagementController;
    private final RegistrationController registrationController;

    public ApplicationContext() {
        // persistence
        this.trainingClassRepository = new JdbcTrainingClassRepository();
        this.trainingClassRuntimeStateRepository = new InMemoryTrainingClassRuntimeStateRepository();
        this.registrationRepository = new JdbcRegistrationRepository();
        this.traineeRepository = new JdbcTraineeRepository();
        this.equipmentRepository = new JdbcEquipmentRepository();
        this.trainingPlanRepository = new JdbcTrainingPlanRepository();
        this.planMemberRepository = new InMemoryPlanMemberRepository();
        this.attendanceRepository = new InMemoryAttendanceRepository();
        this.emergencyAlertRepository = new InMemoryEmergencyAlertRepository();
        this.equipmentInspectionRepository = new InMemoryEquipmentInspectionRepository();
        this.equipmentAssignmentRepository = new JdbcEquipmentAssignmentRepository(() -> {
            try {
                return Db.getConnection();
            } catch (Exception ex) {
                throw new java.sql.SQLException(ex);
            }
        });
        this.classEquipmentAssignmentRepository = new JdbcClassEquipmentAssignmentRepository();
        this.planClassRepository = new InMemoryPlanClassRepository();

        // integrations
        this.notificationGateway = new NotificationGateway();
        this.imageExtractionService = new ImageExtractionService();
        this.swiftFitGateway = new SwiftFitGateway();

        // services
        this.authenticationService = new AuthenticationService();
        this.trainingClassService = new TrainingClassService(trainingClassRepository, trainingClassRuntimeStateRepository);
        this.traineeProfileService = new TraineeProfileService(traineeRepository, authenticationService);

        TrainingPlanService tps = new TrainingPlanService(trainingPlanRepository, planMemberRepository);
        tps.setPlanClassRepository(planClassRepository);
        this.trainingPlanService = tps;

        this.attendanceService = new AttendanceService(registrationRepository, attendanceRepository, traineeRepository, trainingClassService);

        EmergencyAlertService eas = new EmergencyAlertService(emergencyAlertRepository, trainingClassService, notificationGateway);
        eas.setRuntimeStateRepository(trainingClassRuntimeStateRepository);
        this.emergencyAlertService = eas;

        this.trainingClassQueryService = new TrainingClassQueryService(trainingClassService, trainingPlanService, attendanceService, emergencyAlertService);

        EquipmentReviewService ers = new EquipmentReviewService(equipmentRepository, notificationGateway);
        ers.setEquipmentInspectionRepository(equipmentInspectionRepository);
        this.equipmentReviewService = ers;

        this.equipmentImportService = new EquipmentImportService(equipmentRepository, imageExtractionService, notificationGateway, swiftFitGateway);
        this.lowAttendanceReportService = new LowAttendanceReportService(attendanceService);

        InspectionWorkflowService iws = new InspectionWorkflowService(
                trainingClassService, equipmentAssignmentRepository,
                equipmentInspectionRepository, equipmentReviewService);
        iws.setNotificationGateway(notificationGateway);
        iws.setClassEquipmentAssignmentRepository(classEquipmentAssignmentRepository);
        this.inspectionWorkflowService = iws;

        // controllers
        this.inventoryReportController = new InventoryReportController(
                equipmentRepository, classEquipmentAssignmentRepository, trainingClassService, swiftFitGateway);

        EquipmentAvailabilityService eavs = new EquipmentAvailabilityService(
                equipmentAssignmentRepository,
                classId -> {
                    var tc = trainingClassService.findById(classId);
                    if (tc == null) return null;
                    return new EquipmentAvailabilityService.ClassTime(tc.getClassId(), tc.getStartTime(), tc.getEndTime());
                });
        this.equipmentAssignmentController = new EquipmentAssignmentController(
                equipmentAssignmentRepository,
                eavs,
                serial -> {
                    var equipment = equipmentRepository.findBySerial(serial);
                    if (equipment == null) return null;
                    EquipmentAssignmentController.EquipmentInfo info = new EquipmentAssignmentController.EquipmentInfo();
                    info.serial = equipment.getSerialNumber();
                    info.name = equipment.getName();
                    info.qty = equipment.getQuantity();
                    info.status = equipment.getStatus().name();
                    return info;
                });

        this.equipmentManagementController = new EquipmentManagementController(equipmentRepository, imageExtractionService, notificationGateway);

        this.registrationController = new RegistrationController(registrationRepository, trainingClassService, emergencyAlertService);
    }

    // getters — repositories
    public TrainingClassRepository trainingClassRepository() { return trainingClassRepository; }
    public RegistrationRepository registrationRepository() { return registrationRepository; }
    public TraineeRepository traineeRepository() { return traineeRepository; }
    public EquipmentRepository equipmentRepository() { return equipmentRepository; }
    public PlanClassRepository planClassRepository() { return planClassRepository; }
    public EquipmentInspectionRepository equipmentInspectionRepository() { return equipmentInspectionRepository; }
    public ClassEquipmentAssignmentRepository classEquipmentAssignmentRepository() { return classEquipmentAssignmentRepository; }

    // getters — services
    public AuthenticationService authenticationService() { return authenticationService; }
    public TrainingClassService trainingClassService() { return trainingClassService; }
    public TraineeProfileService traineeProfileService() { return traineeProfileService; }
    public TrainingPlanService trainingPlanService() { return trainingPlanService; }
    public AttendanceService attendanceService() { return attendanceService; }
    public EmergencyAlertService emergencyAlertService() { return emergencyAlertService; }
    public TrainingClassQueryService trainingClassQueryService() { return trainingClassQueryService; }
    public EquipmentReviewService equipmentReviewService() { return equipmentReviewService; }
    public EquipmentImportService equipmentImportService() { return equipmentImportService; }
    public LowAttendanceReportService lowAttendanceReportService() { return lowAttendanceReportService; }
    public InspectionWorkflowService inspectionWorkflowService() { return inspectionWorkflowService; }

    // getters — controllers
    public InventoryReportController inventoryReportController() { return inventoryReportController; }
    public EquipmentAssignmentController equipmentAssignmentController() { return equipmentAssignmentController; }
    public EquipmentManagementController equipmentManagementController() { return equipmentManagementController; }
    public RegistrationController registrationController() { return registrationController; }
}
