package fitwell.persistence.api;

import fitwell.domain.registration.ClassRegistration;
import java.time.LocalDateTime;
import java.util.List;

public interface RegistrationRepository {
    List<ClassRegistration> findByClassId(int classId);
    List<ClassRegistration> findByTraineeId(int traineeId);
    int countRegistrationsForClass(int classId);
    boolean isRegistered(int classId, int traineeId);
    void register(int classId, int traineeId, LocalDateTime when);
    void unregister(int classId, int traineeId);
    List<ClassRegistration> findAll();
    List<Integer> findUnregisteredClassIdsByYear(int year);
}
