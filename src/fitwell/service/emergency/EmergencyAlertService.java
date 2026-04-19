package fitwell.service.emergency;
import fitwell.service.training.TrainingClassService;

import fitwell.domain.emergency.EmergencyAlert;
import fitwell.domain.training.TrainingClass;
import fitwell.domain.training.TrainingClassStatus;
import fitwell.integration.NotificationGateway;
import fitwell.persistence.api.EmergencyAlertRepository;
import fitwell.persistence.api.TrainingClassRuntimeStateRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class EmergencyAlertService {
    private final EmergencyAlertRepository repository;
    private final TrainingClassService trainingClassService;
    private final NotificationGateway notificationGateway;
    private TrainingClassRuntimeStateRepository runtimeStateRepository;

    private final Map<Integer, TrainingClassStatus> suspendedClassPreviousStatus = new LinkedHashMap<>();

    public EmergencyAlertService(EmergencyAlertRepository repository,
                                 TrainingClassService trainingClassService,
                                 NotificationGateway notificationGateway) {
        this.repository = repository;
        this.trainingClassService = trainingClassService;
        this.notificationGateway = notificationGateway;
    }

    public void setRuntimeStateRepository(TrainingClassRuntimeStateRepository runtimeStateRepository) {
        this.runtimeStateRepository = runtimeStateRepository;
    }

    public EmergencyAlert activate(String message) {
        EmergencyAlert alert = repository.activate(message);
        suspendedClassPreviousStatus.clear();
        LocalDateTime now = LocalDateTime.now();

        for (TrainingClass tc : trainingClassService.getAllClasses()) {
            TrainingClassStatus currentStatus = tc.getStatus();
            if (currentStatus == TrainingClassStatus.COMPLETED || currentStatus == TrainingClassStatus.CANCELLED) {
                continue;
            }

            suspendedClassPreviousStatus.put(tc.getClassId(), currentStatus);

            if (runtimeStateRepository != null) {
                runtimeStateRepository.saveSuspensionInfo(tc.getClassId(), currentStatus, now);
            }

            tc.suspend(now);
            trainingClassService.updateStatus(tc.getClassId(), TrainingClassStatus.SUSPENDED);
        }

        notificationGateway.notifyConsultants(
                "Emergency alert activated. All ongoing and upcoming classes have been suspended immediately.");
        return alert;
    }

    public EmergencyAlert deactivate() {
        EmergencyAlert alert = repository.getCurrentAlert();
        if (alert == null) {
            throw new IllegalStateException("No active emergency alert.");
        }
        alert.deactivate(LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));
        return alert;
    }

    public void manualResume() {
        resumeSuspendedClasses();
        repository.clear();
    }

    public void autoResumeIfDue() {
        EmergencyAlert alert = repository.getCurrentAlert();
        if (alert != null && !alert.isActive() && alert.getAutoResumeAt() != null
                && !LocalDateTime.now().isBefore(alert.getAutoResumeAt())) {
            resumeSuspendedClasses();
            repository.clear();
        }
    }

    public boolean isEmergencyActive() {
        EmergencyAlert alert = repository.getCurrentAlert();
        return alert != null && alert.isActive();
    }

    public EmergencyAlert getCurrentAlert() {
        autoResumeIfDue();
        return repository.getCurrentAlert();
    }

    public boolean isClassSuspended(int classId) {
        autoResumeIfDue();
        return suspendedClassPreviousStatus.containsKey(classId);
    }

    private void resumeSuspendedClasses() {
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<Integer, TrainingClassStatus> entry : suspendedClassPreviousStatus.entrySet()) {
            int classId = entry.getKey();
            TrainingClassStatus previousStatus = entry.getValue();
            TrainingClass tc = trainingClassService.findById(classId);

            if (tc != null) {
                if (previousStatus == TrainingClassStatus.IN_PROGRESS) {
                    if (tc.getEndTime() != null && tc.getEndTime().isAfter(now)) {
                        trainingClassService.updateStatus(classId, TrainingClassStatus.IN_PROGRESS);
                    } else {
                        trainingClassService.updateStatus(classId, TrainingClassStatus.COMPLETED);
                    }
                } else {
                    trainingClassService.updateStatus(classId, previousStatus);
                }

                tc.resumeFromSuspension();
                if (runtimeStateRepository != null) {
                    runtimeStateRepository.clearSuspensionInfo(classId);
                }
            }
        }
        suspendedClassPreviousStatus.clear();
        notificationGateway.notifyConsultants(
                "Emergency alert cleared. Suspended classes have been resumed from their point of suspension.");
    }
}
