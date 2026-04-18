package fitwell.entity;

public class LowAttendanceRecord {
    private final Trainee trainee;
    private final int attendanceCount;
    private final String preferredMethod;

    public LowAttendanceRecord(Trainee trainee, int attendanceCount) {
        this.trainee = trainee;
        this.attendanceCount = attendanceCount;
        this.preferredMethod = trainee == null ? "" : trainee.getPreferredUpdateMethod();
    }

    public Trainee getTrainee() {
        return trainee;
    }

    public int getAttendanceCount() {
        return attendanceCount;
    }

    public String getPreferredMethod() {
        return preferredMethod;
    }
}
