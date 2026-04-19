package fitwell.control;

import fitwell.domain.training.GroupPlan;
import fitwell.domain.training.Plan;
import fitwell.domain.training.TrainingClass;
import fitwell.domain.training.TrainingPlanMember;
import fitwell.persistence.api.PlanClassRepository;
import fitwell.persistence.api.PlanMemberRepository;
import fitwell.persistence.api.TrainingPlanRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TrainingPlanService {
    private final TrainingPlanRepository trainingPlanRepository;
    private final PlanMemberRepository planMemberRepository;
    private PlanClassRepository planClassRepository;

    public TrainingPlanService(TrainingPlanRepository trainingPlanRepository,
                               PlanMemberRepository planMemberRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
        this.planMemberRepository = planMemberRepository;
    }

    public void setPlanClassRepository(PlanClassRepository planClassRepository) {
        this.planClassRepository = planClassRepository;
    }

    public List<Plan> getAllPlans() {
        return trainingPlanRepository.findAll();
    }

    public Plan findById(int planId) {
        return trainingPlanRepository.findById(planId);
    }

    public void addMemberToPlan(int planId, int traineeId) {
        planMemberRepository.addMember(new TrainingPlanMember(planId, traineeId, "member"));
    }

    public void removeMemberFromPlan(int planId, int traineeId) {
        planMemberRepository.removeMember(planId, traineeId);
    }

    public void removeAllMembersFromPlan(int planId) {
        planMemberRepository.removeAllForPlan(planId);
    }

    public List<Plan> getPlansForTrainee(int traineeId) {
        List<Plan> owned = trainingPlanRepository.findByTraineeId(traineeId);
        Set<Integer> planIds = new LinkedHashSet<>();
        List<Plan> result = new ArrayList<>();
        for (Plan p : owned) {
            if (planIds.add(p.getPlanId())) {
                result.add(p);
            }
        }
        for (TrainingPlanMember member : planMemberRepository.findByTraineeId(traineeId)) {
            if (planIds.add(member.getPlanId())) {
                Plan plan = trainingPlanRepository.findById(member.getPlanId());
                if (plan != null) {
                    result.add(plan);
                }
            }
        }
        return result;
    }

    public List<TrainingPlanMember> getMembersForPlan(int planId) {
        return planMemberRepository.findByPlanId(planId);
    }

    public Plan savePlan(Plan plan) {
        return trainingPlanRepository.save(plan);
    }

    public void assignClassToPlan(int planId, int classId) {
        if (planClassRepository != null) {
            planClassRepository.assign(planId, classId);
        }
    }

    public void unassignClassFromPlan(int planId, int classId) {
        if (planClassRepository != null) {
            planClassRepository.unassign(planId, classId);
        }
    }

    public Set<Integer> getClassIdsForPlan(int planId) {
        if (planClassRepository == null) return Set.of();
        return planClassRepository.findClassIdsByPlanId(planId);
    }

    public Set<String> getEligibleClassTypesForTrainee(int traineeId) {
        Set<String> types = new LinkedHashSet<>();
        for (Plan plan : getPlansForTrainee(traineeId)) {
            if (!plan.isActive()) {
                continue;
            }
            if (plan instanceof GroupPlan groupPlan) {
                String[] parts = groupPlan.getPreferredClassTypes().split(",");
                for (String part : parts) {
                    String normalized = part.trim();
                    if (!normalized.isEmpty()) {
                        types.add(normalized);
                    }
                }
            }
        }
        return types;
    }

    public List<TrainingClass> filterClassesAssignedToTrainee(int traineeId, List<TrainingClass> classes) {
        Set<Integer> directClassIds = new LinkedHashSet<>();
        Set<String> eligibleTypes = new LinkedHashSet<>();
        boolean hasDirectAssignments = false;

        for (Plan plan : getPlansForTrainee(traineeId)) {
            if (!plan.isActive()) continue;

            Set<Integer> planClassIds = getClassIdsForPlan(plan.getPlanId());
            if (!planClassIds.isEmpty()) {
                directClassIds.addAll(planClassIds);
                hasDirectAssignments = true;
            }

            if (plan instanceof GroupPlan groupPlan) {
                String[] parts = groupPlan.getPreferredClassTypes().split(",");
                for (String part : parts) {
                    String normalized = part.trim();
                    if (!normalized.isEmpty()) {
                        eligibleTypes.add(normalized);
                    }
                }
            }
        }

        boolean hasTypeRestrictions = !eligibleTypes.isEmpty();
        List<TrainingClass> result = new ArrayList<>();
        for (TrainingClass tc : classes) {
            if (tc == null) continue;

            if (hasDirectAssignments && tc.getClassId() != null && directClassIds.contains(tc.getClassId())) {
                result.add(tc);
                continue;
            }

            if (hasTypeRestrictions && eligibleTypes.contains(tc.getType())) {
                result.add(tc);
                continue;
            }

            if (!hasDirectAssignments && !hasTypeRestrictions) {
                result.add(tc);
            }
        }
        return result;
    }
}
