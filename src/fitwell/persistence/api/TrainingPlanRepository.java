package fitwell.persistence.api;

import fitwell.domain.training.Plan;
import java.util.List;

public interface TrainingPlanRepository {
    List<Plan> findAll();
    Plan findById(int planId);
    List<Plan> findByTraineeId(int traineeId);
    Plan save(Plan plan);
}
