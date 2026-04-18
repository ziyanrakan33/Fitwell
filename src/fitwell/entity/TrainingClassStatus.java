package fitwell.entity;

public enum TrainingClassStatus {
    SCHEDULED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    CANCELLED,
    SUSPENDED;

    public static TrainingClassStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return SCHEDULED;
        }
        try {
            return TrainingClassStatus.valueOf(value.trim().replace('-', '_').toUpperCase());
        } catch (IllegalArgumentException ex) {
            return SCHEDULED;
        }
    }
}
