package fitwell.controller;

import fitwell.domain.training.TrainingClass;
import fitwell.service.training.TrainingClassService;
import fitwell.persistence.api.TrainingClassRepository;
import fitwell.persistence.api.TrainingClassRuntimeStateRepository;
import fitwell.persistence.jdbc.InMemoryTrainingClassRuntimeStateRepository;

public class TrainingClassController {

    private final TrainingClassService service;

    public TrainingClassController(TrainingClassRepository repo) {
        this.service = new TrainingClassService(repo, new InMemoryTrainingClassRuntimeStateRepository());
    }

    public void add(TrainingClass tc) {
        service.saveClass(tc);
    }

    public void update(TrainingClass tc) {
        service.saveClass(tc);
    }

    public void delete(int classId) {
        service.deleteClass(classId);
    }
}