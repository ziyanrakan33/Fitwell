package fitwell.domain.equipment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EquipmentBatch {
    private final String batchId;
    private final LocalDateTime receivedAt;
    private final String source;
    private final List<EquipmentUpdate> updates = new ArrayList<>();

    public EquipmentBatch(String batchId, LocalDateTime receivedAt, String source) {
        this.batchId = batchId;
        this.receivedAt = receivedAt;
        this.source = source;
    }

    public String getBatchId() { return batchId; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public String getSource() { return source; }

    public void addUpdate(EquipmentUpdate u) {
        if (u != null) updates.add(u);
    }

    public List<EquipmentUpdate> getUpdates() {
        return Collections.unmodifiableList(updates);
    }
}
