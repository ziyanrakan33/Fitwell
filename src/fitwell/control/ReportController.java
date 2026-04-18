package fitwell.control;

import fitwell.domain.training.TrainingClass;
import fitwell.repo.RegistrationRepository;
import fitwell.repo.TrainingClassRepository;

import java.util.*;
import java.util.stream.Collectors;

public class ReportController {

    private final RegistrationRepository regRepo;
    private final TrainingClassRepository classRepo;

    public ReportController(RegistrationRepository regRepo, TrainingClassRepository classRepo) {
        this.regRepo = regRepo;
        this.classRepo = classRepo;
    }

    public List<TrainingClass> unregisteredClassesByYear(int year) {
        List<Integer> ids = regRepo.findUnregisteredClassIdsByYear(year);
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        Map<Integer, TrainingClass> map = classRepo.findAll().stream()
                .filter(c -> c != null && c.getClassId() != null)
                .collect(Collectors.toMap(TrainingClass::getClassId, c -> c, (a, b) -> a));

        List<TrainingClass> out = new ArrayList<>();
        for (Integer id : ids) {
            if (id == null) continue;
            TrainingClass tc = map.get(id);
            if (tc != null) out.add(tc);
        }
        return out;
    }
}