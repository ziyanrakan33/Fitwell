package fitwell.persistence.api;

import fitwell.domain.equipment.EquipmentInspection;
import java.util.List;

public interface EquipmentInspectionRepository {
    EquipmentInspection save(EquipmentInspection inspection);
    List<EquipmentInspection> findAll();
    List<EquipmentInspection> findOpenInspections();
    List<EquipmentInspection> findUnresolvedBySerial(String serial);
    EquipmentInspection findById(int inspectionId);
}
