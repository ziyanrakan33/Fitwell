package fitwell.domain.shared;

public enum PreferredUpdateMethod {
    EMAIL,
    SMS;

    public static PreferredUpdateMethod fromValue(String value) {
        if (value == null || value.isBlank()) {
            return EMAIL;
        }
        try {
            return PreferredUpdateMethod.valueOf(value.trim().replace('-', '_').toUpperCase());
        } catch (IllegalArgumentException ex) {
            return EMAIL;
        }
    }
}
