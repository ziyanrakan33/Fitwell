package fitwell.entity;

public enum ConsultantRole {
    MANAGER,
    TRAINER,
    DIETITIAN;

    public static ConsultantRole fromString(String role) {
        if (role == null || role.isBlank()) return MANAGER;
        try {
            return ConsultantRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MANAGER;
        }
    }
}
