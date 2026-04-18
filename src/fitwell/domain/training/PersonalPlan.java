package fitwell.domain.training;

import java.time.LocalDate;

public class PersonalPlan extends Plan {
    private String dietaryRestrictions;
    private String dietitianNotes;

    public PersonalPlan(int planId, int traineeId, LocalDate startDate, int durationMonths, String status, String dietaryRestrictions) {
        super(planId, traineeId, startDate, durationMonths, status);
        this.dietaryRestrictions = dietaryRestrictions == null ? "" : dietaryRestrictions.trim();
        this.dietitianNotes = "";
    }

    public PersonalPlan(int planId, int traineeId, LocalDate startDate, int durationMonths, PlanStatus status, String dietaryRestrictions, String dietitianNotes) {
        super(planId, traineeId, startDate, durationMonths, status);
        this.dietaryRestrictions = dietaryRestrictions == null ? "" : dietaryRestrictions.trim();
        this.dietitianNotes = dietitianNotes == null ? "" : dietitianNotes.trim();
    }

    public String getDietaryRestrictions() { return dietaryRestrictions; }
    public String getDietitianNotes() { return dietitianNotes; }

    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions == null ? "" : dietaryRestrictions.trim();
    }

    public void setDietitianNotes(String dietitianNotes) {
        this.dietitianNotes = dietitianNotes == null ? "" : dietitianNotes.trim();
    }
}
