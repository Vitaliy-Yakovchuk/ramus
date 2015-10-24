package com.ramussoft.pb.dfds.visual;

import java.awt.Color;
import java.awt.Graphics2D;

import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.dsoft.utils.DataLoader.MemoryData;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.attribute.RectangleVisualOptions;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.data.negine.NSectorBorder;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.elements.ReplaceStreamType;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingText;

public class DFDSRole extends DFDSObject implements Comparable<DFDSRole> {

    public DFDSRole(MovingArea movingArea, Function function) {
        super(movingArea, function);
    }

    @Override
    public void paint(Graphics2D g) {

        g.setColor(function.getBackground());
        final Rectangle2D rect = movingArea.getBounds(getBounds());

        RoundRectangle2D.Double rec = new RoundRectangle2D.Double(rect.getX(),
                rect.getY(), rect.getWidth(), rect.getHeight(),
                movingArea.getIDoubleOrdinate(4),
                movingArea.getIDoubleOrdinate(4));

        g.fill(rec);
        g.setFont(function.getFont());
        paintText(g);

        paintBorder(g);

        final Stroke tmp = g.getStroke();
        g.draw(rec);
        g.setStroke(tmp);
        paintTringle(g);

    }

    @Override
    public void paintTringle(Graphics2D g) {
        if (paintTriangle >= 0) {
            g.setColor(Color.BLACK);
            final Rectangle2D rect = movingArea.getBounds(getBounds());
            if (getFunction().getOwner() != null) {
                g.draw(new Rectangle2D.Double(rect.getX() + 3, rect.getY() + 3,
                        rect.getWidth() - 6, rect.getHeight() - 6));
            } else {

                g.fill(getTrianglePath(paintTriangle).createTransformedShape(
                        AffineTransform.getScaleInstance(movingArea.zoom,
                                movingArea.zoom)));
            }
        }
    }

    @Override
    public void onProcessEndBoundsChange() {
        final FRectangle oldRec = function.getBounds();

        myBounds.setTransformNetBounds(MovingArea.NET_LENGTH);
        List<PaintSector> list = new ArrayList<PaintSector>();

        final SectorRefactor refactor = movingArea.getRefactor();

        boolean sectorReplaced = false;

        DFDSFunction function = movingArea.findDFDSFunction(this.getBounds());
        if (function != null) {
            getFunction().setOwner(function.getFunction());

            for (int i = 0; i < refactor.getSectorsCount(); i++) {
                PaintSector ps = refactor.getSector(i);
                Function function2 = ps.getSector().getStart().getFunction();
                if (function2 != null && function2.equals(this.function)) {
                    replaceFunction(ps.getSector().getStart(), ps, list,
                            ps.getStartPoint(), ps.getSector().getStart()
                                    .getFunctionType(), function, refactor,
                            true);
                }
                Function function3 = ps.getSector().getEnd().getFunction();
                if (function3 != null && function3.equals(this.function)) {
                    replaceFunction(ps.getSector().getEnd(), ps, list,
                            ps.getEndPoint(), ps.getSector().getEnd()
                                    .getFunctionType(), function, refactor,
                            false);
                }
            }

            function.justifyRoles();
            sectorReplaced = true;
        } else {
            if (this.getFunction().getOwner() != null) {
                DFDSFunction function2 = movingArea.findDFDSFunction(this
                        .getFunction().getOwner());
                if (function2 != null
                        && function2.getBounds().intersects(this.getBounds())) {
                    function2.justifyRoles();
                } else
                    this.function.setBounds(new FRectangle(myBounds));
            } else
                this.function.setBounds(new FRectangle(myBounds));
        }

        MemoryData memoryData = new MemoryData();
        if (!sectorReplaced) {
            for (int i = 0; i < refactor.getSectorsCount(); i++) {
                PaintSector sector = refactor.getSector(i);
                setAddedSectorPos(oldRec, sector, list);
            }
        }
        for (PaintSector ps : list)
            PaintSector.save(ps, memoryData,
                    ((NFunction) this.function).getEngine());
        long l = this.function.getLink();
        if (l >= 0l) {
            Stream stream = (Stream) movingArea.getDataPlugin()
                    .findRowByGlobalId(l);
            if (stream != null) {
                Row[] rows = stream.getAdded();
                RectangleVisualOptions ops = new RectangleVisualOptions();
                ops.bounds = this.function.getBounds();
                ops.background = this.function.getBackground();
                ops.font = this.function.getFont();
                ops.foreground = this.function.getForeground();
                for (Row row : rows)
                    if (row != null) {
                        IDEF0Plugin.setDefaultRectangleVisualOptions(movingArea
                                        .getDataPlugin().getEngine(), row.getElement(),
                                ops);
                    }
            }
        }
    }

    private void replaceFunction(NSectorBorder border, PaintSector paintSector,
                                 List<PaintSector> list, Point point, int functionType,
                                 DFDSFunction function, SectorRefactor refactor, boolean start) {
        FRectangle bounds = function.getBounds();
        FRectangle thisBounds = this.function.getBounds();
        border.setFunctionA(function.getFunction());
        border.commit();
        if (!list.contains(paintSector))
            list.add(paintSector);
        double p;
        double inPos = 0;
        switch (functionType) {
            case MovingText.LEFT:
                point.setX(bounds.getX());
                p = (point.getY() - thisBounds.getY()) / thisBounds.getHeight();
                point.setY(bounds.getY() + bounds.getHeight() * p);
                inPos = point.getY();
                break;

            case MovingText.RIGHT:
                point.setX(bounds.getRight());
                p = (point.getY() - thisBounds.getY()) / thisBounds.getHeight();
                point.setY(bounds.getY() + bounds.getHeight() * p);
                inPos = point.getY();
                break;
            case MovingText.TOP:
                p = (point.getX() - thisBounds.getX()) / thisBounds.getWidth();
                point.setX(bounds.getX() + bounds.getWidth() * p);
                point.setY(bounds.getY());
                inPos = point.getX();
                break;
            case MovingText.BOTTOM:
                p = (point.getX() - thisBounds.getX()) / thisBounds.getWidth();
                point.setX(bounds.getX() + bounds.getWidth() * p);
                point.setY(bounds.getBottom());
                inPos = point.getX();
                break;
        }

        refactor.createPartIn(border.getCrosspoint(), function.getFunction(),
                functionType, inPos, !start);

    }

    @Override
    public int compareTo(DFDSRole obj) {
        FRectangle my = getBounds();
        FRectangle o = obj.getBounds();

        if (my.getX() < o.getX())
            return -1;
        if (my.getX() > o.getX())
            return 1;
        return 0;
    }

    @Override
    public void mouseClicked(FloatPoint point) {
        Row owner = getFunction().getOwner();
        if (owner == null)
            super.mouseClicked(point);
        else {
            DFDSFunction function = movingArea.findDFDSFunction(owner);
            if (function == null)
                super.mouseClicked(point);
            else {
                final Ordinate x = new Ordinate(Ordinate.TYPE_X);
                final Ordinate y = new Ordinate(Ordinate.TYPE_Y);
                final Point p = new Point(x, y);
                final SectorRefactor.PerspectivePoint pp = new SectorRefactor.PerspectivePoint();
                pp.point = p;

                pp.setFunction(function.getFunction(), BOTTOM);
                x.setPosition(function.getBounds().getX() + point.getX());
                y.setPosition(getY(x.getPosition(), false, function.getBounds()));

                if (movingArea.getPointChangingType() == SectorRefactor.TYPE_START) {
                    pp.type = SectorRefactor.TYPE_START;
                    movingArea.getRefactor().setPoint(pp);
                    movingArea.doSector();
                } else if (movingArea.getPointChangingType() == SectorRefactor.TYPE_END) {
                    pp.type = SectorRefactor.TYPE_END;
                    movingArea.getRefactor().setPoint(pp);
                    movingArea.doSector();
                }
            }
        }
    }

    @Override
    public String getText() {
        String nn = function.getNativeName();
        if (nn != null && nn.trim().length() > 0)
            return nn;
        return function.getName();
    }

    @Override
    public String getShownText() {
        return getText();
    }

    @Override
    protected String getXText() {
        return getText();
    }

    public Stream getStream() {
        return (Stream) movingArea.getDataPlugin().findRowByGlobalId(
                function.getLink());
    }

    public String getAlternativeText() {
        return function.getNativeName();
    }

    public void setAlternativeText(String text) {
        function.setNativeName(text);
    }

    public void setStream(Stream stream, ReplaceStreamType replaceStreamType) {
        function.setLink((stream == null) ? -1 : stream.getElement().getId());
        Function function = (Function) this.function.getOwner();
        if (function != null) {
            HashSet<Sector> toUpdate = new HashSet<Sector>();
            for (Sector sector : ((Function) this.function.getParent())
                    .getSectors()) {
                if (function.equals(sector.getStart().getFunction())
                        || function.equals(sector.getEnd().getFunction()))
                    if (!toUpdate.contains(sector))
                        toUpdate.add(sector);
            }
            for (Sector sector : toUpdate)
                SectorRefactor.fixOwners(sector, movingArea.dataPlugin);
        } else {
            SectorRefactor.copyOwnersFrom(this.function, movingArea.dataPlugin);
        }
    }

    public void setRows(Row[] rs) {

    }
}
