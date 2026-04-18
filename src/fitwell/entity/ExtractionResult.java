package fitwell.entity;

public class ExtractionResult {
    private final String extractedName;
    private final String extractedDescription;
    private final EquipmentCategory extractedCategory;
    private final double confidence;
    private final boolean isComplete;

    public ExtractionResult(String name, String desc, EquipmentCategory cat, double confidence, boolean isComplete) {
        this.extractedName = name;
        this.extractedDescription = desc;
        this.extractedCategory = cat;
        this.confidence = confidence;
        this.isComplete = isComplete;
    }

    public String getExtractedName() { return extractedName; }
    public String getExtractedDescription() { return extractedDescription; }
    public EquipmentCategory getExtractedCategory() { return extractedCategory; }
    public double getConfidence() { return confidence; }
    public boolean isComplete() { return isComplete; }
}
