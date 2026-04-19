package fitwell.persistence.api;

import fitwell.domain.equipment.ClassEquipmentAssignment;
import java.util.List;

public interface ClassEquipmentAssignmentRepository {
    List<ClassEquipmentAssignment> findAll();
    List<ClassEquipmentAssignment> findByClassId(int classId);
    void insert(ClassEquipmentAssignment a);
    void deleteByClassId(int classId);
    void replaceForClass(int classId, List<ClassEquipmentAssignment> assignments);
}
