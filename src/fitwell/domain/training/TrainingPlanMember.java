package fitwell.domain.training;

public class TrainingPlanMember {
    private final int planId;
    private final int traineeId;
    private final String role;

    public TrainingPlanMember(int planId, int traineeId, String role) {
        this.planId = planId;
        this.traineeId = traineeId;
        this.role = role == null || role.isBlank() ? "member" : role.trim();
    }

    public int getPlanId() {
        return planId;
    }

    public int getTraineeId() {
        return traineeId;
    }

    public String getRole() {
        return role;
    }
}
