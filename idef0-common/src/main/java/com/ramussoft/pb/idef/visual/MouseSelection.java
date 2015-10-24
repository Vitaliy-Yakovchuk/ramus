package com.ramussoft.pb.idef.visual;

import java.awt.Color;
import java.awt.Graphics2D;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.dsoft.utils.DataLoader.MemoryData;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.data.FunctionBean;
import com.ramussoft.pb.dfds.visual.DFDSRole;
import com.ramussoft.pb.dmaster.UserTemplate;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.Point;

public class MouseSelection extends Rectangle2D.Double implements VisualPanel {

    private List<Function> functions = new ArrayList<Function>();

    private List<MovingLabel> labels = new ArrayList<MovingLabel>();

    public boolean dr = true;

    private List<PaintSector> sectors = new ArrayList<PaintSector>();

    public MouseSelection(double x, double y) {
        super(x, y, 0, 0);
    }

    public void move(double newX, double newY, MovingArea area) {
        width = newX - getX();
        height = newY - getY();
        area.addToSelection(this);
    }

    public void add(MovingPanel panel) {
        if (panel instanceof IDEF0Object)
            functions.add(((IDEF0Object) panel).getFunction());
        else if (panel instanceof MovingLabel) {
            MovingLabel movingLabel = (MovingLabel) panel;
            movingLabel.boundsCopy = new FRectangle(movingLabel.getBounds());
            labels.add(movingLabel);
        }
    }

    public void remove(MovingPanel panel) {
        if (panel instanceof IDEF0Object)
            functions.remove(((IDEF0Object) panel).getFunction());
        else
            labels.remove(panel);
    }

    public boolean contains(MovingPanel panel) {
        if (panel instanceof IDEF0Object)
            return functions.contains(((IDEF0Object) panel).getFunction());
        else {
            int index = labels.indexOf(panel);
            if (index >= 0) {
                MovingLabel movingLabel = (MovingLabel) panel;
                movingLabel.boundsCopy = labels.get(index).boundsCopy;
                labels.set(index, movingLabel);
                return true;
            }
            return false;
        }
    }

    public boolean isRemoveable() {
        for (Function function : functions)
            if (!function.isRemoveable())
                return false;
        return true;
    }

    public Set<FunctionBean> copy() {
        HashSet<FunctionBean> beans = new HashSet<FunctionBean>();
        for (Function function : functions)
            beans.add(toBean(function));
        return beans;
    }

    private FunctionBean toBean(Function function) {
        FunctionBean fb = new FunctionBean();
        fb.setBackground(function.getBackground());
        fb.setBounds(function.getBounds());
        fb.setFont(function.getFont());
        fb.setForeground(function.getForeground());
        fb.setName(function.getName());
        fb.setType(function.getType());
        return fb;
    }

    @Override
    public Color getBackgroundA() {
        Color background = functions.get(0).getBackground();
        for (int i = 1; i < functions.size(); i++) {
            if (!background.equals(functions.get(i).getBackground()))
                return null;
        }
        return background;
    }

    @Override
    public Color getForegroundA() {
        Color foreground = functions.get(0).getForeground();
        for (int i = 1; i < functions.size(); i++) {
            if (!foreground.equals(functions.get(i).getForeground()))
                return null;
        }
        return foreground;
    }

    @Override
    public Font getFontA() {
        Font font = functions.get(0).getFont();
        for (int i = 1; i < functions.size(); i++) {
            if (!font.equals(functions.get(i).getFont()))
                return null;
        }
        return font;
    }

    @Override
    public void setBackgroundA(Color background) {
        for (Function function : functions)
            function.setBackground(background);
    }

    @Override
    public void setForegroundA(Color foreground) {
        for (Function function : functions)
            function.setForeground(foreground);
    }

    @Override
    public void setFontA(Font font) {
        for (Function function : functions)
            function.setFont(font);
    }

    @Override
    public FRectangle getBoundsA() {
        FRectangle rect = functions.get(0).getBounds();
        for (int i = 1; i < functions.size(); i++) {
            if (rect.getWidth() != functions.get(i).getBounds().getWidth()
                    || rect.getHeight() != functions.get(i).getBounds()
                    .getHeight())
                return null;
        }
        return rect;
    }

    @Override
    public void setBoundsA(FRectangle bounds) {
    }

    public void addSectors(MovingArea movingArea) {
        List<PaintSector> sectors = new ArrayList<PaintSector>();
        sectors.addAll(movingArea.getRefactor().getSectors());
        UserTemplate.clearSectors(sectors, functions,
                movingArea.getDataPlugin());

		/*movingArea.startUserTransaction();

		for (PaintSector ps : sectors)
			ps.remove();

		movingArea.commitUserTransaction();*/

        this.sectors = sectors;
    }

    public void onProcessEndBoundsChange(MovingArea movingArea,
                                         MovingText[] panels, FloatPoint diff) {

        for (MovingText text : panels)
            if (text instanceof DFDSRole) {
                IDEF0Object idef = (IDEF0Object) text;
                if (contains(text))
                    idef.onProcessEndBoundsChange(sectors);
            }
        for (MovingText text : panels) {
            if (!(text instanceof DFDSRole))
                if (contains(text)) {
                    if (text instanceof IDEF0Object) {
                        IDEF0Object idef = (IDEF0Object) text;
                        idef.onProcessEndBoundsChange(sectors);
                    }
                }
        }

        List<Ordinate> xOrdinates = new ArrayList<Ordinate>();
        List<Ordinate> yOrdinates = new ArrayList<Ordinate>();

        for (PaintSector ps : sectors) {
            for (Point point : ps.getPoints()) {
                boolean has;
                has = false;
                for (Ordinate o : xOrdinates)
                    if (o.getOrdinateId() == point.getXOrdinate()
                            .getOrdinateId()) {
                        has = true;
                        break;
                    }
                if (!has)
                    xOrdinates.add(point.getXOrdinate());
                has = false;
                for (Ordinate o : yOrdinates)
                    if (o.getOrdinateId() == point.getYOrdinate()
                            .getOrdinateId()) {
                        has = true;
                        break;
                    }
                if (!has)
                    yOrdinates.add(point.getYOrdinate());
            }
        }

        List<PaintSector> psrs = new ArrayList<PaintSector>();

        psrs.addAll(sectors);

        for (Ordinate xOrdinate : xOrdinates) {
            xOrdinate.setPosition(xOrdinate.getPosition() + diff.getX());
            for (Point point : xOrdinate.getPoints())
                if (!psrs.contains(point.getSector()))
                    psrs.add(point.getSector());
        }
        for (Ordinate yOrdinate : yOrdinates) {
            yOrdinate.setPosition(yOrdinate.getPosition() + diff.getY());
            for (Point point : yOrdinate.getPoints())
                if (!psrs.contains(point.getSector()))
                    psrs.add(point.getSector());
        }

        MemoryData data = new MemoryData();

        for (PaintSector paintSector : sectors) {
            Function sFunction = paintSector.getSector().getStart()
                    .getFunction();
            if (sFunction != null) {
                switch (paintSector.getSector().getStart().getFunctionType()) {
                    case MovingFunction.LEFT:
                        paintSector.getStartPoint().setX(
                                sFunction.getBounds().getLeft());
                        break;

                    case MovingFunction.RIGHT:
                        paintSector.getStartPoint().setX(
                                sFunction.getBounds().getRight());
                        break;

                    case MovingFunction.TOP:
                        paintSector.getStartPoint().setY(
                                sFunction.getBounds().getTop());
                        break;
                    case MovingFunction.BOTTOM:
                        paintSector.getStartPoint().setY(
                                sFunction.getBounds().getBottom());
                        break;
                }
            }

            sFunction = paintSector.getSector().getEnd().getFunction();
            if (sFunction != null) {
                switch (paintSector.getSector().getEnd().getFunctionType()) {
                    case MovingFunction.LEFT:
                        paintSector.getEndPoint().setX(
                                sFunction.getBounds().getLeft());
                        break;

                    case MovingFunction.RIGHT:
                        paintSector.getEndPoint().setX(
                                sFunction.getBounds().getRight());
                        break;

                    case MovingFunction.TOP:
                        paintSector.getEndPoint().setY(
                                sFunction.getBounds().getTop());
                        break;
                    case MovingFunction.BOTTOM:
                        paintSector.getEndPoint().setY(
                                sFunction.getBounds().getBottom());
                        break;
                }
            }
        }

        for (MovingText text : panels) {
            if (!(text instanceof DFDSRole))
                if (contains(text)) {
                    if (!(text instanceof IDEF0Object)) {
                        text.onProcessEndBoundsChange();
                        if (text instanceof MovingLabel)
                            ((MovingLabel) text).boundsCopy = text.getBounds();
                    }
                }
        }

        for (PaintSector ps : psrs)
            PaintSector.save(ps, data, movingArea.getDataPlugin().getEngine());
    }

    public void paint(Graphics2D g, MovingArea area) {
        for (Function function : functions) {
            FRectangle rectangle = function.getBounds();
            g.drawRect(area.getIntOrdinate(rectangle.getX() - 2),
                    area.getIntOrdinate(rectangle.getY() - 2),
                    area.getIntOrdinate(rectangle.getWidth() + 4),
                    area.getIntOrdinate(rectangle.getHeight() + 4));
        }
        for (MovingLabel label : labels) {
            FRectangle rectangle = label.boundsCopy;
            g.drawRect(area.getIntOrdinate(rectangle.getX() - 2),
                    area.getIntOrdinate(rectangle.getY() - 2),
                    area.getIntOrdinate(rectangle.getWidth() + 4),
                    area.getIntOrdinate(rectangle.getHeight() + 4));
        }
    }

    public void clear() {
        functions.clear();
        labels.clear();
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public List<MovingLabel> getLabels() {
        return labels;
    }

    public List<Row> getSelectedArrowsAddedRows() {
        List<Row> rows = new ArrayList<Row>();
        for (MovingLabel label : labels) {
            Sector sector = label.getSector().getSector();
            if (sector.getStream() != null)
                for (Row row : sector.getStream().getAdded())
                    if (row != null && !rows.contains(row))
                        rows.add(row);
        }

        return rows;
    }
}
