package fitwell.entity;

public class EquipmentUpdate {
    private final String serialNumber;
    private final int newQuantity;
    private final String photoUrl;
    private final boolean isNewItem;
    private final String name;
    private final String category;
    private final int x;
    private final int y;
    private final int shelf;

    public EquipmentUpdate(String serialNumber, int newQuantity, String photoUrl, boolean isNewItem,
                           String name, String category, int x, int y, int shelf) {
        this.serialNumber = serialNumber;
        this.newQuantity = newQuantity;
        this.photoUrl = photoUrl == null ? "" : photoUrl;
        this.isNewItem = isNewItem;
        this.name = name;
        this.category = category;
        this.x = x;
        this.y = y;
        this.shelf = shelf;
    }

    public String getSerialNumber() { return serialNumber; }
    public int getNewQuantity() { return newQuantity; }
    public String getPhotoUrl() { return photoUrl; }
    public boolean isNewItem() { return isNewItem; }

    public String getName() { return name; }
    public String getCategory() { return category; }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getShelf() { return shelf; }
}
