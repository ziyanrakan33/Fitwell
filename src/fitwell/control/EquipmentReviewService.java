package fitwell.control;

import fitwell.domain.equipment.Equipment;
import fitwell.domain.equipment.EquipmentCategory;
import fitwell.domain.equipment.EquipmentInspection;
import fitwell.domain.equipment.EquipmentStatus;
import fitwell.integration.NotificationGateway;
import fitwell.repo.EquipmentInspectionRepository;
import fitwell.repo.EquipmentRepository;

import java.util.ArrayList;
import java.util.List;

public class EquipmentReviewService {
    private final EquipmentRepository equipmentRepository;
    private final NotificationGateway notificationGateway;
    private EquipmentInspectionRepository equipmentInspectionRepository;

    public EquipmentReviewService(EquipmentRepository equipmentRepository,
                                  NotificationGateway notificationGateway) {
        this.equipmentRepository = equipmentRepository;
        this.notificationGateway = notificationGateway;
    }

    public void setEquipmentInspectionRepository(EquipmentInspectionRepository repo) {
        this.equipmentInspectionRepository = repo;
    }

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    public List<Equipment> getFlaggedEquipment() {
        List<Equipment> flagged = new ArrayList<>();
        for (Equipment equipment : equipmentRepository.findAll()) {
            if (equipment.isFlagged()) {
                flagged.add(equipment);
            }
        }
        return flagged;
    }

    public Equipment findBySerial(String serial) {
        return equipmentRepository.findBySerial(serial);
    }

    public void updateEquipment(String serial, String name, String description, EquipmentCategory category,
                                EquipmentStatus status, int quantity, int x, int y, int shelf) {
        Equipment equipment = require(serial);
        equipment.setDetails(name, description, category);
        equipment.setLocation(x, y, shelf);
        int delta = quantity - equipment.getQuantity();
        if (delta > 0) {
            equipment.increaseQuantity(delta);
        } else if (delta < 0) {
            equipment.decreaseQuantity(-delta);
        }
        if (status == EquipmentStatus.OUT_OF_SERVICE) {
            equipment.markOutOfService("Updated by consultant");
        } else if (status == EquipmentStatus.NON_USABLE) {
            equipment.markNonUsable();
        } else {
            equipment.returnToService();
            equipment.unflag();
        }
        equipmentRepository.save(equipment);
    }

    public void markOutOfService(String serial, String reason) {
        Equipment equipment = require(serial);
        equipment.markOutOfService(reason);
        equipmentRepository.save(equipment);
        notificationGateway.notifyConsultants("Equipment " + serial + " marked out of service. " + reason);
    }

    public void returnToService(String serial) {
        Equipment equipment = require(serial);
        if (equipmentInspectionRepository != null) {
            List<EquipmentInspection> unresolved = equipmentInspectionRepository.findUnresolvedBySerial(serial);
            if (!unresolved.isEmpty()) {
                throw new IllegalStateException(
                        "Cannot return equipment " + serial + " to service: "
                        + unresolved.size() + " unresolved inspection(s) must be resolved first.");
            }
        }
        equipment.returnToService();
        equipment.unflag();
        equipmentRepository.save(equipment);
    }

    public void resolveInspection(int inspectionId, String resolutionNote) {
        if (equipmentInspectionRepository == null) {
            throw new IllegalStateException("Inspection repository not available.");
        }
        EquipmentInspection inspection = equipmentInspectionRepository.findById(inspectionId);
        if (inspection == null) {
            throw new IllegalArgumentException("Inspection not found: " + inspectionId);
        }
        inspection.resolve(resolutionNote);
    }

    public void markReviewed(String serial) {
        Equipment equipment = require(serial);
        equipment.unflag();
        equipmentRepository.save(equipment);
    }

    public void markNonUsableAndRemove(String serial) {
        Equipment equipment = require(serial);
        equipment.markNonUsable();
        equipmentRepository.save(equipment);
        equipmentRepository.delete(serial);
        notificationGateway.notifyConsultants("Equipment " + serial + " was removed from service permanently.");
    }

    public void save(Equipment equipment) {
        equipmentRepository.save(equipment);
    }

    private Equipment require(String serial) {
        Equipment equipment = equipmentRepository.findBySerial(serial);
        if (equipment == null) {
            throw new IllegalArgumentException("Equipment not found: " + serial);
        }
        return equipment;
    }
}
