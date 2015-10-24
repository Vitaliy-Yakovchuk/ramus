/*
 * Created on 22/4/2005
 */

package com.ramussoft.pb.idef.visual;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.dsoft.utils.Options;

/**
 * Клас для роботи з віконними об’єктами, які необхідно переміщати змінювати
 * їхні розміри...
 *
 * @author ZDD
 */

public class MovingPanel {

    public static final int RIGHT = 0;

    public static final int BOTTOM = 1;

    public static final int LEFT = 2;

    public static final int TOP = 3;

    public static final int RIGHT_BOTTOM = 4;

    public static final int BOTTOM_LEFT = 5;

    public static final int LEFT_TOP = 6;

    public static final int TOP_RIGHT = 7;

    public static final int LEFT_BOTTOM = 20;

    public static final int LEFT_TOP_BOTTOM = 21;

    protected boolean canMove = true;

    protected MovingArea movingArea = null;

    private double movingTextResizeX = Options.getDouble(
            "MOVING_TEXT_SPOT_RESIZE", 0.003);

    private double movinTextResizeY = Options.getDouble(
            "MOVING_TEXT_SPOT_RESIZE", 0.003);

    private static final double MOVING_TEXT_HEIGHT = Options.getDouble(
            "MOVING_TEXT_HEIGHT", 0.05);

    private static final double MOVING_TEXT_WIDTH = Options.getDouble(
            "MOVING_TEXT_WIDTH", 0.1);

    protected FRectangle myBounds = new FRectangle(0, 0, MOVING_TEXT_WIDTH,
            MOVING_TEXT_HEIGHT);

    public static final int STATE_STOP = 0;

    public static final int STATE_MOVE = 1;

    public static final int STATE_RESIZE_RIGHT = 2;

    public static final int STATE_RESIZE_LEFT = 3;

    public static final int STATE_RESIZE_TOP = 4;

    public static final int STATE_RESIZE_BOTTOM = 5;

    public static final int STATE_RESIZE_RIGHT_BOTTOM = 6;

    public static final int STATE_RESIZE_LEFT_TOP = 7;

    public static final int STATE_RESIZE_TOP_RIGHT = 8;

    public static final int STATE_RESIZE_BOTTOM_LEFT = 9;

    protected int state = STATE_STOP;

    private FloatPoint pressedPoint;

    protected boolean isFocused;

    int xt;

    int yt;

    private int mouseType;

    protected FloatPoint convertPoint(final FloatPoint point) {
        return point.minus(getBounds().getLocation());
    }

    public boolean contain(final FloatPoint point) {
        return point.getX() >= getBounds().getX()
                && point.getY() >= getBounds().getY()
                && point.getX() <= getBounds().getRight()
                && point.getY() <= getBounds().getBottom();
    }

    protected int getPressType(final FloatPoint point) {
        movingTextResizeX = movingArea.getDoubleOrdinate(4);
        movinTextResizeY = movingArea.getDoubleOrdinate(4);
        xt = 0;
        yt = 0;
        if (point.getX() <= movingTextResizeX)
            xt = 1;
        else if (point.getX() >= getBounds().getWidth() - movingTextResizeX)
            xt = 2;

        if (point.getY() <= movinTextResizeY)
            yt = 1;
        else if (point.getY() >= getBounds().getHeight() - movinTextResizeY)
            yt = 2;

        if (xt == 1) {
            switch (yt) {
                case 0:
                    return STATE_RESIZE_LEFT;
                case 1:
                    return STATE_RESIZE_LEFT_TOP;
                case 2:
                    return STATE_RESIZE_BOTTOM_LEFT;
            }
        } else if (xt == 2) {
            switch (yt) {
                case 0:
                    return STATE_RESIZE_RIGHT;
                case 1:
                    return STATE_RESIZE_TOP_RIGHT;
                case 2:
                    return STATE_RESIZE_RIGHT_BOTTOM;
            }
        } else {
            switch (yt) {
                case 0:
                    return STATE_MOVE;
                case 1:
                    return STATE_RESIZE_TOP;
                case 2:
                    return STATE_RESIZE_BOTTOM;
            }
        }
        return STATE_STOP;
    }

    public void mousePressed(final FloatPoint point) {
        state = getPressType(point);
        pressedPoint = point;
    }

    public int mouseMoved(final FloatPoint point) {
        final int type = getPressType(point);
        if (type != mouseType) {
            mouseType = type;
            if (movingArea.getVisualCopyCursor() != null)
                movingArea.setCursor(movingArea.getVisualCopyCursor());
            else if (type == -1)
                movingArea.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            else if (type == STATE_MOVE)
                movingArea.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            else if (type == STATE_STOP)
                movingArea.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            else if (type == STATE_RESIZE_LEFT)
                movingArea.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
            else if (type == STATE_RESIZE_RIGHT)
                movingArea.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
            else if (type == STATE_RESIZE_TOP)
                movingArea.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
            else if (type == STATE_RESIZE_BOTTOM)
                movingArea.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
            else if (type == STATE_RESIZE_BOTTOM_LEFT)
                movingArea.setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
            else if (type == STATE_RESIZE_RIGHT_BOTTOM)
                movingArea.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
            else if (type == STATE_RESIZE_TOP_RIGHT)
                movingArea.setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));
            else if (type == STATE_RESIZE_LEFT_TOP)
                movingArea.setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
        }
        movingArea.repaint();
        return -1;
    }

    public void mouseDragged(final FloatPoint point) {
        if (!canMove)
            return;
        final FloatPoint add = point.minus(pressedPoint);
        if (xt == 2)
            pressedPoint.setX(point.getX());
        if (yt == 2)
            pressedPoint.setY(point.getY());
        switch (state) {
            case STATE_MOVE:
                move(add);
                break;
            case STATE_RESIZE_BOTTOM: {
                moveBottomWall(add.getY());
                moveRightWall(0);
            }
            break;
            case STATE_RESIZE_TOP: {
                moveTopWall(add.getY());
                moveRightWall(0);
            }
            break;
            case STATE_RESIZE_LEFT: {
                moveLeftWall(add.getX());
                moveBottomWall(0);
            }
            break;
            case STATE_RESIZE_RIGHT: {
                moveRightWall(add.getX());
                moveBottomWall(0);
            }
            break;
            case STATE_RESIZE_BOTTOM_LEFT: {
                moveBottomWall(add.getY());
                moveLeftWall(add.getX());
            }
            break;
            case STATE_RESIZE_LEFT_TOP: {
                moveTopWall(add.getY());
                moveLeftWall(add.getX());
            }
            break;
            case STATE_RESIZE_TOP_RIGHT: {
                moveTopWall(add.getY());
                moveRightWall(add.getX());
            }
            break;
            case STATE_RESIZE_RIGHT_BOTTOM: {
                moveBottomWall(add.getY());
                moveRightWall(add.getX());
            }
            break;
        }
        onBoundsChange();
    }

    public void mouseReleased(final FloatPoint point) {
        if (state != STATE_STOP)
            onEndBoundsChange();
        state = STATE_STOP;
    }

    public void mouseEnter() {

    }

    public void mouseLeave() {
        mouseType = STATE_STOP;
        movingArea.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        movingArea.repaint();
    }

    public void mouseClicked(final FloatPoint point) {

    }

    public void moveRightWall(double x) {
        if (getBounds().getRight() + x > movingArea.getDoubleWidth()
                - getMinRightWidth())
            x = movingArea.getDoubleWidth() - getMinRightWidth()
                    - getBounds().getRight();
        if (getBounds().getWidth() + x < getMinWidth())
            x = getMinWidth() - getBounds().getWidth();
        getBounds().setWidth(getBounds().getWidth() + x);
    }

    public void moveLeftWall(double x) {
        if (getBounds().getX() + x < getMinLeftWidth())
            x = getMinLeftWidth() - getBounds().getX();
        if (getBounds().getWidth() - x < getMinWidth())
            x = -getMinWidth() + getBounds().getWidth();
        getBounds().setWidth(getBounds().getWidth() - x);
        getBounds().setX(getBounds().getX() + x);
    }

    public void moveTopWall(double y) {
        if (getBounds().getY() + y < getMinTopHeight())
            y = getMinTopHeight() - getBounds().getY();
        if (getBounds().getHeight() - y < getMinHeight())
            y = -getMinHeight() + getBounds().getHeight();
        getBounds().setHeight(getBounds().getHeight() - y);
        getBounds().setY(getBounds().getY() + y);
    }

    public void moveBottomWall(double y) {
        if (getBounds().getBottom() + y > movingArea.getDoubleHeight()
                - getMinBottomHeight())
            y = movingArea.getDoubleHeight() - getMinBottomHeight()
                    - getBounds().getBottom();
        if (getBounds().getHeight() + y < getMinHeight())
            y = getMinHeight() - getBounds().getHeight();
        getBounds().setHeight(getBounds().getHeight() + y);
    }

    public void move(final FloatPoint point) {
        if (point.getX() + getBounds().getRight() > movingArea.getDoubleWidth()
                - getMinRightWidth())
            point.setX(movingArea.getDoubleWidth() - getMinRightWidth()
                    - getBounds().getRight());
        else if (point.getX() + getBounds().getX() < getMinLeftWidth())
            point.setX(getMinLeftWidth() - getBounds().getX());

        if (point.getY() + getBounds().getBottom() > movingArea
                .getDoubleHeight() - getMinBottomHeight())
            point.setY(movingArea.getDoubleHeight() - getMinBottomHeight()
                    - getBounds().getBottom());
        else if (point.getY() + getBounds().getY() < getMinTopHeight())
            point.setY(getMinTopHeight() - getBounds().getY());

        getBounds().setX(getBounds().getX() + point.getX());
        getBounds().setY(getBounds().getY() + point.getY());
    }

    /**
     * @param canMove The canMove to set.
     */
    public void setCanMove(final boolean canMove) {
        this.canMove = canMove;
    }

    /**
     * @return Returns the canMove.
     */
    public boolean isCanMove() {
        return canMove;
    }

    /**
     * @return Returns the movingArea.
     */
    public MovingArea getMovingArea() {
        return movingArea;
    }

    public static int getOpposite(final int type) {
        return (type + 2) % 4;
    }

    public void onBoundsChange() {
        movingArea.repaint();
    }

    /**
     * Метод повертає мінімальну допустиму відстань до верхнього краю області
     * малювання.
     *
     * @return Мінімальна допустима відстань до верхнього краю області
     * малювання.
     */

    protected double getMinTopHeight() {
        return MovingArea.TOP_PART;
    }

    /**
     * Метод повертає мінімальну допустиму відстань до нижнього краю області
     * малювання.
     *
     * @return Мінімальна допустима відстань до нижнього краю області малювання.
     */

    protected double getMinBottomHeight() {
        return MovingArea.BOTTOM_PART;
    }

    /**
     * Метод повертає мінімальну допустиму відстань до лівого краю області
     * малювання.
     *
     * @return Мінімальна допустима відстань до лівого краю області малювання.
     */

    protected double getMinLeftWidth() {
        return MovingArea.LEFT_PART;
    }

    /**
     * Метод повертає мінімальну допустиму відстань до правого краю області
     * малювання.
     *
     * @return Мінімальна допустима відстань до правого краю області малювання.
     */

    protected double getMinRightWidth() {
        return MovingArea.RIGHT_PART;
    }

    /**
     * Конструктор.
     *
     * @param movingArea Область для малювання, з якої зчитуються налаштування, блок не
     *                   обов’язково має розташовуватись саме на цій панелі.
     */

    public MovingPanel(final MovingArea movingArea) {
        super();
        this.movingArea = movingArea;
        myBounds = new FRectangle(0, 0, 40, 20);
    }

    /**
     * Повертає мінімально можливу ширину прямокутника.
     *
     * @return мінімально можлива ширина прямокутника.
     */

    public double getMinWidth() {
        return 8;
    }

    /**
     * Повертає мінімально можливу висоту прямокутника.
     *
     * @return мінімально можлива висота прямокутника.
     */

    public double getMinHeight() {
        return 8;
    }

    public void onProcessEndBoundsChange() {
        myBounds.setTransformNetBounds(MovingArea.NET_LENGTH);
    }

    public void onEndBoundsChange() {
        onProcessEndBoundsChange();
    }

    /**
     * Повертає координати прямокутника.
     *
     * @return Координати прямокутника.
     */

    public FRectangle getBounds() {
        return myBounds;
    }

    /**
     * @param myBounds The myBounds to set.
     */
    public void setBounds(final FRectangle mBounds) {
        myBounds = new FRectangle(mBounds);
        if (myBounds.getX() < getMinLeftWidth())
            myBounds.setX(getMinLeftWidth());
        if (myBounds.getY() < getMinTopHeight())
            myBounds.setY(getMinTopHeight());
        if (myBounds.getX() + myBounds.getWidth() > movingArea.getDoubleWidth()
                - getMinRightWidth())
            myBounds.setX(movingArea.getDoubleWidth() - getMinRightWidth()
                    - getBounds().getWidth());
        if (myBounds.getY() + myBounds.getHeight() > movingArea
                .getDoubleHeight() - getMinBottomHeight())
            myBounds.setY(movingArea.getDoubleHeight() - getMinBottomHeight()
                    - getBounds().getHeight());
    }

    public void setBounds(final double x, final double y, final double width,
                          final double height) {
        setBounds(new FRectangle(x, y, width, height));
    }

    public void setLocation(final FloatPoint point) {
        if (point.getX() + getBounds().getWidth() > movingArea.getDoubleWidth()
                - getMinRightWidth())
            point.setX(movingArea.getDoubleWidth() - getMinRightWidth()
                    - getBounds().getWidth());
        else if (point.getX() < getMinLeftWidth())
            point.setX(getMinLeftWidth());

        if (point.getY() + getBounds().getHeight() > movingArea
                .getDoubleHeight() - getMinBottomHeight())
            point.setY(movingArea.getDoubleHeight() - getMinBottomHeight()
                    - getBounds().getHeight());
        else if (point.getY() < getMinTopHeight())
            point.setY(getMinTopHeight());

        getBounds().setX(point.getX());
        getBounds().setY(point.getY());
        onBoundsChange();
    }

    public FloatPoint getLocation() {
        return getBounds().getLocation();
    }

    public void focusGained(boolean silent) {
        isFocused = true;
        movingArea.repaint();
    }

    public void focusLost() {
        isFocused = false;
        movingArea.repaint();
    }

    public void paint(final Graphics2D g) {
        paintBorder(g);

    }

    protected void paintBorder(final Graphics2D g) {
        if (mouseType != STATE_MOVE && mouseType != STATE_STOP) {
            final Rectangle2D r = movingArea.getBounds(getBounds());
            final double w = movingArea.getIntOrdinate(movingTextResizeX);
            final double h = movingArea.getIntOrdinate(movinTextResizeY);
            switch (mouseType) {
                case STATE_RESIZE_BOTTOM:
                    g.draw(new Rectangle2D.Double(r.getX(), r.getY()
                            + r.getHeight() - h, r.getWidth(), h));
                    break;
                case STATE_RESIZE_TOP:
                    g.draw(new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(),
                            h));
                    break;
                case STATE_RESIZE_LEFT:
                    g.draw(new Rectangle2D.Double(r.getX(), r.getY(), w, r
                            .getHeight()));
                    break;
                case STATE_RESIZE_RIGHT:
                    g.draw(new Rectangle2D.Double(r.getX() + r.getWidth() - w, r
                            .getY(), w, r.getHeight()));
                    break;
                case STATE_RESIZE_BOTTOM_LEFT:
                    g.draw(new Rectangle2D.Double(r.getX(), r.getY() - h
                            + r.getHeight(), w, h));
                    break;
                case STATE_RESIZE_LEFT_TOP:
                    g.draw(new Rectangle2D.Double(r.getX(), r.getY(), w, h));
                    break;
                case STATE_RESIZE_TOP_RIGHT:
                    g.draw(new Rectangle2D.Double(r.getX() + r.getWidth() - w, r
                            .getY(), w, h));
                    break;
                case STATE_RESIZE_RIGHT_BOTTOM:
                    g.draw(new Rectangle2D.Double(r.getX() + r.getWidth() - w, r
                            .getY() - h + r.getHeight(), w, h));
                    break;
            }
        }
        if (isFocused) {
            final Rectangle2D r = movingArea.getBounds(getBounds());
            final double w = movingArea.getIDoubleOrdinate(movingTextResizeX);
            final double h = movingArea.getIDoubleOrdinate(movinTextResizeY);
            // g.setColor(Color.black);
            g.draw(new Rectangle2D.Double(r.getX(), r.getY(), w, h));
            g.draw(new Rectangle2D.Double(r.getX() + r.getWidth() - w,
                    r.getY(), w, h));
            g.draw(new Rectangle2D.Double(r.getX(), r.getY() + r.getHeight()
                    - h, w, h));
            g.draw(new Rectangle2D.Double(r.getX() + r.getWidth() - w, r.getY()
                    + r.getHeight() - h, w, h));

            g.draw(new Rectangle2D.Double(r.getX() + r.getWidth() / 2 - w / 2,
                    r.getY(), w, h));
            g.draw(new Rectangle2D.Double(r.getX() + r.getWidth() - w, r.getY()
                    + r.getHeight() / 2 - h / 2, w, h));
            g.draw(new Rectangle2D.Double(r.getX() + r.getWidth() / 2 - w / 2,
                    r.getY() + r.getHeight() - h, w, h));
            g.draw(new Rectangle2D.Double(r.getX(), r.getY() + r.getHeight()
                    / 2 - h / 2, w, h));

        }
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
