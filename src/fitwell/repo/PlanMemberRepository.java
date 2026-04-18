package fitwell.repo;

import fitwell.domain.training.TrainingPlanMember;

import java.util.ArrayList;
import java.util.List;

public class PlanMemberRepository {
    private static final List<TrainingPlanMember> MEMBERS = new ArrayList<>();

    static {
        if (MEMBERS.isEmpty()) {
            MEMBERS.add(new TrainingPlanMember(102, 1, "member"));
            MEMBERS.add(new TrainingPlanMember(102, 2, "member"));
        }
    }

    public List<TrainingPlanMember> findByPlanId(int planId) {
        List<TrainingPlanMember> result = new ArrayList<>();
        for (TrainingPlanMember member : MEMBERS) {
            if (member.getPlanId() == planId) {
                result.add(member);
            }
        }
        return result;
    }

    public List<TrainingPlanMember> findByTraineeId(int traineeId) {
        List<TrainingPlanMember> result = new ArrayList<>();
        for (TrainingPlanMember member : MEMBERS) {
            if (member.getTraineeId() == traineeId) {
                result.add(member);
            }
        }
        return result;
    }

    public synchronized void addMember(TrainingPlanMember member) {
        if (member == null) {
            return;
        }
        MEMBERS.add(member);
    }

    public synchronized void removeMember(int planId, int traineeId) {
        MEMBERS.removeIf(m -> m.getPlanId() == planId && m.getTraineeId() == traineeId);
    }

    public synchronized void removeAllForPlan(int planId) {
        MEMBERS.removeIf(m -> m.getPlanId() == planId);
    }
}
