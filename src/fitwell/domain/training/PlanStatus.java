package fitwell.domain.training;

public enum PlanStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    CANCELLED;

    public static PlanStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVE;
        }
        try {
            return PlanStatus.valueOf(value.trim().replace('-', '_').toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ACTIVE;
        }
    }
}
