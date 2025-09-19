package com.ramussoft.ai.gui;

import java.util.HashMap;
import java.util.Map;

import com.dsoft.pb.types.FRectangle;
import com.dsoft.utils.DataLoader.MemoryData;
import com.ramussoft.ai.AiDiagramDefinition;
import com.ramussoft.ai.AiFunctionDefinition;
import com.ramussoft.ai.AiStreamDefinition;
import com.ramussoft.ai.AiStreamEndpoint;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.Qualifier;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.idef0.IDEF0ViewPlugin;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.idef0.OpenDiagram;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.elements.ReplaceStreamType;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingPanel;

/**
 * Applies an {@link AiDiagramDefinition} to the current diagram in the GUI.
 */
public class AiDiagramApplier {

    private static final double DEFAULT_WIDTH = 0.2d;
    private static final double DEFAULT_HEIGHT = 0.12d;

    private final GUIFramework framework;

    public AiDiagramApplier(GUIFramework framework) {
        this.framework = framework;
    }

    public void apply(OpenDiagram target, AiDiagramDefinition definition) throws Exception {
        if (definition == null) {
            return;
        }
        Qualifier qualifier = (target != null) ? target.getQualifier() : null;
        if (qualifier == null) {
            qualifier = framework.getEngine().createQualifier();
            target = new OpenDiagram(qualifier, -1l);
        }
        DataPlugin dataPlugin = NDataPluginFactory.getDataPlugin(qualifier,
                framework.getEngine(), framework.getAccessRules());
        if (dataPlugin == null) {
            throw new IllegalStateException("IDEF0 data plugin is not available");
        }
        Function context = resolveContextFunction(dataPlugin, target);
        if (context == null) {
            throw new IllegalStateException("Unable to resolve active function for diagram");
        }

        MovingArea movingArea = new MovingArea(dataPlugin, context);
        movingArea.setActiveFunction(context);

        boolean startedByUs = false;
        if (!movingArea.isUserTransactionStarted()) {
            movingArea.startUserTransaction();
            startedByUs = true;
        }

        Map<String, Function> functions = new HashMap<String, Function>();
        try {
            createFunctions(definition, context, movingArea, functions);
            createStreams(definition, context, movingArea, functions);
            movingArea.getRefactor().setUndoPoint();
            if (startedByUs && movingArea.isUserTransactionStarted()) {
                movingArea.commitUserTransaction();
            }
        } catch (Exception ex) {
            rollback(movingArea);
            throw ex;
        }

        NDataPluginFactory.fullRefrash(framework);
        framework.propertyChanged(IDEF0ViewPlugin.ACTIVE_DIAGRAM, target);
    }

    private void rollback(MovingArea area) {
        Engine engine = area.getDataPlugin().getEngine();
        if (engine instanceof Journaled) {
            ((Journaled) engine).rollbackUserTransaction();
        }
    }

    private Function resolveContextFunction(DataPlugin dataPlugin, OpenDiagram target) {
        if (target == null) {
            return dataPlugin.getBaseFunction();
        }
        if (target.getFunctionId() < 0) {
            return dataPlugin.getBaseFunction();
        }
        return (Function) dataPlugin.findRowByGlobalId(target.getFunctionId());
    }

    private void createFunctions(AiDiagramDefinition definition, Function context,
                                 MovingArea area, Map<String, Function> functions) {
        if (definition.getFunctions() == null) {
            return;
        }
        for (AiFunctionDefinition functionDefinition : definition.getFunctions()) {
            if (functionDefinition == null) {
                continue;
            }
            double x = valueOrDefault(functionDefinition.getX(), context.getBounds().getX());
            double y = valueOrDefault(functionDefinition.getY(), context.getBounds().getY());
            int type = resolveFunctionType(functionDefinition.getType());
            Function created = area.createFunctionalObject(x, y, type, context);
            FRectangle bounds = created.getBounds();
            bounds.setX(x);
            bounds.setY(y);
            bounds.setWidth(valueOrDefault(functionDefinition.getWidth(),
                    bounds.getWidth() > 0 ? bounds.getWidth() : DEFAULT_WIDTH));
            bounds.setHeight(valueOrDefault(functionDefinition.getHeight(),
                    bounds.getHeight() > 0 ? bounds.getHeight() : DEFAULT_HEIGHT));
            created.setBounds(bounds);
            if (functionDefinition.getName() != null && functionDefinition.getName().trim().length() > 0) {
                created.setName(functionDefinition.getName().trim());
            }
            if (functionDefinition.getId() != null) {
                functions.put(functionDefinition.getId(), created);
            }
        }
    }

    private void createStreams(AiDiagramDefinition definition, Function context,
                               MovingArea area, Map<String, Function> functions) {
        if (definition.getStreams() == null) {
            return;
        }
        SectorRefactor refactor = area.getRefactor();
        for (AiStreamDefinition stream : definition.getStreams()) {
            if (stream == null) {
                continue;
            }
            AiStreamEndpoint source = stream.getSource();
            AiStreamEndpoint target = stream.getTarget();
            if (source == null || target == null) {
                continue;
            }
            SectorRefactor.PerspectivePoint startPoint = buildPoint(source, functions, context, true);
            SectorRefactor.PerspectivePoint endPoint = buildPoint(target, functions, context, false);
            refactor.setPoint(startPoint);
            refactor.setPoint(endPoint);
            refactor.createNewSector();
            refactor.fixOwners();
            PaintSector sector = refactor.getSector();
            applyStreamName(area, sector, stream.getName());
            PaintSector.save(sector, new MemoryData(), area.getDataPlugin().getEngine());
        }
    }

    private SectorRefactor.PerspectivePoint buildPoint(AiStreamEndpoint endpoint,
                                                       Map<String, Function> functions,
                                                       Function context, boolean start) {
        SectorRefactor.PerspectivePoint point = new SectorRefactor.PerspectivePoint();
        point.type = start ? SectorRefactor.TYPE_START : SectorRefactor.TYPE_END;
        int side = resolveSide(endpoint.getSide());
        if ("function".equalsIgnoreCase(endpoint.getType())) {
            Function function = endpoint.getRef() != null ? functions.get(endpoint.getRef()) : null;
            if (function == null) {
                throw new IllegalArgumentException("Unknown function reference: " + endpoint.getRef());
            }
            point.setFunction(function, side);
            Point p = createFunctionPoint(function, side);
            point.point = p;
            point.x = endpoint.getX() != null ? endpoint.getX() : p.getX();
            point.y = endpoint.getY() != null ? endpoint.getY() : p.getY();
        } else {
            point.borderType = side;
            double[] coords = computeBoundaryCoordinates(side, context);
            double x = endpoint.getX() != null ? endpoint.getX() : coords[0];
            double y = endpoint.getY() != null ? endpoint.getY() : coords[1];
            point.x = x;
            point.y = y;
        }
        return point;
    }

    private Point createFunctionPoint(Function function, int side) {
        FRectangle bounds = function.getBounds();
        double x = bounds.getX() + bounds.getWidth() / 2d;
        double y = bounds.getY() + bounds.getHeight() / 2d;
        switch (side) {
            case MovingPanel.LEFT:
                x = bounds.getX();
                break;
            case MovingPanel.RIGHT:
                x = bounds.getRight();
                break;
            case MovingPanel.TOP:
                y = bounds.getY();
                break;
            case MovingPanel.BOTTOM:
                y = bounds.getBottom();
                break;
            default:
                break;
        }
        Ordinate xOrd = new Ordinate(Ordinate.TYPE_X);
        xOrd.setPosition(x);
        Ordinate yOrd = new Ordinate(Ordinate.TYPE_Y);
        yOrd.setPosition(y);
        Point p = new Point(xOrd, yOrd);
        if (side == MovingPanel.LEFT || side == MovingPanel.RIGHT) {
            p.setType(Ordinate.TYPE_X);
        } else {
            p.setType(Ordinate.TYPE_Y);
        }
        return p;
    }

    private double[] computeBoundaryCoordinates(int side, Function context) {
        FRectangle bounds = context != null ? context.getBounds() : new FRectangle();
        double centerX = bounds.getX() + bounds.getWidth() / 2d;
        double centerY = bounds.getY() + bounds.getHeight() / 2d;
        double x = centerX;
        double y = centerY;
        switch (side) {
            case MovingPanel.LEFT:
                x = bounds.getX();
                break;
            case MovingPanel.RIGHT:
                x = bounds.getRight();
                break;
            case MovingPanel.TOP:
                y = bounds.getY();
                break;
            case MovingPanel.BOTTOM:
                y = bounds.getBottom();
                break;
            default:
                break;
        }
        return new double[]{x, y};
    }

    private void applyStreamName(MovingArea area, PaintSector sector, String name) {
        if (sector == null) {
            return;
        }
        String trimmed = name != null ? name.trim() : null;
        Stream stream = sector.getStream();
        if (stream == null || !stream.isEmptyName()) {
            stream = (Stream) area.getDataPlugin().createRow(area.getDataPlugin().getBaseStream(), true);
        }
        if (trimmed != null && trimmed.length() > 0) {
            stream.setName(trimmed);
            stream.setEmptyName(false);
        } else {
            stream.setEmptyName(true);
        }
        sector.setStream(stream, ReplaceStreamType.CHILDREN);
        sector.setShowText(true);
        sector.createTexts();
    }

    private int resolveFunctionType(String type) {
        if (type == null || type.trim().length() == 0) {
            return Function.TYPE_PROCESS;
        }
        String normalized = type.trim().toLowerCase();
        if ("process".equals(normalized) || "function".equals(normalized) || "activity".equals(normalized)) {
            return Function.TYPE_PROCESS;
        }
        if ("operation".equals(normalized) || "action".equals(normalized)) {
            return Function.TYPE_OPERATION;
        }
        if ("external_reference".equals(normalized) || "external-reference".equals(normalized)
                || "external".equals(normalized)) {
            return Function.TYPE_EXTERNAL_REFERENCE;
        }
        if ("data_store".equals(normalized) || "datastore".equals(normalized) || "store".equals(normalized)) {
            return Function.TYPE_DATA_STORE;
        }
        if ("role".equals(normalized) || "dfds_role".equals(normalized) || "resource".equals(normalized)) {
            return Function.TYPE_DFDS_ROLE;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ex) {
            return Function.TYPE_PROCESS;
        }
    }

    private int resolveSide(String side) {
        if (side == null) {
            return MovingPanel.LEFT;
        }
        String normalized = side.trim().toUpperCase();
        if ("RIGHT".equals(normalized)) {
            return MovingPanel.RIGHT;
        }
        if ("TOP".equals(normalized)) {
            return MovingPanel.TOP;
        }
        if ("BOTTOM".equals(normalized)) {
            return MovingPanel.BOTTOM;
        }
        return MovingPanel.LEFT;
    }

    private double valueOrDefault(Double value, double fallback) {
        if (value == null) {
            return fallback;
        }
        return value.doubleValue();
    }
}
