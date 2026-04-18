package fitwell.repo;

import fitwell.entity.EquipmentInspection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EquipmentInspectionRepository {
    private static final List<EquipmentInspection> INSPECTIONS = new ArrayList<>();
    private static int nextInspectionId = 1;

    public synchronized EquipmentInspection save(EquipmentInspection inspection) {
        EquipmentInspection stored = inspection;
        if (inspection.getInspectionId() <= 0) {
            stored = new EquipmentInspection(nextInspectionId++, inspection.getClassId(), inspection.getEquipmentSerial(),
                    inspection.getConsultantId(), inspection.getInspectionTime(), inspection.getSeverity(),
                    inspection.getIssueDescription());
            if (inspection.isResolved()) {
                stored.resolve(inspection.getResolutionNote());
            }
        }
        INSPECTIONS.add(stored);
        return stored;
    }

    public List<EquipmentInspection> findAll() {
        return INSPECTIONS.stream()
                .sorted(Comparator.comparing(EquipmentInspection::getInspectionTime).reversed())
                .toList();
    }

    public List<EquipmentInspection> findOpenInspections() {
        return INSPECTIONS.stream()
                .filter(inspection -> !inspection.isResolved())
                .sorted(Comparator.comparing(EquipmentInspection::getInspectionTime).reversed())
                .toList();
    }

    public List<EquipmentInspection> findUnresolvedBySerial(String serial) {
        if (serial == null) return List.of();
        return INSPECTIONS.stream()
                .filter(i -> !i.isResolved() && serial.equalsIgnoreCase(i.getEquipmentSerial()))
                .toList();
    }

    public EquipmentInspection findById(int inspectionId) {
        return INSPECTIONS.stream()
                .filter(i -> i.getInspectionId() == inspectionId)
                .findFirst()
                .orElse(null);
    }
}
