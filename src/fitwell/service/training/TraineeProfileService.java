package fitwell.service.training;

import fitwell.domain.shared.PreferredUpdateMethod;
import fitwell.service.auth.AuthenticationService;
import fitwell.domain.user.Trainee;
import fitwell.persistence.api.TraineeRepository;

import java.util.List;

public class TraineeProfileService {
    private final TraineeRepository traineeRepository;
    private final AuthenticationService authenticationService;

    public TraineeProfileService(TraineeRepository traineeRepository, AuthenticationService authenticationService) {
        this.traineeRepository = traineeRepository;
        this.authenticationService = authenticationService;
    }

    public List<Trainee> getAllTrainees() {
        return traineeRepository.findAll();
    }

    public Trainee getCurrentTrainee() {
        AuthenticationService auth = authenticationService;
        if (auth != null && auth.isLoggedIn() && "trainee".equals(auth.getCurrentRole())) {
            Trainee loggedIn = traineeRepository.findById(auth.getCurrentUserId());
            if (loggedIn != null) {
                return loggedIn;
            }
        }
        List<Trainee> trainees = traineeRepository.findAll();
        if (trainees.isEmpty()) {
            Trainee seeded = new Trainee(null, "Default", "Trainee", "050-0000000",
                    "trainee@fitwell.local", PreferredUpdateMethod.EMAIL);
            int id = traineeRepository.insert(seeded);
            seeded.setId(id);
            return seeded;
        }
        return trainees.get(0);
    }

    public Trainee findById(int traineeId) {
        for (Trainee trainee : traineeRepository.findAll()) {
            if (trainee.getId() != null && trainee.getId() == traineeId) {
                return trainee;
            }
        }
        return null;
    }

    public Trainee saveProfile(Trainee trainee) {
        validate(trainee);
        if (trainee.getId() == null || trainee.getId() <= 0) {
            int id = traineeRepository.insert(trainee);
            trainee.setId(id);
        } else {
            traineeRepository.update(trainee);
        }
        return trainee;
    }

    public void deleteProfile(int traineeId) {
        traineeRepository.delete(traineeId);
    }

    public void validate(Trainee trainee) {
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee is required.");
        }
        if (trainee.getFirstName() == null || trainee.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (trainee.getLastName() == null || trainee.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (trainee.getPhone() == null || trainee.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        if (trainee.getEmail() == null || !trainee.getEmail().contains("@")) {
            throw new IllegalArgumentException("A valid email is required.");
        }
    }
}
