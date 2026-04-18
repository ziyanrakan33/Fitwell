package fitwell.entity;

import java.time.LocalDateTime;

public class EquipmentAssignment {

    private int assignmentId;
    private int classId;
    private String equipmentSerial;
    private int qtyAssigned;
    private LocalDateTime assignedAt;
    private Integer assignedByConsultantId;
    private String notes;
    private boolean active = true;

    public EquipmentAssignment() {}

    public EquipmentAssignment(int classId, String equipmentSerial, int qtyAssigned) {
        this.classId = classId;
        this.equipmentSerial = equipmentSerial;
        this.qtyAssigned = qtyAssigned;
        this.active = true;
    }

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getEquipmentSerial() { return equipmentSerial; }
    public void setEquipmentSerial(String equipmentSerial) { this.equipmentSerial = equipmentSerial; }

    public int getQtyAssigned() { return qtyAssigned; }
    public void setQtyAssigned(int qtyAssigned) { this.qtyAssigned = qtyAssigned; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public Integer getAssignedByConsultantId() { return assignedByConsultantId; }
    public void setAssignedByConsultantId(Integer assignedByConsultantId) { this.assignedByConsultantId = assignedByConsultantId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}