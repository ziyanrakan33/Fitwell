package fitwell.domain.registration;

import java.time.LocalDateTime;

public class ClassRegistration {
    private final int classId;
    private final int traineeId;
    private final LocalDateTime registrationDateTime;
    private AttendanceStatus attendanceStatus;
    private LocalDateTime attendanceMarkedAt;

    public ClassRegistration(int classId, int traineeId, LocalDateTime registrationDateTime) {
        this(classId, traineeId, registrationDateTime, AttendanceStatus.REGISTERED, null);
    }

    public ClassRegistration(int classId,
                             int traineeId,
                             LocalDateTime registrationDateTime,
                             AttendanceStatus attendanceStatus,
                             LocalDateTime attendanceMarkedAt) {
        this.classId = classId;
        this.traineeId = traineeId;
        this.registrationDateTime = registrationDateTime;
        this.attendanceStatus = attendanceStatus == null ? AttendanceStatus.REGISTERED : attendanceStatus;
        this.attendanceMarkedAt = attendanceMarkedAt;
    }

    public int getClassId() { return classId; }
    public int getTraineeId() { return traineeId; }
    public LocalDateTime getRegistrationDateTime() { return registrationDateTime; }
    public AttendanceStatus getAttendanceStatus() { return attendanceStatus; }
    public LocalDateTime getAttendanceMarkedAt() { return attendanceMarkedAt; }

    public void markAttendance(AttendanceStatus attendanceStatus, LocalDateTime markedAt) {
        this.attendanceStatus = attendanceStatus == null ? AttendanceStatus.REGISTERED : attendanceStatus;
        this.attendanceMarkedAt = markedAt == null ? LocalDateTime.now() : markedAt;
    }
}
