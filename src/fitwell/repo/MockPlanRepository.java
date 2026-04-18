package fitwell.repo;

import fitwell.domain.training.Plan;
import java.util.ArrayList;
import java.util.List;

public class MockPlanRepository {
    private final TrainingPlanRepository delegate = new TrainingPlanRepository();

    public MockPlanRepository() {
    }

    public List<Plan> getPlansForTrainee(int traineeId) {
        return new ArrayList<>(delegate.findByTraineeId(traineeId));
    }
}
