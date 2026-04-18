package fitwell.control;

import fitwell.entity.TrainingClass;
import fitwell.repo.TrainingClassRepository;
import fitwell.repo.TrainingClassRuntimeStateRepository;

public class TrainingClassController {

    private final TrainingClassService service;

    public TrainingClassController(TrainingClassRepository repo) {
        this.service = new TrainingClassService(repo, new TrainingClassRuntimeStateRepository());
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