package com.ramussoft.local.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small dependency-free parser for the deliberately narrow tool-plan schema. */
public final class AiPlanParser {
    private static final Pattern ARRAY = Pattern.compile("\\\"operations\\\"\\s*:\\s*\\[");
    private static final Pattern FIELD = Pattern.compile("\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"");
    public AiPlan parse(String json) {
        Matcher array = ARRAY.matcher(json);
        if (!array.find()) throw new IllegalArgumentException("План не содержит массив operations");
        ArrayList<AiPlan.AiOperation> operations = new ArrayList<>();
        boolean inString = false, escaped = false; int depth = 0, start = -1; boolean closed = false;
        for (int i = array.end(); i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == '"') inString = false;
                continue;
            }
            if (c == '"') { inString = true; continue; }
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}') {
                if (--depth < 0) throw new IllegalArgumentException("Некорректный объект в плане");
                if (depth == 0) {
                    Map<String, String> fields = fields(json.substring(start, i + 1));
                    String tool = fields.getOrDefault("tool", "");
                    if (!"create_element".equals(tool) && !"create_stream".equals(tool))
                        throw new IllegalArgumentException("Недопустимая операция ИИ: " + tool);
                    String qualifier = fields.getOrDefault("qualifier", ""), name = fields.getOrDefault("name", "");
                    if (qualifier.isBlank() || name.isBlank()) throw new IllegalArgumentException("У операции отсутствует название или классификатор");
                    operations.add(new AiPlan.AiOperation(tool, qualifier, name, fields.getOrDefault("source", ""),
                            fields.getOrDefault("target", ""), fields.getOrDefault("icom", ""),
                            fields.getOrDefault("style", ""), fields.getOrDefault("color", ""),
                            fields.getOrDefault("description", ""), fields.getOrDefault("tunnel", ""),
                            fields.getOrDefault("parentStream", "")));
                    if (operations.size() > 200) throw new IllegalArgumentException("В плане слишком много операций");
                }
            } else if (c == ']' && depth == 0) { closed = true; break; }
        }
        if (!closed || inString || depth != 0) throw new IllegalArgumentException("План ИИ содержит незавершённый JSON");
        if (operations.isEmpty()) throw new IllegalArgumentException("План ИИ не содержит допустимых операций");
        return new AiPlan("AI-generated Ramus plan", operations);
    }
    private static Map<String, String> fields(String object) {
        Map<String, String> result = new HashMap<>(); Matcher matcher = FIELD.matcher(object);
        while (matcher.find()) {
            String key = decode(matcher.group(1));
            if (result.put(key, decode(matcher.group(2))) != null)
                throw new IllegalArgumentException("Повторяющееся поле операции: " + key);
        }
        return result;
    }
    private static String decode(String value) {
        return DeepSeekClient.content("{\"content\":\"" + value + "\"}");
    }
}
