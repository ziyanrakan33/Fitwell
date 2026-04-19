package fitwell.persistence.jdbc;

import fitwell.domain.registration.AttendanceStatus;
import fitwell.persistence.api.AttendanceRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class InMemoryAttendanceRepository implements AttendanceRepository {

    private static final Map<String, AttendanceRecord> ATTENDANCE = new HashMap<>();

    public AttendanceRecord find(int classId, int traineeId) {
        return ATTENDANCE.get(key(classId, traineeId));
    }

    public void save(int classId, int traineeId, AttendanceStatus status, LocalDateTime markedAt) {
        ATTENDANCE.put(key(classId, traineeId), new AttendanceRecord(
                status == null ? AttendanceStatus.REGISTERED : status,
                markedAt == null ? LocalDateTime.now() : markedAt
        ));
    }

    public void clear(int classId, int traineeId) {
        ATTENDANCE.remove(key(classId, traineeId));
    }

    private String key(int classId, int traineeId) {
        return classId + "::" + traineeId;
    }
}
