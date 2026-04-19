package fitwell.persistence.api;

import fitwell.domain.equipment.Equipment;
import java.util.List;

public interface EquipmentRepository {
    void save(Equipment e);
    Equipment findBySerial(String serial);
    List<Equipment> findAll();
    boolean delete(String serial);
}
