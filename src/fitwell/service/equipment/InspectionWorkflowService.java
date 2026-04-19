package fitwell.service.equipment;
import fitwell.service.training.TrainingClassService;

import fitwell.domain.equipment.ClassEquipmentAssignment;
import fitwell.domain.equipment.Equipment;
import fitwell.domain.equipment.EquipmentAssignment;
import fitwell.domain.equipment.EquipmentInspection;
import fitwell.domain.equipment.InspectionSeverity;
import fitwell.domain.training.TrainingClass;
import fitwell.domain.training.TrainingClassStatus;
import fitwell.integration.NotificationGateway;
import fitwell.persistence.api.ClassEquipmentAssignmentRepository;
import fitwell.persistence.api.EquipmentAssignmentRepository;
import fitwell.persistence.api.EquipmentInspectionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class InspectionWorkflowService {
    private final TrainingClassService trainingClassService;
    private final EquipmentAssignmentRepository equipmentAssignmentRepository;
    private final EquipmentInspectionRepository equipmentInspectionRepository;
    private final EquipmentReviewService equipmentReviewService;
    private NotificationGateway notificationGateway;
    private ClassEquipmentAssignmentRepository classEquipmentAssignmentRepository;

    public InspectionWorkflowService(TrainingClassService trainingClassService,
                                     EquipmentAssignmentRepository equipmentAssignmentRepository,
                                     EquipmentInspectionRepository equipmentInspectionRepository,
                                     EquipmentReviewService equipmentReviewService) {
        this.trainingClassService = trainingClassService;
        this.equipmentAssignmentRepository = equipmentAssignmentRepository;
        this.equipmentInspectionRepository = equipmentInspectionRepository;
        this.equipmentReviewService = equipmentReviewService;
    }

    public void setNotificationGateway(NotificationGateway notificationGateway) {
        this.notificationGateway = notificationGateway;
    }

    public void setClassEquipmentAssignmentRepository(ClassEquipmentAssignmentRepository repo) {
        this.classEquipmentAssignmentRepository = repo;
    }

    public List<TrainingClass> getClassesDueForInspection() {
        List<TrainingClass> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(2);
        for (TrainingClass trainingClass : trainingClassService.getAllClasses()) {
            if (trainingClass.getStartTime() == null || trainingClass.getStatus() == TrainingClassStatus.CANCELLED) {
                continue;
            }
            if (!trainingClass.getStartTime().isBefore(now) && !trainingClass.getStartTime().isAfter(threshold)) {
                result.add(trainingClass);
            }
        }
        return result;
    }

    public EquipmentInspection inspectEquipment(int classId,
                                                String equipmentSerial,
                                                int consultantId,
                                                InspectionSeverity severity,
                                                String issueDescription) {
        EquipmentInspection inspection = new EquipmentInspection(0, classId, equipmentSerial, consultantId,
                LocalDateTime.now(), severity, issueDescription);
        EquipmentInspection stored = equipmentInspectionRepository.save(inspection);
        equipmentReviewService.markOutOfService(equipmentSerial, issueDescription);

        notifyAffectedClassConsultants(equipmentSerial, severity, issueDescription);

        TrainingClass tc = trainingClassService.findById(classId);
        if (severity.requiresReschedule()) {
            trainingClassService.updateStatus(classId, TrainingClassStatus.SUSPENDED);
            if (notificationGateway != null && tc != null) {
                notificationGateway.notifyConsultants(
                        "Class '" + tc.getName() + "' (ID=" + classId + ") has been suspended due to HIGH severity "
                        + "equipment issue. The class needs to be rescheduled by the consultant.");
            }
        } else {
            if (tc != null) {
                tc.markLimitedFunctionality(
                        "Equipment " + equipmentSerial + " is out of service (" + severity + "): " + issueDescription);
                if (notificationGateway != null) {
                    notificationGateway.notifyConsultants(
                            "Class '" + tc.getName() + "' (ID=" + classId + ") will proceed with limited functionality. "
                            + "Equipment " + equipmentSerial + " is unavailable (" + severity + " severity).");
                }
            }
        }
        return stored;
    }

    private void notifyAffectedClassConsultants(String equipmentSerial, InspectionSeverity severity, String issue) {
        if (notificationGateway == null) return;

        Set<Integer> affectedConsultantIds = new LinkedHashSet<>();
        List<String> affectedClassNames = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        List<ClassEquipmentAssignment> assignments =
                (classEquipmentAssignmentRepository != null) ? classEquipmentAssignmentRepository.findAll() : List.of();

        for (ClassEquipmentAssignment assignment : assignments) {
            if (!equipmentSerial.equalsIgnoreCase(assignment.getSerialNumber())) continue;

            TrainingClass tc = trainingClassService.findById(assignment.getClassId());
            if (tc == null || tc.getStartTime() == null) continue;
            if (tc.getStartTime().isBefore(now)) continue;

            affectedConsultantIds.add(tc.getConsultantId());
            affectedClassNames.add(tc.getName() + " (ID=" + tc.getClassId() + ")");
        }

        if (!affectedConsultantIds.isEmpty()) {
            String message = "Equipment " + equipmentSerial + " marked OUT OF SERVICE.\n"
                    + "Issue: " + issue + "\n"
                    + "Severity: " + severity + "\n"
                    + "Affected upcoming classes: " + String.join(", ", affectedClassNames);
            notificationGateway.notifyConsultants(message);
        }
    }

    public List<EquipmentAssignment> findAssignmentsForClass(int classId) {
        try {
            return equipmentAssignmentRepository.findActiveByClassId(classId);
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public List<Equipment> findAssignedEquipment(int classId) {
        List<Equipment> result = new ArrayList<>();
        for (EquipmentAssignment assignment : findAssignmentsForClass(classId)) {
            Equipment equipment = equipmentReviewService.findBySerial(assignment.getEquipmentSerial());
            if (equipment != null) {
                result.add(equipment);
            }
        }
        return result;
    }

    public List<EquipmentInspection> getOpenInspections() {
        return equipmentInspectionRepository.findOpenInspections();
    }
}
