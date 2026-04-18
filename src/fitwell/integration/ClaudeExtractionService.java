package fitwell.integration;

import fitwell.domain.equipment.EquipmentCategory;
import fitwell.domain.reports.ExtractionResult;

/**
 * Production-ready skeleton for Claude API-based equipment image extraction.
 *
 * HOW TO ACTIVATE:
 *   1. Set environment variable: CLAUDE_API_KEY=<your-anthropic-key>
 *   2. Add an HTTP client to the project (java.net.http is built-in from Java 11+)
 *   3. Uncomment and complete the callClaudeApi() method below.
 *   4. In ImageExtractionService, replace simulateExtraction() with:
 *         return new ClaudeExtractionService().extractFromImage(url);
 *
 * API endpoint: POST https://api.anthropic.com/v1/messages
 * Model:        claude-opus-4-7  (or claude-sonnet-4-6 for lower cost)
 * Prompt:       "You are an equipment cataloguing assistant. Given this image URL,
 *                extract: name, short description, and category (cardio/strength/other).
 *                Respond ONLY as JSON: {\"name\":\"...\",\"description\":\"...\",\"category\":\"...\"}"
 */
public class ClaudeExtractionService {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-6";
    private static final String API_KEY_ENV = "CLAUDE_API_KEY";

    public ExtractionResult extractFromImage(String imageUrl) {
        String apiKey = System.getenv(API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            // No API key — fall back to stub
            return new ImageExtractionService().extractFromImage(imageUrl);
        }

        try {
            String responseJson = callClaudeApi(apiKey, imageUrl);
            return parseClaudeResponse(responseJson);
        } catch (Exception ex) {
            // Network or parse failure — flag for manual review
            return new ExtractionResult("", "", EquipmentCategory.other, 0.0, false);
        }
    }

    /**
     * Sends a request to the Claude API and returns the raw JSON response body.
     * Requires Java 11+ (java.net.http).
     *
     * TODO: uncomment when ready to activate
     */
    @SuppressWarnings("unused")
    private String callClaudeApi(String apiKey, String imageUrl) throws Exception {
        // String prompt = "You are an equipment cataloguing assistant. "
        //     + "Given this image URL, extract: name, short description, and category "
        //     + "(cardio / strength / other). "
        //     + "Respond ONLY as JSON with keys: name, description, category.\n"
        //     + "Image URL: " + imageUrl;
        //
        // String body = "{"
        //     + "\"model\":\"" + MODEL + "\","
        //     + "\"max_tokens\":256,"
        //     + "\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}]"
        //     + "}";
        //
        // java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        // java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
        //     .uri(java.net.URI.create(API_URL))
        //     .header("x-api-key", apiKey)
        //     .header("anthropic-version", "2023-06-01")
        //     .header("content-type", "application/json")
        //     .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
        //     .build();
        //
        // java.net.http.HttpResponse<String> response =
        //     client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        // return response.body();

        throw new UnsupportedOperationException("ClaudeExtractionService not yet activated. Set CLAUDE_API_KEY.");
    }

    private ExtractionResult parseClaudeResponse(String json) {
        // Extract fields from Claude's JSON reply using the existing SimpleJsonParser approach
        String name = extractJsonString(json, "name");
        String description = extractJsonString(json, "description");
        String categoryStr = extractJsonString(json, "category");

        EquipmentCategory category;
        try {
            category = EquipmentCategory.valueOf(categoryStr != null ? categoryStr.toLowerCase() : "other");
        } catch (IllegalArgumentException e) {
            category = EquipmentCategory.other;
        }

        boolean complete = name != null && !name.isBlank() && description != null && !description.isBlank();
        double confidence = complete ? 0.92 : 0.3;
        return new ExtractionResult(name != null ? name : "", description != null ? description : "",
                category, confidence, complete);
    }

    private String extractJsonString(String json, String key) {
        if (json == null) return null;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        int start = json.indexOf('"', colon + 1);
        if (start < 0) return null;
        int end = json.indexOf('"', start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }
}
