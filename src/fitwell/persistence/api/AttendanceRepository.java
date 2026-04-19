package fitwell.persistence.api;

import fitwell.domain.registration.AttendanceStatus;
import java.time.LocalDateTime;

public interface AttendanceRepository {

    class AttendanceRecord {
        private final AttendanceStatus status;
        private final LocalDateTime markedAt;

        public AttendanceRecord(AttendanceStatus status, LocalDateTime markedAt) {
            this.status = status;
            this.markedAt = markedAt;
        }

        public AttendanceStatus getStatus() { return status; }
        public LocalDateTime getMarkedAt() { return markedAt; }
    }

    AttendanceRecord find(int classId, int traineeId);
    void save(int classId, int traineeId, AttendanceStatus status, LocalDateTime markedAt);
    void clear(int classId, int traineeId);
}
