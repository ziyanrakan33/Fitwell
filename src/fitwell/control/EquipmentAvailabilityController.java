package fitwell.control;

import fitwell.domain.equipment.ClassEquipmentAssignment;
import fitwell.domain.equipment.Equipment;
import fitwell.domain.equipment.EquipmentStatus;
import fitwell.domain.training.TrainingClass;
import fitwell.repo.ClassEquipmentAssignmentRepository;
import fitwell.repo.EquipmentRepository;
import fitwell.repo.TrainingClassRepository;

import java.time.LocalDateTime;
import java.util.*;

public class EquipmentAvailabilityController {

    private final EquipmentRepository equipmentRepo;
    private final TrainingClassRepository classRepo;
    private final ClassEquipmentAssignmentRepository assignmentRepo;

    public EquipmentAvailabilityController(EquipmentRepository equipmentRepo,
                                           TrainingClassRepository classRepo,
                                           ClassEquipmentAssignmentRepository assignmentRepo) {
        this.equipmentRepo = equipmentRepo;
        this.classRepo = classRepo;
        this.assignmentRepo = assignmentRepo;
    }

    /**
     * Validate assignments for a class BEFORE save/update.
     * @param candidateClass the class being created/updated (may have null classId for create)
     * @param requested list of equipment assignments for candidate class
     * @param existingClassIdForUpdate pass classId on update (to exclude its old assignments from overlap calc), else null
     */
    public void validateAvailabilityForClass(TrainingClass candidateClass,
                                             List<ClassEquipmentAssignment> requested,
                                             Integer existingClassIdForUpdate) {

        if (candidateClass == null) throw new IllegalArgumentException("Class is null");
        if (candidateClass.getStartTime() == null || candidateClass.getEndTime() == null) {
            throw new IllegalArgumentException("Class start/end time is required for equipment availability check.");
        }
        if (!candidateClass.getEndTime().isAfter(candidateClass.getStartTime())) {
            throw new IllegalArgumentException("Class end time must be after start time.");
        }

        if (requested == null || requested.isEmpty()) return;

        // Normalize duplicates (same serial repeated) by summing quantities
        Map<String, Integer> requestedBySerial = new LinkedHashMap<>();
        for (ClassEquipmentAssignment a : requested) {
            if (a == null) continue;
            String serial = safe(a.getSerialNumber()).trim();
            if (serial.isEmpty()) throw new IllegalArgumentException("Equipment serial is required.");
            if (a.getRequiredQuantity() <= 0) throw new IllegalArgumentException("Required quantity must be > 0 for " + serial);
            requestedBySerial.merge(serial, a.getRequiredQuantity(), Integer::sum);
        }

        List<Equipment> allEquipment = safeList(equipmentRepo.findAll());
        Map<String, Equipment> equipmentMap = new HashMap<>();
        for (Equipment e : allEquipment) {
            if (e != null && e.getSerialNumber() != null) {
                equipmentMap.put(e.getSerialNumber().trim(), e);
            }
        }

        List<TrainingClass> allClasses = safeList(classRepo.findAll());
        List<ClassEquipmentAssignment> allAssignments = safeList(assignmentRepo.findAll());

        for (Map.Entry<String, Integer> reqEntry : requestedBySerial.entrySet()) {
            String serial = reqEntry.getKey();
            int requestedQty = reqEntry.getValue();

            Equipment eq = equipmentMap.get(serial);
            if (eq == null) {
                throw new IllegalArgumentException("Equipment not found: " + serial);
            }

            if (eq.getStatus() == EquipmentStatus.NON_USABLE) {
                throw new IllegalArgumentException("Equipment " + serial + " is NON_USABLE.");
            }

            if (eq.getStatus() == EquipmentStatus.OUT_OF_SERVICE) {
                throw new IllegalArgumentException("Equipment " + serial + " is OUT_OF_SERVICE.");
            }

            int totalAvailable = Math.max(0, eq.getQuantity());
            if (requestedQty > totalAvailable) {
                throw new IllegalArgumentException("Equipment " + serial + " requested qty (" + requestedQty +
                        ") exceeds total available (" + totalAvailable + ").");
            }

            int overlappingDemand = 0;

            for (ClassEquipmentAssignment existingAssign : allAssignments) {
                if (existingAssign == null) continue;
                if (!serial.equalsIgnoreCase(safe(existingAssign.getSerialNumber()).trim())) continue;

                int existingClassId = existingAssign.getClassId();

                // Exclude same class old assignments when editing
                if (existingClassIdForUpdate != null && existingClassId == existingClassIdForUpdate) continue;

                TrainingClass existingClass = findClassById(allClasses, existingClassId);
                if (existingClass == null) continue;

                if (overlaps(candidateClass.getStartTime(), candidateClass.getEndTime(),
                             existingClass.getStartTime(), existingClass.getEndTime())) {
                    overlappingDemand += Math.max(0, existingAssign.getRequiredQuantity());
                }
            }

            int totalDemandDuringOverlap = overlappingDemand + requestedQty;
            if (totalDemandDuringOverlap > totalAvailable) {
                throw new IllegalArgumentException(
                        "Equipment conflict for " + serial +
                        ": required=" + requestedQty +
                        ", overlapping demand=" + overlappingDemand +
                        ", total during overlap=" + totalDemandDuringOverlap +
                        ", available=" + totalAvailable + "."
                );
            }
        }
    }

    private boolean overlaps(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        if (s1 == null || e1 == null || s2 == null || e2 == null) return false;
        // [s1,e1) overlaps [s2,e2) if s1 < e2 && s2 < e1
        return s1.isBefore(e2) && s2.isBefore(e1);
    }

    private TrainingClass findClassById(List<TrainingClass> classes, int classId) {
        for (TrainingClass tc : classes) {
            if (tc != null && tc.getClassId() != null && tc.getClassId() == classId) return tc;
        }
        return null;
    }

    private String safe(String s) { return s == null ? "" : s; }

    private <T> List<T> safeList(List<T> list) { return list == null ? Collections.emptyList() : list; }
}