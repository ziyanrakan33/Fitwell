package fitwell.persistence.api;

import fitwell.domain.training.PlanClassAssignment;
import java.util.List;
import java.util.Set;

public interface PlanClassRepository {
    void assign(int planId, int classId);
    void unassign(int planId, int classId);
    boolean isAssigned(int planId, int classId);
    Set<Integer> findClassIdsByPlanId(int planId);
    Set<Integer> findPlanIdsByClassId(int classId);
    List<PlanClassAssignment> findAll();
}
