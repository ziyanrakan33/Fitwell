package fitwell.domain.equipment;

public enum InspectionSeverity {
    LOW,
    MEDIUM,
    HIGH;

    public boolean requiresReschedule() {
        return this == HIGH;
    }
}
