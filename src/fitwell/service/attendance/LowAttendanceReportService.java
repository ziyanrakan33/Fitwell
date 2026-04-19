package fitwell.service.attendance;

import fitwell.domain.reports.LowAttendanceRecord;

import java.time.LocalDate;
import java.util.List;

public class LowAttendanceReportService {
    private final AttendanceService attendanceService;

    public LowAttendanceReportService(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    public List<LowAttendanceRecord> generate(LocalDate from, LocalDate toInclusive, int threshold) {
        return attendanceService.buildLowAttendanceReport(from, toInclusive, threshold);
    }

    public String renderAsText(LocalDate from, LocalDate toInclusive, int threshold) {
        List<LowAttendanceRecord> records = generate(from, toInclusive, threshold);
        StringBuilder builder = new StringBuilder();
        builder.append("Low Attendance Report\n");
        builder.append("====================\n");
        builder.append("Period: ").append(from).append(" -> ").append(toInclusive).append("\n");
        builder.append("Threshold: fewer than ").append(threshold).append(" attended classes\n\n");
        if (records.isEmpty()) {
            builder.append("No trainees matched the report criteria.\n");
            return builder.toString();
        }

        for (LowAttendanceRecord record : records) {
            builder.append("- ")
                    .append(record.getTrainee().fullName())
                    .append(" | attended=")
                    .append(record.getAttendanceCount())
                    .append(" | phone=")
                    .append(record.getTrainee().getPhone())
                    .append(" | email=")
                    .append(record.getTrainee().getEmail())
                    .append(" | preferred=")
                    .append(record.getPreferredMethod())
                    .append('\n');
        }
        return builder.toString();
    }
}
