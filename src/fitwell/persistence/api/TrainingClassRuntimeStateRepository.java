package fitwell.persistence.api;

import fitwell.domain.training.TrainingClassStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface TrainingClassRuntimeStateRepository {

    RuntimeState getState(int classId);

    void saveTips(int classId, List<String> tips);

    void saveStatus(int classId, TrainingClassStatus status);

    void saveSuspensionInfo(int classId, TrainingClassStatus statusBefore, LocalDateTime suspendedAt);

    void clearSuspensionInfo(int classId);

    class RuntimeState {
        private List<String> tips;
        private TrainingClassStatus status;
        private TrainingClassStatus statusBeforeSuspension;
        private LocalDateTime suspendedAt;

        public List<String> getTips() { return tips; }
        public void setTips(List<String> tips) { this.tips = tips; }
        public TrainingClassStatus getStatus() { return status; }
        public void setStatus(TrainingClassStatus status) { this.status = status; }
        public TrainingClassStatus getStatusBeforeSuspension() { return statusBeforeSuspension; }
        public void setStatusBeforeSuspension(TrainingClassStatus s) { this.statusBeforeSuspension = s; }
        public LocalDateTime getSuspendedAt() { return suspendedAt; }
        public void setSuspendedAt(LocalDateTime t) { this.suspendedAt = t; }
    }
}
