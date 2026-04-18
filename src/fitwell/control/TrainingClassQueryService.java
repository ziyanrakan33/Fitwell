package fitwell.control;
import fitwell.domain.training.TrainingClass;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrainingClassQueryService {
    private final TrainingClassService trainingClassService;
    private final TrainingPlanService trainingPlanService;
    private final AttendanceService attendanceService;
    private final EmergencyAlertService emergencyAlertService;

    public TrainingClassQueryService(TrainingClassService trainingClassService,
                                     TrainingPlanService trainingPlanService,
                                     AttendanceService attendanceService,
                                     EmergencyAlertService emergencyAlertService) {
        this.trainingClassService = trainingClassService;
        this.trainingPlanService = trainingPlanService;
        this.attendanceService = attendanceService;
        this.emergencyAlertService = emergencyAlertService;
    }

    public List<TrainingClass> getClassesForConsultant(String query, String type) {
        List<TrainingClass> all = trainingClassService.getAllClasses();
        return filterClasses(all, query, type);
    }

    public List<TrainingClass> getClassesForTrainee(int traineeId, String query, String type) {
        List<TrainingClass> assigned = trainingPlanService.filterClassesAssignedToTrainee(
                traineeId, trainingClassService.getAllClasses());
        List<TrainingClass> filtered = filterClasses(assigned, query, type);
        List<TrainingClass> available = new ArrayList<>();
        for (TrainingClass trainingClass : filtered) {
            if (trainingClass.getStartTime() != null
                    && trainingClass.getStartTime().isAfter(LocalDateTime.now())
                    && !emergencyAlertService.isClassSuspended(trainingClass.getClassId())) {
                available.add(trainingClass);
            }
        }
        return available;
    }

    public List<AttendanceService.AttendanceView> getRegistrationsForClass(int classId) {
        return attendanceService.getAttendanceForClass(classId);
    }

    public int countUpcomingClassesForTrainee(int traineeId) {
        return getClassesForTrainee(traineeId, "", "All Types").size();
    }

    public int countAvailableClassesForTrainee(int traineeId) {
        return getClassesForTrainee(traineeId, "", "All Types").size();
    }

    private List<TrainingClass> filterClasses(List<TrainingClass> classes, String query, String type) {
        List<TrainingClass> filtered = new ArrayList<>();
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        boolean allTypes = type == null || type.isBlank() || "All Types".equalsIgnoreCase(type);
        for (TrainingClass trainingClass : classes) {
            if (trainingClass == null) {
                continue;
            }
            boolean matchesQuery = normalizedQuery.isEmpty()
                    || safe(trainingClass.getName()).toLowerCase().contains(normalizedQuery)
                    || safe(trainingClass.getType()).toLowerCase().contains(normalizedQuery);
            boolean matchesType = allTypes || safe(trainingClass.getType()).equalsIgnoreCase(type);
            if (matchesQuery && matchesType) {
                filtered.add(trainingClass);
            }
        }
        return filtered;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
