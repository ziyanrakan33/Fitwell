package fitwell.controller;

import java.time.LocalDateTime;

import fitwell.integration.ImageExtractionService;
import fitwell.integration.NotificationGateway;
import fitwell.domain.equipment.Equipment;
import fitwell.domain.equipment.EquipmentBatch;
import fitwell.domain.equipment.EquipmentCategory;
import fitwell.domain.equipment.EquipmentLocation;
import fitwell.domain.equipment.EquipmentStatus;
import fitwell.domain.equipment.EquipmentUpdate;
import fitwell.domain.reports.ExtractionResult;
import fitwell.persistence.api.EquipmentRepository;
import fitwell.util.SimpleJsonParser;

public class EquipmentManagementController {

    private final EquipmentRepository repo;
    private final ImageExtractionService imageToTextService;
    private final NotificationGateway notificationService;

    public EquipmentManagementController(EquipmentRepository repo,
                                         ImageExtractionService imageToTextService,
                                         NotificationGateway notificationService) {
        this.repo = repo;
        this.imageToTextService = imageToTextService;
        this.notificationService = notificationService;
    }

    public EquipmentRepository getRepo() { return repo; }

    // ECB operation: create or update equipment (manual)
    public void createOrUpdateEquipment(Equipment e) {
        repo.save(e);
    }

    // Update details
    public void updateEquipmentDetails(String serial, String name, String desc,
                                       EquipmentCategory cat, EquipmentStatus status,
                                       int qty, int x, int y, int shelf) {

        Equipment e = mustGet(serial);

        e.setDetails(name, desc, cat);
        e.setLocation(x, y, shelf);

        int current = e.getQuantity();
        if (qty > current) e.increaseQuantity(qty - current);
        else e.decreaseQuantity(current - qty);

        if (status == EquipmentStatus.OUT_OF_SERVICE) {
            e.markOutOfService("Manual update");
        } else if (status == EquipmentStatus.NON_USABLE) {
            e.markNonUsable();
        } else {
            e.returnToService();
        }

        repo.save(e);
    }

    public void removeEquipment(String serial) {
        repo.delete(serial);
    }

    public void reduceQuantity(String serial, int amount) {
        Equipment e = mustGet(serial);
        e.decreaseQuantity(amount);
        repo.save(e);
    }

    public void markOutOfService(String serial, String reason) {
        Equipment e = mustGet(serial);
        e.markOutOfService(reason);
        repo.save(e);
        notificationService.notifyConsultants(
                "Equipment " + serial + " marked OUT OF SERVICE. Reason: " + reason
        );
    }

    public void returnToService(String serial) {
        Equipment e = mustGet(serial);
        e.returnToService();
        e.unflag();
        repo.save(e);
    }

    public void markNonUsableAndRemove(String serial) {
        Equipment e = mustGet(serial);
        e.markNonUsable();
        repo.save(e);
        repo.delete(serial);
        notificationService.notifyConsultants("Equipment " + serial + " removed (NON_USABLE).");
    }

    public void flag(String serial, String reason) {
        Equipment e = mustGet(serial);
        e.flag(reason);
        repo.save(e);
    }

    public void unflag(String serial) {
        Equipment e = mustGet(serial);
        e.unflag();
        repo.save(e); 
    }

    // ===== SwiftFit JSON Import =====

    public EquipmentBatch importMonthlyUpdate(String json) {
        EquipmentBatch batch = SimpleJsonParser.parseUpdateBatch(json);

        if (batch == null) {
            batch = new EquipmentBatch("BATCH-" + System.currentTimeMillis(), LocalDateTime.now(), "SwiftFit");
        }

        for (EquipmentUpdate u : batch.getUpdates()) {
            applyUpdate(u);
        }
        return batch;
    }

    private EquipmentCategory mapCategory(String s) {
        if (s == null || s.isBlank()) return EquipmentCategory.other;

        String normalized = s.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase();

        // try exact match
        try {
            return EquipmentCategory.valueOf(normalized);
        } catch (Exception ignored) {}

        // try match ignoring case/underscores
        for (EquipmentCategory c : EquipmentCategory.values()) {
            String enumNorm = c.name().trim().replace('-', '_').replace(' ', '_').toUpperCase();
            if (enumNorm.equals(normalized)) return c;
        }

        return EquipmentCategory.other;
    }


    public void applyUpdate(EquipmentUpdate update) {
        if (update == null) return;
        String serial = update.getSerialNumber();
        if (serial == null || serial.isBlank()) return;
        Equipment e = repo.findBySerial(serial);
        int desiredQty = Math.max(0, update.getNewQuantity());
        int x = update.getX();
        int y = update.getY();
        int shelf = update.getShelf();
        String jsonName = update.getName();
        String jsonCategory = update.getCategory();

        if (e == null) {
            EquipmentCategory cat = (jsonCategory == null || jsonCategory.isBlank())
                    ? EquipmentCategory.other
                    : mapCategory(jsonCategory);

            String name = (jsonName == null || jsonName.isBlank()) ? "" : jsonName;

            e = new Equipment(serial, name, "", cat,
                    desiredQty,
                    EquipmentStatus.IN_SERVICE,
                    new EquipmentLocation(x, y, shelf));

            repo.save(e);
        } else {
            int current = e.getQuantity();
            if (desiredQty > current) e.increaseQuantity(desiredQty - current);
            else e.decreaseQuantity(current - desiredQty);
            e.setLocation(x, y, shelf);
            if (jsonName != null && !jsonName.isBlank()) {
                e.setDetails(jsonName, e.getDescription(), e.getCategory());
            }
            if (jsonCategory != null && !jsonCategory.isBlank()) {
                e.setDetails(e.getName(), e.getDescription(), mapCategory(jsonCategory));
            }

            repo.save(e);
        }

        requestExtractionIfPhoto(update, e);
    }

    private void requestExtractionIfPhoto(EquipmentUpdate update, Equipment e) {
        if (update == null || e == null) return;

        String photoUrl = update.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) return;

        ExtractionResult r = imageToTextService.extractFromImage(photoUrl);

        if (r != null && r.isComplete() && r.getConfidence() >= 0.7) {
            e.setDetails(r.getExtractedName(), r.getExtractedDescription(), r.getExtractedCategory());
        } else {
            e.flag("Low confidence extraction");
            notificationService.notifyConsultants(
                    "Equipment " + e.getSerialNumber() + " flagged for review (image extraction)."
            );
        }
    }


    private Equipment mustGet(String serial) {
        Equipment e = repo.findBySerial(serial);
        if (e == null) throw new IllegalArgumentException("Equipment not found: " + serial);
        return e;
    }
}
