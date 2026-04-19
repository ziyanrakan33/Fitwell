package fitwell.service.training;

import fitwell.domain.training.TrainingClass;
import fitwell.domain.training.TrainingClassStatus;
import fitwell.persistence.api.TrainingClassRepository;
import fitwell.persistence.api.TrainingClassRuntimeStateRepository;

import java.util.ArrayList;
import java.util.List;

public class TrainingClassService {
    private final TrainingClassRepository repository;
    private final TrainingClassRuntimeStateRepository runtimeStateRepository;

    public TrainingClassService(TrainingClassRepository repository,
                                TrainingClassRuntimeStateRepository runtimeStateRepository) {
        this.repository = repository;
        this.runtimeStateRepository = runtimeStateRepository;
    }

    public List<TrainingClass> getAllClasses() {
        List<TrainingClass> classes = repository.findAll();
        List<TrainingClass> hydrated = new ArrayList<>();
        for (TrainingClass trainingClass : classes) {
            hydrated.add(applyRuntimeState(trainingClass));
        }
        return hydrated;
    }

    public TrainingClass findById(int classId) {
        for (TrainingClass trainingClass : getAllClasses()) {
            if (trainingClass.getClassId() != null && trainingClass.getClassId() == classId) {
                return trainingClass;
            }
        }
        return null;
    }

    public TrainingClass saveClass(TrainingClass trainingClass) {
        validate(trainingClass);
        if (trainingClass.getClassId() == null) {
            int newId = repository.insert(trainingClass);
            trainingClass.setClassId(newId);
        } else {
            repository.update(trainingClass);
        }

        runtimeStateRepository.saveTips(trainingClass.getClassId(), trainingClass.getTips());
        runtimeStateRepository.saveStatus(trainingClass.getClassId(), trainingClass.getStatus());
        return applyRuntimeState(trainingClass);
    }

    public void deleteClass(int classId) {
        if (classId <= 0) {
            throw new IllegalArgumentException("Invalid class id.");
        }
        repository.delete(classId);
    }

    public void updateStatus(int classId, TrainingClassStatus status) {
        runtimeStateRepository.saveStatus(classId, status);
        try { repository.updateStatus(classId, status); } catch (Exception ignored) {}
    }

    public void saveTips(int classId, List<String> tips) {
        runtimeStateRepository.saveTips(classId, tips);
    }

    public TrainingClass applyRuntimeState(TrainingClass trainingClass) {
        if (trainingClass == null || trainingClass.getClassId() == null) {
            return trainingClass;
        }
        int classId = trainingClass.getClassId();
        TrainingClassRuntimeStateRepository.RuntimeState state = runtimeStateRepository.getState(classId);
        // On first load (HashMap default is SCHEDULED), check DB for persisted status
        if (state.getStatus() == TrainingClassStatus.SCHEDULED) {
            try {
                TrainingClassStatus dbStatus = repository.findStatus(classId);
                if (dbStatus != null && dbStatus != TrainingClassStatus.SCHEDULED) {
                    state.setStatus(dbStatus);
                }
            } catch (Exception ignored) {}
        }
        trainingClass.setStatus(state.getStatus());
        trainingClass.setTips(state.getTips());
        return trainingClass;
    }

    public void validate(TrainingClass trainingClass) {
        if (trainingClass == null) {
            throw new IllegalArgumentException("Training class is required.");
        }
        if (trainingClass.getName() == null || trainingClass.getName().trim().isBlank()) {
            throw new IllegalArgumentException("Class name is required.");
        }
        if (trainingClass.getType() == null || trainingClass.getType().trim().isBlank()) {
            throw new IllegalArgumentException("Class type is required.");
        }
        if (trainingClass.getStartTime() == null || trainingClass.getEndTime() == null) {
            throw new IllegalArgumentException("Start and end time are required.");
        }
        if (!trainingClass.getEndTime().isAfter(trainingClass.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        if (trainingClass.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException("Maximum participants must be greater than 0.");
        }
        if (trainingClass.getConsultantId() <= 0) {
            throw new IllegalArgumentException("Consultant is required.");
        }
        if (trainingClass.getTips().size() > 5) {
            throw new IllegalArgumentException("A class can contain up to 5 tips.");
        }
    }
}
