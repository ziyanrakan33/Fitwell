package fitwell.domain.equipment;

public class Equipment {
    private final String serialNumber;
    private String name;
    private String description;
    private EquipmentCategory category;
    private int quantity;
    private EquipmentStatus status;
    private boolean isFlagged;
    private String flagReason;
    private EquipmentLocation location;

    private int timesUsedThisYear;

    public Equipment(String serialNumber, String name, String description,
                     EquipmentCategory category, int quantity,
                     EquipmentStatus status, EquipmentLocation location) {

        if (serialNumber == null || serialNumber.isBlank())
            throw new IllegalArgumentException("serialNumber is required");

        this.serialNumber = serialNumber.trim();
        this.name = (name == null ? "" : name.trim());
        this.description = (description == null ? "" : description.trim());
        this.category = (category == null ? EquipmentCategory.other : category);
        this.quantity = Math.max(0, quantity);
        this.status = (status == null ? EquipmentStatus.IN_SERVICE : status);
        this.location = (location == null ? new EquipmentLocation(0,0,0) : location);

        this.isFlagged = false;
        this.flagReason = "";
        this.timesUsedThisYear = 0;
    }

    public String getSerialNumber() { return serialNumber; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public EquipmentCategory getCategory() { return category; }
    public int getQuantity() { return quantity; }
    public EquipmentStatus getStatus() { return status; }
    public boolean isFlagged() { return isFlagged; }
    public String getFlagReason() { return flagReason; }
    public EquipmentLocation getLocation() { return location; }
    public int getTimesUsedThisYear() { return timesUsedThisYear; }

    public void setDetails(String name, String description, EquipmentCategory category) {
        this.name = (name == null ? "" : name.trim());
        this.description = (description == null ? "" : description.trim());
        this.category = (category == null ? EquipmentCategory.other : category);
    }

    public void increaseQuantity(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        this.quantity = Math.max(0, this.quantity - amount);
    }

    public void setLocation(int x, int y, int shelf) {
        if (this.location == null) this.location = new EquipmentLocation(x,y,shelf);
        else this.location.set(x,y,shelf);
    }

    public void flag(String reason) {
        this.isFlagged = true;
        this.flagReason = (reason == null ? "" : reason.trim());
    }

    public void unflag() {
        this.isFlagged = false;
        this.flagReason = "";
    }

    public void markOutOfService(String reason) {
        this.status = EquipmentStatus.OUT_OF_SERVICE;
        flag("OUT_OF_SERVICE: " + (reason == null ? "" : reason.trim()));
    }

    public void returnToService() {
        if (this.status != EquipmentStatus.NON_USABLE) {
            this.status = EquipmentStatus.IN_SERVICE;
        }
    }

    public void markNonUsable() {
        this.status = EquipmentStatus.NON_USABLE;
        flag("NON_USABLE");
    }

    public void addUsage(int times) {
        if (times < 0) return;
        this.timesUsedThisYear += times;
    }
}
