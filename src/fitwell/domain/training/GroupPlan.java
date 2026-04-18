package fitwell.domain.training;

import java.time.LocalDate;

public class GroupPlan extends Plan {
    private String ageRange;
    private String preferredClassTypes;
    private String generalGuidelines;

    public GroupPlan(int planId, int traineeId, LocalDate startDate, int durationMonths, String status, String ageRange, String preferredClassTypes) {
        super(planId, traineeId, startDate, durationMonths, status);
        this.ageRange = ageRange == null ? "" : ageRange.trim();
        this.preferredClassTypes = preferredClassTypes == null ? "" : preferredClassTypes.trim();
        this.generalGuidelines = "";
    }

    public GroupPlan(int planId, int traineeId, LocalDate startDate, int durationMonths, PlanStatus status, String ageRange, String preferredClassTypes, String generalGuidelines) {
        super(planId, traineeId, startDate, durationMonths, status);
        this.ageRange = ageRange == null ? "" : ageRange.trim();
        this.preferredClassTypes = preferredClassTypes == null ? "" : preferredClassTypes.trim();
        this.generalGuidelines = generalGuidelines == null ? "" : generalGuidelines.trim();
    }

    public String getAgeRange() { return ageRange; }
    public String getPreferredClassTypes() { return preferredClassTypes; }
    public String getGeneralGuidelines() { return generalGuidelines; }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange == null ? "" : ageRange.trim();
    }

    public void setPreferredClassTypes(String preferredClassTypes) {
        this.preferredClassTypes = preferredClassTypes == null ? "" : preferredClassTypes.trim();
    }

    public void setGeneralGuidelines(String generalGuidelines) {
        this.generalGuidelines = generalGuidelines == null ? "" : generalGuidelines.trim();
    }
}
