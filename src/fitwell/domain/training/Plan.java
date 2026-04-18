package fitwell.domain.training;

import java.time.LocalDate;

public abstract class Plan {
    private final int planId;
    private final int ownerTraineeId;
    private LocalDate startDate;
    private int durationMonths;
    private PlanStatus status;

    public Plan(int planId, int ownerTraineeId, LocalDate startDate, int durationMonths, String status) {
        this(planId, ownerTraineeId, startDate, durationMonths, PlanStatus.fromValue(status));
    }

    public Plan(int planId, int ownerTraineeId, LocalDate startDate, int durationMonths, PlanStatus status) {
        this.planId = planId;
        this.ownerTraineeId = ownerTraineeId;
        this.startDate = startDate;
        this.durationMonths = Math.max(1, durationMonths);
        this.status = status == null ? PlanStatus.ACTIVE : status;
    }

    public int getPlanId() { return planId; }

    /** Returns the ID of the trainee who owns/created this plan. For group plans, other members are tracked via TrainingPlanMember. */
    public int getOwnerTraineeId() { return ownerTraineeId; }

    /** @deprecated Use {@link #getOwnerTraineeId()} for clarity. Kept for backward compatibility. */
    @Deprecated
    public int getTraineeId() { return ownerTraineeId; }

    public LocalDate getStartDate() { return startDate; }
    public int getDurationMonths() { return durationMonths; }
    public String getStatus() { return status.name(); }
    public PlanStatus getPlanStatus() { return status; }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = Math.max(1, durationMonths);
    }

    public void setStatus(PlanStatus status) {
        this.status = status == null ? PlanStatus.ACTIVE : status;
    }

    public boolean isActive() {
        return status == PlanStatus.ACTIVE;
    }
}
