package com.ramussoft.reportgef.gui;

import static java.lang.Math.floor;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Dimension2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import com.ramussoft.report.editor.xml.components.XMLComponent;
import com.ramussoft.reportgef.Component;
import com.ramussoft.reportgef.Group;
import com.ramussoft.reportgef.model.Bounds;
import com.ramussoft.reportgef.model.QBounds;

public class GEFComponent extends JComponent implements MouseListener,
        MouseMotionListener {

    /**
     *
     */
    private static final long serialVersionUID = -491404689995059186L;

    private static final int ROTATE_LINE_LENGTH = 13;

    private static final int ROTATE_SPOT_WIDTH = 8;

    private static final double CONTROL_SPOT_WIDTH = 8;

    private static final Color ROTATE_SPOT_COLOR = Color.green;

    private static final int GROUP_SELECTION_ADD = 5;

    private Diagram diagram;

    private double zoom = 1;

    private Point mousePressedPosition;

    private Point mouseDragPosition;

    private Stroke selectionStroke = createSelectionStroke();

    private Object mouseLock = new Object();

    private Group selection = createGroup();

    private boolean drag = false;

    private int cursorType = Cursor.DEFAULT_CURSOR;

    private List<Side> dragSides = new ArrayList<Side>(2);

    private AlphaComposite alpha = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 0.3f);

    private double tx;

    private double ty;

    private boolean readOnly;

    private boolean mouseDragged;

    public GEFComponent(Diagram diagram) {
        this(diagram, false);
    }

    public GEFComponent(Diagram diagram, boolean readOnly) {
        this.readOnly = readOnly;
        setDiagramam(diagram);
        setBackground(Color.white);
        setForeground(Color.black);
        if (!readOnly) {
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
        }
    }

    protected Group createGroup() {
        return new Group();
    }

    public void setDiagramam(Diagram diagramam) {
        this.diagram = diagramam;
        Dimension2D size = diagramam.zoom(diagramam.getSize(), zoom);
        setSize((int) floor(size.getWidth()) + 2,
                (int) floor(size.getHeight()) + 2);
        setPreferredSize(getSize());
    }

    @Override
    public void paint(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;

        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        gr.setColor(getBackground());

        Rectangle2D rect = new Rectangle2D.Double(0, 0, diagram.getSize()
                .getWidth(), diagram.getSize().getHeight());

        rect = translate(rect);

        Dimension size = getSize();
        tx = (size.getWidth() - rect.getWidth()) / 2;
        ty = 0;

        g.translate(tx, ty);

        g.fill(rect);
        gr.setColor(getForeground());
        g.draw(rect);

        g.translate(1, 1);

        g.scale(zoom, zoom);

        diagram.paint(g);

        Composite composite = g.getComposite();

        g.setComposite(alpha);

        diagram.paintGroup(g, selection);
        g.setComposite(composite);

        g.scale(1d / zoom, 1d / zoom);
        g.translate(-1, -1);
        if (!drag) {
            Rectangle rectangle = getSelectionRectangle();
            if (rectangle != null) {
                g.setStroke(selectionStroke);
                g.setColor(getForeground());
                g.draw(rectangle);
            }
        }

        paintSelected(g);
    }

    private void paintSelected(Graphics2D g) {
        Point2D center = translate(new Point2D.Double(selection.getCenter()
                .getX()
                + selection.getTranslate().getX(), selection.getCenter().getY()
                + selection.getTranslate().getY()));
        g.rotate(selection.getRotate(), center.getX(), center.getY());
        g.setColor(getBackground());
        g.setStroke(new BasicStroke());
        paintSelectedBorder(g, 1);
        g.setStroke(selectionStroke);
        g.setColor(Color.gray);
        paintSelectedBorder(g, 1);
        if (selection.getBounds().length > 0) {
            paintSelection(g);
            if (selection.getBounds().length == 1)
                paintScaleControls(g);
        }

        g.rotate(-selection.getRotate(), center.getX(), center.getY());

    }

    private void paintScaleControls(Graphics2D g) {

        g.setStroke(new BasicStroke());
        boolean resizableX = selection.isResizeableX(diagram);
        boolean resizableY = selection.isResizeableY(diagram);
        if (resizableX) {
            fillControl(g, getControlRectangle(getLeftSelectionPoint()));
            fillControl(g, getControlRectangle(getRightSelectionPoint()));
            if (resizableY) {
                fillControl(g,
                        getControlRectangle(getLeftBottomSelectionPoint()));
                fillControl(g,
                        getControlRectangle(getRightBottomSelectionPoint()));
                fillControl(g, getControlRectangle(getTopLeftSelectionPoint()));
                fillControl(g, getControlRectangle(getTopRightSelectionPoint()));
            }
        }

        if (resizableY) {
            fillControl(g, getControlRectangle(getTopSelectionPoint()));
            fillControl(g, getControlRectangle(getBottomSelectionPoint()));
        }

    }

    private void fillControl(Graphics2D g, Rectangle2D controlRectangle) {
        g.setColor(Color.green);
        g.fill(controlRectangle);
        g.setColor(Color.black);
        g.draw(controlRectangle);
    }

    private Rectangle2D getControlRectangle(Point2D point) {
        return new Rectangle2D.Double(point.getX() - CONTROL_SPOT_WIDTH / 2d,
                point.getY() - CONTROL_SPOT_WIDTH / 2d, CONTROL_SPOT_WIDTH,
                CONTROL_SPOT_WIDTH);
    }

    protected void paintSelection(Graphics2D g) {
        Rectangle2D rect = translate(selection.getRectangle(), selection);
        double plus = 0;
        if (selection.getBounds().length > 1) {
            plus = GROUP_SELECTION_ADD;
            g.draw(new Rectangle2D.Double(rect.getX() - plus, rect.getY()
                    - plus, rect.getWidth() + plus * 2, rect.getHeight() + plus
                    * 2));
        }
        if (drag)
            return;
        if (!drag)
            return;
        double x = rect.getCenterX();
        double y1 = rect.getMinY() - plus;
        double y2 = y1 - ROTATE_LINE_LENGTH;
        g.draw(new Line2D.Double(x, y1, x, y2));
        Ellipse2D.Double ellipse = new Ellipse2D.Double(x - ROTATE_SPOT_WIDTH
                / 2, y2 - ROTATE_SPOT_WIDTH, ROTATE_SPOT_WIDTH,
                ROTATE_SPOT_WIDTH);
        g.setColor(ROTATE_SPOT_COLOR);
        g.fill(ellipse);
        g.setColor(Color.black);
        g.draw(ellipse);

    }

    private void paintSelectedBorder(Graphics2D g, double plus) {
        for (Bounds bounds : selection.getBounds())
            if (bounds instanceof QBounds) {
                QBounds qBounds = (QBounds) bounds;
                Rectangle2D rect = translate(qBounds.getRectangle(), selection);
                g.draw(new Rectangle2D.Double(rect.getX() - plus, rect.getY()
                        - plus, rect.getWidth() + plus * 4, rect.getHeight()
                        + plus * 4));
            }
    }

    private Rectangle2D translate(Rectangle2D rectangle, Group group) {
        Rectangle2D rectangle2 = group.translate(rectangle);

        return new Rectangle2D.Double(translate(rectangle2.getX()),
                translate(rectangle2.getY()), rectangle2.getWidth() * zoom,
                rectangle2.getHeight() * zoom);
    }

    private Rectangle2D translate(Rectangle2D rectangle) {
        return new Rectangle2D.Double(translate(rectangle.getX()),
                translate(rectangle.getY()), rectangle.getWidth() * zoom,
                rectangle.getHeight() * zoom);
    }

    private double translate(double coordinate) {
        return coordinate * zoom;
    }

    private Point2D translate(Point2D point) {
        return new Point2D.Double(translate(point.getX()), translate(point
                .getY()));
    }

    private Rectangle getSelectionRectangle() {
        synchronized (mouseLock) {
            if (mousePressedPosition != null) {
                int x;
                int y;
                int width2;
                int height2;

                if (mouseDragPosition.x > mousePressedPosition.x) {
                    x = mousePressedPosition.x;
                    width2 = mouseDragPosition.x - mousePressedPosition.x;
                } else {
                    x = mouseDragPosition.x;
                    width2 = mousePressedPosition.x - mouseDragPosition.x;
                }
                if (mouseDragPosition.y > mousePressedPosition.y) {
                    y = mousePressedPosition.y;
                    height2 = mouseDragPosition.y - mousePressedPosition.y;
                } else {
                    y = mouseDragPosition.y;
                    height2 = mousePressedPosition.y - mouseDragPosition.y;
                }

                Rectangle rectangle = new Rectangle(x, y, width2, height2);
                return rectangle;
            }
        }
        return null;
    }

    public Diagram getDiagramam() {
        return diagram;
    }

    public void mouseClicked(Point point) {
        if (selection.getBounds().length == 0)
            onClick(untranslate(point), point);
    }

    private Point2D untranslate(Point point) {
        return new Point2D.Double(untranslate(point.getX()), untranslate(point
                .getY()));
    }

    public void mouseEntered(Point point) {
        synchronized (mouseLock) {

        }
    }

    public void mouseExited(Point point) {
        synchronized (mouseLock) {

        }
    }

    public void mousePressed(Point point) {
        synchronized (mouseLock) {
            mousePressedPosition = point;
            mouseDragPosition = mousePressedPosition;
            mouseDragged = false;
            Bounds bounds = getSelected(point);
            setDragSides(point);
            drag = false;
            if (bounds == null) {
                if (selection.getBounds().length > 0) {
                    if (dragSides.size() == 0) {
                        selection.setBounds(new Bounds[]{});
                        selectionChanged();
                    }
                }
            } else {
                boolean selected = false;
                for (Bounds bounds2 : selection.getBounds()) {
                    if (bounds2.equals(bounds))
                        selected = true;
                }
                if (!selected) {
                    selection.setBounds(new Bounds[]{bounds});
                }
                selectionChanged();
            }
        }
    }

    protected void selectionChanged() {
        updateSelectionBounds();
    }

    public void updateSelectionBounds() {
        updateBounds(selection.getBounds());
    }

    public void updateAllBounds() {
        updateBounds(diagram.getBounds());
    }

    public void updateBounds(Bounds[] bounds) {
        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i] instanceof QBounds) {
                QBounds qBounds = (QBounds) bounds[i];
                Component component = diagram.getComponent(qBounds);
                double width = qBounds.getSize().getWidth();
                double height = qBounds.getSize().getHeight();
                if (!component.isResizeableX()) {
                    width = component.getMinWidth();
                }
                if (!component.isResizeableY()) {
                    height = component.getMinHeight();
                }
                qBounds.getSize().setSize(width, height);
            }
        }
    }

    protected void onClick(Point2D point, Point mousePoint) {
    }

    private void setDragSides(Point point) {
        dragSides.clear();

        boolean resizableX = selection.isResizeableX(diagram);
        boolean resizableY = selection.isResizeableY(diagram);

        if (selection.getBounds().length == 0)
            return;
        if (isLeftMove(point))
            dragSides.add(Side.LEFT);
        else if (isRightMove(point))
            dragSides.add(Side.RIGHT);
        else if (isTopMove(point))
            dragSides.add(Side.TOP);
        else if (isBottomMove(point))
            dragSides.add(Side.BOTTOM);
        else if (isLeftBottomMove(point)) {
            dragSides.add(Side.LEFT);
            dragSides.add(Side.BOTTOM);
        } else if (isRightBottomMove(point)) {
            dragSides.add(Side.RIGHT);
            dragSides.add(Side.BOTTOM);
        } else if (isTopLeftMove(point)) {
            dragSides.add(Side.TOP);
            dragSides.add(Side.LEFT);
        } else if (isTopRightMove(point)) {
            dragSides.add(Side.TOP);
            dragSides.add(Side.RIGHT);
        }
        if (!resizableX) {
            dragSides.remove(Side.LEFT);
            dragSides.remove(Side.RIGHT);
        }
        if (!resizableY) {
            dragSides.remove(Side.TOP);
            dragSides.remove(Side.BOTTOM);
        }
    }

    private Bounds getSelected(Point point) {
        Bounds[] bounds = diagram.getBounds();
        double x = untranslate(point.getX());
        double y = untranslate(point.getY());
        Bounds bounds3 = null;
        for (int i = bounds.length - 1; i >= 0; i--) {
            Bounds bounds2 = bounds[i];
            if ((bounds2.getLeft() < x) && (bounds2.getTop() < y)
                    && (bounds2.getRight() > x) && (bounds2.getBottom() > y)) {
                bounds3 = bounds2;
                break;
            }
        }
        return bounds3;
    }

    public void mouseReleased(Point point) {
        synchronized (mouseLock) {
            if (drag) {
                drag = false;
                selection.applyTransforms();
                apply();
            } else if (mouseDragged)
                select();
            mousePressedPosition = null;
            mouseDragPosition = null;
        }
        repaint();
    }

    private void select() {
        Rectangle rectangle = getSelectionRectangle();
        selection.setBounds(getBoundsInRectangle(rectangle));
        selectionChanged();
    }

    protected void apply() {

    }

    public void mouseDragged(Point point) {
        synchronized (mouseLock) {
            mouseDragged = true;
            if (mousePressedPosition != null) {
                mouseDragPosition = point;
                if (selection.getBounds().length > 0) {
                    drag = true;

                    double uX = untranslate(-mousePressedPosition.getX()
                            + mouseDragPosition.getX());
                    double uY = untranslate(-mousePressedPosition.getY()
                            + mouseDragPosition.getY());

                    if (((XMLComponent) diagram.getComponent(selection
                            .getBounds()[0])).isY())
                        uX = 0;
                    else
                        uY = 0;

                    Point2D.Double translate = new Point2D.Double(uX, uY);
                    selection.setTranslate(translate, dragSides, diagram);
                }

            }
        }
        repaint();
    }

    public void mouseMoved(Point point) {
        synchronized (mouseLock) {
            if (mousePressedPosition != null)
                mouseDragPosition = point;

            Bounds bounds = getSelected(point);
            int cursorType;
            if (bounds == null)
                cursorType = Cursor.DEFAULT_CURSOR;
            else {
                cursorType = Cursor.MOVE_CURSOR;
            }
            if (selection.getBounds().length == 1) {

                boolean resizableX = selection.isResizeableX(diagram);
                boolean resizableY = selection.isResizeableY(diagram);

                if ((resizableX) && (isRightMove(point)))
                    cursorType = Cursor.E_RESIZE_CURSOR;
                else if ((resizableX) && (isLeftMove(point)))
                    cursorType = Cursor.W_RESIZE_CURSOR;
                else if ((resizableY) && (isTopMove(point)))
                    cursorType = Cursor.N_RESIZE_CURSOR;
                else if ((resizableY) && (isBottomMove(point)))
                    cursorType = Cursor.S_RESIZE_CURSOR;
                else if ((resizableY) && (resizableX)
                        && (isRightBottomMove(point)))
                    cursorType = Cursor.SE_RESIZE_CURSOR;
                else if ((resizableY) && (resizableX)
                        && (isLeftBottomMove(point)))
                    cursorType = Cursor.SW_RESIZE_CURSOR;
                else if ((resizableY) && (resizableX)
                        && (isTopRightMove(point)))
                    cursorType = Cursor.NE_RESIZE_CURSOR;
                else if ((resizableY) && (resizableX) && (isTopLeftMove(point)))
                    cursorType = Cursor.NW_RESIZE_CURSOR;

            }

            if (this.cursorType != cursorType) {
                this.cursorType = cursorType;
                this.setCursor(new Cursor(cursorType));
            }

        }
        repaint();
    }

    private boolean isRightMove(Point point) {
        return getControlRectangle(getRightSelectionPoint()).contains(point);
    }

    private boolean isLeftMove(Point point) {
        return getControlRectangle(getLeftSelectionPoint()).contains(point);
    }

    private boolean isRightBottomMove(Point point) {
        return getControlRectangle(getRightBottomSelectionPoint()).contains(
                point);
    }

    private boolean isLeftBottomMove(Point point) {
        return getControlRectangle(getLeftBottomSelectionPoint()).contains(
                point);
    }

    private boolean isTopRightMove(Point point) {
        return getControlRectangle(getTopRightSelectionPoint()).contains(point);
    }

    private boolean isTopLeftMove(Point point) {
        return getControlRectangle(getTopLeftSelectionPoint()).contains(point);
    }

    private boolean isTopMove(Point point) {
        return getControlRectangle(getTopSelectionPoint()).contains(point);
    }

    private boolean isBottomMove(Point point) {
        return getControlRectangle(getBottomSelectionPoint()).contains(point);
    }

    private Stroke createSelectionStroke() {
        float[] f = new float[4];
        f[0] = 10;
        f[1] = 3;
        f[2] = 10;
        f[3] = 3;

        return new BasicStroke(2f, BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER, 1, f, 0);

    }

    private Bounds[] getBoundsInRectangle(Rectangle rectangle) {
        List<Bounds> list = new ArrayList<Bounds>();
        double maxX = untranslate(rectangle.getMaxX());
        double minX = untranslate(rectangle.getMinX());
        double maxY = untranslate(rectangle.getMaxY());
        double minY = untranslate(rectangle.getMinY());

        for (Bounds bounds : diagram.getBounds()) {
            if ((bounds.getLeft() <= maxX) && (bounds.getLeft() >= minX)
                    && (bounds.getRight() <= maxX)
                    && (bounds.getRight() >= minX)) {
                if ((bounds.getTop() <= maxY) && (bounds.getTop() >= minY)
                        && (bounds.getBottom() <= maxY)
                        && (bounds.getBottom() >= minY)) {
                    list.add(bounds);
                }
            }
        }

        return list.toArray(new Bounds[list.size()]);
    }

    private double untranslate(double coordinate) {
        return (coordinate) / zoom;
    }

    private Point2D getRightSelectionPoint() {
        Rectangle2D rect = selection.getTranslateRectangle();
        return translate(new Point2D.Double(rect.getMaxX(), rect.getCenterY()));
    }

    private Point2D getLeftSelectionPoint() {
        Rectangle2D rect = selection.getTranslateRectangle();
        return translate(new Point2D.Double(rect.getMinX(), rect.getCenterY()));
    }

    private Point2D getTopSelectionPoint() {
        Rectangle2D rect = selection.getTranslateRectangle();
        return translate(new Point2D.Double(rect.getCenterX(), rect.getMinY()));
    }

    private Point2D getBottomSelectionPoint() {
        Rectangle2D rect = selection.getTranslateRectangle();
        return translate(new Point2D.Double(rect.getCenterX(), rect.getMaxY()));
    }

    private Point2D getRightBottomSelectionPoint() {
        Rectangle2D rect = selection.getTranslateRectangle();
        return translate(new Point2D.Double(rect.getMaxX(), rect.getMaxY()));
    }

    private Point2D getLeftBottomSelectionPoint() {
        Rectangle2D rect = selection.getTranslateRectangle();
        return translate(new Point2D.Double(rect.getMinX(), rect.getMaxY()));
    }

    private Point2D getTopRightSelectionPoint() {
        Rectangle2D rect = selection.getTranslateRectangle();
        return translate(new Point2D.Double(rect.getMaxX(), rect.getMinY()));
    }

    private Point2D getTopLeftSelectionPoint() {
        Rectangle2D rect = selection.getTranslateRectangle();
        return translate(new Point2D.Double(rect.getMinX(), rect.getMinY()));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            mouseClicked(translateMouseEvent(e));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseEntered(translateMouseEvent(e));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseExited(translateMouseEvent(e));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Bounds[] bounds = selection.getBounds();
            mousePressed(translateMouseEvent(e));
            if (e.isControlDown()) {
                Bounds[] bounds2 = selection.getBounds();
                if (!Arrays.equals(bounds, bounds2)) {
                    Bounds[] bounds3 = Arrays.copyOf(bounds, bounds.length
                            + bounds2.length);
                    for (int i = 0; i < bounds2.length; i++) {
                        bounds3[i + bounds.length] = bounds2[i];
                    }
                    selection.setBounds(bounds3);
                    selectionChanged();
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            mouseReleased(translateMouseEvent(e));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseDragged(translateMouseEvent(e));
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseMoved(translateMouseEvent(e));
    }

    private Point translateMouseEvent(MouseEvent event) {
        return new Point((int) (event.getPoint().getX() - tx), (int) (event
                .getPoint().getY() - ty));
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void clearSelection() {
        selection.setBounds(new QBounds[]{});
    }

    public Component[] getSelectedComponents() {
        return diagram.getComponents(selection.getBounds());
    }

    public Group getSelection() {
        return selection;
    }
}
