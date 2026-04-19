package fitwell.persistence.api;

import fitwell.domain.equipment.EquipmentAssignment;
import java.sql.SQLException;
import java.util.List;

public interface EquipmentAssignmentRepository {
    int insert(EquipmentAssignment a) throws SQLException;
    List<EquipmentAssignment> findActiveByClassId(int classId) throws SQLException;
    List<EquipmentAssignment> findActiveByEquipmentSerial(String serial) throws SQLException;
    void deactivateById(int assignmentId) throws SQLException;
    int sumActiveQtyByClassIdAndSerial(int classId, String serial) throws SQLException;
}
