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

        ImportState state = new ImportState();

        ImportResult result;
        if (root.isArray()) {
            result = importCompactDiagram(root, state);
        } else if (root.isObject()) {
            result = importLegacyDiagram(root, state);
        } else {
            throw new IllegalArgumentException("Expected top level JSON array or object");
        }

        return result;
    }

    private ImportResult importCompactDiagram(JsonNode root, ImportState state) {
        DiagramNode rootNode = new DiagramNode(rootFunction);

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

        createCompactFunctions(boxes, rootNode, state);

        for (JsonNode arrow : arrows) {
            long arrowId = requireLong(arrow, "id");
            Function diagramFunction = resolveDiagramFunction(arrow, rootNode);

            Sector sector = dataPlugin.createSector();
            sector.setFunction(diagramFunction);
            sector.setCreateState(Sector.STATE_NONE, 0.5);

            Stream stream = createStreamForArrow(arrow);
            sector.setStream(stream, ReplaceStreamType.CHILDREN);
            state.streams.put(arrowId, stream);
            state.streamLookup.put(arrowId, stream);

            if (sector instanceof NSector) {
                configureBorders((NSector) sector, arrow, state);
                ((NSector) sector).setShowText(true);
            }

            state.sectors.put(arrowId, sector);
        }

        return new ImportResult(state.functions, state.streams, state.sectors);
    }

    private ImportResult importLegacyDiagram(JsonNode root, ImportState state) {
        List<JsonNode> elements = new ArrayList<>();
        JsonNode elementsNode = root.get("elements");
        if (elementsNode != null && elementsNode.isArray()) {
            elementsNode.forEach(elements::add);
        }

        List<JsonNode> boxes = new ArrayList<>();
        List<JsonNode> arrows = new ArrayList<>();
        for (JsonNode element : elements) {
            String type = textValue(element.get("type"));
            if ("box".equalsIgnoreCase(type)) {
                boxes.add(element);
            } else if ("arrow".equalsIgnoreCase(type)) {
                arrows.add(element);
            }
        }

        JsonNode streamsNode = root.get("streams");
        if (streamsNode != null && streamsNode.isArray()) {
            for (JsonNode streamNode : streamsNode) {
                long streamId = requireLong(streamNode, "id");
                Stream stream = (Stream) dataPlugin.createRow(dataPlugin.getBaseStream(), true);
                if (streamNode.has("name")) {
                    stream.setName(streamNode.path("name").asText(""));
                }
                if (streamNode.has("emptyName")) {
                    stream.setEmptyName(streamNode.path("emptyName").asBoolean(false));
                } else {
                    stream.setEmptyName(stream.getName() == null || stream.getName().isEmpty());
                }
                state.streams.put(streamId, stream);
                state.streamLookup.put(streamId, stream);
            }
        }

        createLegacyFunctions(boxes, state);

        for (JsonNode arrow : arrows) {
            long arrowId = requireLong(arrow, "id");
            JsonNode sectorNode = arrow.get("sector");
            if (sectorNode == null || !sectorNode.isObject()) {
                throw new IllegalArgumentException("Arrow element is missing required object 'sector'");
            }

            long functionId = sectorNode.path("functionId").asLong(0L);
            Function diagramFunction = resolveFunctionById(state, functionId);

            Sector sector = dataPlugin.createSector();
            sector.setFunction(diagramFunction);
            NSector nSector = (NSector) sector;

            JsonNode geometry = sectorNode.get("geometry");
            if (geometry != null && geometry.isObject()) {
                int createState = geometry.path("createState").asInt(Sector.STATE_NONE);
                double createPos = geometry.path("createPos").asDouble(0.5);
                sector.setCreateState(createState, createPos);
                sector.setShowText(geometry.path("showText").asInt(1) != 0);
                if (geometry.has("alternativeText")) {
                    sector.setAlternativeText(geometry.path("alternativeText").asText(""));
                }
                if (geometry.has("textAlignment")) {
                    sector.setTextAligment(geometry.path("textAlignment").asInt(0));
                }
                if (geometry.has("visualAttributes")) {
                    String visual = geometry.path("visualAttributes").asText("");
                    if (!visual.isEmpty()) {
                        sector.setVisualAttributes(decodeBase64(visual));
                    }
                }
            } else {
                sector.setCreateState(Sector.STATE_NONE, 0.5);
                sector.setShowText(true);
            }

            JsonNode labelFrame = sectorNode.get("labelFrame");
            if (labelFrame != null && labelFrame.isObject()) {
                SectorPropertiesPersistent properties = nSector.getSectorProperties();
                if (labelFrame.has("showText")) {
                    properties.setShowText(labelFrame.path("showText").asInt(properties.getShowText()));
                }
                if (labelFrame.has("textX")) {
                    properties.setTextX(labelFrame.path("textX").asDouble(properties.getTextX()));
                }
                if (labelFrame.has("textY")) {
                    properties.setTextY(labelFrame.path("textY").asDouble(properties.getTextY()));
                }
                if (labelFrame.has("textWidth")) {
                    properties.setTextWidth(labelFrame.path("textWidth").asDouble(properties.getTextWidth()));
                }
                if (labelFrame.has("textHeight")) {
                    properties.setTextHieght(labelFrame.path("textHeight").asDouble(properties.getTextHieght()));
                }
                if (labelFrame.has("transparent")) {
                    properties.setTransparent(labelFrame.path("transparent").asInt(properties.getTransparent()));
                }
                if (labelFrame.has("showTilda")) {
                    properties.setShowTilda(labelFrame.path("showTilda").asInt(properties.getShowTilda()));
                }
                nSector.setSectorProperties(properties);
            }

            JsonNode bendPoints = sectorNode.get("bendPoints");
            if (bendPoints != null && bendPoints.isArray()) {
                List<SectorPointPersistent> points = new ArrayList<>();
                for (JsonNode pointNode : bendPoints) {
                    SectorPointPersistent point = new SectorPointPersistent();
                    if (pointNode.has("xOrdinateId")) {
                        point.setXOrdinateId(pointNode.path("xOrdinateId").asLong(0L));
                    }
                    if (pointNode.has("yOrdinateId")) {
                        point.setYOrdinateId(pointNode.path("yOrdinateId").asLong(0L));
                    }
                    if (pointNode.has("x")) {
                        point.setXPosition(pointNode.path("x").asDouble(0.0));
                    }
                    if (pointNode.has("y")) {
                        point.setYPosition(pointNode.path("y").asDouble(0.0));
                    }
                    if (pointNode.has("type")) {
                        point.setPointType(parsePointType(pointNode.path("type").asText("")));
                    }
                    if (pointNode.has("position")) {
                        point.setPosition(pointNode.path("position").asInt(point.getPosition()));
                    }
                    points.add(point);
                }
                nSector.setSectorPointPersistents(points);
            }

            long streamId = sectorNode.path("streamId").asLong(Long.MIN_VALUE);
            Stream stream = state.streamLookup.get(streamId);
            if (stream == null) {
                stream = createStreamForArrow(arrow);
                if (streamId != Long.MIN_VALUE) {
                    state.streams.put(streamId, stream);
                    state.streamLookup.put(streamId, stream);
                } else {
                    state.streams.put(arrowId, stream);
                    state.streamLookup.put(arrowId, stream);
                }
            }
            sector.setStream(stream, ReplaceStreamType.CHILDREN);

            configureLegacyBorders(nSector, sectorNode, state);

            state.sectors.put(arrowId, sector);
        }

        return new ImportResult(state.functions, state.streams, state.sectors);
    }

    private void createCompactFunctions(List<JsonNode> boxes, DiagramNode rootNode, ImportState state) {
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

                state.functions.put(id, function);
                state.functionLookup.put(id, function);
                parentNode.children.put(id, new DiagramNode(function));

                iterator.remove();
                progress = true;
            }
            if (!progress) {
                throw new IllegalArgumentException("Cannot resolve parents for all box elements");
            }
        }
    }

    private void createLegacyFunctions(List<JsonNode> boxes, ImportState state) {
        List<JsonNode> pending = new ArrayList<>(boxes);
        while (!pending.isEmpty()) {
            boolean progress = false;
            Iterator<JsonNode> iterator = pending.iterator();
            while (iterator.hasNext()) {
                JsonNode box = iterator.next();
                JsonNode parentNode = box.get("parent");
                long parentId = parentNode == null ? 0L : parentNode.path("functionId").asLong(0L);
                Function parent = parentId == 0L ? rootFunction : state.functionLookup.get(parentId);
                if (parent == null) {
                    continue;
                }
                if (parentNode != null && parentNode.has("diagramType")) {
                    parent.setDecompositionType(parentNode.path("diagramType").asInt(parent.getDecompositionType()));
                }

                long id = requireLong(box, "id");
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

                state.functions.put(id, function);
                state.functionLookup.put(id, function);

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

    private void configureBorders(NSector sector, JsonNode arrow, ImportState state) {
        Attachment source = parseAttachment(arrow, "source", state.functionLookup, true);
        Attachment target = parseAttachment(arrow, "target", state.functionLookup, false);

        NSectorBorder start = sector.getStart();
        applyAttachment(start, source);

        NSectorBorder end = sector.getEnd();
        applyAttachment(end, target);
    }

    private void configureLegacyBorders(NSector sector, JsonNode sectorNode, ImportState state) {
        configureLegacyBorder(sector.getStart(), sectorNode.get("start"), state, true);
        configureLegacyBorder(sector.getEnd(), sectorNode.get("end"), state, false);
    }

    private void configureLegacyBorder(NSectorBorder border, JsonNode node, ImportState state, boolean source) {
        if (node == null || !node.isObject()) {
            return;
        }

        int side = toSide(node.path("functionSide").asText(null), source ? MovingPanel.RIGHT : MovingPanel.LEFT);
        border.setFunctionTypeA(side);

        if (node.has("borderType")) {
            border.setBorderTypeA(parseBorderType(node.path("borderType").asText("")));
        }

        if (node.has("tunnelType")) {
            border.getSbp().setTunnelSoft(node.path("tunnelType").asInt(border.getSbp().getTunnelSoft()));
        } else if (node.has("tunnelSoft")) {
            border.getSbp().setTunnelSoft(node.path("tunnelSoft").asInt(border.getSbp().getTunnelSoft()));
        }

        long functionId = node.path("functionId").asLong(-1L);
        Function function = resolveFunctionById(state, functionId);
        border.setFunctionA(function);
        if (function == null) {
            border.setBorderTypeA(SectorBorder.TYPE_BORDER);
        }

        if (node.has("crosspointId")) {
            long crosspointId = node.path("crosspointId").asLong(-1L);
            if (crosspointId >= 0) {
                Crosspoint crosspoint = resolveCrosspoint(state, crosspointId);
                border.setCrosspointA(crosspoint);
            }
        }

        border.commit();
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

    private Function resolveFunctionById(ImportState state, long functionId) {
        if (functionId < 0) {
            return null;
        }
        if (functionId == 0L) {
            return rootFunction;
        }
        Function function = state.functionLookup.get(functionId);
        if (function == null) {
            throw new IllegalArgumentException("Unknown function id '" + functionId + "'");
        }
        return function;
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

    private int parsePointType(String type) {
        if (type == null) {
            return 0;
        }
        switch (type.toUpperCase()) {
            case "MIDDLE":
                return 1;
            case "START":
                return 0;
            case "END":
                return 2;
            default:
                return 0;
        }
    }

    private int parseBorderType(String borderType) {
        if (borderType == null) {
            return SectorBorder.TYPE_BORDER;
        }
        switch (borderType.toUpperCase()) {
            case "FUNCTION":
                return SectorBorder.TYPE_FUNCTION;
            case "SPOT":
                return SectorBorder.TYPE_SPOT;
            case "BORDER":
            default:
                return SectorBorder.TYPE_BORDER;
        }
    }

    private Crosspoint resolveCrosspoint(ImportState state, long crosspointId) {
        if (crosspointId < 0) {
            return null;
        }
        Crosspoint crosspoint = state.crosspoints.get(crosspointId);
        if (crosspoint != null) {
            return crosspoint;
        }
        Crosspoint existing = dataPlugin.findCrosspointByGlobalId(crosspointId);
        if (existing != null) {
            crosspoint = existing;
        } else {
            crosspoint = dataPlugin.createCrosspoint(crosspointId);
        }
        state.crosspoints.put(crosspointId, crosspoint);
        return crosspoint;
    }

    private byte[] decodeBase64(String value) {
        if (value == null || value.isEmpty()) {
            return new byte[0];
        }
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid base64 value", ex);
        }
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

    private final class ImportState {
        private final Map<Long, Function> functions = new LinkedHashMap<>();
        private final Map<Long, Function> functionLookup = new HashMap<>();
        private final Map<Long, Stream> streams = new LinkedHashMap<>();
        private final Map<Long, Stream> streamLookup = new HashMap<>();
        private final Map<Long, Sector> sectors = new LinkedHashMap<>();
        private final Map<Long, Crosspoint> crosspoints = new HashMap<>();

        private ImportState() {
            functionLookup.put(0L, rootFunction);
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
