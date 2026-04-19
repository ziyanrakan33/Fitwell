package fitwell.controller;
import fitwell.service.equipment.EquipmentAvailabilityService;

import fitwell.domain.equipment.EquipmentAssignment;
import fitwell.persistence.api.EquipmentAssignmentRepository;

/**
 * Controller for equipment assignment workflow.
 * Handles quantity lookup and equipment status validation.
 */
public class EquipmentAssignmentController {

    public interface EquipmentInfoProvider {
        EquipmentInfo getBySerial(String serial) throws Exception;
    }

    public static class EquipmentInfo {
        public String serial;
        public String name;
        public int qty;
        public String status; // ACTIVE / OUT_OF_SERVICE / NON_USABLE
    }

    public static class AssignmentResponse {
        public boolean success;
        public String message;
        public EquipmentAvailabilityService.AvailabilityResult availability;
        public Integer assignmentId;
    }

    private final EquipmentAssignmentRepository assignmentRepo;
    private final EquipmentAvailabilityService availabilityService;
    private final EquipmentInfoProvider equipmentInfoProvider;

    public EquipmentAssignmentController(EquipmentAssignmentRepository assignmentRepo,
                                         EquipmentAvailabilityService availabilityService,
                                         EquipmentInfoProvider equipmentInfoProvider) {
        this.assignmentRepo = assignmentRepo;
        this.availabilityService = availabilityService;
        this.equipmentInfoProvider = equipmentInfoProvider;
    }

    public AssignmentResponse assignEquipmentToClass(int classId,
                                                     String equipmentSerial,
                                                     int qty,
                                                     Integer consultantId,
                                                     String notes) {
        AssignmentResponse res = new AssignmentResponse();

        try {
            EquipmentInfo eq = equipmentInfoProvider.getBySerial(equipmentSerial);
            if (eq == null) {
                res.success = false;
                res.message = "Equipment not found for serial: " + equipmentSerial;
                return res;
            }

            String status = eq.status == null ? "" : eq.status.trim().toUpperCase();
            if ("OUT_OF_SERVICE".equals(status)) {
                res.success = false;
                res.message = "Equipment is OUT_OF_SERVICE and cannot be assigned.";
                return res;
            }
            if ("NON_USABLE".equals(status) || "NON-USABLE".equals(status)) {
                res.success = false;
                res.message = "Equipment is NON_USABLE and cannot be assigned.";
                return res;
            }

            EquipmentAvailabilityService.AvailabilityResult ar =
                    availabilityService.checkAvailability(classId, equipmentSerial, qty, eq.qty);
            res.availability = ar;

            if (!ar.ok) {
                res.success = false;
                res.message = ar.message;
                return res;
            }

            EquipmentAssignment a = new EquipmentAssignment(classId, equipmentSerial, qty);
            a.setAssignedByConsultantId(consultantId);
            a.setNotes(notes);
            int id = assignmentRepo.insert(a);

            res.success = true;
            res.assignmentId = id;
            res.message = "Equipment assigned successfully.";
            return res;

        } catch (Exception ex) {
            res.success = false;
            res.message = "Assignment failed: " + ex.getMessage();
            return res;
        }
    }
}