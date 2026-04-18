package fitwell.entity;

public enum AttendanceStatus {
    REGISTERED,
    ATTENDED,
    MISSED,
    CANCELLED;

    public static AttendanceStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return REGISTERED;
        }
        try {
            return AttendanceStatus.valueOf(value.trim().replace('-', '_').toUpperCase());
        } catch (IllegalArgumentException ex) {
            return REGISTERED;
        }
    }
}
