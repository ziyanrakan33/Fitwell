package fitwell.entity;

public enum InspectionSeverity {
    LOW,
    MEDIUM,
    HIGH;

    public boolean requiresReschedule() {
        return this == HIGH;
    }
}
