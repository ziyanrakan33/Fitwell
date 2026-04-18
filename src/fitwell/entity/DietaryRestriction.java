package fitwell.entity;

public enum DietaryRestriction {
    VEGAN("Vegan"),
    VEGETARIAN("Vegetarian"),
    GLUTEN_FREE("Gluten Free"),
    LACTOSE_FREE("Lactose Free"),
    NUT_ALLERGY("Nut Allergy"),
    HALAL("Halal"),
    KOSHER("Kosher"),
    LOW_SODIUM("Low Sodium"),
    DIABETIC("Diabetic"),
    OTHER("Other");

    private final String displayName;

    DietaryRestriction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /** Parse a comma-separated list back into an array of restrictions. */
    public static DietaryRestriction[] fromCsv(String csv) {
        if (csv == null || csv.isBlank()) return new DietaryRestriction[0];
        String[] parts = csv.split(",");
        java.util.List<DietaryRestriction> result = new java.util.ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            for (DietaryRestriction dr : values()) {
                if (dr.name().equalsIgnoreCase(trimmed) || dr.displayName.equalsIgnoreCase(trimmed)) {
                    result.add(dr);
                    break;
                }
            }
        }
        return result.toArray(new DietaryRestriction[0]);
    }

    /** Serialize a list of restrictions to a comma-separated string for DB storage. */
    public static String toCsv(java.util.Collection<DietaryRestriction> restrictions) {
        if (restrictions == null || restrictions.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (DietaryRestriction dr : restrictions) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(dr.name());
        }
        return sb.toString();
    }
}
