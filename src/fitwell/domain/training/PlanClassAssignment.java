package fitwell.domain.training;

public class PlanClassAssignment {
    private final int planId;
    private final int classId;

    public PlanClassAssignment(int planId, int classId) {
        this.planId = planId;
        this.classId = classId;
    }

    public int getPlanId() { return planId; }
    public int getClassId() { return classId; }
}
