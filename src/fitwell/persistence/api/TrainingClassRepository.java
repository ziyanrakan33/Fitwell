package fitwell.persistence.api;

import fitwell.domain.training.TrainingClass;
import fitwell.domain.training.TrainingClassStatus;
import java.util.List;

public interface TrainingClassRepository {
    TrainingClass findById(int classId);
    List<TrainingClass> findAll();
    int insert(TrainingClass tc);
    void update(TrainingClass tc);
    void updateStatus(int classId, TrainingClassStatus status);
    TrainingClassStatus findStatus(int classId);
    void delete(int classId);
}
