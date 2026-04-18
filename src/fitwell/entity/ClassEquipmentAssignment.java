package fitwell.entity;

public class ClassEquipmentAssignment {
    private Integer assignmentId;      // AutoNumber
    private int classId;
    private String serialNumber;
    private int requiredQuantity;
    private String notes;

    public ClassEquipmentAssignment(Integer assignmentId, int classId, String serialNumber, int requiredQuantity, String notes) {
        this.assignmentId = assignmentId;
        this.classId = classId;
        this.serialNumber = serialNumber;
        this.requiredQuantity = requiredQuantity;
        this.notes = notes == null ? "" : notes;
    }

    public ClassEquipmentAssignment(int classId, String serialNumber, int requiredQuantity) {
        this(null, classId, serialNumber, requiredQuantity, "");
    }

    public Integer getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Integer assignmentId) { this.assignmentId = assignmentId; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public int getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(int requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes == null ? "" : notes; }
}