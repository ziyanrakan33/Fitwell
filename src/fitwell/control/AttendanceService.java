package fitwell.control;

import fitwell.domain.registration.AttendanceStatus;
import fitwell.domain.registration.ClassRegistration;
import fitwell.domain.reports.LowAttendanceRecord;
import fitwell.domain.user.Trainee;
import fitwell.domain.training.TrainingClass;
import fitwell.repo.AttendanceRepository;
import fitwell.repo.RegistrationRepository;
import fitwell.repo.TraineeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceService {
    public static class AttendanceView {
        private final ClassRegistration registration;
        private final String traineeName;

        public AttendanceView(ClassRegistration registration, String traineeName) {
            this.registration = registration;
            this.traineeName = traineeName;
        }

        public ClassRegistration getRegistration() {
            return registration;
        }

        public String getTraineeName() {
            return traineeName;
        }
    }

    private final RegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingClassService trainingClassService;

    public AttendanceService(RegistrationRepository registrationRepository,
                             AttendanceRepository attendanceRepository,
                             TraineeRepository traineeRepository,
                             TrainingClassService trainingClassService) {
        this.registrationRepository = registrationRepository;
        this.attendanceRepository = attendanceRepository;
        this.traineeRepository = traineeRepository;
        this.trainingClassService = trainingClassService;
    }

    public List<AttendanceView> getAttendanceForClass(int classId) {
        Map<Integer, String> names = new HashMap<>();
        for (Trainee trainee : traineeRepository.findAll()) {
            if (trainee.getId() != null) {
                names.put(trainee.getId(), trainee.fullName());
            }
        }

        List<AttendanceView> views = new ArrayList<>();
        for (ClassRegistration registration : registrationRepository.findByClassId(classId)) {
            hydrate(registration);
            views.add(new AttendanceView(registration,
                    names.getOrDefault(registration.getTraineeId(), "Trainee #" + registration.getTraineeId())));
        }
        return views;
    }

    public void markAttendance(int classId, int traineeId, AttendanceStatus status) {
        attendanceRepository.save(classId, traineeId, status, LocalDateTime.now());
    }

    public List<LowAttendanceRecord> buildLowAttendanceReport(LocalDate from, LocalDate toInclusive, int threshold) {
        LocalDate toExclusive = toInclusive.plusDays(1);
        Map<Integer, Integer> counts = new HashMap<>();
        for (ClassRegistration registration : registrationRepository.findAll()) {
            TrainingClass trainingClass = trainingClassService.findById(registration.getClassId());
            if (trainingClass == null || trainingClass.getStartTime() == null) {
                continue;
            }
            LocalDate classDate = trainingClass.getStartTime().toLocalDate();
            if (classDate.isBefore(from) || !classDate.isBefore(toExclusive)) {
                continue;
            }
            hydrate(registration);
            if (registration.getAttendanceStatus() == AttendanceStatus.ATTENDED) {
                counts.merge(registration.getTraineeId(), 1, Integer::sum);
            }
        }

        List<LowAttendanceRecord> records = new ArrayList<>();
        for (Trainee trainee : traineeRepository.findAll()) {
            int attended = counts.getOrDefault(trainee.getId(), 0);
            if (attended < threshold) {
                records.add(new LowAttendanceRecord(trainee, attended));
            }
        }
        return records;
    }

    public void hydrate(ClassRegistration registration) {
        AttendanceRepository.AttendanceRecord record =
                attendanceRepository.find(registration.getClassId(), registration.getTraineeId());
        if (record != null) {
            registration.markAttendance(record.getStatus(), record.getMarkedAt());
        }
    }
}
