package fitwell.domain.emergency;

import java.time.LocalDateTime;

public class EmergencyAlert {
    private final int alertId;
    private final LocalDateTime activatedAt;
    private LocalDateTime deactivatedAt;
    private LocalDateTime autoResumeAt;
    private boolean active;
    private final String message;

    public EmergencyAlert(int alertId, LocalDateTime activatedAt, String message) {
        this.alertId = alertId;
        this.activatedAt = activatedAt == null ? LocalDateTime.now() : activatedAt;
        this.message = message == null ? "Emergency alert activated" : message.trim();
        this.active = true;
    }

    public int getAlertId() { return alertId; }
    public LocalDateTime getActivatedAt() { return activatedAt; }
    public LocalDateTime getDeactivatedAt() { return deactivatedAt; }
    public LocalDateTime getAutoResumeAt() { return autoResumeAt; }
    public boolean isActive() { return active; }
    public String getMessage() { return message; }

    public void deactivate(LocalDateTime deactivatedAt, LocalDateTime autoResumeAt) {
        this.active = false;
        this.deactivatedAt = deactivatedAt == null ? LocalDateTime.now() : deactivatedAt;
        this.autoResumeAt = autoResumeAt;
    }
}
