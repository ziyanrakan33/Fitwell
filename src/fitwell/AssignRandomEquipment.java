package fitwell;

import fitwell.control.EquipmentAssignmentController;
import fitwell.control.FitWellServiceRegistry;
import fitwell.domain.equipment.Equipment;
import fitwell.domain.training.TrainingClass;
import fitwell.repo.ConsultantRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AssignRandomEquipment {

    public static void main(String[] args) {
        System.out.println("Starting random equipment assignment...");
        FitWellServiceRegistry registry = FitWellServiceRegistry.getInstance();
        
        List<TrainingClass> classes = registry.trainingClassService().getAllClasses();
        List<Equipment> equipmentList = registry.equipmentReviewService().getAllEquipment();
        
        if (classes.isEmpty() || equipmentList.isEmpty()) {
            System.out.println("No classes or equipment found.");
            return;
        }
        
        System.out.println("Found " + classes.size() + " classes and " + equipmentList.size() + " equipment types.");
        
        // Find a consultant ID to use for assigning
        int consultantId = 1;
        var consultants = new ConsultantRepository().findAll();
        if (!consultants.isEmpty()) {
            consultantId = consultants.get(0).getId();
        }
        
        Random random = new Random();
        Set<String> assignedEquipmentSerials = new HashSet<>();
        EquipmentAssignmentController controller = registry.equipmentAssignmentController();
        
        int successCount = 0;
        
        // Phase 1: Assign equipment to all classes
        for (TrainingClass c : classes) {
            List<Equipment> eqCopy = new ArrayList<>(equipmentList);
            Collections.shuffle(eqCopy, random);
            
            boolean assigned = false;
            for (Equipment eq : eqCopy) {
                var res = controller.assignEquipmentToClass(c.getClassId(), eq.getSerialNumber(), 1, consultantId, "Random assignment");
                if (res.success) {
                    assignedEquipmentSerials.add(eq.getSerialNumber());
                    assigned = true;
                    successCount++;
                    break;
                }
            }
            if (!assigned) {
                System.out.println("Warning: Could not assign any equipment to class " + c.getClassId() + " (" + c.getName() + "). May be overlapping times.");
            }
        }
        
        // Phase 2: Make sure all equipment is assigned at least once
        for (Equipment eq : equipmentList) {
            if (!assignedEquipmentSerials.contains(eq.getSerialNumber())) {
                List<TrainingClass> classCopy = new ArrayList<>(classes);
                Collections.shuffle(classCopy, random);
                
                boolean assigned = false;
                for (TrainingClass c : classCopy) {
                    var res = controller.assignEquipmentToClass(c.getClassId(), eq.getSerialNumber(), 1, consultantId, "Random assignment for unused equipment");
                    if (res.success) {
                        assignedEquipmentSerials.add(eq.getSerialNumber());
                        assigned = true;
                        successCount++;
                        break;
                    }
                }
                if (!assigned) {
                    System.out.println("Warning: Could not assign unused equipment " + eq.getSerialNumber() + " to any class.");
                }
            }
        }
        
        System.out.println("Finished! Total successful random assignments made: " + successCount);
        System.exit(0);
    }
}
