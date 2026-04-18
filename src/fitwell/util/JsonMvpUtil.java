package fitwell.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonMvpUtil {
    private JsonMvpUtil() {}

    // Full update pattern: serialNumber, newQuantity, photoUrl, isNewItem
    private static final Pattern FULL_UPDATE = Pattern.compile(
            "\\{\\s*\"serialNumber\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"newQuantity\"\\s*:\\s*(\\d+)\\s*,\\s*\"photoUrl\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"isNewItem\"\\s*:\\s*(true|false)\\s*\\}"
    );

    // Simple update pattern: serialNumber, newQuantity
    private static final Pattern SIMPLE_UPDATE = Pattern.compile(
            "\\{\\s*\"serialNumber\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"newQuantity\"\\s*:\\s*(\\d+)\\s*\\}"
    );

    public static String preprocess(String json) {
        if (json == null) return null;
        String s = json.trim();
        if (s.isEmpty()) return s;

        // auto add batchId/source ONLY if it looks like a JSON object
        if (s.startsWith("{") && s.endsWith("}")) {
            boolean hasBatch = s.contains("\"batchId\"");
            boolean hasSource = s.contains("\"source\"");

            if (!hasBatch || !hasSource) {
                StringBuilder header = new StringBuilder();
                header.append("{\n");

                if (!hasBatch) {
                    header.append("  \"batchId\": \"BATCH-").append(System.currentTimeMillis()).append("\",\n");
                }
                if (!hasSource) {
                    header.append("  \"source\": \"SwiftFit\",\n");
                }

                // Remove the first '{' and prepend our header
                String body = s.substring(1).trim();

                // If body starts with '}', means empty object - just close it
                if (body.startsWith("}")) {
                    header.append("}\n");
                    return header.toString();
                }

                header.append("  ").append(body);
                return header.toString();
            }
        }
        return s;
    }

    /** Returns number of updates that match supported patterns. */
    public static int countSupportedUpdates(String json) {
        if (json == null) return 0;

        int count = 0;

        Matcher m = FULL_UPDATE.matcher(json);
        while (m.find()) count++;

        // If we already found full updates, don't double-count using the simple pattern
        if (count > 0) return count;

        Matcher m2 = SIMPLE_UPDATE.matcher(json);
        while (m2.find()) count++;

        return count;
    }

    /** Validates JSON structure; throws IllegalArgumentException if invalid. */
    public static void validateOrThrow(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON is empty.");
        }
        int updates = countSupportedUpdates(json);
        if (updates == 0) {
            throw new IllegalArgumentException(
                    "JSON does not contain supported updates.\n" +
                    "Expected objects like:\n" +
                    "{\"serialNumber\":\"EQ-1001\",\"newQuantity\":10,\"photoUrl\":\"...\",\"isNewItem\":true}\n" +
                    "or simpler:\n" +
                    "{\"serialNumber\":\"EQ-1001\",\"newQuantity\":10}"
            );
        }
    }
}
