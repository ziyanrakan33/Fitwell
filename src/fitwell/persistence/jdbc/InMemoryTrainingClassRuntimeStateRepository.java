package fitwell.persistence.jdbc;

import fitwell.domain.training.TrainingClassStatus;
import fitwell.persistence.api.TrainingClassRuntimeStateRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTrainingClassRuntimeStateRepository implements TrainingClassRuntimeStateRepository {

    private static final Map<Integer, RuntimeState> STATE_BY_CLASS_ID = new HashMap<>();

    public RuntimeState getState(int classId) {
        return STATE_BY_CLASS_ID.computeIfAbsent(classId, key -> {
            RuntimeState s = new RuntimeState();
            s.setTips(new ArrayList<>());
            s.setStatus(TrainingClassStatus.SCHEDULED);
            return s;
        });
    }

    public void saveTips(int classId, List<String> tips) {
        getState(classId).setTips(tips == null ? new ArrayList<>() : new ArrayList<>(tips));
    }

    public void saveStatus(int classId, TrainingClassStatus status) {
        getState(classId).setStatus(status == null ? TrainingClassStatus.SCHEDULED : status);
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
