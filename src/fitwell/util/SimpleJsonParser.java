package fitwell.util;

import fitwell.entity.EquipmentUpdate;
import fitwell.entity.EquipmentUpdateBatch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Robust character-by-character JSON parser for EquipmentUpdateBatch payloads.
 * Handles nested objects, arrays, escape sequences, and whitespace correctly.
 */
public class SimpleJsonParser {

    public static EquipmentUpdateBatch parseUpdateBatch(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            Map<String, Object> root = parseObject(json.trim(), new int[]{0});
            String batchId = getString(root, "batchId");
            String source = getString(root, "source");
            if (batchId == null) batchId = "BATCH-" + System.currentTimeMillis();
            if (source == null) source = "SwiftFit";

            EquipmentUpdateBatch batch = new EquipmentUpdateBatch(batchId, LocalDateTime.now(), source);

            Object updatesObj = root.get("updates");
            if (updatesObj instanceof List<?>) {
                for (Object item : (List<?>) updatesObj) {
                    if (item instanceof Map<?, ?>) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> obj = (Map<String, Object>) item;
                        String serial = getString(obj, "serialNumber");
                        if (serial == null || serial.isBlank()) continue;
                        int qty = getInt(obj, "newQuantity", 0);
                        String photo = getString(obj, "photoUrl");
                        boolean isNew = getBool(obj, "isNewItem", false);
                        String name = getString(obj, "name");
                        String category = getString(obj, "category");
                        int x = getInt(obj, "x", 0);
                        int y = getInt(obj, "y", 0);
                        int shelf = getInt(obj, "shelf", 0);
                        batch.addUpdate(new EquipmentUpdate(serial, qty, photo, isNew, name, category, x, y, shelf));
                    }
                }
            }
            return batch;
        } catch (Exception ex) {
            return null;
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof String ? (String) v : null;
    }

    private static int getInt(Map<String, Object> map, String key, int def) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try { return Integer.parseInt((String) v); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    private static boolean getBool(Map<String, Object> map, String key, boolean def) {
        Object v = map.get(key);
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof String) return Boolean.parseBoolean((String) v);
        return def;
    }

    // ── parser core ───────────────────────────────────────────────────────────

    private static Map<String, Object> parseObject(String json, int[] pos) {
        Map<String, Object> map = new HashMap<>();
        skipWhitespace(json, pos);
        if (pos[0] >= json.length() || json.charAt(pos[0]) != '{') return map;
        pos[0]++; // consume '{'
        skipWhitespace(json, pos);
        while (pos[0] < json.length() && json.charAt(pos[0]) != '}') {
            skipWhitespace(json, pos);
            if (pos[0] >= json.length() || json.charAt(pos[0]) == '}') break;
            String key = parseString(json, pos);
            skipWhitespace(json, pos);
            if (pos[0] < json.length() && json.charAt(pos[0]) == ':') pos[0]++;
            skipWhitespace(json, pos);
            Object value = parseValue(json, pos);
            map.put(key, value);
            skipWhitespace(json, pos);
            if (pos[0] < json.length() && json.charAt(pos[0]) == ',') pos[0]++;
            skipWhitespace(json, pos);
        }
        if (pos[0] < json.length()) pos[0]++; // consume '}'
        return map;
    }

    private static List<Object> parseArray(String json, int[] pos) {
        List<Object> list = new ArrayList<>();
        skipWhitespace(json, pos);
        if (pos[0] >= json.length() || json.charAt(pos[0]) != '[') return list;
        pos[0]++; // consume '['
        skipWhitespace(json, pos);
        while (pos[0] < json.length() && json.charAt(pos[0]) != ']') {
            Object value = parseValue(json, pos);
            list.add(value);
            skipWhitespace(json, pos);
            if (pos[0] < json.length() && json.charAt(pos[0]) == ',') pos[0]++;
            skipWhitespace(json, pos);
        }
        if (pos[0] < json.length()) pos[0]++; // consume ']'
        return list;
    }

    private static Object parseValue(String json, int[] pos) {
        skipWhitespace(json, pos);
        if (pos[0] >= json.length()) return null;
        char c = json.charAt(pos[0]);
        if (c == '"') return parseString(json, pos);
        if (c == '{') return parseObject(json, pos);
        if (c == '[') return parseArray(json, pos);
        if (c == 't' || c == 'f') return parseBoolean(json, pos);
        if (c == 'n') { pos[0] += 4; return null; } // null
        return parseNumber(json, pos);
    }

    private static String parseString(String json, int[] pos) {
        if (pos[0] >= json.length() || json.charAt(pos[0]) != '"') return "";
        pos[0]++; // consume opening '"'
        StringBuilder sb = new StringBuilder();
        while (pos[0] < json.length()) {
            char c = json.charAt(pos[0]++);
            if (c == '"') break;
            if (c == '\\' && pos[0] < json.length()) {
                char esc = json.charAt(pos[0]++);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(esc); break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Number parseNumber(String json, int[] pos) {
        int start = pos[0];
        while (pos[0] < json.length()) {
            char c = json.charAt(pos[0]);
            if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) break;
            pos[0]++;
        }
        String num = json.substring(start, pos[0]).trim();
        try {
            if (num.contains(".")) return Double.parseDouble(num);
            return Long.parseLong(num);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Boolean parseBoolean(String json, int[] pos) {
        if (json.startsWith("true", pos[0])) { pos[0] += 4; return Boolean.TRUE; }
        if (json.startsWith("false", pos[0])) { pos[0] += 5; return Boolean.FALSE; }
        return Boolean.FALSE;
    }

    private static void skipWhitespace(String json, int[] pos) {
        while (pos[0] < json.length() && Character.isWhitespace(json.charAt(pos[0]))) pos[0]++;
    }
}
