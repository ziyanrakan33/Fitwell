package fitwell.domain.equipment;

public class EquipmentLocation {
    private int x;
    private int y;
    private int shelfNumber;

    public EquipmentLocation(int x, int y, int shelfNumber) {
        this.x = x;
        this.y = y;
        this.shelfNumber = shelfNumber;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getShelfNumber() { return shelfNumber; }

    public void set(int x, int y, int shelfNumber) {
        this.x = x;
        this.y = y;
        this.shelfNumber = shelfNumber;
    }
}
