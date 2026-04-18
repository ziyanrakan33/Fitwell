package fitwell.repo;

import fitwell.entity.TrainingClassStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainingClassRuntimeStateRepository {
    public static class RuntimeState {
        private List<String> tips = new ArrayList<>();
        private TrainingClassStatus status = TrainingClassStatus.SCHEDULED;
        private TrainingClassStatus statusBeforeSuspension;
        private LocalDateTime suspendedAt;

        public List<String> getTips() {
            return tips;
        }

        public void setTips(List<String> tips) {
            this.tips = tips == null ? new ArrayList<>() : new ArrayList<>(tips);
        }

        public TrainingClassStatus getStatus() {
            return status;
        }

        public void setStatus(TrainingClassStatus status) {
            this.status = status == null ? TrainingClassStatus.SCHEDULED : status;
        }

        public TrainingClassStatus getStatusBeforeSuspension() {
            return statusBeforeSuspension;
        }

        public void setStatusBeforeSuspension(TrainingClassStatus statusBeforeSuspension) {
            this.statusBeforeSuspension = statusBeforeSuspension;
        }

        public LocalDateTime getSuspendedAt() {
            return suspendedAt;
        }

        public void setSuspendedAt(LocalDateTime suspendedAt) {
            this.suspendedAt = suspendedAt;
        }
    }

    private static final Map<Integer, RuntimeState> STATE_BY_CLASS_ID = new HashMap<>();

    public RuntimeState getState(int classId) {
        return STATE_BY_CLASS_ID.computeIfAbsent(classId, key -> new RuntimeState());
    }

    public void saveTips(int classId, List<String> tips) {
        getState(classId).setTips(tips);
    }

    public void saveStatus(int classId, TrainingClassStatus status) {
        getState(classId).setStatus(status);
    }

    public void saveSuspensionInfo(int classId, TrainingClassStatus statusBefore, LocalDateTime suspendedAt) {
        RuntimeState state = getState(classId);
        state.setStatusBeforeSuspension(statusBefore);
        state.setSuspendedAt(suspendedAt);
    }

    public void clearSuspensionInfo(int classId) {
        RuntimeState state = getState(classId);
        state.setStatusBeforeSuspension(null);
        state.setSuspendedAt(null);
    }
}
