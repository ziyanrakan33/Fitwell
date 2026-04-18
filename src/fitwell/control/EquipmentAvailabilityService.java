package fitwell.control;

import fitwell.entity.EquipmentAssignment;
import fitwell.repo.EquipmentAssignmentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks if requested equipment quantity is available for a class time window,
 * considering overlapping classes and existing assignments.
 * Uses a ClassTimeProvider adapter to resolve class start/end times.
 */
public class EquipmentAvailabilityService {

    public interface ClassTimeProvider {
        ClassTime getClassTimeById(int classId) throws Exception;
    }

    public static class ClassTime {
        public final int classId;
        public final LocalDateTime start;
        public final LocalDateTime end;

        public ClassTime(int classId, LocalDateTime start, LocalDateTime end) {
            this.classId = classId;
            this.start = start;
            this.end = end;
        }
    }

    public static class AvailabilityResult {
        public boolean ok;
        public String message;
        public int availableQtyAtTime;
        public int requestedQty;
        public int totalInventoryQty;
        public final List<Integer> overlappingClassIds = new ArrayList<>();
    }

    private final EquipmentAssignmentRepository assignmentRepo;
    private final ClassTimeProvider classTimeProvider;

    public EquipmentAvailabilityService(EquipmentAssignmentRepository assignmentRepo,
                                        ClassTimeProvider classTimeProvider) {
        this.assignmentRepo = assignmentRepo;
        this.classTimeProvider = classTimeProvider;
    }

    public AvailabilityResult checkAvailability(int targetClassId,
                                                String equipmentSerial,
                                                int requestedQty,
                                                int totalInventoryQty) throws Exception {
        AvailabilityResult r = new AvailabilityResult();
        r.requestedQty = requestedQty;
        r.totalInventoryQty = totalInventoryQty;

        if (equipmentSerial == null || equipmentSerial.trim().isEmpty()) {
            r.ok = false;
            r.message = "Equipment serial is required.";
            return r;
        }
        if (requestedQty <= 0) {
            r.ok = false;
            r.message = "Assigned quantity must be greater than 0.";
            return r;
        }
        if (totalInventoryQty < 0) {
            r.ok = false;
            r.message = "Inventory quantity cannot be negative.";
            return r;
        }

        ClassTime target = classTimeProvider.getClassTimeById(targetClassId);
        if (target == null) {
            r.ok = false;
            r.message = "Class time not found for class ID " + targetClassId;
            return r;
        }

        List<EquipmentAssignment> sameEquipmentAssignments = assignmentRepo.findActiveByEquipmentSerial(equipmentSerial);

        int reservedByOverlaps = 0;
        for (EquipmentAssignment a : sameEquipmentAssignments) {
            if (a.getClassId() == targetClassId) continue; // same class handled separately (update flow)
            ClassTime other = classTimeProvider.getClassTimeById(a.getClassId());
            if (other == null) continue;

            if (overlaps(target.start, target.end, other.start, other.end)) {
                reservedByOverlaps += a.getQtyAssigned();
                r.overlappingClassIds.add(a.getClassId());
            }
        }

        int available = totalInventoryQty - reservedByOverlaps;
        r.availableQtyAtTime = Math.max(available, 0);

        if (requestedQty > available) {
            r.ok = false;
            r.message = "Not enough available quantity during overlapping time. " +
                    "Requested=" + requestedQty + ", Available=" + r.availableQtyAtTime +
                    ", ReservedByOverlaps=" + reservedByOverlaps +
                    (r.overlappingClassIds.isEmpty() ? "" : ", OverlappingClasses=" + r.overlappingClassIds);
            return r;
        }

        r.ok = true;
        r.message = "Equipment is available for assignment.";
        return r;
    }

    private boolean overlaps(LocalDateTime aStart, LocalDateTime aEnd,
                             LocalDateTime bStart, LocalDateTime bEnd) {
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) return false;
        // [start, end) overlap logic
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }
}