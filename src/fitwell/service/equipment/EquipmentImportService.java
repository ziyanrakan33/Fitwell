package fitwell.service.equipment;

import fitwell.domain.equipment.Equipment;
import fitwell.domain.equipment.EquipmentCategory;
import fitwell.domain.equipment.EquipmentLocation;
import fitwell.domain.equipment.EquipmentStatus;
import fitwell.domain.equipment.EquipmentUpdate;
import fitwell.domain.equipment.EquipmentBatch;
import fitwell.domain.reports.ExtractionResult;
import fitwell.integration.ImageExtractionService;
import fitwell.integration.NotificationGateway;
import fitwell.integration.SwiftFitGateway;
import fitwell.persistence.api.EquipmentRepository;
import fitwell.util.SimpleJsonParser;

import java.time.LocalDateTime;

public class EquipmentImportService {
    private final EquipmentRepository equipmentRepository;
    private final ImageExtractionService imageExtractionService;
    private final NotificationGateway notificationGateway;
    private final SwiftFitGateway swiftFitGateway;

    public EquipmentImportService(EquipmentRepository equipmentRepository,
                                  ImageExtractionService imageExtractionService,
                                  NotificationGateway notificationGateway,
                                  SwiftFitGateway swiftFitGateway) {
        this.equipmentRepository = equipmentRepository;
        this.imageExtractionService = imageExtractionService;
        this.notificationGateway = notificationGateway;
        this.swiftFitGateway = swiftFitGateway;
    }

    public EquipmentBatch importMonthlyUpdate(String json) {
        swiftFitGateway.receiveMonthlyEquipmentUpdates(json);
        EquipmentBatch batch = SimpleJsonParser.parseUpdateBatch(json);
        if (batch == null) {
            batch = new EquipmentBatch("BATCH-" + System.currentTimeMillis(), LocalDateTime.now(), "SwiftFit");
        }

        for (EquipmentUpdate update : batch.getUpdates()) {
            applyUpdate(update);
        }
        return batch;
    }

    public void applyUpdate(EquipmentUpdate update) {
        if (update == null || update.getSerialNumber() == null || update.getSerialNumber().isBlank()) {
            return;
        }

        Equipment equipment = equipmentRepository.findBySerial(update.getSerialNumber());
        if (equipment == null) {
            equipment = new Equipment(update.getSerialNumber(),
                    valueOrBlank(update.getName()),
                    "",
                    mapCategory(update.getCategory()),
                    Math.max(0, update.getNewQuantity()),
                    EquipmentStatus.IN_SERVICE,
                    new EquipmentLocation(update.getX(), update.getY(), update.getShelf()));
        } else {
            if (update.getName() != null && !update.getName().isBlank()) {
                equipment.setDetails(update.getName(), equipment.getDescription(), equipment.getCategory());
            }
            if (update.getCategory() != null && !update.getCategory().isBlank()) {
                equipment.setDetails(equipment.getName(), equipment.getDescription(), mapCategory(update.getCategory()));
            }
            int desiredQuantity = Math.max(0, update.getNewQuantity());
            if (desiredQuantity > equipment.getQuantity()) {
                equipment.increaseQuantity(desiredQuantity - equipment.getQuantity());
            } else if (desiredQuantity < equipment.getQuantity()) {
                equipment.decreaseQuantity(equipment.getQuantity() - desiredQuantity);
            }
            equipment.setLocation(update.getX(), update.getY(), update.getShelf());
        }

        if (update.getPhotoUrl() != null && !update.getPhotoUrl().isBlank()) {
            ExtractionResult extractionResult = imageExtractionService.extractFromImage(update.getPhotoUrl());
            if (extractionResult != null && extractionResult.isComplete() && extractionResult.getConfidence() >= 0.7) {
                equipment.setDetails(extractionResult.getExtractedName(),
                        extractionResult.getExtractedDescription(),
                        extractionResult.getExtractedCategory());
            } else {
                equipment.flag("Low-confidence image extraction; consultant review required.");
                notificationGateway.notifyConsultants("Equipment " + equipment.getSerialNumber()
                        + " needs consultant review after image extraction.");
            }
        }

        equipmentRepository.save(equipment);
    }

    private EquipmentCategory mapCategory(String value) {
        if (value == null || value.isBlank()) {
            return EquipmentCategory.other;
        }
        try {
            return EquipmentCategory.valueOf(value.trim().replace('-', '_').replace(' ', '_').toLowerCase());
        } catch (IllegalArgumentException ex) {
            for (EquipmentCategory category : EquipmentCategory.values()) {
                if (category.name().equalsIgnoreCase(value.trim())) {
                    return category;
                }
            }
            return EquipmentCategory.other;
        }
    }

    private String valueOrBlank(String value) {
        return value == null ? "" : value.trim();
    }
}
