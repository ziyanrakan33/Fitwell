package fitwell.persistence.api;

import fitwell.domain.user.Trainee;
import java.util.List;

public interface TraineeRepository {
    Trainee findById(int traineeId);
    Trainee findByEmail(String email);
    Trainee authenticate(String email, String password);
    List<Trainee> findAll();
    int insert(Trainee t);
    void update(Trainee t);
    void delete(int traineeId);
    void ensurePasswordColumn();
    void ensureDefaultExists();
}
