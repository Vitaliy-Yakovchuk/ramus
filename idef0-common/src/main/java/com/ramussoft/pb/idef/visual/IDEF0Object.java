package com.ramussoft.pb.idef.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.idef.elements.ProjectOptions;
import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.dsoft.utils.Options;
import com.dsoft.utils.DataLoader.MemoryData;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.data.negine.NRow;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.elements.SectorRefactor;

public class IDEF0Object extends MovingText {

    protected Function function = null;

    protected static final int STANDOFF = Options.getInteger(
            "MOVINT_FUNCTION_STANDOFF", 3);

    protected int paintTriangle = -1;

    public FRectangle getMyBounds() {
        return myBounds;
    }

    /**
     * @param function The function to set.
     */
    public void setFunction(final Function function) {
        this.function = function;
    }

    private boolean isShowRight() {
        return movingArea.getPointChangingType() == SectorRefactor.TYPE_START;
    }

    /**
     * @see com.ramussoft.pb.idef.visual.MovingPanel#mouseClicked(com.dsoft.pb.types.FloatPoint)
     */
    @Override
    public void mouseClicked(final FloatPoint point) {
        super.mouseClicked(point);
        if (movingArea.getChangingState() == MovingArea.ARROW_CHANGING_STATE) {
            if (movingArea.getPointChangingType() == SectorRefactor.TYPE_START) {
                if (getTriangle(point) == RIGHT) {
                    final Ordinate x = new Ordinate(Ordinate.TYPE_X);
                    final Ordinate y = new Ordinate(Ordinate.TYPE_Y);
                    final Point p = new Point(x, y);
                    x.setPosition(getBounds().getRight());
                    y.setPosition(getBounds().getY() + point.getY());
                    final SectorRefactor.PerspectivePoint pp = new SectorRefactor.PerspectivePoint();
                    pp.point = p;
                    pp.x = p.getX();
                    pp.y = p.getY();
                    pp.setFunction(getFunction(), RIGHT);
                    pp.type = SectorRefactor.TYPE_START;
                    movingArea.getRefactor().setPoint(pp);
                    movingArea.doSector();
                }
            } else {
                final int type = getTriangle(point);
                if (type == -1 || type == RIGHT)
                    return;
                final Ordinate x = new Ordinate(Ordinate.TYPE_X);
                final Ordinate y = new Ordinate(Ordinate.TYPE_Y);
                final Point p = new Point(x, y);
                final SectorRefactor.PerspectivePoint pp = new SectorRefactor.PerspectivePoint();
                pp.point = p;
                switch (type) {
                    case LEFT: {
                        pp.setFunction(getFunction(), LEFT);
                        x.setPosition(getBounds().getLeft());
                        y.setPosition(getBounds().getY() + point.getY());
                    }
                    break;
                    case TOP: {
                        pp.setFunction(getFunction(), TOP);
                        x.setPosition(getBounds().getX() + point.getX());
                        y.setPosition(getBounds().getTop());
                    }
                    break;
                    case BOTTOM: {
                        pp.setFunction(getFunction(), BOTTOM);
                        x.setPosition(getBounds().getX() + point.getX());
                        y.setPosition(getBounds().getBottom());
                    }
                    break;
                }
                pp.x = x.getPosition();
                pp.y = y.getPosition();
                pp.type = SectorRefactor.TYPE_END;
                movingArea.getRefactor().setPoint(pp);
                movingArea.doSector();
            }
        }
    }

    public IDEF0Object(final MovingArea movingArea, final Function function) {
        super(movingArea);
        this.function = function;
        myBounds = new FRectangle(function.getBounds());

        // myBounds.setTransformNetBounds(MovingArea.NET_LENGTH);
    }

    /*
     * (non-Javadoc)
     *
     * @seecom.jason.clasificators.frames.idf.MovingPanel#mouseMoved(com.jason.
     * clasificators.frames.idf.FloatPoint)
     */
    @Override
    public int mouseMoved(final FloatPoint point) {
        super.mouseMoved(point);
        int res;
        if ((movingArea.getChangingState() == MovingArea.ARROW_CHANGING_STATE)
                && (movingArea.isOkForCross()))
            res = getTriangle(point);
        else
            res = -1;
        if (res != paintTriangle) {
            paintTriangle = res;
            movingArea.repaintAsync();
        }
        return paintTriangle;
    }

    /**
     * @see com.ramussoft.pb.idef.visual.MovingText#setText(java.lang.String)
     */
    @Override
    public void setText(final String text) {
        function.setName(text);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.frames.idf.MovingPanel#mouseLeave()
     */
    @Override
    public void mouseLeave() {
        super.mouseLeave();
        paintTriangle = -1;
    }

    /**
     * @see com.ramussoft.pb.idef.visual.MovingText#getText()
     */
    @Override
    public String getText() {
        return function.getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.frames.idf.MovingText#getTextBounds()
     */
    @Override
    public FRectangle getTextBounds() {
        final FRectangle b = getBounds();
        return new FRectangle(b.getX(), b.getY(), b.getWidth(),
                b.getHeight() - 7);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.frames.idf.MovingText#getColor()
     */
    @Override
    public Color getColor() {
        return function.getForeground();
    }

    public String getIDEF0Kod() {
        NFunction nFunction = (NFunction) function;
        return getIDEF0Kod(nFunction);
    }

    @Override
    public void paint(final Graphics2D g) {
        g.setColor(function.getBackground());
        final Rectangle2D rect = movingArea.getBounds(getBounds());
        g.fill(rect);
        g.setFont(function.getFont());
        // g.setColor(function.getForeground());
        super.paint(g);

        final Stroke tmp = g.getStroke();
        g.draw(rect);
        if (!function.isHaveChilds()) {
            g.draw(new Line2D.Double(rect.getX()
                    + Math.round(movingArea.getIDoubleOrdinate(4)),
                    rect.getY(), rect.getX(), rect.getY()
                    + Math.round(movingArea.getIDoubleOrdinate(4))));
        }
        g.setStroke(new BasicStroke(2));
        /*
         * g.draw(new Line2D.Double(rect.getX() + rect.getWidth() - 1,
		 * rect.getY() + 1, rect.getX() + rect.getWidth() - 1, rect.getY() +
		 * rect.getHeight() - 1)); g.draw(new Line2D.Double(rect.getX() + 1,
		 * rect.getY() + rect.getHeight() - 1, rect.getX() + rect.getWidth() -
		 * 1, rect .getY() + rect.getHeight() - 1));
		 */
        final String string = getIDEF0Kod();
        g.setFont(function.getFont());
        double h = MovingArea.getWidth(0)
                + MovingArea.getWidth((int) function.getFont()
                .getStringBounds(string, g.getFontRenderContext())
                .getHeight());
        h = h * 0.7;
        movingArea.paintText(g, string,
                new FRectangle(getBounds().getX(), getBounds().getBottom() - h
                        - 3, getBounds().getWidth() - 3, h),
                com.ramussoft.pb.print.old.Line.RIGHT_ALIGN, 1, true);

        g.setStroke(tmp);
        paintTringle(g);
    }

    public void paintTringle(Graphics2D g) {
        if (paintTriangle >= 0)
            g.fill(getTrianglePath(paintTriangle).createTransformedShape(
                    AffineTransform.getScaleInstance(movingArea.zoom,
                            movingArea.zoom)));
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jason.clasificators.frames.idf.MovingText#onBoundsChange()
	 */

    /**
     * @see javax.swing.JComponent#setBackground(java.awt.Color)
     */
    public void setBackground(final Color arg0) {
        function.setBackground(arg0);
    }

    /**
     * @see javax.swing.JComponent#setForeground(java.awt.Color)
     */
    public void setForeground(final Color arg0) {
        function.setForeground(arg0);
    }

    /**
     * @return Returns the function.
     */
    public Function getFunction() {
        return function;
    }

    protected int getTriangle(final FloatPoint point) {
        int res = -1;

        FloatPoint l = getLocation();

        for (int type = MovingPanel.RIGHT; type <= MovingPanel.TOP; type++) {
            GeneralPath gp = getTrianglePath(type);
            double y = point.getY() + l.getY();
            double x = point.getX() + l.getX();
            if (gp.contains(new Point2D.Double(x, y))) {
                res = type;
                break;
            }
        }

        if (isShowRight() ^ res == Point.RIGHT)
            res = -1;
        return res;
    }

    protected GeneralPath getTrianglePath(int type) {
        FRectangle bounds = getBounds();
        final double w = bounds.getWidth();
        final double h = bounds.getHeight();
        GeneralPath res = new GeneralPath();
        double min = (w > h) ? h : w;
        min /= 2;
        switch (type) {
            case MovingText.LEFT:
                res.moveTo(bounds.getLeft(), bounds.getTop());
                res.lineTo(bounds.getLeft(), bounds.getBottom());
                res.lineTo(bounds.getLeft() + min, bounds.getBottom() - min);
                res.lineTo(bounds.getLeft() + min, bounds.getTop() + min);
                break;
            case MovingText.TOP:
                res.moveTo(bounds.getLeft(), bounds.getTop());
                res.lineTo(bounds.getRight(), bounds.getTop());
                res.lineTo(bounds.getRight() - min, bounds.getTop() + min);
                res.lineTo(bounds.getLeft() + min, bounds.getTop() + min);
                break;
            case MovingText.BOTTOM:
                res.moveTo(bounds.getLeft(), bounds.getBottom());
                res.lineTo(bounds.getRight(), bounds.getBottom());
                res.lineTo(bounds.getRight() - min, bounds.getBottom() - min);
                res.lineTo(bounds.getLeft() + min, bounds.getBottom() - min);
                break;
            case MovingText.RIGHT:
                res.moveTo(bounds.getRight(), bounds.getTop());
                res.lineTo(bounds.getRight(), bounds.getBottom());
                res.lineTo(bounds.getRight() - min, bounds.getBottom() - min);
                res.lineTo(bounds.getRight() - min, bounds.getTop() + min);
                break;
        }

        res.closePath();

        return res;
    }

    public void setName(final String text) {
        getFunction().setName(text);
    }

    /**
     * @return
     */
    public String getName() {
        return getFunction().getName();
    }

    @Override
    public void setFont(final Font font) {
        super.setFont(font);
        if (function != null)
            function.setFont(font);
    }

    @Override
    public Font getFont() {
        if (function == null)
            return super.getFont();
        else
            return function.getFont();
    }

    protected int getMinX() {
        return movingArea.getIntOrdinate(PaintSector.LINE_MIN_LENGTH * 2);
    }

    protected int getMinY() {
        return movingArea.getIntOrdinate(PaintSector.LINE_MIN_LENGTH * 2);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.frames.idf.MovingPanel#onEndBoundsChange()
     */
    @Override
    public void onEndBoundsChange() {
        final FRectangle oldRec = function.getBounds();
        if (oldRec.equals(myBounds))
            return;
        final SectorRefactor refactor = movingArea.getRefactor();
        ((NRow) function).startUserTransaction();
        onProcessEndBoundsChange();
        refactor.setUndoPoint();
    }

    @Override
    public void onProcessEndBoundsChange() {
        onProcessEndBoundsChange(null);

    }

    public void onProcessEndBoundsChange(List<PaintSector> notToMove) {
        final FRectangle oldRec = function.getBounds();
        super.onProcessEndBoundsChange();
        function.setBounds(new FRectangle(myBounds));
        final SectorRefactor refactor = movingArea.getRefactor();
        MemoryData memoryData = new MemoryData();
        List<PaintSector> list = new ArrayList<PaintSector>();
        for (int i = 0; i < refactor.getSectorsCount(); i++) {
            PaintSector sector = refactor.getSector(i);
            boolean con = false;
            if (notToMove != null)
                for (PaintSector ps : notToMove) {
                    if (ps.getSector().equals(sector.getSector())) {
                        con = true;
                        break;
                    }
                }
            if (con)
                continue;
            setAddedSectorPos(oldRec, sector, list);
        }
        for (PaintSector ps : list)
            PaintSector
                    .save(ps, memoryData, ((NFunction) function).getEngine());
    }

    public double getX(double y, boolean left, FRectangle rectangle) {
        if (left)
            return rectangle.getLeft();
        return rectangle.getRight();
    }

    public double getY(double x, boolean top, FRectangle rectangle) {
        if (top)
            return rectangle.getTop();
        return rectangle.getBottom();
    }

    public void setAddedSectorPos(final FRectangle oldRec,
                                  final PaintSector sector, List<PaintSector> list) {
        List<Ordinate> ordinates = new ArrayList<Ordinate>();
        if (setAddedSectorPosWC(oldRec, sector, ordinates)) {
            for (Ordinate ordinate : ordinates) {
                addSectors(ordinate, list);
                addSectors(ordinate, list);
            }
        }
    }

    private void addSectors(Ordinate ordinate, List<PaintSector> list) {
        for (Point point : ordinate.getPoints())
            if (!list.contains(point.getSector()))
                list.add(point.getSector());
    }

    /**
     * Метод організовує зміни в секторі, що виходить або входить в
     * функціональний блок.
     *
     * @param oldRec Старі властивості розмірів функціонального блоку.
     * @param sector Сектор прив’язаний до функціонального блоку.
     */

    public boolean setAddedSectorPosWC(final FRectangle oldRec,
                                       final PaintSector sector, List<Ordinate> ordinates) {
        boolean res = false;
        final FRectangle currRec = function.getBounds();
        if (function.equals(sector.getSector().getStart().getFunction())) {
            final Point p = sector.getStartPoint();
            if (!p.isMoveable(currRec.getRight(), Ordinate.TYPE_X)) {
                sector.regeneratePoints();
                addOrdinates(sector, ordinates);
            }
            double t = currRec.getY() + currRec.getHeight()
                    * (p.getY() - oldRec.getY()) / oldRec.getHeight();
            if (!p.isMoveable(t, Ordinate.TYPE_Y)) {
                if (sector.getPinCount() == 1)
                    p.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                p.setY(t);
                sector.regeneratePoints();
                addOrdinates(sector, ordinates);
            } else {
                p.setY(t);
                addOrdinate(ordinates, p.getYOrdinate());
            }

            if (sector.getSector().getStart().getFunctionType() == MovingPanel.RIGHT) {
                p.setX(getX(t, false, currRec));
                addOrdinate(ordinates, p.getXOrdinate());
            } else if (sector.getSector().getStart().getFunctionType() == MovingPanel.LEFT) {
                p.setX(getX(t, true, currRec));
                addOrdinate(ordinates, p.getXOrdinate());
            } else {
                t = currRec.getX() + currRec.getWidth()
                        * (p.getX() - oldRec.getX()) / oldRec.getWidth();

                if (sector.getSector().getStart().getFunctionType() == MovingPanel.TOP) {
                    p.setY(getY(t, true, currRec));
                } else {
                    p.setY(getY(t, false, currRec));
                }
                addOrdinate(ordinates, p.getYOrdinate());

                if (!p.isMoveable(t, Ordinate.TYPE_X)) {
                    p.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    p.setX(t);
                    sector.regeneratePoints();
                    addOrdinates(sector, ordinates);
                } else {
                    p.setX(t);
                    addOrdinate(ordinates, p.getXOrdinate());
                }
                fixCuttedEnd(sector);
                return true;
            }
            res = true;
            fixCuttedEnd(sector);
        }
        if (function.equals(sector.getSector().getEnd().getFunction())) {
            final Point p = sector.getEndPoint();
            if (sector.getSector().getEnd().getFunctionType() == MovingPanel.LEFT) {
                if (!p.isMoveable(currRec.getLeft(), Ordinate.TYPE_X)) {
                    sector.regeneratePoints();
                    addOrdinates(sector, ordinates);
                }
                final double t = currRec.getY() + currRec.getHeight()
                        * (p.getY() - oldRec.getY()) / oldRec.getHeight();
                p.setX(getX(t, true, currRec));
                addOrdinate(ordinates, p.getXOrdinate());
                if (!p.isMoveable(t, Ordinate.TYPE_Y)) {
                    if (sector.getPinCount() == 1)
                        p.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    p.setY(t);
                    sector.regeneratePoints();
                    addOrdinates(sector, ordinates);
                } else {
                    p.setY(t);
                    addOrdinate(ordinates, p.getYOrdinate());
                }
            } else if (sector.getSector().getEnd().getFunctionType() == MovingPanel.RIGHT) {
                if (!p.isMoveable(currRec.getLeft(), Ordinate.TYPE_X)) {
                    sector.regeneratePoints();
                    addOrdinates(sector, ordinates);
                }
                final double t = currRec.getY() + currRec.getHeight()
                        * (p.getY() - oldRec.getY()) / oldRec.getHeight();
                p.setX(getX(t, false, currRec));
                addOrdinate(ordinates, p.getXOrdinate());
                if (!p.isMoveable(t, Ordinate.TYPE_Y)) {
                    if (sector.getPinCount() == 1)
                        p.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                    p.setY(t);
                    sector.regeneratePoints();
                    addOrdinates(sector, ordinates);
                } else {
                    p.setY(t);
                    addOrdinate(ordinates, p.getYOrdinate());
                }

            } else {
                final double t = currRec.getX() + currRec.getWidth()
                        * (p.getX() - oldRec.getX()) / oldRec.getWidth();
                if (sector.getSector().getEnd().getFunctionType() == MovingPanel.TOP) {
                    p.setY(getY(t, true, currRec));
                    if (!p.isMoveable(currRec.getTop(), Ordinate.TYPE_Y)) {
                        sector.regeneratePoints();
                        addOrdinates(sector, ordinates);
                    } else
                        addOrdinate(ordinates, p.getYOrdinate());
                } else {
                    p.setY(getY(t, false, currRec));
                    if (!p.isMoveable(currRec.getBottom(), Ordinate.TYPE_Y)) {
                        sector.regeneratePoints();
                        addOrdinates(sector, ordinates);
                    } else
                        addOrdinate(ordinates, p.getYOrdinate());
                }

                if (!p.isMoveable(t, Ordinate.TYPE_X)) {
                    p.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                    p.setX(t);
                    sector.regeneratePoints();
                    addOrdinates(sector, ordinates);
                } else {
                    p.setX(t);
                    addOrdinate(ordinates, p.getXOrdinate());
                }

            }
            res = true;

            fixCuttedStart(sector);
        }
        sector.tryRemovePin(movingArea);
        return res;
    }

    private void fixCuttedStart(final PaintSector sector) {
        if (sector.getStart() == null) {
            switch (sector.getSector().getEnd().getFunctionType()) {
                case MovingText.LEFT:
                    sector.getStartPoint().setX(
                            sector.getEndPoint().getX()
                                    - SectorRefactor.PART_SECTOR_LENGTH);
                    break;

                case MovingText.RIGHT:
                    sector.getStartPoint().setX(
                            sector.getEndPoint().getX()
                                    + SectorRefactor.PART_SECTOR_LENGTH);
                    break;
                case MovingText.TOP:
                    sector.getStartPoint().setY(
                            sector.getEndPoint().getY()
                                    - SectorRefactor.PART_SECTOR_LENGTH);
                    break;
                case MovingText.BOTTOM:
                    sector.getStartPoint().setY(
                            sector.getEndPoint().getY()
                                    + SectorRefactor.PART_SECTOR_LENGTH);
                    break;
            }
        }
    }

    private void fixCuttedEnd(final PaintSector sector) {
        if (sector.getEnd() == null) {
            switch (sector.getSector().getStart().getFunctionType()) {
                case MovingText.LEFT:
                    sector.getEndPoint().setX(
                            sector.getStartPoint().getX()
                                    - SectorRefactor.PART_SECTOR_LENGTH);
                    break;

                case MovingText.RIGHT:
                    sector.getEndPoint().setX(
                            sector.getStartPoint().getX()
                                    + SectorRefactor.PART_SECTOR_LENGTH);
                    break;
                case MovingText.TOP:
                    sector.getEndPoint().setY(
                            sector.getStartPoint().getY()
                                    - SectorRefactor.PART_SECTOR_LENGTH);
                    break;
                case MovingText.BOTTOM:
                    sector.getEndPoint().setY(
                            sector.getStartPoint().getY()
                                    + SectorRefactor.PART_SECTOR_LENGTH);
                    break;
            }
        }
    }

    private void addOrdinates(PaintSector sector, List<Ordinate> ordinates) {
        for (int i = 0; i < sector.getPointCount(); i++) {
            Ordinate xOrdinate = sector.getPoint(i).getXOrdinate();
            addOrdinate(ordinates, xOrdinate);
            Ordinate yOrdinate = sector.getPoint(i).getYOrdinate();
            addOrdinate(ordinates, yOrdinate);
        }
    }

    private void addOrdinate(List<Ordinate> ordinates, Ordinate yOrdinate) {
        if (!ordinates.contains(yOrdinate))
            ordinates.add(yOrdinate);
    }

    /**
     * @return
     */
    public Color getBackground() {
        return function.getBackground();
    }

    /**
     * @return
     */
    public Color getForeground() {
        return function.getForeground();
    }

    /**
     * @see com.ramussoft.pb.idef.visual.MovingPanel#getMinBottomHeight()
     */
    @Override
    protected double getMinBottomHeight() {
        return super.getMinBottomHeight() + PaintSector.LINE_MIN_LENGTH * 2;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.frames.idf.MovingPanel#getMinLeftWidth()
     */
    @Override
    protected double getMinLeftWidth() {
        return super.getMinLeftWidth() + PaintSector.LINE_MIN_LENGTH * 2;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.frames.idf.MovingPanel#getMinRightWidth()
     */
    @Override
    protected double getMinRightWidth() {
        return super.getMinRightWidth() + PaintSector.LINE_MIN_LENGTH * 2;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.frames.idf.MovingPanel#getMinTopHeight()
     */
    @Override
    protected double getMinTopHeight() {
        return super.getMinTopHeight() + PaintSector.LINE_MIN_LENGTH * 2;
    }

    @Override
    public String getToolTipText() {
        return function.getToolTipText(false);
    }

    /**
     * Метод вирівнює розташування масиву коордикоординат між точками a і b.
     *
     * @param ds Масив координат, який необхідно вирівняти.
     * @param a  Ліва координата.
     * @param b  Права координата.
     */

    protected void centerArray(final double[] ds, final double a, final double b) {
        final int[] is = new int[ds.length];
        int i, j;
        for (i = 0; i < ds.length; i++)
            is[i] = i;
        int t;
        // sort
        for (i = 1; i < ds.length; i++)
            for (j = 0; j < ds.length - i; j++) {
                if (ds[is[j]] > ds[is[j + 1]]) {
                    t = is[j];
                    is[j] = is[j + 1];
                    is[j + 1] = t;
                }
            }
        final double pl = (b - a) / (ds.length + 1);
        for (i = 0; i < ds.length; i++) {
            ds[is[i]] = a + pl * (i + 1);
        }
    }

    /**
     * Центрує набір точок на стороні.
     *
     * @param points Вестор з посиланнями на точки.
     * @param type   Ordinate.TYPE_X - точки знаходяться на верхній або нижній
     *               стороні. Ordinate.TYPE_Y - точки знаходяться на лівій або
     *               правій стороні.
     */

    protected void centerPoints(final Vector<Point> points, final int type,
                                int borderType) {
        final double[] ds = new double[points.size()];
        int i;
        FRectangle bounds = getBounds();
        if (type == Ordinate.TYPE_X) {
            for (i = 0; i < ds.length; i++)
                ds[i] = points.get(i).getX();
            centerArray(ds, bounds.getLeft(), bounds.getRight());
            for (i = 0; i < ds.length; i++) {
                final Point p = points.get(i);
                if (p.isMoveable(ds[i], Ordinate.TYPE_X)) {
                    p.setX(ds[i]);
                    p.setY(getY(ds[i], TOP == borderType, bounds));
                }
            }
        } else {
            for (i = 0; i < ds.length; i++)
                ds[i] = points.get(i).getY();
            centerArray(ds, bounds.getTop(), bounds.getBottom());
            for (i = 0; i < ds.length; i++) {
                final Point p = points.get(i);
                if (p.isMoveable(ds[i], Ordinate.TYPE_Y)) {
                    p.setY(ds[i]);
                    p.setX(getX(ds[i], LEFT == borderType, bounds));
                }
            }
        }
    }

    /**
     * Метод вирівнює розташування всіх під’єднаних до функціонального блока
     * секторів.
     */

    @SuppressWarnings("unchecked")
    public void centerAddedSetors() {
        final Vector<Point>[] vectors = new Vector[4];
        vectors[LEFT] = new Vector<Point>();
        vectors[RIGHT] = new Vector<Point>();
        vectors[TOP] = new Vector<Point>();
        vectors[BOTTOM] = new Vector<Point>();
        final SectorRefactor refactor = movingArea.getRefactor();
        for (int i = 0; i < refactor.getSectorsCount(); i++) {
            final PaintSector sector = refactor.getSector(i);
            if (function.equals(sector.getSector().getStart().getFunction()))
                vectors[sector.getSector().getStart().getFunctionType()]
                        .add(sector.getStartPoint());
            if (function.equals(sector.getSector().getEnd().getFunction()))
                vectors[sector.getSector().getEnd().getFunctionType()]
                        .add(sector.getEndPoint());
        }
        for (int i = 0; i < 4; i++)
            centerPoints(vectors[i], i == LEFT || i == RIGHT ? Ordinate.TYPE_Y
                    : Ordinate.TYPE_X, i);
        for (int i = 0; i < refactor.getSectorsCount(); i++) {
            final PaintSector sector = refactor.getSector(i);
            if (function.equals(sector.getSector().getStart().getFunction()))
                sector.tryRemovePin(getMovingArea());
            else if (function.equals(sector.getSector().getEnd().getFunction()))
                sector.tryRemovePin(getMovingArea());
        }
    }

    private static String getRecIDEF0Kod(
            final com.ramussoft.database.common.Row function) {
        final com.ramussoft.database.common.Row f = function.getParent();
        if (f == null || f.getParent() == null)
            return "";
        String id = Integer.toString(function.getId());
        if (id.length() > 1)
            id = "." + id + ".";
        return getRecIDEF0Kod(f) + id;
    }

    private static String getRecDFDSKod(
            final com.ramussoft.database.common.Row function, boolean first) {
        final com.ramussoft.database.common.Row f = function.getParent();
        if (f == null)
            return "";
        String id = Integer.toString(function.getId());
        if (!first)
            id = id + ".";
        return getRecDFDSKod(f, false) + id;
    }

    public static String getDFDSKod(Row function, DataPlugin dataPlugin) {
        if (function instanceof Function) {
            ProjectOptions po = ((Function) function).getProjectOptions();
            String letter = po.getDeligate().getModelLetter();
            if (letter != null && letter.length() > 0) {
                if (function.equals(dataPlugin.getBaseFunction()))
                    return letter + "-0";
                return letter + getRecDFDSKod(function, true);
            }
        }
        if (function.equals(dataPlugin.getBaseFunction()))
            return "A-0";
        return "A-" + getRecDFDSKod(function, true);
    }

    /**
     * Метод визначає код функціонального блоку у відповідності до стандарту
     * IDEF0
     *
     * @param function Функціональний блок, для якого буде визначений його код.
     * @return Код функціонального блока у відповідності до стандарту IDEF0.
     */

    public static String getIDEF0Kod(Row function) {
        Engine engine = function.getEngine();
        Integer integer = (Integer) function.getAttribute(IDEF0Plugin
                .getDecompositionTypeAttribute(engine));
        if (integer != null
                && integer.intValue() == MovingArea.DIAGRAM_TYPE_DFDS)
            return function.getNativeCode();
        Qualifier qualifier = engine.getQualifier(function.getElement()
                .getQualifierId());
        DataPlugin dataPlugin = NDataPluginFactory.getDataPlugin(qualifier,
                engine, null);

        com.ramussoft.pb.Row f1 = dataPlugin.findRowByGlobalId(function
                .getElementId());
        if (f1 != null)
            function = (Row) f1;
        final com.ramussoft.database.common.Row f = function.getParent();

        if (function instanceof Function) {
            ProjectOptions po = ((Function) function).getProjectOptions();
            String letter = po.getDeligate().getModelLetter();
            if (letter != null && letter.length() > 0) {
                if (f == null)
                    return letter + "-0";
                if (f.getParent() == null)
                    return letter + "0";
                return letter + getRecIDEF0Kod(function);
            }
        }
        if (f == null)
            return "A-0";
        if (f.getParent() == null)
            return "A0";
        return "A" + getRecIDEF0Kod(function);
    }

    @Override
    protected int getPressType(final FloatPoint point) {
        if (paintTriangle >= 0)
            return -1;
        return super.getPressType(point);
    }

    @Override
    protected void createEditFrame() {
        final GUIFramework framework = movingArea.getPanel().getFramework();
        final Engine engine = framework.getEngine();

        long id = getFunction().getLink();

        Element element2 = null;
        if (id >= 0)
            element2 = engine.getElement(id);

        if (element2 == null)
            element2 = getFunction().getElement();

        final Element element = element2;
        Qualifier qualifier = engine.getQualifier(element.getQualifierId());

        Attribute name = null;
        if (qualifier.getAttributeForName() >= 0)
            name = engine.getAttribute(qualifier.getAttributeForName());

        if (name == null) {
            JOptionPane.showMessageDialog(framework.getMainFrame(),
                    ResourceLoader.getString("NameAttributeNotSet"));
            return;
        }

        final Attribute nameAttribute = name;

        AttributePlugin plugin = framework.findAttributePlugin(name);

        final AttributeEditor editor = plugin.getAttributeEditor(engine,
                framework.getAccessRules(), element, name, "activity_name",
                null);

        setTextComponent(new JScrollPane(editor.getLastComponent()));

        final Object value = engine.getAttribute(element, name);
        editor.setValue(value);

        final Rectangle r = movingArea.getIBounds(getBounds());
        final JTextArea textArea = (JTextArea) editor.getLastComponent();
        processTextArea(textArea);

        // textArea.setBackground(getFunction().getBackground());
        // textArea.setForeground(getFunction().getForeground());
        // textArea.setFont(movingArea.getFont(getFont()));
        // textArea.setCaretColor(getFunction().getForeground());
        final JPopupMenu menu = textArea.getComponentPopupMenu();
        textArea.setComponentPopupMenu(null);

        movingArea.getPanel().setActionDisable(true);

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    stopEdit(element, engine, nameAttribute, editor, value,
                            textArea, menu);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    cancelEdit(editor, textArea, menu);
            }
        });

        textArea.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                stopEdit(element, engine, nameAttribute, editor, value,
                        textArea, menu);
            }

            @Override
            public void focusGained(FocusEvent e) {
            }
        });
        movingArea.add(getTextComponent());
        movingArea.setbImage(null);
        getTextComponent().setBounds(r);
        movingArea.revalidate();
        movingArea.repaint();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                textArea.requestFocus();
            }
        });
    }

    private void stopEdit(final Element element, final Engine engine,
                          final Attribute nameAttribute, final AttributeEditor editor,
                          final Object value, final JTextArea textArea, final JPopupMenu menu) {
        if (getTextComponent() == null)
            return;
        Object value2 = editor.getValue();
        if ((value2 == null) || (!value2.equals(value))) {
            ((Journaled) engine).startUserTransaction();
            engine.setAttribute(element, nameAttribute, value2);
            ((Journaled) engine).commitUserTransaction();
        }
        cancelEdit(editor, textArea, menu);
    }

    private void cancelEdit(final AttributeEditor editor,
                            final JTextArea textArea, final JPopupMenu menu) {
        movingArea.remove(getTextComponent());
        movingArea.revalidate();
        movingArea.repaint();
        textArea.setComponentPopupMenu(menu);
        editor.close();
        setTextComponent(null);
        movingArea.getPanel().setActionDisable(false);
    }

    @Override
    public Color getBackgroundA() {
        return function.getBackground();
    }

    @Override
    public Color getForegroundA() {
        return function.getForeground();
    }

    @Override
    public Font getFontA() {
        return function.getFont();
    }

    public void setBackgroundA(Color background) {
        function.setBackground(background);
    }

    @Override
    public void setForegroundA(Color foreground) {
        function.setForeground(foreground);
    }

    public void setFontA(Font font) {
        function.setFont(font);
    }

    @Override
    public void setBoundsA(FRectangle bounds) {
        super.setBoundsA(bounds);
        onProcessEndBoundsChange();
    }
}
