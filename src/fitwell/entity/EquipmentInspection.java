package fitwell.entity;

import java.time.LocalDateTime;

public class EquipmentInspection {
    private final int inspectionId;
    private final int classId;
    private final String equipmentSerial;
    private final int consultantId;
    private final LocalDateTime inspectionTime;
    private final InspectionSeverity severity;
    private final String issueDescription;
    private boolean resolved;
    private String resolutionNote;

    public EquipmentInspection(int inspectionId,
                               int classId,
                               String equipmentSerial,
                               int consultantId,
                               LocalDateTime inspectionTime,
                               InspectionSeverity severity,
                               String issueDescription) {
        this.inspectionId = inspectionId;
        this.classId = classId;
        this.equipmentSerial = equipmentSerial;
        this.consultantId = consultantId;
        this.inspectionTime = inspectionTime == null ? LocalDateTime.now() : inspectionTime;
        this.severity = severity == null ? InspectionSeverity.MEDIUM : severity;
        this.issueDescription = issueDescription == null ? "" : issueDescription.trim();
    }

    public int getInspectionId() {
        return inspectionId;
    }

    public int getClassId() {
        return classId;
    }

    public String getEquipmentSerial() {
        return equipmentSerial;
    }

    public int getConsultantId() {
        return consultantId;
    }

    public LocalDateTime getInspectionTime() {
        return inspectionTime;
    }

    public InspectionSeverity getSeverity() {
        return severity;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public boolean isResolved() {
        return resolved;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void resolve(String note) {
        this.resolved = true;
        this.resolutionNote = note == null ? "" : note.trim();
    }
}
