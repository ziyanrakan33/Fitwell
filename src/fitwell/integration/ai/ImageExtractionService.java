package fitwell.integration.ai;

import fitwell.domain.equipment.EquipmentCategory;
import fitwell.domain.reports.ExtractionResult;

/**
 * MVP stub for image-based equipment data extraction.
 *
 * Production path: replace simulateExtraction() with a real HTTP call to the
 * Claude API (POST https://api.anthropic.com/v1/messages) using the image URL.
 * Set the CLAUDE_API_KEY environment variable and delegate to ClaudeExtractionService.
 *
 * Current behaviour: derives name, description, and category from URL path
 * segments so that the downstream flagging / review workflow is exercised
 * correctly even without a live AI service.
 */
public class ImageExtractionService {

    public ExtractionResult extractFromImage(String url) {
        if (url == null || url.isBlank()) {
            return new ExtractionResult("", "", EquipmentCategory.other, 0.0, false);
        }
        return simulateExtraction(url);
    }

    /**
     * Simulates AI extraction by analysing the URL path.
     * Replace the body of this method with a ClaudeExtractionService call
     * once an API key is available.
     */
    private ExtractionResult simulateExtraction(String url) {
        String lower = url.toLowerCase();

        // Derive category from URL path segments
        EquipmentCategory category;
        if (lower.contains("cardio") || lower.contains("treadmill") || lower.contains("bike")
                || lower.contains("rowing") || lower.contains("elliptic")) {
            category = EquipmentCategory.cardio;
        } else if (lower.contains("strength") || lower.contains("weight") || lower.contains("bench")
                || lower.contains("barbell") || lower.contains("dumbbell") || lower.contains("rack")) {
            category = EquipmentCategory.strength;
        } else {
            category = EquipmentCategory.other;
        }

        // Derive a human-readable name from the last path segment
        String segment = extractLastPathSegment(url);
        String name = segment.isBlank() ? "Equipment Item" : toDisplayName(segment);
        String description = "Auto-extracted from image source. Verify details before saving.";

        // Low-confidence when we can't derive a meaningful name or the URL
        // explicitly signals a test/low-quality image
        boolean lowQuality = lower.contains("low") || lower.contains("test") || lower.contains("placeholder");
        double confidence = lowQuality ? 0.35 : (category == EquipmentCategory.other ? 0.65 : 0.88);
        boolean complete = confidence >= 0.7;

        if (!complete) {
            name = "";
            description = "";
        }

        return new ExtractionResult(name, description, category, confidence, complete);
    }

    private String extractLastPathSegment(String url) {
        try {
            String path = url.contains("?") ? url.substring(0, url.indexOf('?')) : url;
            int lastSlash = path.lastIndexOf('/');
            String segment = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
            // Strip file extension
            int dot = segment.lastIndexOf('.');
            return dot > 0 ? segment.substring(0, dot) : segment;
        } catch (Exception e) {
            return "";
        }
    }

    private String toDisplayName(String segment) {
        return segment.replace('-', ' ').replace('_', ' ')
                .chars()
                .collect(StringBuilder::new,
                        (sb, c) -> {
                            if (sb.length() == 0 || sb.charAt(sb.length() - 1) == ' ') {
                                sb.append(Character.toUpperCase(c));
                            } else {
                                sb.append((char) c);
                            }
                        },
                        StringBuilder::append)
                .toString();
    }
}
