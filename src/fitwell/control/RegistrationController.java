package fitwell.control;

import fitwell.domain.training.TrainingClass;
import fitwell.domain.training.TrainingClassStatus;
import fitwell.repo.RegistrationRepository;

import java.time.LocalDateTime;

public class RegistrationController {

    private final RegistrationRepository repo;
    private final TrainingClassService trainingClassService;
    private final EmergencyAlertService emergencyAlertService;

    public RegistrationController() {
        FitWellServiceRegistry registry = FitWellServiceRegistry.getInstance();
        this.repo = registry.registrationRepository();
        this.trainingClassService = registry.trainingClassService();
        this.emergencyAlertService = registry.emergencyAlertService();
    }

    // ===== UI Friendly API (Integer) =====
    public void register(Integer classId, Integer traineeId) throws Exception {
        if (classId == null || traineeId == null) {
            throw new IllegalArgumentException("classId/traineeId cannot be null");
        }
        register(classId.intValue(), traineeId.intValue());
    }

    // ===== Core API (int) =====
    public void register(int classId, int traineeId) throws Exception {
        registerInternal(classId, traineeId);
    }

    public void unregister(Integer classId, Integer traineeId) throws Exception {
        if (classId == null || traineeId == null) {
            throw new IllegalArgumentException("classId/traineeId cannot be null");
        }
        unregister(classId.intValue(), traineeId.intValue());
    }

    public void unregister(int classId, int traineeId) {
        if (!repo.isRegistered(classId, traineeId)) {
            throw new IllegalArgumentException("Trainee is not registered to this class.");
        }
        repo.unregister(classId, traineeId);
    }

    // ===== Logic =====
    private void registerInternal(int classId, int traineeId) throws Exception {
        if (repo.isRegistered(classId, traineeId)) {
            throw new IllegalArgumentException("This trainee is already registered to this class.");
        }

        TrainingClass tc = trainingClassService.findById(classId);
        if (tc == null) {
            throw new IllegalArgumentException("Class not found.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = tc.getStartTime();

        if (start == null) {
            throw new IllegalArgumentException("Class start time is missing.");
        }

        // 24 hours rule
        if (!start.isAfter(now)) {
            throw new IllegalArgumentException("Cannot register: class already started (or in the past).");
        }

        if (start.isBefore(now.plusHours(24))) {
            throw new IllegalArgumentException("Registration allowed only up to 24 hours in advance.");
        }

        if (emergencyAlertService.isClassSuspended(classId) || tc.getStatus() == TrainingClassStatus.SUSPENDED) {
            throw new IllegalArgumentException("Registration is blocked while the class is suspended.");
        }

        // capacity
        int current = repo.countRegistrationsForClass(classId);
        if (current >= tc.getMaxParticipants()) {
            throw new IllegalArgumentException(
                    "Class is full (" + current + "/" + tc.getMaxParticipants() + ")."
            );
        }

        // write to DB
        repo.register(classId, traineeId, now);
    }

    /**
     * Registers a trainee on behalf of a consultant.
     *
     * @param consultantId the consultant performing the registration
     * @param classId      target class id
     * @param traineeId    trainee id to register
     */
    public void registerAsConsultant(int consultantId, Integer classId, Integer traineeId) {
        // keep method signature unchanged for compatibility with existing UI code
        if (consultantId <= 0) {
            throw new IllegalArgumentException("consultantId must be > 0");
        }
        if (classId == null || traineeId == null) {
            throw new IllegalArgumentException("classId/traineeId cannot be null");
        }

        try {
            // Consultant registration follows the same rules (capacity + 24h + duplicate check)
            register(classId, traineeId);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception ex) {
            // convert checked exception to runtime to preserve current method signature
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}