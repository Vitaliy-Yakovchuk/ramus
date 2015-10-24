package com.ramussoft.pb.dfds.visual;

import java.awt.BasicStroke;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.dsoft.utils.DataLoader.MemoryData;
import com.ramussoft.idef0.attribute.DFDSName;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.data.negine.NRow;
import com.ramussoft.pb.dfd.visual.DFDFunction;
import com.ramussoft.pb.idef.elements.ArrowPainter;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.print.old.Line;

public class DFDSFunction extends DFDFunction {

    private static final double ROLES_PERCENT = 1d;

    private static String TERMS = ResourceLoader.getString("Term");

    public DFDSFunction(MovingArea movingArea, Function function) {
        super(movingArea, function);
    }

    @Override
    public void paint(Graphics2D g) {
        g.setColor(function.getBackground());
        final Rectangle2D rect = movingArea.getBounds(getBounds());

        g.fill(rect);
        g.setFont(function.getFont());
        Object nameObject = ((NFunction) function).getNameObject();
        //final String string = function.getKod();

        if (!(nameObject instanceof DFDSName)) {
            DFDSName dfdsName = new DFDSName();
            if (nameObject == null)
                nameObject = "";
            dfdsName.setShortName(String.valueOf(nameObject));
            dfdsName.setLongName("");
            nameObject = dfdsName;
        }

        DFDSName name = (DFDSName) nameObject;
        if (!transparent && !movingArea.isPrinting() || isNegative()) {
            final Rectangle2D rec = movingArea.getBounds(getBounds());
            g.setColor(fiterColor(movingArea.getBackground()));
            g.fill(rec);
        }
        Font font = getFont();
        g.setFont(font);
        g.setColor(fiterColor(getColor()));

        FRectangle textBounds = getTextBounds();
        FRectangle p = movingArea.paintText(g,
                //		string + ". " +
                name.getShortName(), textBounds,
                Line.CENTER_ALIGN, 0, true);
        textBounds.setY(textBounds.getY() + p.getHeight() + 2);
        textBounds.setHeight(textBounds.getHeight() - p.getHeight() - 2);
        String term = function.getTerm();
        if (term != null && term.length() > 0) {

            double y = movingArea.getIDoubleOrdinate(p.getBottom()) + 1;
            Stroke stroke = g.getStroke();
            g.setStroke(new BasicStroke(1, 1, 1, 2, new float[]{
                    (float) movingArea.getIDoubleOrdinate(1),
                    (float) movingArea.getIDoubleOrdinate(2)},
                    (float) movingArea.getIDoubleOrdinate(2)));
            g.draw(new Line2D.Double(rect.getX(), y, rect.getMaxX(), y));
            y++;
            g.setStroke(stroke);

            int size = font.getSize() - movingArea.secondNamePartMinus;
            if (size < 1)
                size = 1;

            g.setFont(new Font(font.getName(), font.getStyle(), size));
            FRectangle fr = movingArea.paintText(g, TERMS, textBounds,
                    Line.LEFT_ALIGN, 0, true);

            FRectangle r = new FRectangle(textBounds);
            r.setX(textBounds.getX() + fr.getWidth() + 2);
            r.setWidth(textBounds.getWidth() - fr.getWidth() - 2);

            p = movingArea.paintText(g, term, r, Line.LEFT_ALIGN, 0, true);
            textBounds.setY(textBounds.getY() + p.getHeight() + 2);
            textBounds.setHeight(textBounds.getHeight() - p.getHeight() - 2);

        }
        if (name.getLongName() != null && name.getLongName().length() > 0) {
            double y = movingArea.getIDoubleOrdinate(p.getBottom()) + 1;
            Stroke stroke = g.getStroke();
            g.setStroke(new BasicStroke(1, 1, 1, 2, new float[]{
                    (float) movingArea.getIDoubleOrdinate(1),
                    (float) movingArea.getIDoubleOrdinate(2)},
                    (float) movingArea.getIDoubleOrdinate(2)));
            g.draw(new Line2D.Double(rect.getX(), y, rect.getMaxX(), y));
            y++;
            g.setStroke(stroke);
            int size = font.getSize() - movingArea.secondNamePartMinus;
            if (size < 1)
                size = 1;

            g.setFont(new Font(font.getName(), font.getStyle(), size));

            movingArea.paintText(g, name.getLongName(), textBounds,
                    Line.LEFT_ALIGN, 0, true);
        }

        paintBorder(g);

        final Stroke tmp = g.getStroke();
        g.draw(rect);
        if (!function.isHaveRealChilds()) {
            g.draw(new Line2D.Double(rect.getMaxX()
                    - Math.round(movingArea.getIDoubleOrdinate(4)),
                    rect.getY(), rect.getMaxX(), rect.getY()
                    + Math.round(movingArea.getIDoubleOrdinate(4))));
        }
        g.setStroke(new BasicStroke(2));

        g.setFont(function.getFont());

        paintTringle(g);
        g.setStroke(tmp);

        List<DFDSRole> roles = movingArea.getDFDSRoles(this);
        double xc = rect.getCenterX();
        double yc = rect.getCenterY();
        for (DFDSRole role : roles) {
            Rectangle2D r = movingArea.getBounds(role.getBounds());
            if (!rect.intersects(r)) {
                double x = r.getCenterX();
                double y = r.getCenterY();
                double dx = Math.abs(xc - x);
                double dy = Math.abs(yc - y);

                if (dx > dy) {
                    if (xc < x)
                        x = r.getX();
                    else
                        x = r.getMaxX();
                } else {
                    if (yc < y)
                        y = r.getY();
                    else
                        y = r.getMaxY();
                }

                ArrowPainter.paintTilda(g, x, y, rect.getX(), rect.getY(),
                        rect.getMaxX(), rect.getMaxY(), 4, movingArea);
            }
        }

    }

    @Override
    public void paintTringle(Graphics2D g) {
        if (paintTriangle >= 0) {
            // g.setClip(rect);
            g.fill(getTrianglePath(paintTriangle).createTransformedShape(
                    AffineTransform.getScaleInstance(movingArea.zoom,
                            movingArea.zoom)));
            // g.setClip(null);
        }
    }

    public void justifyRoles() {
        justifyByDFDSRoles(movingArea.getDFDSRoles(this));
    }

    public void justifyByDFDSRoles(List<DFDSRole> roles1) {
        FRectangle bounds = getBounds();
        List<DFDSRole> roles = new ArrayList<DFDSRole>();
        for (DFDSRole role : roles1)
            if (bounds.intersects(role.getBounds()))
                roles.add(role);

        List<List<FRectangle>> lines = new ArrayList<List<FRectangle>>();
        lines.add(new ArrayList<FRectangle>());
        List<double[]> maxHights = new ArrayList<double[]>();
        maxHights.add(new double[]{0});

        int line = 0;
        double x = bounds.getX() + 2;
        double maxX = bounds.getX() + bounds.getWidth() * ROLES_PERCENT;

        for (DFDSRole role : roles) {
            FRectangle rectangle = role.getBounds();
            if (x + rectangle.getWidth() > maxX) {
                line++;
                lines.add(new ArrayList<FRectangle>());
                x = bounds.getX() + 2;
                maxHights.add(new double[]{0});
            }
            List<FRectangle> list = lines.get(line);

            FRectangle rect = role.getBounds();
            list.add(rect);

            rect.setX(x);
            if (rect.getHeight() > maxHights.get(line)[0])
                maxHights.get(line)[0] = rect.getHeight();

            x += rect.getWidth() + 3;
        }

        double d = maxHights.get(line)[0];

        double y = bounds.getBottom() - d - 2;
        for (int i = line; i >= 0; --i) {
            List<FRectangle> rectangles = lines.get(i);
            for (FRectangle rectangle : rectangles)
                rectangle.setY(y);
            if (i > 0)
                y -= maxHights.get(i - 1)[0];
        }

        int i = 0;

        for (List<FRectangle> list : lines)
            for (FRectangle rectangle : list) {
                roles.get(i).getFunction().setBounds(new FRectangle(rectangle));
                i++;
            }
    }

    public void justifyRoles(List<Function> roles1) {
        FRectangle bounds = getBounds();
        List<Function> roles = new ArrayList<Function>();
        for (Function role : roles1)
            if (bounds.intersects(role.getBounds()))
                roles.add(role);

        List<List<FRectangle>> lines = new ArrayList<List<FRectangle>>();
        lines.add(new ArrayList<FRectangle>());
        List<double[]> maxHights = new ArrayList<double[]>();
        maxHights.add(new double[]{0});

        int line = 0;
        double x = bounds.getX() + 2;
        double maxX = bounds.getX() + bounds.getWidth() * ROLES_PERCENT;

        for (Function role : roles) {
            FRectangle rectangle = role.getBounds();
            if (x + rectangle.getWidth() > maxX) {
                line++;
                lines.add(new ArrayList<FRectangle>());
                x = bounds.getX() + 2;
                maxHights.add(new double[]{0});
            }
            List<FRectangle> list = lines.get(line);

            FRectangle rect = role.getBounds();
            list.add(rect);

            rect.setX(x);
            if (rect.getHeight() > maxHights.get(line)[0])
                maxHights.get(line)[0] = rect.getHeight();

            x += rect.getWidth() + 3;
        }

        double d = maxHights.get(line)[0];

        double y = bounds.getBottom() - d - 2;
        for (int i = line; i >= 0; --i) {
            List<FRectangle> rectangles = lines.get(i);
            for (FRectangle rectangle : rectangles)
                rectangle.setY(y);
            if (i > 0)
                y -= maxHights.get(i - 1)[0];
        }

        int i = 0;

        for (List<FRectangle> list : lines)
            for (FRectangle rectangle : list) {
                roles.get(i).setBounds(new FRectangle(rectangle));
                i++;
            }
    }

    @Override
    public void onEndBoundsChange() {
        FRectangle oldRec = function.getBounds();
        if (oldRec.equals(myBounds))
            return;
        final SectorRefactor refactor = movingArea.getRefactor();
        ((NRow) function).startUserTransaction();
        onProcessEndBoundsChange();
        refactor.setUndoPoint();

    }

    @Override
    public void onProcessEndBoundsChange() {
        FRectangle oldRec = function.getBounds();
        myBounds.setTransformNetBounds(MovingArea.NET_LENGTH);
        FRectangle newRect = new FRectangle(myBounds);
        function.setBounds(newRect);
        final SectorRefactor refactor = movingArea.getRefactor();
        MemoryData memoryData = new MemoryData();
        List<PaintSector> list = new ArrayList<PaintSector>();
        for (int i = 0; i < refactor.getSectorsCount(); i++) {
            PaintSector sector = refactor.getSector(i);
            try {
                setAddedSectorPos(oldRec, sector, list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (PaintSector ps : list)
            PaintSector
                    .save(ps, memoryData, ((NFunction) function).getEngine());

        setAddedRolesPos(oldRec, newRect);
    }

    private void setAddedRolesPos(FRectangle oldRec, FRectangle bounds) {
        if (oldRec.getWidth() != bounds.getWidth())
            justifyRoles();
        else {
            double dx = bounds.getX() - oldRec.getX();
            double dy = bounds.getBottom() - oldRec.getBottom();
            for (DFDSRole role : movingArea.getDFDSRoles(this)) {
                FRectangle rectangle = role.getBounds();
                if (oldRec.intersects(rectangle)) {
                    rectangle.setX(rectangle.getX() + dx);
                    rectangle.setY(rectangle.getY() + dy);
                    role.getFunction().setBounds(new FRectangle(rectangle));
                }
            }
        }
    }

    @Override
    protected int getTriangle(FloatPoint point) {
        int changingState = getMovingArea().getPointChangingType();
        PaintSector activeSector = getMovingArea().getRefactor().getSector();

        if (activeSector != null)
            if (changingState == SectorRefactor.TYPE_START) {
                Crosspoint crosspoint = activeSector.getStart();
                Function function2 = activeSector.getSector().getStart()
                        .getFunction();
                if (crosspoint != null && crosspoint.isDLevel()
                        && function2 != null
                        && function2.getType() == Function.TYPE_DFDS_ROLE)
                    return -1;
            } else if (changingState == SectorRefactor.TYPE_END) {
                Crosspoint crosspoint = activeSector.getEnd();
                Function function2 = activeSector.getSector().getEnd()
                        .getFunction();
                if (crosspoint != null && crosspoint.isDLevel()
                        && function2 != null
                        && function2.getType() == Function.TYPE_DFDS_ROLE)
                    return -1;
            }
        return super.getTriangle(point);
    }
}
