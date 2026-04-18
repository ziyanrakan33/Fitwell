package fitwell.domain.training;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrainingClass {
    private Integer classId; // AutoNumber in DB
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String type;
    private int maxParticipants;
    private int consultantId;
    private TrainingClassStatus status;
    private final List<String> tips = new ArrayList<>();
    private LocalDateTime suspendedAt;
    private int suspendedMinutes;
    private boolean limitedFunctionality;
    private String limitedFunctionalityReason;

    public TrainingClass(Integer classId, String name, LocalDateTime startTime, LocalDateTime endTime,
                         String type, int maxParticipants, int consultantId) {
        this.classId = classId;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.maxParticipants = maxParticipants;
        this.consultantId = consultantId;
        this.status = TrainingClassStatus.SCHEDULED;
    }

    /**
     * Constructor for NEW class creation (before DB generates AutoNumber classId)
     */
    public TrainingClass(String name, LocalDateTime startTime, LocalDateTime endTime,
                         String type, int maxParticipants, int consultantId) {
        this(null, name, startTime, endTime, type, maxParticipants, consultantId);
    }

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }

    public String getName() { return name; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getType() { return type; }
    public int getMaxParticipants() { return maxParticipants; }
    public int getConsultantId() { return consultantId; }
    public TrainingClassStatus getStatus() { return status; }
    public LocalDateTime getSuspendedAt() { return suspendedAt; }
    public int getSuspendedMinutes() { return suspendedMinutes; }
    public List<String> getTips() { return Collections.unmodifiableList(tips); }

    public void setName(String name) { this.name = name; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setType(String type) { this.type = type; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    public void setConsultantId(int consultantId) { this.consultantId = consultantId; }
    public void setStatus(TrainingClassStatus status) { this.status = status == null ? TrainingClassStatus.SCHEDULED : status; }

    public void setTips(List<String> values) {
        tips.clear();
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String tip = value.trim();
            if (!tip.isEmpty()) {
                tips.add(tip);
            }
            if (tips.size() == 5) {
                break;
            }
        }
    }

    public boolean isLimitedFunctionality() { return limitedFunctionality; }
    public String getLimitedFunctionalityReason() { return limitedFunctionalityReason; }

    public void markLimitedFunctionality(String reason) {
        this.limitedFunctionality = true;
        this.limitedFunctionalityReason = reason == null ? "" : reason.trim();
    }

    public void clearLimitedFunctionality() {
        this.limitedFunctionality = false;
        this.limitedFunctionalityReason = null;
    }

    public void suspend(LocalDateTime suspendedAt) {
        this.status = TrainingClassStatus.SUSPENDED;
        this.suspendedAt = suspendedAt == null ? LocalDateTime.now() : suspendedAt;
    }

    public void resumeFromSuspension() {
        if (suspendedAt != null) {
            suspendedMinutes += (int) Math.max(0, java.time.Duration.between(suspendedAt, LocalDateTime.now()).toMinutes());
        }
        this.suspendedAt = null;
        this.status = TrainingClassStatus.SCHEDULED;
    }
}
