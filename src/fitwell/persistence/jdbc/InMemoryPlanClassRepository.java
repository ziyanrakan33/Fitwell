package fitwell.persistence.jdbc;

import fitwell.domain.training.PlanClassAssignment;
import fitwell.persistence.api.PlanClassRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class InMemoryPlanClassRepository implements PlanClassRepository {

    private static final List<PlanClassAssignment> ASSIGNMENTS = new ArrayList<>();

    public void assign(int planId, int classId) {
        if (!isAssigned(planId, classId)) {
            ASSIGNMENTS.add(new PlanClassAssignment(planId, classId));
        }
    }

    public void unassign(int planId, int classId) {
        ASSIGNMENTS.removeIf(a -> a.getPlanId() == planId && a.getClassId() == classId);
    }

    public boolean isAssigned(int planId, int classId) {
        return ASSIGNMENTS.stream().anyMatch(a -> a.getPlanId() == planId && a.getClassId() == classId);
    }

    public Set<Integer> findClassIdsByPlanId(int planId) {
        Set<Integer> ids = new LinkedHashSet<>();
        for (PlanClassAssignment a : ASSIGNMENTS) {
            if (a.getPlanId() == planId) ids.add(a.getClassId());
        }
        return ids;
    }

    public Set<Integer> findPlanIdsByClassId(int classId) {
        Set<Integer> ids = new LinkedHashSet<>();
        for (PlanClassAssignment a : ASSIGNMENTS) {
            if (a.getClassId() == classId) ids.add(a.getPlanId());
        }
        return ids;
    }

    public List<PlanClassAssignment> findAll() {
        return new ArrayList<>(ASSIGNMENTS);
    }
}
