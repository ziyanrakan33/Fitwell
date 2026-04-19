package fitwell.controller;
import fitwell.service.training.TrainingClassService;

import java.util.List;

import fitwell.integration.swiftfit.SwiftFitGateway;
import fitwell.domain.equipment.ClassEquipmentAssignment;
import fitwell.domain.equipment.Equipment;
import fitwell.domain.training.TrainingClass;
import fitwell.persistence.api.ClassEquipmentAssignmentRepository;
import fitwell.persistence.jdbc.InMemoryTrainingClassRuntimeStateRepository;
import fitwell.persistence.jdbc.JdbcClassEquipmentAssignmentRepository;
import fitwell.persistence.jdbc.JdbcTrainingClassRepository;
import fitwell.persistence.api.EquipmentRepository;
import fitwell.persistence.api.TrainingClassRuntimeStateRepository;
import fitwell.util.XmlUtil;

public class InventoryReportController {

    private final EquipmentRepository repo;
    private final ClassEquipmentAssignmentRepository classEquipmentAssignmentRepository;
    private final TrainingClassService trainingClassService;
    private final SwiftFitGateway swiftFitGateway;

    public InventoryReportController(EquipmentRepository repo,
                                     ClassEquipmentAssignmentRepository classEquipmentAssignmentRepository,
                                     TrainingClassService trainingClassService,
                                     SwiftFitGateway swiftFitGateway) {
        this.repo = repo;
        this.classEquipmentAssignmentRepository = classEquipmentAssignmentRepository;
        this.trainingClassService = trainingClassService;
        this.swiftFitGateway = swiftFitGateway;
    }

    public InventoryReportController(EquipmentRepository repo, fitwell.integration.swiftfit.SwiftFitGateway ignoredGateway) {
        this(repo, new JdbcClassEquipmentAssignmentRepository(),
                new TrainingClassService(new JdbcTrainingClassRepository(), new InMemoryTrainingClassRuntimeStateRepository()),
                new SwiftFitGateway());
    }

    public String generateInventoryReportXML(int year) {
        List<Equipment> items = repo.findAll();
        applyUsageCounts(year, items);
        return XmlUtil.buildInventoryXml(year, items);
    }

    public void exportAnnualReportToSwiftFit(int year) {
        String xml = generateInventoryReportXML(year);
        swiftFitGateway.sendAnnualInventoryReportXML(xml);
    }

    private void applyUsageCounts(int year, List<Equipment> items) {
        List<TrainingClass> classes = trainingClassService.getAllClasses();
        List<ClassEquipmentAssignment> assignments = classEquipmentAssignmentRepository.findAll();

        for (Equipment equipment : items) {
            int total = 0;
            for (ClassEquipmentAssignment assignment : assignments) {
                if (!equipment.getSerialNumber().equalsIgnoreCase(assignment.getSerialNumber())) {
                    continue;
                }
                TrainingClass trainingClass = null;
                for (TrainingClass candidate : classes) {
                    if (candidate.getClassId() != null && candidate.getClassId() == assignment.getClassId()) {
                        trainingClass = candidate;
                        break;
                    }
                }
                if (trainingClass != null && trainingClass.getStartTime() != null
                        && trainingClass.getStartTime().toLocalDate().getYear() == year) {
                    total += assignment.getRequiredQuantity();
                }
            }
            if (equipment.getTimesUsedThisYear() > 0) {
                continue;
            }
            equipment.addUsage(total);
        }
    }
}
