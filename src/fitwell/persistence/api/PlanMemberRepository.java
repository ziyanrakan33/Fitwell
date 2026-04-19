package fitwell.persistence.api;

import fitwell.domain.training.TrainingPlanMember;
import java.util.List;

public interface PlanMemberRepository {
    List<TrainingPlanMember> findByPlanId(int planId);
    List<TrainingPlanMember> findByTraineeId(int traineeId);
    void addMember(TrainingPlanMember member);
    void removeMember(int planId, int traineeId);
    void removeAllForPlan(int planId);
}
