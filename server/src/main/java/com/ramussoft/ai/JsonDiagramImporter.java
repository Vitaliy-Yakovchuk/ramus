package com.ramussoft.ai;

import com.dsoft.pb.types.FRectangle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.SectorBorder;
import com.ramussoft.pb.data.negine.NSector;
import com.ramussoft.pb.data.negine.NSectorBorder;
import com.ramussoft.pb.idef.elements.ReplaceStreamType;
import com.ramussoft.pb.idef.visual.MovingPanel;
import com.ramussoft.idef0.attribute.SectorPointPersistent;
import com.ramussoft.idef0.attribute.SectorPropertiesPersistent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Imports IDEF0 elements described by a compact JSON structure and recreates
 * them using the public Ramus {@link DataPlugin} API.
 */
public class JsonDiagramImporter {

    private final DataPlugin dataPlugin;
    private final Function rootFunction;
    private final ObjectMapper objectMapper;

    public JsonDiagramImporter(DataPlugin dataPlugin, Function rootFunction) {
        this(dataPlugin, rootFunction, new ObjectMapper());
    }

    public JsonDiagramImporter(DataPlugin dataPlugin, Function rootFunction, ObjectMapper objectMapper) {
        this.dataPlugin = Objects.requireNonNull(dataPlugin, "dataPlugin");
        this.rootFunction = Objects.requireNonNull(rootFunction, "rootFunction");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public ImportResult importDiagram(String json) throws IOException {
        if (json == null) {
            throw new IllegalArgumentException("json must not be null");
        }

        JsonNode root = objectMapper.readTree(json);

        if (!root.isArray()) {
            throw new IllegalArgumentException("Expected top level JSON array");
        }

        DiagramNode rootNode = new DiagramNode(rootFunction);
        Map<Long, Function> functions = new LinkedHashMap<>();
        Map<Long, Stream> streams = new LinkedHashMap<>();
        Map<Long, Sector> sectors = new LinkedHashMap<>();

        List<JsonNode> boxes = new ArrayList<>();
        List<JsonNode> arrows = new ArrayList<>();
        for (JsonNode element : root) {
            String type = textValue(element.get("type"));
            if ("box".equalsIgnoreCase(type)) {
                boxes.add(element);
            } else if ("arrow".equalsIgnoreCase(type)) {
                arrows.add(element);
            }
        }

        createFunctions(boxes, rootNode, functions);

        for (JsonNode arrow : arrows) {
            long arrowId = requireLong(arrow, "id");
            Function diagramFunction = resolveDiagramFunction(arrow, rootNode);

            Sector sector = dataPlugin.createSector();
            sector.setFunction(diagramFunction);
            sector.setCreateState(Sector.STATE_NONE, 0.5);

            Stream stream = createStreamForArrow(arrow);
            sector.setStream(stream, ReplaceStreamType.CHILDREN);
            streams.put(arrowId, stream);

            if (sector instanceof NSector) {
                configureBorders((NSector) sector, arrow, functions);
                ((NSector) sector).setShowText(true);
            }

            sectors.put(arrowId, sector);
        }

        return new ImportResult(functions, streams, sectors);
    }

    private void createFunctions(List<JsonNode> boxes, DiagramNode rootNode, Map<Long, Function> functions) {
        List<JsonNode> pending = new ArrayList<>(boxes);
        while (!pending.isEmpty()) {
            boolean progress = false;
            Iterator<JsonNode> iterator = pending.iterator();
            while (iterator.hasNext()) {
                JsonNode box = iterator.next();
                DiagramNode parentNode = tryResolveNode(box.get("father"), rootNode);
                if (parentNode == null) {
                    continue;
                }

                long id = requireLong(box, "id");
                Function parent = parentNode.function;
                Function function = dataPlugin.createFunction(parent, parent.getType());
                function.setDecompositionType(parent.getDecompositionType());
                if (box.has("name")) {
                    function.setName(box.path("name").asText(""));
                }
                FRectangle bounds = function.getBounds();
                double x = box.path("x").asDouble(bounds.getX());
                double y = box.path("y").asDouble(bounds.getY());
                double width = box.path("width").asDouble(bounds.getWidth());
                double height = box.path("height").asDouble(bounds.getHeight());
                function.setBounds(new FRectangle(x, y, width, height));

                functions.put(id, function);
                parentNode.children.put(id, new DiagramNode(function));

                iterator.remove();
                progress = true;
            }
            if (!progress) {
                throw new IllegalArgumentException("Cannot resolve parents for all box elements");
            }
        }
    }

    private Stream createStreamForArrow(JsonNode arrow) {
        Stream stream = (Stream) dataPlugin.createRow(dataPlugin.getBaseStream(), true);
        String name = arrow.path("name").asText("");
        stream.setName(name);
        stream.setEmptyName(name == null || name.isEmpty());
        return stream;
    }

    private void configureBorders(NSector sector, JsonNode arrow, Map<Long, Function> functions) {
        Attachment source = parseAttachment(arrow, "source", functions, true);
        Attachment target = parseAttachment(arrow, "target", functions, false);

        NSectorBorder start = sector.getStart();
        applyAttachment(start, source);

        NSectorBorder end = sector.getEnd();
        applyAttachment(end, target);
    }

    private Attachment parseAttachment(JsonNode arrow, String field, Map<Long, Function> functions, boolean source) {
        JsonNode valueNode = arrow.get(field);
        if (valueNode == null || valueNode.isNull()) {
            throw new IllegalArgumentException("Arrow element is missing required field '" + field + "'");
        }
        String valueText = valueNode.asText();
        String sideField = field + "Side";
        JsonNode sideNode = arrow.has(sideField) ? arrow.get(sideField) : arrow.get(field + "_side");
        if (sideNode == null && valueNode.isObject()) {
            sideNode = valueNode.get("side");
        }
        String defaultSide = source ? "RIGHT" : "LEFT";
        String sideText = sideNode == null ? defaultSide : sideNode.asText(defaultSide);
        if (isSideKeyword(valueText)) {
            sideText = valueText;
        }
        int side = toSide(sideText, source ? MovingPanel.RIGHT : MovingPanel.LEFT);

        if (isSideKeyword(valueText)) {
            return Attachment.forBorder(side);
        }
        try {
            long functionId = Long.parseLong(valueText);
            Function function = functions.get(functionId);
            if (function == null) {
                throw new IllegalArgumentException("Unknown function id '" + valueText + "' for " + field);
            }
            return Attachment.forFunction(function, side);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Unsupported " + field + " value '" + valueText + "'", ex);
        }
    }

    private void applyAttachment(NSectorBorder border, Attachment attachment) {
        Crosspoint crosspoint = dataPlugin.createCrosspoint();
        border.setCrosspointA(crosspoint);
        border.setFunctionTypeA(attachment.side);
        if (attachment.function != null) {
            border.setFunctionA(attachment.function);
            border.setBorderTypeA(SectorBorder.TYPE_FUNCTION);
        } else {
            border.setFunctionA(null);
            border.setBorderTypeA(SectorBorder.TYPE_BORDER);
        }
        border.commit();
    }

    private Function resolveDiagramFunction(JsonNode node, DiagramNode rootNode) {
        return resolveNode(node.get("father"), rootNode).function;
    }

    private DiagramNode resolveNode(JsonNode fatherNode, DiagramNode rootNode) {
        DiagramNode node = tryResolveNode(fatherNode, rootNode);
        if (node == null) {
            String value = fatherNode == null ? "" : fatherNode.asText();
            throw new IllegalArgumentException("Unknown father path: '" + value + "'");
        }
        return node;
    }

    private DiagramNode tryResolveNode(JsonNode fatherNode, DiagramNode rootNode) {
        String path = normalizeFather(fatherNode);
        if (path.isEmpty()) {
            return rootNode;
        }
        return resolveRecursively(path, 0, rootNode);
    }

    private DiagramNode resolveRecursively(String path, int index, DiagramNode current) {
        if (index >= path.length()) {
            return current;
        }
        for (Map.Entry<Long, DiagramNode> entry : current.children.entrySet()) {
            String token = Long.toString(entry.getKey());
            if (path.startsWith(token, index)) {
                DiagramNode resolved = resolveRecursively(path, index + token.length(), entry.getValue());
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        return null;
    }

    private String normalizeFather(JsonNode fatherNode) {
        if (fatherNode == null || fatherNode.isNull()) {
            return "";
        }
        if (fatherNode.isNumber()) {
            long value = fatherNode.asLong();
            return value == 0L ? "" : Long.toString(value);
        }
        String text = fatherNode.asText("").trim();
        if (text.isEmpty() || "0".equals(text)) {
            return "";
        }
        return text;
    }

    private boolean isSideKeyword(String value) {
        if (value == null) {
            return false;
        }
        switch (value.toUpperCase()) {
            case "LEFT":
            case "RIGHT":
            case "TOP":
            case "BOTTOM":
                return true;
            default:
                return false;
        }
    }

    private int toSide(String sideText, int defaultValue) {
        if (sideText == null) {
            return defaultValue;
        }
        switch (sideText.toUpperCase()) {
            case "LEFT":
                return MovingPanel.LEFT;
            case "RIGHT":
                return MovingPanel.RIGHT;
            case "TOP":
                return MovingPanel.TOP;
            case "BOTTOM":
                return MovingPanel.BOTTOM;
            case "RIGHT_BOTTOM":
                return MovingPanel.RIGHT_BOTTOM;
            case "BOTTOM_LEFT":
                return MovingPanel.BOTTOM_LEFT;
            case "LEFT_TOP":
                return MovingPanel.LEFT_TOP;
            case "TOP_RIGHT":
                return MovingPanel.TOP_RIGHT;
            default:
                return defaultValue;
        }
    }

    private static String textValue(JsonNode node) {
        return node == null ? null : node.asText(null);
    }

    private long requireLong(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.canConvertToLong()) {
            throw new IllegalArgumentException("Missing required numeric field '" + field + "'");
        }
        return value.asLong();
    }

    private static final class DiagramNode {
        private final Function function;
        private final Map<Long, DiagramNode> children = new LinkedHashMap<>();

        private DiagramNode(Function function) {
            this.function = function;
        }
    }

    private static final class Attachment {
        private final Function function;
        private final int side;

        private Attachment(Function function, int side) {
            this.function = function;
            this.side = side;
        }

        private static Attachment forFunction(Function function, int side) {
            return new Attachment(function, side);
        }

        private static Attachment forBorder(int side) {
            return new Attachment(null, side);
        }
    }

    public static final class ImportResult {
        private final Map<Long, Function> functions;
        private final Map<Long, Stream> streams;
        private final Map<Long, Sector> sectors;

        ImportResult(Map<Long, Function> functions, Map<Long, Stream> streams, Map<Long, Sector> sectors) {
            this.functions = functions;
            this.streams = streams;
            this.sectors = sectors;
        }

        public Map<Long, Function> getFunctions() {
            return functions;
        }

        public Map<Long, Stream> getStreams() {
            return streams;
        }

        public Map<Long, Sector> getSectors() {
            return sectors;
        }
    }
}
