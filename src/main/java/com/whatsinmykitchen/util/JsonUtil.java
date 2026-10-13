package com.whatsinmykitchen.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return quote((String) value);
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    builder.append(",");
                }
                first = false;
                builder.append(quote(String.valueOf(entry.getKey())));
                builder.append(":");
                builder.append(toJson(entry.getValue()));
            }
            builder.append("}");
            return builder.toString();
        }
        if (value instanceof Iterable<?> iterable) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            boolean first = true;
            for (Object item : iterable) {
                if (!first) {
                    builder.append(",");
                }
                first = false;
                builder.append(toJson(item));
            }
            builder.append("]");
            return builder.toString();
        }
        if (value.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(toJson(java.lang.reflect.Array.get(value, i)));
            }
            builder.append("]");
            return builder.toString();
        }

        Map<String, Object> beanMap = new LinkedHashMap<>();
        for (java.lang.reflect.Method method : value.getClass().getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            if (method.getParameterCount() != 0) {
                continue;
            }
            if (method.getName().equals("getClass")) {
                continue;
            }
            String propertyName = null;
            if (method.getName().startsWith("get") && method.getName().length() > 3) {
                propertyName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
            } else if (method.getName().startsWith("is") && method.getName().length() > 2) {
                propertyName = method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3);
            }
            if (propertyName == null) {
                continue;
            }
            try {
                beanMap.put(propertyName, method.invoke(value));
            } catch (ReflectiveOperationException ignored) {
                // Ignore inaccessible or non-readable bean properties.
            }
        }

        if (!beanMap.isEmpty()) {
            return toJson(beanMap);
        }

        return quote(value.toString());
    }

    private static String quote(String value) {
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\':
                    builder.append("\\\\");
                    break;
                case '"':
                    builder.append("\\\"");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                default:
                    if (ch < 0x20) {
                        builder.append(String.format("\\u%04x", (int) ch));
                    } else {
                        builder.append(ch);
                    }
            }
        }
        builder.append('"');
        return builder.toString();
    }

    public static Map<String, Object> parseObject(String raw) throws JsonParseException {
        if (raw == null || raw.trim().isEmpty()) {
            throw new JsonParseException("Body is empty.");
        }
        int[] index = {skipWhitespace(raw, 0)};
        if (index[0] >= raw.length() || raw.charAt(index[0]) != '{') {
            throw new JsonParseException("Expected a JSON object at the top level.");
        }
        Object result = parseObjectValue(raw, index);
        if (!(result instanceof Map<?, ?> map)) {
            throw new JsonParseException("Expected an object result.");
        }
        return new LinkedHashMap<>((Map<String, Object>) map);
    }

    public static Object parseValue(String raw, int[] index) throws JsonParseException {
        index[0] = skipWhitespace(raw, index[0]);
        if (index[0] >= raw.length()) {
            throw new JsonParseException("Unexpected end of JSON input.");
        }
        char ch = raw.charAt(index[0]);
        if (ch == '{') {
            return parseObjectValue(raw, index);
        }
        if (ch == '[') {
            return parseArrayValue(raw, index);
        }
        if (ch == '"') {
            return parseStringValue(raw, index);
        }
        if (ch == 't' || ch == 'f') {
            String literal = raw.substring(index[0], Math.min(raw.length(), index[0] + 5));
            if (literal.startsWith("true")) {
                index[0] += 4;
                return Boolean.TRUE;
            }
            if (literal.startsWith("false")) {
                index[0] += 5;
                return Boolean.FALSE;
            }
        }
        if (ch == 'n') {
            if (raw.startsWith("null", index[0])) {
                index[0] += 4;
                return null;
            }
        }
        if (ch == '-' || Character.isDigit(ch)) {
            int start = index[0];
            index[0]++;
            while (index[0] < raw.length() && (Character.isDigit(raw.charAt(index[0])) || raw.charAt(index[0]) == '.')) {
                index[0]++;
            }
            String number = raw.substring(start, index[0]);
            if (number.contains(".")) {
                return Double.parseDouble(number);
            }
            return Long.parseLong(number);
        }
        throw new JsonParseException("Unexpected token at index " + index[0]);
    }

    private static Object parseObjectValue(String raw, int[] index) throws JsonParseException {
        Map<String, Object> object = new LinkedHashMap<>();
        index[0]++;
        while (true) {
            index[0] = skipWhitespace(raw, index[0]);
            if (index[0] >= raw.length()) {
                throw new JsonParseException("Missing closing brace for object.");
            }
            if (raw.charAt(index[0]) == '}') {
                index[0]++;
                return object;
            }
            String key = parseStringValue(raw, index);
            index[0] = skipWhitespace(raw, index[0]);
            if (index[0] >= raw.length() || raw.charAt(index[0]) != ':') {
                throw new JsonParseException("Expected ':' after object key.");
            }
            index[0]++;
            Object value = parseValue(raw, index);
            object.put(key, value);
            index[0] = skipWhitespace(raw, index[0]);
            if (index[0] >= raw.length()) {
                throw new JsonParseException("Missing closing brace for object.");
            }
            char next = raw.charAt(index[0]);
            if (next == '}') {
                index[0]++;
                return object;
            }
            if (next != ',') {
                throw new JsonParseException("Expected ',' or '}' in object.");
            }
            index[0]++;
        }
    }

    private static List<Object> parseArrayValue(String raw, int[] index) throws JsonParseException {
        List<Object> list = new ArrayList<>();
        index[0]++;
        while (true) {
            index[0] = skipWhitespace(raw, index[0]);
            if (index[0] >= raw.length()) {
                throw new JsonParseException("Missing closing bracket for array.");
            }
            if (raw.charAt(index[0]) == ']') {
                index[0]++;
                return list;
            }
            list.add(parseValue(raw, index));
            index[0] = skipWhitespace(raw, index[0]);
            if (index[0] >= raw.length()) {
                throw new JsonParseException("Missing closing bracket for array.");
            }
            char next = raw.charAt(index[0]);
            if (next == ']') {
                index[0]++;
                return list;
            }
            if (next != ',') {
                throw new JsonParseException("Expected ',' or ']' in array.");
            }
            index[0]++;
        }
    }

    private static String parseStringValue(String raw, int[] index) throws JsonParseException {
        if (index[0] >= raw.length() || raw.charAt(index[0]) != '"') {
            throw new JsonParseException("Expected start of string.");
        }
        index[0]++;
        StringBuilder builder = new StringBuilder();
        while (index[0] < raw.length()) {
            char ch = raw.charAt(index[0]);
            if (ch == '"') {
                index[0]++;
                return builder.toString();
            }
            if (ch == '\\') {
                index[0]++;
                if (index[0] >= raw.length()) {
                    throw new JsonParseException("Unfinished escape sequence.");
                }
                char escaped = raw.charAt(index[0]);
                switch (escaped) {
                    case '"':
                        builder.append('"');
                        break;
                    case '\\':
                        builder.append('\\');
                        break;
                    case '/':
                        builder.append('/');
                        break;
                    case 'b':
                        builder.append('\b');
                        break;
                    case 'f':
                        builder.append('\f');
                        break;
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;
                    case 'u':
                        if (index[0] + 4 >= raw.length()) {
                            throw new JsonParseException("Invalid unicode escape.");
                        }
                        String hex = raw.substring(index[0] + 1, index[0] + 5);
                        builder.append((char) Integer.parseInt(hex, 16));
                        index[0] += 4;
                        break;
                    default:
                        throw new JsonParseException("Unsupported escape sequence: \\" + escaped);
                }
                index[0]++;
                continue;
            }
            builder.append(ch);
            index[0]++;
        }
        throw new JsonParseException("Unterminated string literal.");
    }

    private static int skipWhitespace(String raw, int index) {
        while (index < raw.length() && Character.isWhitespace(raw.charAt(index))) {
            index++;
        }
        return index;
    }

    public static final class JsonParseException extends Exception {
        public JsonParseException(String message) {
            super(message);
        }
    }
}
