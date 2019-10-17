package com.ramussoft.pb.idef.visual;

import java.awt.AlphaComposite;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TooManyListenersException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.dsoft.utils.DataLoader.MemoryData;
import com.dsoft.utils.DataLoader;
import com.dsoft.utils.Options;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.attribute.AttributeEditorView;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.EmptyPlugin;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.gui.qualifier.table.RowTreeTable;
import com.ramussoft.gui.qualifier.table.Rows;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.IDEF0TabView;
import com.ramussoft.idef0.attribute.DFDSName;
import com.ramussoft.idef0.attribute.RectangleVisualOptions;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.AbstractSector;
import com.ramussoft.pb.data.FunctionBeansClipboard;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.data.negine.NSector;
import com.ramussoft.pb.dfd.visual.DFDFunction;
import com.ramussoft.pb.dfd.visual.DFDObject;
import com.ramussoft.pb.dfd.visual.DataStore;
import com.ramussoft.pb.dfd.visual.External;
import com.ramussoft.pb.dfds.visual.DFDSFunction;
import com.ramussoft.pb.dfds.visual.DFDSRole;
import com.ramussoft.pb.dmaster.Template;
import com.ramussoft.pb.dmaster.TemplateFactory;
import com.ramussoft.pb.dmaster.UserTemplate;
import com.ramussoft.pb.frames.BaseDialog;
import com.ramussoft.pb.frames.MainFrame;
import com.ramussoft.pb.frames.docking.MChangeListener;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.PaintSector.Pin;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.elements.ReplaceStreamType;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.elements.SectorRefactor.PerspectivePoint;
import com.ramussoft.pb.idef.frames.ArrowTunnelDialog;
import com.ramussoft.pb.idef.frames.ContextMasterDialog;
import com.ramussoft.pb.idef.frames.IDEFPanel;
import com.ramussoft.pb.idef.frames.VisualOptionsDialog;
import com.ramussoft.pb.idef.frames.VisualPanelCopyOptions;
import com.ramussoft.pb.idef.visual.event.ActiveFunctionEvent;
import com.ramussoft.pb.idef.visual.event.ActiveFunctionListener;
import com.ramussoft.pb.idef.visual.text.TextPaintCache;
import com.ramussoft.pb.print.PStringBounder;

/**
 * Клас на якому відображаються елементи IDEF0. Всі координати елементів дійсні
 * числа, даний клас може змінювати маштаб для підгонки розмірів діаграми.
 * Переміщення елементів відбувається по сітці з певним кроком. Клас сам
 * визначає активні об’єкти вміє створювати стрілки, функціональні блок і т. д.
 * Об’єкт також показує контекстні меню.
 *
 * @author ZDD
 */
public class MovingArea extends JPanel {

    /**
     *
     */

    private static final long serialVersionUID = 2L;

    /**
     * Ширина клітинок сітки.
     */

    public static final int NET_LENGTH = Options.getInteger("NET_LENGTH", 3);

    /**
     * Змінна, яка переводиться в true, якщо іде друк.
     */

    private boolean printing = false;

    private static final ExecutorService bkPainter = Executors
            .newSingleThreadExecutor();

    /**
     * Товщина виділеної полоси для під’єднання сектору до границі області.
     */

    private double BORDER_WIDTH = getWidth(Options.getInteger(
            "MOVING_AREA_ BORDER_WIDTH", 20));

    public int D_MOVING_AREA_WIDTH = 800;

    public int D_MOVING_AREA_HEIGHT = 555;

    /**
     * Ширина області для малювання.
     */
    public int MOVING_AREA_WIDTH = 800;// Options.getInteger(

    // "MOVING_AREA_PAGE_WIDTH", 697);

    /**
     * Висота області для малювання.
     */
    public int MOVING_AREA_HEIGHT = 555;// Options.getInteger(

    // "MOVING_AREA_PAGE_HEIGHT", 451);
    public static final double TOP_PART = 0;

    public static final double BOTTOM_PART = 0;

    public final double TOP_PART_A = getWidth(MOVING_AREA_HEIGHT) / 5 * 0.6;

    public final double BOTTOM_PART_A = getWidth(MOVING_AREA_HEIGHT) / 5 * 0.4;

    public double CLIENT_HEIGHT = getWidth(MOVING_AREA_HEIGHT) - TOP_PART_A
            - BOTTOM_PART_A;

    public static final double LEFT_PART = 0;

    public static final double RIGHT_PART = LEFT_PART;

    public static final int DIAGRAM_TYPE_DFD = 1;

    public static final int DIAGRAM_TYPE_DFDS = 2;

    private final java.awt.Point mp = new java.awt.Point();

    public final static String CURRENT_MOVING_AREA = "CurrentMovingArea";

    public final static String STATE = "MovingAreaState";

    public double CLIENT_WIDTH = getWidth(MOVING_AREA_WIDTH) - LEFT_PART
            - RIGHT_PART;

    private Cursor visualCopyCursor;

    /**
     * Звичайний стан області відображення.
     */

    public static final int ARROW_STATE = 0;

    /**
     * Стан додавання стрілки.
     */

    public static final int ARROW_CHANGING_STATE = 1;

    /**
     * Стан додавання функціонального блоку.
     */

    public static final int FUNCTION_ADDING_STATE = 2;

    /**
     * Стан додавання тільди.
     */

    public static final int TILDA_ADDING_STATE = 3;

    /**
     * Стан додавання тексту.
     */

    public static final int TEXT_ADDING_STATE = 4;

    /**
     * Стан додавання першої точки.
     */

    public static final int START_POINT_ADDING = 5;

    /**
     * Стан додавання останьої точки.
     */

    public static final int END_POINT_ADDING = 6;

    public static final int START_POINT_CHANGING = 7;

    public static final int END_POINT_CHANGING = 8;

    public static final int START_POINT_CHANGING_ADD = 11;

    public static final int END_POINT_CHANGING_ADD = 12;

    public static final int EXTERNAL_REFERENCE_ADDING_STATE = 9;

    public static final int DATA_STORE_ADDING_STATE = 10;

    public static final int DFDS_ROLE_ADDING_STATE = 13;

    public static final double PART_SPACE = getWidth(7);

    public static boolean DISABLE_RENDERING_HINTS = Options.getBoolean(
            "DISABLE_RENDERING_HINTS", getDefaultRenderingHintsDisability());

    public int secondNamePartMinus = Options.getInteger("SECOND_PART_MINUS", 2);

    private Function lockedFunction = null;

    /**
     * Клас, який використовується для виводу тексту та переносу тексту на
     * екран.
     */

    public final PStringBounder stringBounder = new PStringBounder(this);

    /**
     * Посилання на панель, на якій розташована область.
     */

    private IDEFPanel panel = null;

    public DataPlugin dataPlugin;

    /**
     * Активни на даний момент функціональний блок.
     */

    private Function activeFunction;

    /**
     * Посилання на активний в даний момент об’єкт.
     */

    private Object activeObject = null;

    /**
     * Посилання на частину сектора, переміщення якої відбувається.
     */

    private PaintSector.Pin drawPin = null;

    /**
     * Частана ня якій знаходиться мишка в диний час.
     */

    private PaintSector.Pin mousePin = null;

    /**
     * Масив панелей, які розташовані на області відображення.
     */

    private MovingText[] panels = new MovingText[0];

    /**
     * Понель, яка активна на даний момент.
     */

    private MovingText pressedPanel;

    /**
     * Панель, на якій знаходиться курсор.
     */

    protected MovingText enteredPanel;

    /**
     * Виділена частина області малювання. Використовується для перемальовки
     * області при необхідності.
     */

    private int borderType = -1;

    /**
     * Поточний маштаб для малювання.
     */

    public double zoom = 1;

    private int functionIndex = 0;

    private Object mouseLock = new Object();

    private EventListenerList listenerList = new EventListenerList();

    /**
     * Клас, який обробляє зміни секторів.
     */

    private SectorRefactor refactor = new SectorRefactor(this);

    private ArrowTunnelDialog tunnelDialog = null;

    private int pressPanelMoveType = -1;

    protected boolean crosstunnel;

    private Cross cross = new Cross();

    private int state = ARROW_STATE;

    private MouseSelection mouseSelection;

    public TextPaintCache textPaintCache = new TextPaintCache();

    private boolean mousePinWidthCtrl;

    private Object backgroundPaintlock = new Object();

    private boolean bkRepaint = false;

    public void processKeyBinding(final int keyKode) {
        if (pressedPanel != null) {
            switch (keyKode) {
                case KeyEvent.VK_F2:
                    renameObject();
                    return;
                default:
                    // p = false;
                    break;
            }
            // if (p)
            // pressedPanel.onEndBoundsChange();
        }
        if (KeyEvent.VK_ESCAPE == keyKode) {
            if (getState() == END_POINT_ADDING) {
                setState(START_POINT_ADDING);
                if (panel != null)
                    panel.getFramework().remove(CURRENT_MOVING_AREA);
                repaintAsync();
            } else if (getChangingState() == ARROW_CHANGING_STATE) {
                if (panel != null)
                    panel.getFramework().remove(CURRENT_MOVING_AREA);
                cancelAdding();
            }
        }
    }

    private static boolean getDefaultRenderingHintsDisability() {
        String vmName = System.getProperty("java.vm.name");
        if (vmName != null && vmName.contains("OpenJDK"))
            return true;
        return false;
    }

    private final MouseMotionListener moveMoveListener = new MouseMotionListener() {

        public void mouseDragged(MouseEvent e) {
            /*
             * if (drawPin == null && lastDX / NET_LENGTH == e.getX() /
			 * NET_LENGTH && lastDY / NET_LENGTH == e.getY() / NET_LENGTH)
			 * return;
			 */
            syncMouseDragged(e);
        }

        public void mouseMoved(MouseEvent arg0) {
            synchronized (mouseLock) {

                boolean rep = false;

                int tmp = getBorderType();

                PaintSector.Pin pin = getPin(arg0.getX(), arg0.getY());
                if (panel.isShowToolTips()) {
                    if (enteredPanel != null)
                        setToolTipText(enteredPanel.getToolTipText());
                    else if (pin != null) {
                        Stream stream = pin.getSector().getStream();
                        if (stream != null)
                            setToolTipText(stream.getToolTipText(false));
                        else
                            setToolTipText(null);
                    } else
                        setToolTipText(null);
                } else
                    setToolTipText(null);

                if (getChangingState() == ARROW_CHANGING_STATE)
                    rep = true;
                if (pin != mousePin) {
                    mousePin = pin;
                    if (pin == null)
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    else if (getState() == ARROW_STATE
                            || getState() == FUNCTION_ADDING_STATE
                            || getState() == TEXT_ADDING_STATE) {
                        if (pin.getType() == PaintSector.PIN_TYPE_X)
                            setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                        else
                            setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
                    }
                    rep = true;
                }

                if (tmp != borderType) {
                    borderType = tmp;
                    rep = true;
                }
                if (mousePin != null) {
                    if (rep)
                        repaintAsync();
                    return;
                }

                FloatPoint point = toPoint(arg0.getX(), arg0.getY());
                boolean b = true;
                for (int i = panels.length - 1; i >= 0; i--)
                    if (panels[i].contain(point)
                            && !(panels[i] instanceof DFDSRole)) {
                        MovingText pressedPanel = panels[i];
                        if (enteredPanel != pressedPanel) {
                            if (enteredPanel != null)
                                enteredPanel.mouseLeave();
                            enteredPanel = pressedPanel;
                            enteredPanel.mouseEnter();
                        }
                        int mouseMoved = pressedPanel.mouseMoved(pressedPanel
                                .convertPoint(point));
                        if (pressPanelMoveType != mouseMoved) {
                            pressPanelMoveType = mouseMoved;
                            MovingArea ma = getCurrentMovingArea();
                            if ((ma != null) && (ma != MovingArea.this)
                                    && (pressPanelMoveType >= 0)) {
                                ma.crosstunnel = true;
                                ma.repaintAsync();
                            }
                        }
                        b = false;
                        break;
                    }

                for (int i = panels.length - 1; i >= 0; i--)
                    if (panels[i].contain(point)
                            && panels[i] instanceof DFDSRole) {
                        MovingText pressedPanel = panels[i];
                        if (enteredPanel != pressedPanel) {
                            if (enteredPanel != null)
                                enteredPanel.mouseLeave();
                            enteredPanel = pressedPanel;
                            enteredPanel.mouseEnter();
                        }
                        int mouseMoved = pressedPanel.mouseMoved(pressedPanel
                                .convertPoint(point));
                        if (pressPanelMoveType != mouseMoved) {
                            pressPanelMoveType = mouseMoved;
                            MovingArea ma = getCurrentMovingArea();
                            if ((ma != null) && (ma != MovingArea.this)
                                    && (pressPanelMoveType >= 0)) {
                                ma.crosstunnel = true;
                                ma.repaintAsync();
                            }
                        }
                        b = false;
                        break;
                    }

                if (b && enteredPanel != null) {
                    enteredPanel.mouseLeave();
                    enteredPanel = null;
                    MovingArea ma = getCurrentMovingArea();
                    if ((ma != MovingArea.this) && (ma != null)) {
                        if (ma.crosstunnel) {
                            ma.crosstunnel = false;
                            pressPanelMoveType = -1;
                            ma.repaintAsync();
                        }
                    }
                }
                if (rep)
                    repaintAsync();
            }
        }
    };

    private int hPageCountSize;

    protected boolean mousePressed;

    protected FloatPoint selectionPressedPosition;

    public ArrowTunnelDialog getTunnelDialog() {
        if (tunnelDialog == null) {
            tunnelDialog = new ArrowTunnelDialog(this);
        }
        return tunnelDialog;
    }

    protected void syncMouseDragged(MouseEvent e) {
        synchronized (mouseLock) {

            int x = e.getX();
            int y = e.getY();

            if (drawPin != null) {
                if (x < 0)
                    x = 0;
                if (y < 0)
                    y = 0;
                if (x >= getWidth())
                    x = getWidth() - 1;
                if (y >= getHeight())
                    y = getHeight() - 1;
                drawPin.move(getDoubleOrdinate(x), getDoubleOrdinate(y));
                repaintAsync();
            } else if (pressedPanel != null) {
                if (mouseSelection == null
                        || !(mouseSelection.contains(pressedPanel)))
                    try {
                        pressedPanel.mouseDragged(pressedPanel
                                .convertPoint(toPoint(x, y)));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                else {
                    FRectangle.disableTransform = true;
                    for (MovingText text : panels) {
                        if (mouseSelection.contains(text)) {
                            try {
                                text.xt = pressedPanel.xt;
                                text.yt = pressedPanel.yt;
                                text.mouseDragged(text.convertPoint(toPoint(x,
                                        y).transform(NET_LENGTH)));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    FRectangle.disableTransform = false;
                }
            } else if (mouseSelection != null) {
                FRectangle.disableTransform = true;
                mouseSelection.move(x, y, MovingArea.this);
                repaintAsync();
                FRectangle.disableTransform = false;
            }

        }
    }

    /**
     * Повертає клас, який обробляє зміни секторів.
     *
     * @return Клас, який обробляє зміни секторів.
     */

    public SectorRefactor getRefactor() {
        return refactor;
    }

    /**
     * Визначає тип початкової чи кінцевої точки.
     *
     * @return PointRefactor.TYPE_START, якщо змінюється початкова точка, <br>
     * PointRefactor.TYPE_END, якщо змінюється кінцева точка.
     */

    public int getPointChangingType() {
        if (getState() == START_POINT_ADDING
                || getState() == START_POINT_CHANGING)
            return SectorRefactor.TYPE_START;
        return SectorRefactor.TYPE_END;
    }

    /**
     * Повертає поточний стан редагування.
     *
     * @return Поточний стан редагування.
     */

    public int getChangingState() {
        if (getState() == START_POINT_ADDING || getState() == END_POINT_ADDING
                || getState() == START_POINT_CHANGING
                || getState() == END_POINT_CHANGING)
            return ARROW_CHANGING_STATE;
        return getState();
    }

    /**
     * Повертає START_POINT_ADDING, якщо додається початкова точка.
     * END_POINT_ADDING, якщо додається кінцева точка.
     *
     * @return
     */

    public int getRealState() {
        return getState();
    }

    public MovingArea(DataPlugin dataPlugin) {
        this(dataPlugin, dataPlugin.getBaseFunction());
    }

    public MovingArea(DataPlugin dataPlugin, Function activeFunction) {
        super();
        this.dataPlugin = dataPlugin;
        this.activeFunction = activeFunction;
        if (activeFunction == null)
            return;
        String size = activeFunction.getPageSize();

        if (size != null)
            initSize(size);
    }

    public String getDiagramSize() {
        String size = activeFunction.getPageSize();
        if (size != null)
            return size;
        return "A4";
    }

    private void initSize(String size) {
        if (size == null)
            return;
        MOVING_AREA_HEIGHT = D_MOVING_AREA_HEIGHT;
        MOVING_AREA_WIDTH = D_MOVING_AREA_WIDTH;
        int cnt = 1;
        int i = size.indexOf('x');
        if (i >= 0) {
            cnt = Integer.parseInt(size.substring(i + 1));
        }

        if (size.startsWith("A3")) {
            MOVING_AREA_HEIGHT *= 2;
            MOVING_AREA_WIDTH *= 2 * cnt;
        } else
            MOVING_AREA_WIDTH *= cnt;

        hPageCountSize = cnt;

        CLIENT_HEIGHT = getWidth(MOVING_AREA_HEIGHT) - TOP_PART_A
                - BOTTOM_PART_A;
        CLIENT_WIDTH = getWidth(MOVING_AREA_WIDTH) - LEFT_PART - RIGHT_PART;
    }

    public void setDataPlugin(final DataPlugin dataPlugin) {
        this.dataPlugin = dataPlugin;
        refactor = new SectorRefactor(this);
    }

    public MovingArea(DataPlugin dataPlugin, IDEFPanel panel) {
        this(dataPlugin);
        this.panel = panel;

        addMouseListener(new MouseListener() {
            private double drawPinPosition;

            /**
             * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
             */
            public void mousePressed(final MouseEvent me) {
                mousePressed = true;

                synchronized (mouseLock) {
                    MovingText old = pressedPanel;
                    pressNew(me);
                    if (me.isShiftDown()) {
                        if (pressedPanel instanceof MovingPanel) {
                            if (mouseSelection == null
                                    || !mouseSelection.contains(pressedPanel)) {
                                if (old != null)
                                    addPressed(old);
                                addPressed(pressedPanel);
                            } else {
                                removePressed(pressedPanel);
                            }
                        }
                    }
                }

            }

            private void addPressed(MovingText pressedPanel) {
                if (mouseSelection == null) {
                    mouseSelection = new MouseSelection(-1, -1);
                    mouseSelection.dr = false;
                }

                if (!mouseSelection.contains(pressedPanel))
                    mouseSelection.add(pressedPanel);

            }

            private void removePressed(MovingText pressedPanel) {
                if (mouseSelection == null)
                    return;

                mouseSelection.remove(pressedPanel);

            }

            private void pressNew(final MouseEvent me) {
                Object cObject = activeObject;
                activeObject = null;
                try {
                    drawPin = getPin(me.getX(), me.getY());
                    if (drawPin != null) {
                        setActiveObject(drawPin, false);
                        drawPinPosition = drawPin.getOrdinate().getPosition();
                    }
                    if (me.getButton() == MouseEvent.BUTTON3)
                        showPopupMenu(getActiveObject(), me.getX(), me.getY());
                    if (MovingArea.this.panel.isReadOnly())
                        drawPin = null;

                    if (drawPin != null) {
                        mousePinWidthCtrl = me.isControlDown();
                        return;
                    }

                    final FloatPoint point = toPoint(me.getX(), me.getY());
                    for (int i = panels.length - 1; i >= 0; i--) {
                        final MovingText pPanel = panels[i];
                        if (pPanel.contain(point) && pPanel instanceof DFDSRole) {
                            if (mouseSelection != null
                                    && mouseSelection.contains(pPanel))
                                FRectangle.disableTransform = true;

                            pPanel.mousePressed(pPanel.convertPoint(point));
                            if (pressedPanel != null)
                                pressedPanel.focusLost();
                            pressedPanel = pPanel;
                            pressedPanel.focusGained(false);
                            if (me.getButton() == MouseEvent.BUTTON3)
                                showPopupMenu(getActiveObject(), me.getX(),
                                        me.getY());
                            else if (pressedPanel instanceof MovingFunction) {
                                if (me.isControlDown())
                                    down();
                            }
                            if (MovingArea.this.panel.isReadOnly())
                                pressedPanel = null;
                            else
                                selectionPressed(me);
                            FRectangle.disableTransform = false;
                            return;
                        }

                    }

                    for (int i = panels.length - 1; i >= 0; i--) {
                        final MovingText pPanel = panels[i];
                        if (pPanel.contain(point)
                                && (!(pPanel instanceof DFDSRole))) {
                            if (mouseSelection != null
                                    && mouseSelection.contains(pPanel))
                                FRectangle.disableTransform = true;
                            pPanel.mousePressed(pPanel.convertPoint(point));
                            if (pressedPanel != null)
                                pressedPanel.focusLost();
                            pressedPanel = pPanel;
                            pressedPanel.focusGained(false);
                            if (me.getButton() == MouseEvent.BUTTON3)
                                showPopupMenu(getActiveObject(), me.getX(),
                                        me.getY());
                            else if (pressedPanel instanceof MovingFunction) {
                                if (me.isControlDown())
                                    down();
                            }
                            if (MovingArea.this.panel.isReadOnly())
                                pressedPanel = null;
                            else
                                selectionPressed(me);
                            FRectangle.disableTransform = false;
                            return;
                        }

                    }

                    if (pressedPanel != null) {
                        pressedPanel.focusLost();
                        pressedPanel = null;
                    }

                    if (me.isControlDown()) {
                        up();
                        return;
                    }

                    if (pressedPanel == null && drawPin == null) {
                        MovingArea.this.mouseSelection = new MouseSelection(
                                me.getX(), me.getY());
                        return;
                    }

                } finally {
                    if (activeObject == null) {
                        activeObject = cObject;
                        setActiveObject(null, false);
                    }
                }
            }

            private void selectionPressed(final MouseEvent me) {
                if (mouseSelection != null
                        && pressedPanel instanceof MovingPanel
                        && mouseSelection.contains(pressedPanel)) {
                    FRectangle.disableTransform = true;

                    selectionPressedPosition = new FloatPoint(
                            FRectangle.transform(
                                    getDoubleOrdinate(me.getPoint().x),
                                    NET_LENGTH), FRectangle.transform(
                            getDoubleOrdinate(me.getPoint().y),
                            NET_LENGTH));

                    pressedPanel.mousePressed(pressedPanel
                            .convertPoint(toPoint(me.getX(), me.getY())));
                    int state = pressedPanel.getState();
                    for (MovingText text : panels)
                        if (text != pressedPanel) {
                            text.mousePressed(text.convertPoint(toPoint(
                                    me.getX(), me.getY())));
                            text.setState(state);
                        }
                    FRectangle.disableTransform = false;
                }
            }

            private void clickNew(final MouseEvent me) {

                MouseEvent arg0 = me;

                if ((MovingArea.this.panel != null)
                        && (MovingArea.this.panel.isReadOnly()))
                    return;

                if (arg0.getClickCount() > 1) {
                    if ((activeObject instanceof MovingText)
                            && (!(activeObject instanceof MovingLabel))) {
                        if (Options.getString("ON_DOUBLE_CLICK", "EDIT")
                                .equals("EDIT"))
                            editActive();
                        else
                            renameObject();
                    } else {
                        if (activeObject instanceof PaintSector.Pin) {
                            Object fix = getPin(me.getX(), me.getY());
                            if (fix != null)
                                activeObject = fix;
                        }
                        editActive();
                    }
                    return;
                }

                int oldL = panels.length;

                processMouse(arg0);

                if (getPin(me.getX(), me.getY()) != null)
                    return;

                if (oldL != panels.length)
                    return;

                // if(arg0.getButton()!=MouseEvent.BUTTON3){
                final FloatPoint fPoint = toPoint(arg0.getX(), arg0.getY());
                for (int i = panels.length - 1; i >= 0; i--) {
                    final MovingText pressedPanel = panels[i];
                    if (pressedPanel instanceof DFDSRole)
                        if (pressedPanel.contain(fPoint)) {
                            pressedPanel.mouseClicked(pressedPanel
                                    .convertPoint(fPoint));
                            return;
                        }

                }
                for (int i = panels.length - 1; i >= 0; i--) {
                    final MovingText pressedPanel = panels[i];
                    if (!(pressedPanel instanceof DFDSRole))
                        if (pressedPanel.contain(fPoint)) {
                            pressedPanel.mouseClicked(pressedPanel
                                    .convertPoint(fPoint));
                            return;
                        }

                }
                if (arg0.getButton() == MouseEvent.BUTTON3)
                    cancelAdding();
            }

            /**
             * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
             */
            public void mouseReleased(final MouseEvent me) {
                mousePressed = false;

                synchronized (mouseLock) {

                    if (mouseSelection != null) {
                        if (mouseSelection.dr) {
                            mouseSelection.dr = false;
                            mouseSelection.addSectors(MovingArea.this);
                            repaintAsync();
                        }
                    }

                    if (drawPin != null) {
                        if (drawPin.getSector().tryRemovePin(MovingArea.this)) {
                            mousePin = null;
                        }
                        double position = -1d;
                        position = drawPin.getOrdinate().getPosition();

                        if (position != drawPinPosition) {
                            List<PaintSector> srs = new ArrayList<PaintSector>();

                            for (Point point : drawPin.getOrdinate()
                                    .getPoints())
                                if (!srs.contains(point.getSector()))
                                    srs.add(point.getSector());
                            if (!srs.contains(drawPin.getSector()))
                                srs.add(drawPin.getSector());
                            startUserTransaction();
                            drawPin.onEndMove();
                            for (PaintSector ps : srs)
                                PaintSector.save(ps, new MemoryData(),
                                        getDataPlugin().getEngine());
                            refactor.setUndoPoint();
                        }
                    }

                    drawPin = null;
                    if (pressedPanel != null
                            && me.getButton() != MouseEvent.BUTTON3) {
                        if (mouseSelection != null
                                && mouseSelection.contains(pressedPanel)) {
                            FRectangle.disableTransform = true;
                            startUserTransaction();
                            FloatPoint fp;
                            if (selectionPressedPosition == null)
                                fp = new FloatPoint(0, 0);
                            else
                                fp = new FloatPoint(FRectangle.transform(
                                        getDoubleOrdinate(me.getPoint().x),
                                        NET_LENGTH), FRectangle.transform(
                                        getDoubleOrdinate(me.getPoint().y),
                                        NET_LENGTH))
                                        .minus(selectionPressedPosition);

                            mouseSelection.onProcessEndBoundsChange(
                                    MovingArea.this, panels, fp);
                            refactor.setUndoPoint();
                            FRectangle.disableTransform = false;
                        } else
                            pressedPanel
                                    .mouseReleased(pressedPanel
                                            .convertPoint(toPoint(me.getX(),
                                                    me.getY())));
                    }
                }
                getPanel().getFrame().refreshActions(null);
                bImage = null;
            }

            /**
             * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
             */
            public void mouseClicked(final MouseEvent arg0) {
                clickNew(arg0);
                if (arg0.isShiftDown()) {

                } else {
                    if (mouseSelection != null) {
                        mouseSelection = null;
                        repaintAsync();
                    }
                }
            }

            /*
			 * (non-Javadoc)
			 *
			 * @see
			 * java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent
			 * )
			 */
            public void mouseExited(final MouseEvent arg0) {
                if (enteredPanel != null) {
                    enteredPanel.mouseLeave();
                    enteredPanel = null;
                }
                borderType = -1;
                repaintAsync();
            }

            public void mouseEntered(final MouseEvent arg0) {

            }
        });

        addMouseMotionListener(moveMoveListener);
        this.setLayout(null);
    }

    /**
     * Метод показує контекстне меню активного об’єкта.
     */

    protected void showPopupMenu(final Object activeObject, final int x,
                                 final int y) {
        if (activeObject == null)
            return;
        if (activeObject instanceof MovingFunction) {
            panel.setFunctionMenu(((MovingFunction) activeObject).getFunction());
            panel.getFrame().refreshActions(panel);
            panel.getFunctionPopupMenu().show(this, x, y);
        } else if (activeObject instanceof PaintSector.Pin) {
            panel.getFrame().refreshActions(panel);
            panel.getArrowPopupMenu().show(this, x, y);
        } else if (activeObject instanceof DFDSRole) {
            panel.getFrame().refreshActions(panel);
            panel.getDFDSRolePopupMenu().show(this, x, y);
        } else if (activeObject instanceof MovingText) {
            panel.getFrame().refreshActions(panel);
            panel.getTextPopupMenu().show(this, x, y);
        }
    }

    private boolean isOut(final double pos, final double startW, final double bW) {
        return !(pos > startW && bW > pos - startW);
    }

    /**
     * Медод, який обробляє натискання на кнопці миші, якщо стан панелі
     * відображення змінює певні атрибути, то вони змінюються.
     *
     * @param e Пораметри натискання миші.
     */

    protected void processMouse(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3)
            return;
        final int x = e.getX();
        final int y = e.getY();
        final double dX = getDoubleOrdinate(x);
        final double dY = getDoubleOrdinate(y);
        int type = Function.TYPE_OPERATION;
        if (getState() == FUNCTION_ADDING_STATE && getActiveObject() == null) {
            if (isOut(dX, LEFT_PART, CLIENT_WIDTH)
                    || isOut(dY, TOP_PART, CLIENT_HEIGHT))
                return;
            if (!canAddOnContext()) {
                JOptionPane.showMessageDialog(this, ResourceLoader
                        .getString("you_can_not_add_more_then_one_f"));
                return;
            }

            addInOutObject(dX, dY, type);
        } else if (getState() == EXTERNAL_REFERENCE_ADDING_STATE
                && getActiveObject() == null) {
            if (isOut(dX, LEFT_PART, CLIENT_WIDTH)
                    || isOut(dY, TOP_PART, CLIENT_HEIGHT))
                return;
            addInOutObject(dX, dY, Function.TYPE_EXTERNAL_REFERENCE);
        } else if (getState() == DATA_STORE_ADDING_STATE
                && getActiveObject() == null) {
            if (isOut(dX, LEFT_PART, CLIENT_WIDTH)
                    || isOut(dY, TOP_PART, CLIENT_HEIGHT))
                return;
            addInOutObject(dX, dY, Function.TYPE_DATA_STORE);
        } else if (getState() == DFDS_ROLE_ADDING_STATE
                && getActiveObject() == null) {
            if (isOut(dX, LEFT_PART, CLIENT_WIDTH)
                    || isOut(dY, TOP_PART, CLIENT_HEIGHT))
                return;
            addInOutObject(dX, dY, Function.TYPE_DFDS_ROLE);
        } else if (getChangingState() == ARROW_CHANGING_STATE) {
            final PaintSector.Pin activePin = getPin(e.getX(), e.getY());
            if (activePin == null)
                addArrowToArea(dX, dY);
            else {
                PaintSector sector = activePin.getSector();
                if ((sector.isPart())
                        && (sector.isSelEnd() || sector.isSelStart())) {
                    if (activeObject instanceof PaintSector.Pin) {
                        final PaintSector.Pin pin = (PaintSector.Pin) activeObject;
                        if (pin.getSector().isSelEnd()
                                || pin.getSector().isSelStart()) {
                            setActiveObject(null);
                            refactor.setSector(pin.getSector());
                            if (pin.getSector().isSelEnd()) {
                                setState(END_POINT_CHANGING_ADD);
                            } else if (pin.getSector().isSelStart()) {
                                setState(START_POINT_CHANGING_ADD);
                            }
                            panel.getFramework().put(CURRENT_MOVING_AREA, this);
                        }
                    }
                } else {
                    if (getPointChangingType() == SectorRefactor.TYPE_START) {
                        if (sector.isPart()
                                && activePin.getSector().getStart() == null)
                            return;
                    } else {
                        if (activePin.getSector().isPart()
                                && activePin.getSector().getEnd() == null)
                            return;
                    }
                    final SectorRefactor.PerspectivePoint pp = new SectorRefactor.PerspectivePoint();
                    pp.type = getPointChangingType();
                    pp.pin = activePin;
                    pp.x = getDoubleOrdinate(e.getX());
                    pp.y = getDoubleOrdinate(e.getY());
                    refactor.setPoint(pp);
                    doSector();
                }
            }
        } else if (getChangingState() == TILDA_ADDING_STATE) {
            final PaintSector.Pin activePin = getPin(e.getX(), e.getY());
            if (activePin != null) {
                startUserTransaction();
                List<PaintSector> ps = activePin.getSector().setTildaPos(
                        getDoubleOrdinate(e.getX()),
                        getDoubleOrdinate(e.getY()));
                for (PaintSector s : ps)
                    PaintSector.save(s, new MemoryData(),
                            dataPlugin.getEngine());
                refactor.setUndoPoint();
            }
        } else if (getChangingState() == TEXT_ADDING_STATE) {
            final MovingText text = createText();
            cancelAdding();
            refactor.addText(text);
            text.setBounds(dX, dY, text.getBounds().getWidth(), text
                    .getBounds().getHeight());

            text.setFont(Options.getFont("DEFAULT_TEXT_FONT", new Font("Arial",
                    0, 10)));

            text.setColor(Options.getColor("DEFAULT_TEXT_COLOR", Color.black));

            startUserTransaction();
            refactor.setUndoPoint();

        } else {
            if (activeObject instanceof PaintSector.Pin) {
                final PaintSector.Pin pin = (PaintSector.Pin) activeObject;
                if (pin.getSector().isSelEnd() || pin.getSector().isSelStart()) {
                    setActiveObject(null);
                    refactor.setSector(pin.getSector());
                    if (pin.getSector().isSelEnd()) {
                        setState(END_POINT_CHANGING);
                    } else if (pin.getSector().isSelStart()) {
                        setState(START_POINT_CHANGING);
                    }
                    panel.getFramework().put(CURRENT_MOVING_AREA, this);
                }
            }
        }
    }

    private boolean canAddOnContext() {
        if (activeFunction.getDecompositionType() == DIAGRAM_TYPE_DFD
                || activeFunction.getDecompositionType() == DIAGRAM_TYPE_DFDS)
            return true;
        if (getActiveFunction() == dataPlugin.getBaseFunction()) {
            Function function = getActiveFunction();
            int cc = function.getChildCount();
            if (cc == 0)
                return true;
            for (int i = 0; i < cc; i++) {
                Function c = (Function) function.getChildAt(i);
                if (c.getType() < Function.TYPE_EXTERNAL_REFERENCE)
                    return false;
            }
        }
        return true;
    }

    private void addInOutObject(final double dX, final double dY, int type) {
        startUserTransaction();
        createFunctionalObject(dX, dY, type, activeFunction);
        panel.getFrame().propertyChange(
                MChangeListener.RELOAD_FUNCTION_IN_TREE, activeFunction);

        commitUserTransaction();
    }

    public Function createFunctionalObject(final double dX, final double dY,
                                           int type, Function activeFunction) {
        final Function function = (Function) dataPlugin.createFunction(
                activeFunction, type);
        function.setBounds(new FRectangle(dX, dY, function.getBounds()
                .getWidth(), function.getBounds().getHeight()));

        function.setCreateDate(new Date());
        function.setSystemRevDate(new Date());

        if (type == Function.TYPE_DATA_STORE) {
            FRectangle rect = function.getBounds();
            rect.setHeight(rect.getHeight() / 2);
            function.setBounds(rect);
            function.setFont(Options.getFont("DEFAULT_DATA_STORE_FONT",
                    new Font("Dialog", Font.BOLD, 8)));
            function.setBackground(Options.getColor("DEFAULT_DATA_STORE_COLOR",
                    Color.white));
            function.setForeground(Options.getColor(
                    "DEFAULT_DATA_STORE_TEXT_COLOR", Color.black));
        } else if (type == Function.TYPE_EXTERNAL_REFERENCE) {
            FRectangle rect = function.getBounds();
            rect.setHeight(12);
            rect.setWidth(20);
            function.setBounds(rect);
            function.setFont(Options.getFont("DEFAULT_EXTERNAL_REFERENCE_FONT",
                    new Font("Dialog", Font.BOLD, 8)));
            function.setBackground(Options.getColor("DEFAULT_EXTERNAL_COLOR",
                    Color.white));
            function.setForeground(Options.getColor(
                    "DEFAULT_EXTERNAL_TEXT_COLOR", Color.black));
        } else if (type == Function.TYPE_DFDS_ROLE) {
            FRectangle rect = function.getBounds();
            rect.setHeight(Options.getInteger("DEFAULT_DFDS_ROLE_HEIGHT", 12));
            rect.setWidth(Options.getInteger("DEFAULT_DFDS_ROLE_WIDTH", 20));
            function.setBounds(rect);
            function.setFont(Options.getFont("DEFAULT_DFDS_ROLE_FONT",
                    new Font("Dialog", Font.BOLD, 8)));
            function.setBackground(Options.getColor("DEFAULT_DFDS_ROLE_COLOR",
                    Color.white));
            function.setForeground(Options.getColor(
                    "DEFAULT_DFDS_ROLE_TEXT_COLOR", Color.black));
        } else {
            function.setFont(Options.getFont("DEFAULT_FUNCTIONAL_BLOCK_FONT",
                    new Font("Dialog", 0, 12)));
            function.setBackground(Options.getColor(
                    "DEFAULD_FUNCTIONAL_BLOCK_COLOR", Color.white));

            function.setForeground(Options.getColor(
                    "DEFAULD_FUNCTIONAL_BLOCK_TEXT_COLOR", Color.black));
        }

        return function;
    }

    public void commitUserTransaction() {
        Engine e = dataPlugin.getEngine();
        if (e instanceof Journaled)
            ((Journaled) e).commitUserTransaction();

    }

    public void startUserTransaction() {
        Engine e = dataPlugin.getEngine();
        if (e instanceof Journaled) {
            if (!((Journaled) e).isUserTransactionStarted())
                ((Journaled) e).startUserTransaction();
        }
    }

    public boolean isUserTransactionStarted() {
        Engine e = dataPlugin.getEngine();
        if (e instanceof Journaled)
            return ((Journaled) e).isUserTransactionStarted();
        return false;
    }

    /**
     * Метод створює новий елемент відображення тексту на діаграмі, текст
     * нікусти не поміщається, а просто створюється.
     *
     * @return Новостворений об’єкт класа MovingText.
     */

    public MovingText createText() {
        return new MovingText(this) {
            @Override
            public void focusGained(boolean silent) {
                super.focusGained(silent);
                setActiveObject(this);
            }

            @Override
            public void focusLost() {
                super.focusLost();
                if (getActiveObject() == this)
                    setActiveObject(null);
            }

            @Override
            public void onEndBoundsChange() {
                super.onEndBoundsChange();
                startUserTransaction();
                refactor.setUndoPoint();
            }

        };
    }

    /**
     * Метод додає сектор, який починається або закінчується на гриниці активної
     * функції (тієї діаграма якої показується).
     */

    private void addArrowToArea(final double x, final double y) {
        final int type = getBorderType();
        if (type == -1)
            return;

        final SectorRefactor.PerspectivePoint pp = new SectorRefactor.PerspectivePoint();
        pp.x = x;
        pp.y = y;
        pp.borderType = type;
        pp.type = getPointChangingType();
        refactor.setPoint(pp);
        doSector();
    }

    private boolean rec = false;

    private boolean showNet = false;

    private Hashtable<Function, IDEF0Object> movingFunctions = new Hashtable<Function, IDEF0Object>();

    private Object prevActiveObject;

    private Object paintLock = new Object();

    private boolean painted = true;

    private int activeMovingTextIndex;

    private BufferedImage bImage;

    /**
     * Перезавантажує візуальні дані з активної функції.
     */

    public void reloadFunction() {
        refactor.loadFromFunction(activeFunction, false);
        setPanels();
        repaintAsync();
    }

    /**
     * Задає значення активного на блоку (того який являється базовим для
     * решти).
     *
     * @param activeFunction Значення активної функції.
     */

    public void setActiveFunction(final Function activeFunction) {
        synchronized (backgroundPaintlock) {
            bImage = null;
        }

        String size = activeFunction.getPageSize();

        initSize(size);

        if (panel != null) {
            cancelAdding();
            panel.setMovingAreaSize(zoom);
        }
        if (activeFunction.getType() >= Function.TYPE_EXTERNAL_REFERENCE)
            return;

        textPaintCache.clear();

        if (activeFunction.getDecompositionType() == DIAGRAM_TYPE_DFDS) {
            DropTarget dropTarget = new DropTarget();
            this.setDropTarget(dropTarget);
            try {
                dropTarget.addDropTargetListener(new DropTargetAdapter() {

                    @Override
                    public void drop(DropTargetDropEvent event) {
                        if (event.getTransferable().isDataFlavorSupported(
                                RowTreeTable.rowsListFlavor)) {
                            try {
                                event.acceptDrop(event.getSourceActions());
                                Rows rows = (Rows) event.getTransferable()
                                        .getTransferData(
                                                RowTreeTable.rowsListFlavor);
                                FloatPoint point = toPoint(
                                        event.getLocation().x,
                                        event.getLocation().y);
                                Function owner = null;
                                for (MovingPanel panel : panels)
                                    if (panel instanceof DFDSFunction
                                            && panel.contain(point)) {
                                        owner = ((DFDSFunction) panel)
                                                .getFunction();
                                    }

                                startUserTransaction();
                                Function function = createFunctionalObject(
                                        getDoubleOrdinate(event.getLocation()
                                                .getX()),
                                        getDoubleOrdinate(event.getLocation()
                                                .getY()),
                                        Function.TYPE_DFDS_ROLE, activeFunction);

                                RectangleVisualOptions ops = IDEF0Plugin
                                        .getDefaultRectangleVisualOptions(
                                                dataPlugin.getEngine(), rows
                                                        .get(0).getElement());
                                if (ops != null) {
                                    FRectangle rect = function.getBounds();
                                    rect.setHeight(ops.bounds.getHeight());
                                    rect.setWidth(ops.bounds.getWidth());
                                    function.setBounds(rect);
                                    function.setFont(ops.font);
                                    function.setBackground(ops.background);
                                    function.setForeground(ops.foreground);
                                }

                                Stream stream = (Stream) dataPlugin.createRow(
                                        dataPlugin.getBaseStream(), true);
                                Row[] rows2 = new Row[rows.size()];
                                for (int i = 0; i < rows2.length; i++)
                                    rows2[i] = dataPlugin
                                            .findRowByGlobalId(rows.get(i)
                                                    .getElementId());
                                stream.setRows(rows2);
                                function.setLink(stream.getElement().getId());
                                if (owner != null) {
                                    function.setOwner(owner);
                                    DFDSFunction function2 = findDFDSFunction(owner);
                                    List<Function> roles = getRoles(function2);
                                    if (!roles.contains(function))
                                        roles.add(function);
                                    if (function2 != null)
                                        function2.justifyRoles(roles);
                                }
                                commitUserTransaction();
                                panel.getFrame()
                                        .propertyChange(
                                                MChangeListener.RELOAD_FUNCTION_IN_TREE,
                                                activeFunction);
                            } catch (UnsupportedFlavorException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else
                            event.rejectDrop();
                    }
                });
            } catch (TooManyListenersException e) {
                e.printStackTrace();
            }
        } else
            this.setDropTarget(null);

        if (rec)
            return;
        rec = true;
        if (panel != null) {
            boolean locked;
            if (activeFunction.equals(lockedFunction))
                locked = true;
            else
                locked = activeFunction.lock();
            refactor.loadFromFunction(activeFunction, false);
            if (!activeFunction.equals(lockedFunction)) {
                if (lockedFunction != null)
                    lockedFunction.unlock();
                if (locked)
                    lockedFunction = activeFunction;
                else
                    lockedFunction = null;
            }
            panel.setMovingActiveFunction(activeFunction);
            if (getState() == END_POINT_ADDING)
                setState(START_POINT_ADDING);
            if ((getState() == END_POINT_CHANGING)
                    || (getState() == START_POINT_CHANGING)
                    || (getState() == FUNCTION_ADDING_STATE)
                    || (getState() == TEXT_ADDING_STATE))
                cancelAdding();
        } else
            refactor.loadFromFunction(activeFunction, false);

        boolean updateListeneres = this.activeFunction != activeFunction;

        this.activeFunction = activeFunction;

        if (panel != null) {
            panel.getFrame().propertyChange(
                    MChangeListener.REFRESH_FUNCTION_IN_TREE, activeFunction);
        }

        setPanels();
        if (panel != null) {
            setActiveObject(null);
            final java.awt.Point mp = getMousePosition();
            if (mp != null)
                moveMoveListener.mouseMoved(new MouseEvent(this, 0, 0, 0, mp.x,
                        mp.y, 0, false));
        }
        functionIndex = dataPlugin.indexOfFunction(activeFunction);
        if (functionIndex < 0)
            functionIndex = 0;
        rec = false;
        if (updateListeneres) {
            ActiveFunctionEvent event = new ActiveFunctionEvent(activeFunction);
            for (ActiveFunctionListener l : getActiveFunctionListeners())
                l.activeFunctionChanged(event);
        }
        if (panel != null) {
            panel.getFrame().refreshActions(panel);
        }
    }

    public void silentRefresh(Function function) {
        bImage = null;
        Object object = getActiveObject();
        Function function2 = null;
        if (object instanceof IDEF0Object) {
            function2 = ((IDEF0Object) object).getFunction();
        }
        activeFunction = function;
        refactor.loadFromFunction(getActiveFunction(), false);
        panel.setMovingActiveFunction(function);
        setPanels();
        if (!(this.activeObject instanceof PaintSector.Pin))
            this.activeObject = null;
        this.pressedPanel = null;
        repaintAsync();
        if (function2 != null)
            for (MovingText text : panels) {
                if (text instanceof IDEF0Object) {
                    if (((IDEF0Object) text).getFunction().equals(function2)) {
                        this.pressedPanel = text;
                        text.focusGained(true);
                        setActiveObject(text, true);
                        break;
                    }
                }
            }
    }

    /**
     * Повертає значення базової на даний момент функції.
     *
     * @return
     */

    public Function getActiveFunction() {
        return activeFunction;
    }

    /**
     * Повертає активного на даний момент об’єкта.
     *
     * @return
     */

    public Object getActiveObject() {
        return activeObject;
    }

    /**
     * Задає значенн активного об’єкта на діаграмі.
     *
     * @param activeObject Активний об’єкт на діаграмі.
     */

    void setActiveObject(final Object activeObject) {
        setActiveObject(activeObject, true);
    }

    public void setActiveObject(final Object activeObject, boolean silent) {
        this.prevActiveObject = this.getActiveObject();

        if (activeObject instanceof MovingText)
            this.activeMovingTextIndex = refactor.getTexts().indexOf(
                    activeObject);
        else
            this.activeMovingTextIndex = -1;

        if (activeObject instanceof MovingFunction) {
            NFunction function = (NFunction) ((MovingFunction) activeObject)
                    .getFunction();
            if (!silent) {
                panel.getFramework().propertyChanged(Commands.ACTIVATE_ELEMENT,
                        function.getElement());
                long id = dataPlugin.getBaseFunctionQualifier()
                        .getAttributeForName();
                for (Attribute attr : dataPlugin.getBaseFunctionQualifier()
                        .getAttributes())
                    if (attr.getId() == id) {
                        panel.getFramework().propertyChanged(
                                Commands.ACTIVATE_ATTRIBUTE,
                                new AttributeEditorView.ElementAttribute(
                                        function.getElement(), attr));
                    }
            }
            Enumeration e = ((MovingFunction) activeObject)
                    .getFunction().children();
            while (e.hasMoreElements())
                ((Function)e.nextElement()).getSectors();
        }
        final Object o = this.activeObject;
        if ((activeObject == null) && (!silent)) {
            panel.getFramework().propertyChanged(Commands.ACTIVATE_ATTRIBUTE,
                    null);
        }
        this.activeObject = activeObject;
        if (o != activeObject)
            panel.getFrame().refreshActions(panel);
        if (activeObject instanceof PaintSector.Pin) {
            final PaintSector.Pin pin = (PaintSector.Pin) activeObject;
            panel.getFrame().getJCheckBoxMenuItemShowTilda()
                    .setSelected(pin.getSector().isShowTilda());
            panel.getFrame().getJCheckBoxMenuItemTransparentText()
                    .setSelected(pin.getSector().isTransparent());
            if (!silent) {
                panel.getFramework().propertyChanged(Commands.ACTIVATE_ELEMENT,
                        ((NSector) pin.getSector().getSector()).getElement(),
                        pin);
                panel.getFramework().propertyChanged(
                        Commands.ACTIVATE_ATTRIBUTE,
                        new AttributeEditorView.ElementAttribute(((NSector) pin
                                .getSector().getSector()).getElement(),
                                IDEF0Plugin.getStreamAttribute(dataPlugin
                                        .getEngine())), pin);
            }
        }
        repaintAsync();
    }

    /**
     * Переводить число у внутрішніх одиницях в число пікселів з врахуванням
     * маштабу.
     *
     * @param d Число у внутрішніх одиницях.
     * @return Число переведене в пікселі.
     */

    public double getWidth(final double d) {
        return d * zoom;
    }

    /**
     * Переводить кількість пікселів у внутрішній формат, без врахування
     * маштабу.
     *
     * @param i Кількість пікселів.
     * @return Число у внутрішніх одиницях.
     */

    public static double getWidth(final int i) {
        return i;
    }

    /**
     * Видаляє з тексту зайві пробіли, переводить символ переносу на наступній
     * рядок в пробіл.
     *
     * @param name
     * @return
     */

    public static String trimText(String text) {
        String res = "";
        text = text.trim();
        for (int i = 0; i < text.length(); i++)
            if (text.charAt(i) == '\n')
                res += ' ';
            else
                res += text.charAt(i);
        return res;
    }

    /**
     * Мотод показує контекстну діаграму активного функціонального блоку, якщо
     * на поточній діаграмі всього один функціональний блок то показується його
     * контекстна діаграма.
     */

    public void down() {
        Function function = null;
        if (activeFunction.getRealChildCount() == 1)
            function = ((Function) activeFunction).getRealChildAt(0);
        else if (activeObject instanceof MovingFunction) {
            function = ((MovingFunction) activeObject).getFunction();
        }
        if ((function != null)
                && (function.getType() < Function.TYPE_EXTERNAL_REFERENCE)) {
            if (function.getRealChildCount() == 0) {
                if (panel.isReadOnly())
                    return;
                panel.getFramework().setOpenDynamikViewEvent(
                        panel.getView().getOpenAction());
                ContextMasterDialog contextMasterDialog = panel
                        .getContextMasterDialog();
                contextMasterDialog.setDecompositionType(activeFunction
                        .getDecompositionType());
                final Template model = contextMasterDialog.showModal();
                panel.getFramework().setOpenDynamikViewEvent(null);
                int type = contextMasterDialog.getDecompositionType();
                if (model != null) {
                    startUserTransaction();
                    function.setDecompositionType(type);
                    refactor.saveToFunction();
                    model.createChilds(function, dataPlugin);
                    setActiveFunction(function);
                    refactor.fixNoNameBug();
                    refactor.saveToFunction();
                    if (isUserTransactionStarted())
                        commitUserTransaction();
                }
            } else
                setActiveFunction(function);
            panel.getJPanel3().repaint();
        }
        pressedPanel = null;
    }

    /**
     * Показує контекстну діаграму батьківського до поточної контекстної
     * діаграми функціонального блоку, якщо батьківський функціональний блок
     * відсутній то метод нічого не робить.
     */

    public void up() {
        if (activeFunction.getParent() != null) {
            setActiveFunction((Function) activeFunction.getParent());
            panel.getJPanel3().repaint();
            if (isUserTransactionStarted())
                commitUserTransaction();
        }
    }

    public void setArrowState() {
        setState(ARROW_STATE);
        crosstunnel = false;
        // panel.cancelEditing();
    }

    public void setArrowAddingState() {
        setState(START_POINT_ADDING);
        refactor.setSector(null);
    }

    public void setFunctionAddingState() {
        setState(FUNCTION_ADDING_STATE);
    }

    public void setTildaAddingState() {
        setState(TILDA_ADDING_STATE);
    }

    public void setTextAddingState() {
        setState(TEXT_ADDING_STATE);
    }

    private void removeAddedSectors(final Function f) {
        final Vector<PaintSector> v = new Vector<PaintSector>();
        for (int i = 0; i < refactor.getSectorsCount(); i++) {
            final PaintSector s = refactor.getSector(i);
            if (f.equals(s.getSector().getStart().getFunction()))
                if (v.indexOf(s) < 0)
                    v.add(s);
            if (f.equals(s.getSector().getEnd().getFunction()))
                if (v.indexOf(s) < 0)
                    v.add(s);
        }
        for (int i = 0; i < v.size(); i++)
            v.get(i).remove();
    }

    /**
     * Метод видаляє активний об’єкт.
     */

    public boolean removeActiveObject() {
        if (activeObject instanceof IDEF0Object) {
            Function f = ((IDEF0Object) activeObject).getFunction();
            if (f.isRemoveable()) {
                startUserTransaction();
                if (mouseSelection != null
                        && mouseSelection.contains((IDEF0Object) activeObject)) {
                    for (Function function : mouseSelection.getFunctions()) {
                        f = function;
                        removeAddedSectors(f);
                        if (dataPlugin.removeRow(f)) {
                            panel.getFrame().propertyChange(
                                    MChangeListener.RELOAD_FUNCTION_IN_TREE,
                                    dataPlugin.getBaseFunction());
                            panel.getFrame().refreshActions(panel);
                        }
                    }
                    mouseSelection = null;
                    setPanels();

                    refactor.setUndoPoint();

                    return true;
                }

                removeAddedSectors(f);
                if (dataPlugin.removeRow(f)) {
                    setPanels();
                    panel.getFrame().propertyChange(
                            MChangeListener.RELOAD_FUNCTION_IN_TREE,
                            dataPlugin.getBaseFunction());
                    panel.getFrame().refreshActions(panel);
                    refactor.setUndoPoint();
                    return true;
                }
                refactor.setUndoPoint();
            }
            return false;
        } else if (activeObject instanceof PaintSector.Pin) {
            startUserTransaction();
            PaintSector sector = ((PaintSector.Pin) activeObject).getSector();
            sector.remove();
            refactor.setUndoPointSaveAll();
            return true;
        } else if (activeObject instanceof MovingText) {
            startUserTransaction();
            refactor.removeText((MovingText) activeObject);
            refactor.setUndoPoint();
            return true;
        } else {
            if (mouseSelection != null
                    && mouseSelection.getFunctions().size() > 0
                    && mouseSelection.isRemoveable()) {
                startUserTransaction();
                for (Function function : mouseSelection.getFunctions()) {
                    removeAddedSectors(function);
                    if (dataPlugin.removeRow(function)) {
                        panel.getFrame().propertyChange(
                                MChangeListener.RELOAD_FUNCTION_IN_TREE,
                                dataPlugin.getBaseFunction());
                        panel.getFrame().refreshActions(panel);
                    }
                }
                mouseSelection = null;
                setPanels();

                refactor.setUndoPoint();

                return true;
            }
        }
        return false;
    }

    /**
     * Метод змінює батьківський елемент для активного функціонального блоку.
     *
     * @param newParent Навий батьківський елемент для функціонального блоку.
     */

    public void moveFunction(final Function newParent) {
        final MovingFunction movingFunction = (MovingFunction) getActiveObject();
        final Function f = movingFunction.getFunction();
        removeAddedSectors(f);
        f.setParentRow(newParent);
        setPanels();
        panel.getFrame().propertyChange(
                MChangeListener.RELOAD_FUNCTION_IN_TREE,
                dataPlugin.getBaseFunction());
        repaintAsync();
    }

    public void repaintAsync() {
        synchronized (paintLock) {
            if (!painted)
                return;
            painted = false;
            repaint();
        }
    }

    private IDEF0Object addIDEF0Object(final Function function) {
        IDEF0Object movingFunction;
        if (function.getType() < Function.TYPE_EXTERNAL_REFERENCE) {
            if (DIAGRAM_TYPE_DFD == activeFunction.getDecompositionType())
                movingFunction = new DFDFunction(this, function) {
                    /**
                     * @see com.ramussoft.pb.idef.visual.MovingText#onFocusGained(java.awt.event.FocusEvent)
                     */
                    @Override
                    public void focusGained(boolean silent) {
                        super.focusGained(silent);
                        setActiveObject(this, silent);
                    }

                    @Override
                    public void focusLost() {
                        super.focusLost();
                        if (getActiveObject() == this)
                            setActiveObject(null);
                    }

                };
            else if (DIAGRAM_TYPE_DFDS == activeFunction.getDecompositionType())
                movingFunction = new DFDSFunction(this, function) {
                    /**
                     * @see com.ramussoft.pb.idef.visual.MovingText#onFocusGained(java.awt.event.FocusEvent)
                     */
                    @Override
                    public void focusGained(boolean silent) {
                        super.focusGained(silent);
                        setActiveObject(this, silent);
                    }

                    @Override
                    public void focusLost() {
                        super.focusLost();
                        if (getActiveObject() == this)
                            setActiveObject(null);
                    }

                };
            else
                movingFunction = new MovingFunction(this, function) {
                    /**
                     * @see com.ramussoft.pb.idef.visual.MovingText#onFocusGained(java.awt.event.FocusEvent)
                     */
                    @Override
                    public void focusGained(boolean silent) {
                        super.focusGained(silent);
                        setActiveObject(this, silent);
                    }

                    @Override
                    public void focusLost() {
                        super.focusLost();
                        if (getActiveObject() == this)
                            setActiveObject(null, false);
                    }

                };
        } else {
            if (function.getType() == Function.TYPE_EXTERNAL_REFERENCE) {
                movingFunction = new External(this, function) {
                    /**
                     * @see com.ramussoft.pb.idef.visual.MovingText#onFocusGained(java.awt.event.FocusEvent)
                     */
                    @Override
                    public void focusGained(boolean silent) {
                        super.focusGained(silent);
                        setActiveObject(this);
                    }

                    @Override
                    public void focusLost() {
                        super.focusLost();
                        if (getActiveObject() == this)
                            setActiveObject(null);
                    }

                };
            } else if (function.getType() == Function.TYPE_DFDS_ROLE) {
                movingFunction = new DFDSRole(this, function) {
                    /**
                     * @see com.ramussoft.pb.idef.visual.MovingText#onFocusGained(java.awt.event.FocusEvent)
                     */
                    @Override
                    public void focusGained(boolean silent) {
                        super.focusGained(silent);
                        setActiveObject(this, silent);
                    }

                    @Override
                    public void focusLost() {
                        super.focusLost();
                        if (getActiveObject() == this)
                            setActiveObject(null, false);
                    }

                };
            } else {// DATA STORE
                movingFunction = new DataStore(this, function) {
                    /**
                     * @see com.ramussoft.pb.idef.visual.MovingText#onFocusGained(java.awt.event.FocusEvent)
                     */
                    @Override
                    public void focusGained(boolean silent) {
                        super.focusGained(silent);
                        setActiveObject(this);
                    }

                    @Override
                    public void focusLost() {
                        super.focusLost();
                        if (getActiveObject() == this)
                            setActiveObject(null);
                    }

                };
            }
        }
        movingFunctions.put(function, movingFunction);
        return movingFunction;
    }

    /**
     * Якщо виділений якийсь об’єкт, тоді з’являється діалогове вікно для роботи
     * з його властивостями.
     */

    @SuppressWarnings("unused")
    public void editActive() {
        if ((Metadata.EDUCATIONAL) && (Math.random() < 0.2d)) {
            AttributePlugin plugin = panel.getFramework().findAttributePlugin(
                    new AttributeType("Core", "Table", false));
            if (!(plugin instanceof EmptyPlugin)) {
                System.exit(233);
            }

        }

        if ((activeObject instanceof MovingFunction)
                || (activeObject instanceof DFDFunction)) {
            final IDEF0Object movingFunction = (IDEF0Object) activeObject;
            // String text = movingFunction.getFunction().getName();
            panel.getFunctionOptions().showModal(movingFunction);
			/*
			 * if(!text.equals(movingFunction.getFunction().getName()))
			 * movingFunction.resetBounds();
			 */
        } else if (activeObject instanceof DFDSRole) {
            panel.getDFDSRoleOptionsDialog().showModal((DFDSRole) activeObject);
        } else if (activeObject instanceof DFDObject) {
            panel.getDFDObjectOptionsDialog().showModal(
                    (DFDObject) activeObject);
        } else if (activeObject instanceof MovingText)
            panel.getTextOptionsDialog().showModal((MovingText) activeObject);
        else if (activeObject instanceof PaintSector.Pin) {
            panel.getArrowOptionsDialog().showModal(
                    ((PaintSector.Pin) activeObject).getSector(), this);
            // setPanels();
        }
        repaintAsync();
    }

    /**
     * Медод відміняє редагування об’єктів в області редагування, також робить
     * виділеною відповідну кнопку на панелі.
     */

    public void cancelAdding() {
        setArrowState();
        panel.getFrame().findAction(MainFrame.CURSOR_TOOL)
                .putValue(Action.SELECTED_KEY, Boolean.TRUE);
        ((IDEF0TabView) panel.getView()).getHandAction().putValue(
                Action.SELECTED_KEY, Boolean.FALSE);
        repaintAsync();
    }

    /**
     * Робить так, що показується тільда на активному секторі.
     */
    public void setShowTilda() {
        startUserTransaction();
        final PaintSector sector = ((PaintSector.Pin) activeObject).getSector();
        sector.setShowTilda(!sector.isShowTilda());
        PaintSector.save(sector, new MemoryData(), getDataPlugin().getEngine());
        refactor.setUndoPoint();
    }

    /**
     * Оновлює відображення даних на активній функції.
     */

    public void refresh() {
        final Function activeFunction = dataPlugin.getBaseFunction();
        setActiveFunction(activeFunction);
        panel.getFrame().propertyChange(
                MChangeListener.RELOAD_FUNCTION_IN_TREE, activeFunction);
    }

    /**
     * Змінює прозорість напису для активного сектора.
     */

    public void setTransparent() {
        final PaintSector sector = ((PaintSector.Pin) activeObject).getSector();
        sector.setTransparent(!sector.isTransparent());
        startUserTransaction();
        PaintSector.save(sector, new MemoryData(), dataPlugin.getEngine());
        refactor.setUndoPoint();
    }

    /**
     * Показує діалог роботи з тунелями для активного сектора, і здійснює
     * відповідну роботу.
     */
    public void tunelArrow() {
        getTunnelDialog().showModal(
                ((PaintSector.Pin) activeObject).getSector());
    }

    /**
     * Задає параметри вигляду для дочірніх елементів, аналогічні активному
     * елементу.
     */
    public void setLookForChildrens() {
        startUserTransaction();
        if (getActiveObject() instanceof MovingFunction)
            ((MovingFunction) getActiveObject()).getFunction()
                    .setLookForChildrens();
        else
            ((PaintSector.Pin) getActiveObject()).getSector()
                    .setLookForChildrens();
        commitUserTransaction();
    }

    public void setLookForChildrensBackground() {
        startUserTransaction();
        if (getActiveObject() instanceof MovingFunction)
            ((MovingFunction) getActiveObject()).getFunction()
                    .setLookForChildrensBackground();
        else
            ((PaintSector.Pin) getActiveObject()).getSector()
                    .setLookForChildrens();
        commitUserTransaction();
    }

    public void setLookForChildrensForeground() {
        startUserTransaction();
        if (getActiveObject() instanceof MovingFunction)
            ((MovingFunction) getActiveObject()).getFunction()
                    .setLookForChildrensForeground();
        else
            ((PaintSector.Pin) getActiveObject()).getSector()
                    .setLookForChildrens();
        commitUserTransaction();
    }

    public void setLookForChildrensFont() {
        startUserTransaction();
        if (getActiveObject() instanceof MovingFunction)
            ((MovingFunction) getActiveObject()).getFunction()
                    .setLookForChildrensFont();
        else
            ((PaintSector.Pin) getActiveObject()).getSector()
                    .setLookForChildrens();
        commitUserTransaction();
    }

    /**
     * Переводить коортинати в пікселях у внутрішні координати, враховуючи
     * маштаб.
     *
     * @param i Координата в пікселях.
     * @return Координата у внутрішньому форматі.
     */

    public double getDoubleOrdinate(final double i) {
        return i / zoom;
    }

    /**
     * Метод, який переводить координату з внутрішнього формату в пікселі з
     * врахуванням маштабу.
     *
     * @param d Координата у внутрішньому форматі.
     * @return Координата в пікселях.
     */
    public int getIntOrdinate(final double d) {
        return (int) (d * zoom);
    }

    /**
     * Метод переводить в пікселі внутрішні координати. Відрізняється від методу
     * getIntOrdinate лише тим що результат не приводиться типу int.
     *
     * @param d Координата у внутрішньому форматі.
     * @return Координата в пікселях.
     */

    public double getIDoubleOrdinate(final double d) {
        return d * zoom;
    }

    /**
     * Метод переводить прямокутник з внутрішнього формату в прямокутник в
     * пікселях.
     *
     * @param bounds Прямокутник у внутрішньому форматі.
     * @return Прямокутник в пікселях.
     */

    public Rectangle2D getBounds(final FRectangle bounds) {
        return new Rectangle2D.Double(getIDoubleOrdinate(bounds.getX()),
                getIDoubleOrdinate(bounds.getY()),
                getIDoubleOrdinate(bounds.getWidth()),
                getIDoubleOrdinate(bounds.getHeight()));
    }

    /**
     * Переводить прямокутник з формату в пікселях в прямокутник з форматом у
     * внутрішніх координатах.
     *
     * @param bounds Прямокутник в піксельних координатах.
     * @return Прямокутник у внутрішньому форматі.
     */

    public FRectangle getFBounds(final Rectangle2D bounds) {
        return new FRectangle(getDoubleOrdinate(bounds.getX()),
                getDoubleOrdinate(bounds.getY()),
                getDoubleOrdinate(bounds.getWidth()),
                getDoubleOrdinate(bounds.getHeight()));
    }

    /**
     * Повертає частину сектора, на якій розташовані на координати (x, y).
     *
     * @param x Координата x.
     * @param y Координата y.
     * @return Частина сектора, на якій розташована точка з координатами (x, y).
     */

    private PaintSector.Pin getPin(final int x, final int y) {
        final double dx = getDoubleOrdinate(x);
        final double dy = getDoubleOrdinate(y);
        for (int i = 0; i < refactor.getSectorsCount(); i++) {
            final PaintSector.Pin tmp = refactor.getSector(i).isOnMe(dx, dy);
            if (tmp != null)
                return tmp;
        }
        return null;
    }

    /**
     * Медод переводить координати x, y з піксельного формату у внутрішній.
     *
     * @param x Координата x.
     * @param y Координата y.
     * @return Новостворену точку у внутрішніх координатах.
     */

    public FloatPoint toPoint(final int x, final int y) {
        return new FloatPoint(getDoubleOrdinate(x), getDoubleOrdinate(y));
    }

    /**
     * Повертає край, на якому знаходиться курсор.
     *
     * @return Край, на якому знаходиться курсор, якщо курсор не на краю, то
     * повертається -1.
     */

    private int getBorderType() {
        if (mp == null || getMousePin() != null)
            return -1;

        int state2 = getState();

        if ((state2 == END_POINT_ADDING || state2 == START_POINT_CHANGING || state2 == END_POINT_CHANGING)
                && (getCurrentMovingArea() != this))
            return -1;

        final double x = getDoubleOrdinate(mp.x);
        final double y = getDoubleOrdinate(mp.y);
        if (state2 == START_POINT_ADDING
                || state2 == START_POINT_CHANGING
                && refactor.getSector().getSector().getEnd().getBorderType() == -1) {
            if (x - LEFT_PART <= BORDER_WIDTH && x > LEFT_PART)
                return MovingPanel.LEFT;
            else if (y - TOP_PART <= BORDER_WIDTH && y > TOP_PART)
                return MovingPanel.TOP;
            else if (y >= getDoubleHeight() - BORDER_WIDTH - BOTTOM_PART
                    && y < getDoubleHeight() - BOTTOM_PART)
                return MovingPanel.BOTTOM;
            if (activeFunction.getDecompositionType() == DIAGRAM_TYPE_DFD
                    || activeFunction.getDecompositionType() == DIAGRAM_TYPE_DFDS) {
                if (x >= getDoubleWidth() - RIGHT_PART - BORDER_WIDTH
                        && y < getDoubleWidth() - RIGHT_PART)
                    return MovingPanel.RIGHT;
            }
        } else if (state2 == END_POINT_ADDING
                || state2 == END_POINT_CHANGING
                && refactor.getSector().getSector().getStart().getBorderType() == -1) {
            if (x >= getDoubleWidth() - RIGHT_PART - BORDER_WIDTH
                    && y < getDoubleWidth() - RIGHT_PART)
                return MovingPanel.RIGHT;
            if (activeFunction.getDecompositionType() == DIAGRAM_TYPE_DFD
                    || activeFunction.getDecompositionType() == DIAGRAM_TYPE_DFDS) {
                if (x - LEFT_PART <= BORDER_WIDTH && x > LEFT_PART)
                    return MovingPanel.LEFT;
                else if (y - TOP_PART <= BORDER_WIDTH && y > TOP_PART)
                    return MovingPanel.TOP;
                else if (y >= getDoubleHeight() - BORDER_WIDTH - BOTTOM_PART
                        && y < getDoubleHeight() - BOTTOM_PART)
                    return MovingPanel.BOTTOM;
            }
        }
        return -1;
    }

    /**
     * Медод ініцалізує масив з панелями для перетягування.
     */

    public void setPanels() {
        movingFunctions.clear();
        final Vector fs = dataPlugin.getChilds(activeFunction, true);
        final Vector<MovingText> v = new Vector<MovingText>();
        for (int i = 0; i < fs.size(); i++)
            v.add(addIDEF0Object((Function) fs.get(i)));
        for (int i = 0; i < refactor.getTextCount(); i++)
            v.add(refactor.getText(i));
        for (int i = 0; i < refactor.getSectorsCount(); i++) {
            final PaintSector sector = refactor.getSector(i);
            if (sector.getText() != null)
                v.add(sector.getText());
        }
        panels = new MovingText[v.size()];
        for (int i = 0; i < panels.length; i++)
            panels[i] = v.get(i);
    }

    int oldW = -1;
    int oldH = -1;

    private AlphaComposite a = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 0.5f);

    @Override
    public void paint(final Graphics gr) {
        synchronized (paintLock) {
            painted = true;
        }
        if (oldH != getHeight() || oldW != getWidth())
            synchronized (backgroundPaintlock) {
                if (bImage != null) {
                    oldH = getHeight();
                    oldW = getWidth();
                    bImage = null;
                }
            }
        Graphics2D g = (Graphics2D) gr;
        boolean initRepaint = false;
        synchronized (backgroundPaintlock) {
            if (bImage == null) {
                paintBackground(gr);
                if (!printing)
                    initRepaint = true;

            } else
                g.drawImage(bImage, 0, 0, null);
        }
        if (initRepaint && getComponentCount() == 0)
            init();

        if (!DISABLE_RENDERING_HINTS && !isPrinting()) {
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
        }

        if (enteredPanel != null && enteredPanel != pressedPanel)
            enteredPanel.paintBorder(g);

        Composite comp = g.getComposite();
        g.setComposite(a);

        if (mousePressed) {
            if (pressedPanel != null)
                pressedPanel.paint(g);
            if (activeObject instanceof Pin)
                try {
                    ((Pin) activeObject).getSector().paint(g, this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            if (mouseSelection != null) {
                for (MovingPanel panel : panels) {
                    if (mouseSelection.contains(panel))
                        panel.paint(g);
                }
            }
        }
        g.setComposite(comp);

        if (enteredPanel instanceof IDEF0Object) {
            g.setColor(Color.BLACK);
            ((IDEF0Object) enteredPanel).paintTringle(g);
        }

        if (mousePin != null) {
            mousePin.getSector().paint(g, this);
        }

        if (!printing) {

            final int borderWidth = getIntOrdinate(BORDER_WIDTH);
            switch (getBorderType()) {
                case MovingPanel.LEFT: {
                    g.fillRect(getIntOrdinate(LEFT_PART), getIntOrdinate(TOP_PART),
                            borderWidth, getIntOrdinate(CLIENT_HEIGHT));
                }
                break;
                case MovingPanel.TOP: {
                    g.fillRect(getIntOrdinate(LEFT_PART), getIntOrdinate(TOP_PART),
                            getIntOrdinate(CLIENT_WIDTH), borderWidth);
                }
                break;
                case MovingPanel.RIGHT: {
                    g.fillRect(getIntOrdinate(CLIENT_WIDTH + LEFT_PART)
                                    - borderWidth, getIntOrdinate(TOP_PART), borderWidth,
                            getHeight());
                }
                break;
                case MovingPanel.BOTTOM: {
                    g.fillRect(getIntOrdinate(LEFT_PART), getIntOrdinate(TOP_PART
                                    + CLIENT_HEIGHT)
                                    - borderWidth, getIntOrdinate(CLIENT_WIDTH),
                            borderWidth);
                }
                break;
            }

            if (getChangingState() == ARROW_CHANGING_STATE && mp != null
                    && getState() != START_POINT_ADDING) {
                if (getCurrentMovingArea() != this) {
                    if (pressPanelMoveType >= 0) {
                        int x1 = mp.x;
                        int y1 = mp.y;

                        switch (pressPanelMoveType) {
                            case MovingFunction.LEFT:
                                x1 = getIntOrdinate(0);
                                break;
                            case MovingFunction.RIGHT:
                                x1 = getIntOrdinate(CLIENT_WIDTH);
                                break;
                            case MovingFunction.TOP:
                                y1 = getIntOrdinate(0);
                                break;
                            case MovingFunction.BOTTOM:
                                y1 = getIntOrdinate(CLIENT_HEIGHT);
                                break;
                        }

                        final float d[] = {5, 10};
                        g.setColor(Color.black);
                        g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_ROUND, 10, d, 0));
                        g.drawLine(x1, y1, mp.x, mp.y);
                    }
                } else {

                    if (crosstunnel) {
                        final int x1 = getIntOrdinate(refactor.getX());
                        final int y1 = getIntOrdinate(refactor.getY());
                        final float d[] = {5, 10};
                        g.setColor(Color.black);
                        g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_ROUND, 10, d, 0));
                        if (getPointChangingType() == SectorRefactor.TYPE_END)
                            g.drawLine(x1, y1, getIntOrdinate(CLIENT_WIDTH), y1);
                        else
                            g.drawLine(x1, y1, getIntOrdinate(0), y1);
                    } else {

                        final int x1 = getIntOrdinate(refactor.getX());
                        final int y1 = getIntOrdinate(refactor.getY());
                        final float d[] = {5, 10};
                        g.setColor(Color.black);
                        g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_ROUND, 10, d, 0));
                        g.drawLine(x1, y1, mp.x, mp.y);
                    }
                }
            } else if ((getChangingState() == ARROW_STATE)
                    && (pressPanelMoveType >= 0)) {

                MovingArea ma = (MovingArea) panel.getFramework().get(
                        CURRENT_MOVING_AREA);
                if ((ma != null)
                        && (ma.getState() == ARROW_CHANGING_STATE)
                        && (ma.getPointChangingType() == SectorRefactor.TYPE_END)) {

                }
            }
        }

        if (mouseSelection != null) {
            g.setStroke(new BasicStroke(1.5f, 1, 1, 1, new float[]{1f, 5f},
                    0f));
            g.setColor(Color.DARK_GRAY);
            if (mouseSelection.dr) {
                if (mouseSelection.width >= 0 && mouseSelection.height >= 0)
                    g.draw(mouseSelection);
                else {
                    double x = mouseSelection.x;
                    double y = mouseSelection.y;
                    if (mouseSelection.width < 0)
                        x += mouseSelection.width;

                    if (mouseSelection.height < 0)
                        y += mouseSelection.height;
                    g.draw(new Rectangle2D.Double(x, y, Math
                            .abs(mouseSelection.width), Math
                            .abs(mouseSelection.height)));
                }
            }
            mouseSelection.paint(g, this);
        }
    }

    private void init() {
        synchronized (backgroundPaintlock) {
            if (bkRepaint)
                return;
            bkRepaint = true;
        }

        bkPainter.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    BufferedImage bi = getGraphicsConfiguration()
                            .createCompatibleImage(getWidth(), getHeight());
                    Graphics2D g2d = bi.createGraphics();
                    try {
                        paintBackground(g2d);
                    } finally {
                        g2d.dispose();
                    }
                    synchronized (backgroundPaintlock) {
                        bImage = bi;
                        bkRepaint = false;
                    }
                } catch (Exception e) {
                    synchronized (backgroundPaintlock) {
                        bkRepaint = false;
                    }
                }
            }
        });
    }

    public synchronized void paintBackground(final Graphics gr) {

        final Graphics2D g = (Graphics2D) gr;
        if (!isPrinting()) {
            PStringBounder.FONT_CONTEXT = g.getFontRenderContext();

            g.setColor(getBackground());

            if (showNet) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getSize().width, getSize().height);
                g.setColor(Color.black);
                drawNet(g, 0, 0, getSize().width, getSize().height);
            } else {
                g.setColor(getBackground());
                g.fillRect(0, 0, getSize().width, getSize().height);
            }
            g.setColor(getForeground());

            if (!DISABLE_RENDERING_HINTS && !isPrinting()) {
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
            }
        } else {

        }

        g.drawLine(0, 0, 0, getIntOrdinate(CLIENT_HEIGHT));
        g.drawLine(getIntOrdinate(CLIENT_WIDTH) - 1, 0,
                getIntOrdinate(CLIENT_WIDTH) - 1, getIntOrdinate(CLIENT_HEIGHT));
        final Stroke s = g.getStroke();

        for (final MovingText element : panels)
            if (!(element instanceof DFDSRole)
                    && !(element instanceof MovingLabel))
                element.paint(g, this);
        for (final MovingText element : panels)
            if (element instanceof DFDSRole
                    && !(element instanceof MovingLabel))
                element.paint(g, this);

        g.setStroke(s);
        Iterator<PaintSector> iterator = refactor.getSectors().iterator();
        while (iterator.hasNext()) {
            try {
                iterator.next().paint(g, this);
            } catch (Exception exception) {
                iterator.remove();
            }
        }
        g.setStroke(s);

        for (final MovingText element : panels)
            if (element instanceof MovingLabel)
                element.paint(g, this);

        if (hPageCountSize > 1) {
            g.setStroke(new BasicStroke(0.7f, 1, 1, 2, new float[]{
                    (float) getIDoubleOrdinate(6),
                    (float) getIDoubleOrdinate(10)},
                    (float) getIDoubleOrdinate(10)));
            double x = getIDoubleOrdinate(CLIENT_WIDTH) / hPageCountSize;
            double px = x;
            for (int i = 0; i < hPageCountSize; i++) {
                g.draw(new Line2D.Double(x, 0, x,
                        getIDoubleOrdinate(CLIENT_HEIGHT - 2)));
                x += px;
            }
        }
    }

    private double oldWidth = -1;
    private double oldHeight = -1;

    private GeneralPath path;

    private void drawNet(Graphics2D g, int x, int y, int width, int height) {
        double w = getIDoubleOrdinate(width);
        double h = getIDoubleOrdinate(height);
        if (path == null
                || Double.doubleToLongBits(oldHeight) != Double
                .doubleToLongBits(h)
                || Double.doubleToLongBits(oldWidth) != Double
                .doubleToLongBits(w)) {
            path = new GeneralPath();
            oldHeight = h;
            oldWidth = w;
            for (int i = 0; i <= MOVING_AREA_WIDTH; i += NET_LENGTH)
                for (int j = NET_LENGTH; j <= MOVING_AREA_HEIGHT; j += NET_LENGTH) {
                    path.moveTo(getIDoubleOrdinate(i), getIDoubleOrdinate(j));
                    path.lineTo(getIDoubleOrdinate(i), getIDoubleOrdinate(j));
                }
            path.closePath();
        }
        g.draw(path);
    }

    public Font getFont(final Font f) {
        final double size = f.getSize2D() * zoom;
        return new Font(f.getName(), f.getStyle(), (int) size);
    }

    public FRectangle paintTrimText(final Graphics2D g, final String text,
                                    final FRectangle rect, final int align, boolean cached) {
        return paintText(g, trimText(text), rect, align, cached);
    }

    public FRectangle paintText(final Graphics2D g, final String text,
                                final FRectangle rect, final int align, boolean cached) {
        return paintText(g, text, rect, align, 0, cached);
    }

    public FRectangle paintText(final Graphics2D g, final String text,
                                final FRectangle frect, final int align, final int pos,
                                boolean cached) {
        return paintText(g, text, frect, align, pos, true, cached);
    }

    public FRectangle paintText(final Graphics2D g, final String text,
                                final FRectangle frect, final int align, final int pos,
                                final boolean zoom, boolean cached) {
        return textPaintCache.paintText(g, text, frect, align, pos, zoom, this,
                cached);
    }

    /**
     * Повертає значення ширини у внутрішніх координатах.
     *
     * @return Значення ширини у внутрішніх координатах.
     */

    public double getDoubleWidth() {
        return CLIENT_WIDTH;
    }

    /**
     * Повертає значення висоти у внутрішніх координатах.
     *
     * @return Значення висоти у внутрішніх координатах.
     */

    public double getDoubleHeight() {
        return CLIENT_HEIGHT;
    }

    /**
     * Повертає значення частини секора, на якій в даний момент знаходиться
     * курсор.
     *
     * @return
     */

    public PaintSector.Pin getMousePin() {
        return mousePin;
    }

    /**
     * Перевіряє чи дві точки співпадаються покординатно між собою.
     *
     * @param a Перша точка.
     * @param b Друга точка.
     * @return <b>true</b>, якщо перша і друга точка співпадають,<br>
     * <b>false</b>, якщо перша і друга точки не співпадають.
     */

    public boolean isSame(final Point a, final Point b) {
        final int delta = NET_LENGTH;
        return Math.abs(getIntOrdinate(a.getX()) - getIntOrdinate(b.getX())) <= delta
                && Math.abs(getIntOrdinate(a.getY()) - getIntOrdinate(b.getY())) <= delta;
    }

    /**
     * Повертає значення активного сектора.
     *
     * @return Фактично повертає значення активного об’єкта, якщо це сектор.
     */

    public PaintSector getActiveSector() {
        if (activeObject instanceof PaintSector.Pin) {
            return ((PaintSector.Pin) activeObject).getSector();
        }
        return null;
    }

    public void doSector() {
        if ((getState() != START_POINT_ADDING) && (!isOkForCross()))
            return;

        if (getState() == START_POINT_ADDING) {
            setState(END_POINT_ADDING);
            if (panel != null)
                panel.getFramework().put(CURRENT_MOVING_AREA, this);
        } else if (getState() == END_POINT_ADDING) {
            boolean started = isUserTransactionStarted();
            if (!started)
                startUserTransaction();
            fillOtherMovingAreaIfNeen();
            setState(START_POINT_ADDING);
            refactor.createNewSector();
            refactor.fixOwners();
            if (!started) {
                commitUserTransaction();
                if (panel != null)
                    panel.getFramework().remove(CURRENT_MOVING_AREA);
            }
        } else if (getState() == START_POINT_CHANGING
                || getState() == END_POINT_CHANGING) {

            boolean started = isUserTransactionStarted();
            if (!started)
                startUserTransaction();
            fillOtherMovingAreaIfNeen();
            MovingArea ma = getCurrentMovingArea();
            if (ma != this) {
                refactor.createNewSector();
                refactor.fixOwners();
            } else {
                refactor.changeSector();
                refactor.fixOwners();
            }
            if (!started) {
                commitUserTransaction();
                if (panel != null)
                    panel.getFramework().remove(CURRENT_MOVING_AREA);
            }
            Integer state = (Integer) panel.getFramework().get(STATE);
            if ((state.intValue() == START_POINT_CHANGING)
                    || (state.intValue() == END_POINT_CHANGING))
                cancelAdding();
            else {
                setArrowAddingState();
            }
        }
    }

    public boolean isOkForCross() {
        MovingArea ma = getCurrentMovingArea();
        if ((ma != this) && (ma != null)) {
            Function a = getActiveFunction();
            Function b = ma.getActiveFunction();
            if ((cross.a == a) && (cross.b == b))
                return cross.res;
            cross.res = true;
            cross.a = a;
            cross.b = b;
            if (a.equals(b))
                cross.res = false;
            if (isParent(a, b))
                cross.res = false;
            if (isParent(b, a))
                cross.res = false;
            return cross.res;
        }
        return true;
    }

    private boolean isParent(Function child, Function par) {
        while (child != null) {
            if (child.equals(par))
                return true;
            child = (Function) child.getParentRow();
        }
        return false;
    }

    private void fillOtherMovingAreaIfNeen() {
        MovingArea ma = getCurrentMovingArea();
        if ((ma != this) && (ma != null)) {
            int state = getState();
            ma.fillMovingArea();
            ma.doSector();
            setState(state);
            SectorRefactor.PerspectivePoint pp = new SectorRefactor.PerspectivePoint();
            pp.x = mp.x;
            pp.y = mp.y;
            int pointChangingType = getPointChangingType();
            if (pressPanelMoveType >= 0)
                pp.borderType = pressPanelMoveType;
            else {
                if ((pointChangingType == SectorRefactor.TYPE_END))
                    pp.borderType = MovingFunction.LEFT;
                else
                    pp.borderType = MovingFunction.RIGHT;
            }
            SectorRefactor.PerspectivePoint po;

            if (pointChangingType == SectorRefactor.TYPE_END) {
                pp.type = SectorRefactor.TYPE_START;
                pp.crosspoint = ma.refactor.getSector().getEnd();
                pp.sector = ma.refactor.getSector();
                po = refactor.getPoint(SectorRefactor.TYPE_END);
            } else {
                pp.type = SectorRefactor.TYPE_END;
                pp.crosspoint = ma.refactor.getSector().getStart();
                pp.sector = ma.refactor.getSector();
                po = refactor.getPoint(SectorRefactor.TYPE_START);
            }
            if (po != null) {
                if ((pp.borderType == MovingFunction.LEFT)
                        || (pp.borderType == MovingFunction.RIGHT)) {
                    pp.y = po.y;
                } else {
                    pp.x = po.x;
                }
            }
            refactor.setPoint(pp);
        }
    }

    private void fillMovingArea() {
        final SectorRefactor.PerspectivePoint pp = new SectorRefactor.PerspectivePoint();
        pp.x = mp.x;
        pp.y = mp.y;
        if (getPointChangingType() == SectorRefactor.TYPE_END) {
            pp.borderType = MovingFunction.RIGHT;
            PerspectivePoint point = refactor
                    .getPoint(SectorRefactor.TYPE_START);
            if (point != null)
                pp.y = point.y;
        } else {
            pp.borderType = MovingFunction.LEFT;
            PerspectivePoint point = refactor.getPoint(SectorRefactor.TYPE_END);
            if (point != null)
                pp.y = point.y;
        }
        int pointChangingType = getPointChangingType();
        pp.type = pointChangingType;
        refactor.setPoint(pp);
    }

    public void setZoom(final double zoom) {
        this.zoom = zoom;
    }

    @Override
    public boolean contains(final int x, final int y) {
        mp.x = x;
        mp.y = y;
        return super.contains(x, y);
    }

    /**
     * Метод центрує сектори під’єднані до активного функціонального блока, якщо
     * ніякий функціональний блок не активний, то винекне буде помилка.
     */

    public void centerAdderSectors() {
        ((MovingFunction) getActiveObject()).centerAddedSetors();
        refactor.setUndoPoint();
    }

    public void beforeExit() {
        if (activeFunction != null)
            if (activeFunction.equals(lockedFunction)) {
                refactor.saveToFunction();
                activeFunction.unlock();
                lockedFunction = null;
            }
    }

    /**
     * Метод перевіряє, чи правильно розташовані сектори, щоб функціональний
     * блок міг бути видалений.
     *
     * @param context Функціональний блок, який має бути видалений.
     * @return <code>true</code>, якщо функціональний блок може бути видалений,
     * <code>false</code>, якщо функціональний блок не може бути
     * видалений.
     */

    private boolean checkRemoveLevelFunction(final Function context) {
        final Function f = (Function) context.getParentRow();
        ((NFunction) f).clearSectorsBuffer();
        final Vector<Sector> sectors = f.getSectors();
        for (int i = 0; i < sectors.size(); i++) {
            final Sector sector = sectors.get(i);
            if (context.equals(sector.getStart().getFunction())) {
                if (sector.getEnd().getBorderType() != sector.getStart()
                        .getFunctionType())
                    return false;
            } else if (context.equals(sector.getEnd().getFunction())) {
                if (sector.getStart().getBorderType() != sector.getEnd()
                        .getFunctionType())
                    return false;
            } else
                return false;
        }
        return true;
    }

    /**
     * Метод, який видяляє рівень з функціонального блоку
     */

    public void removeLevel() {
        final Function f = ((MovingFunction) getActiveObject()).getFunction();
        if (f.getChildCount() != 1) {
            JOptionPane.showMessageDialog(this, ResourceLoader
                    .getString("MovingArea.functionHaveNotOneChild"));
            return;
        }
        Vector<Row> childs = dataPlugin.getChilds(f, true);
        if (childs.size() != 1)
            return;
        final Function c = (Function) childs.get(0);
        if (f.isLocked() || c.isLocked()) {
            JOptionPane.showMessageDialog(this,
                    ResourceLoader.getString("MovingArea.functionIsLocked"));
            return;
        }
        if (!checkRemoveLevelFunction(c)) {
            JOptionPane.showMessageDialog(this, ResourceLoader
                    .getString("MovingArea.removingLevelSectorError"));
            return;
        }

        if (JOptionPane.showConfirmDialog(this,
                ResourceLoader.getString("MovingArea.removingLevelWarning"),
                ResourceLoader.getString("warning"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;

        f.lock();
        c.lock();

        startUserTransaction();

        Vector<Sector> sectors = new Vector<Sector>(f.getSectors());
        for (int i = 0; i < sectors.size(); i++) {
            final Sector sector = sectors.get(i);
            Crosspoint opp;
            Crosspoint crosspoint;
            if (c.equals(sector.getStart().getFunction())) {
                opp = sector.getStart().getCrosspoint();
                crosspoint = sector.getEnd().getCrosspoint();
            } else {
                opp = sector.getEnd().getCrosspoint();
                crosspoint = sector.getStart().getCrosspoint();
            }
            Sector o = null;
            final Sector[] os = opp.getOppozite(sector);
            for (final Sector s : os)
                o = s;
            class X {
                int createState;

                double createPos;

                Stream stream;

                byte[] visual;

                int sBorderType;

                int eBorderType;
            }
            X x = null;
            if (o != null
                    && (o.getStart().getCrosspoint() == null || o.getEnd()
                    .getCrosspoint() == null)) {
                x = new X();
                x.createState = o.getCreateState();
                x.createPos = o.getCreatePos();
                x.stream = o.getStream();
                x.visual = o.getVisualAttributes();
                x.sBorderType = o.getStart().getBorderType();
                x.eBorderType = o.getEnd().getBorderType();
            }
            sector.remove();
            if (x != null) {
                o = dataPlugin.createSector();
                o.setCreateState(x.createState, x.createPos);
                ((AbstractSector) o).setThisStream(x.stream);
                o.setVisualAttributes(x.visual);
                o.setFunction(c);
                o.getStart().setBorderTypeA(x.sBorderType);
                o.getEnd().setBorderTypeA(x.eBorderType);
                o.getStart().commit();
                o.getEnd().commit();
            }
            if (o != null) {
                if (c.equals(sector.getStart().getFunction())) {
                    o.getEnd().setCrosspointA(crosspoint);
                    o.getEnd().commit();
                } else {
                    o.getStart().setCrosspointA(crosspoint);
                    o.getEnd().commit();
                }
            }
        }

        sectors = new Vector<Sector>(c.getSectors());

        for (int i = 0; i < sectors.size(); i++) {
            final Sector sector = sectors.get(i);
            sector.setFunction(f);
        }

        f.setSectorData(c.getSectorData());

        c.setSectorData(new byte[0]);

        childs = dataPlugin.getChilds(c, true);

        for (int i = 0; i < childs.size(); i++) {
            final Function c1 = (Function) childs.get(i);
            c1.setParentRow(f);
        }

        c.unlock();
        if (!dataPlugin.removeRow(c)) {
            JOptionPane.showMessageDialog(this, ResourceLoader
                    .getString("MovingArea.cantRemoveFunctionByRemovingLevel"));
        }
        f.unlock();
        if (panel != null)
            panel.getFrame().propertyChange(
                    MChangeListener.RELOAD_FUNCTION_IN_TREE,
                    dataPlugin.getBaseFunction());
        commitUserTransaction();
    }

    public void clear() {
        refactor.setFunction(null);
        activeFunction = dataPlugin.getBaseFunction();
        activeObject = null;
        if (lockedFunction != null)
            lockedFunction.unlock();
        lockedFunction = null;
    }

    public void netAction() {
        showNet = !showNet;
        synchronized (backgroundPaintlock) {
            bImage = null;
        }
        repaintAsync();
    }

    public String getFunctionNumber() {
        return Integer.toString(functionIndex + 1);
    }

    public void addModelToTemplate() {
        final String name = activeFunction.getName();
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        JPanel pin = new JPanel(new FlowLayout());
        c.gridx = 0;
        c.gridy = 1;
        panel.add(pin, c);

        pin = new JPanel(new FlowLayout());
        c.gridx = 0;
        c.gridy = 0;
        panel.add(pin, c);

        final JLabel label = new JLabel("Template.Name");
        c.gridx = 1;
        c.gridy = 1;
        panel.add(label, c);

        pin = new JPanel(new FlowLayout());
        c.gridx = 2;
        c.gridy = 1;
        panel.add(pin, c);

        pin = new JPanel(new FlowLayout());
        c.gridx = 1;
        c.gridy = 2;
        panel.add(pin, c);

        final JTextField field = new JTextField(name);
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1;
        panel.add(field, c);

        pin = new JPanel(new FlowLayout());
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 4;
        panel.add(pin, c);

        final BaseDialog dialog = new BaseDialog(this.panel.getFramework()
                .getMainFrame()) {
            @Override
            protected void onOk() {
                String name = field.getText();
                if (TemplateFactory.isPresent(name)) {
                    if (JOptionPane.showConfirmDialog(
                            this,
                            MessageFormat.format(ResourceLoader
                                            .getString("Template_Present"),
                                    new Object[]{name})
                                    + " "
                                    + ResourceLoader
                                    .getString("Replace.Quation"),
                            ResourceLoader.getString("worning"),
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        TemplateFactory.removeUserTemplate(name);
                        TemplateFactory.saveTemplate(dataPlugin,
                                activeFunction, name, refactor);
                        super.onOk();
                    }
                } else {
                    TemplateFactory.saveTemplate(dataPlugin, activeFunction,
                            name, refactor);
                    super.onOk();
                }
            }
        };
        dialog.setTitle("Template.Name");
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setMainPane(panel);
        ResourceLoader.setJComponentsText(dialog);
        dialog.pack();
        dialog.setMinimumSize(dialog.getSize());
        dialog.setLocationRelativeTo(null);
        Options.loadOptions("InputTempatNameDialog", dialog);
        dialog.setVisible(true);
        Options.saveOptions("InputTempatNameDialog", dialog);
    }

    public void centerAllSectors() {
        for (final MovingText t : panels)
            if (t instanceof DFDSRole)
                ((IDEF0Object) t).centerAddedSetors();

        for (final MovingText t : panels)
            if (t instanceof IDEF0Object && !(t instanceof DFDSRole))
                ((IDEF0Object) t).centerAddedSetors();

        refactor.setUndoPoint();
    }

    public void setSelectedFunction(final Function newObject) {

        for (final MovingText t : panels) {
            if (t instanceof MovingFunction)
                if (((MovingFunction) t).getFunction().equals(newObject)) {
                    t.focusGained(false);
                    break;
                }
        }
    }

    public void addActiveFunctionListener(ActiveFunctionListener listener) {
        listenerList.add(ActiveFunctionListener.class, listener);
    }

    public void removeActiveFunctionListener(ActiveFunctionListener listener) {
        listenerList.remove(ActiveFunctionListener.class, listener);
    }

    public ActiveFunctionListener[] getActiveFunctionListeners() {
        return listenerList.getListeners(ActiveFunctionListener.class);
    }

    public IDEFPanel getPanel() {
        return panel;
    }

    public DataPlugin getDataPlugin() {
        return dataPlugin;
    }

    public IDEF0Object getIDEF0Object(Function function) {
        if (function == null)
            return null;
        return movingFunctions.get(function);
    }

    /**
     * @param printing the printing to set
     */
    public void setPrinting(boolean printing) {
        this.printing = printing;
    }

    /**
     * @return the printing
     */
    public boolean isPrinting() {
        return printing;
    }

    /**
     * @param state the state to set
     */
    public void setState(int state) {
        if (panel == null)
            this.state = state;
        else
            panel.getFramework().put(STATE, state);
    }

    /**
     * @return the state
     */
    public int getState() {
        if (panel == null)
            return state;
        Integer state = (Integer) panel.getFramework().get(STATE);
        if (state == null)
            return ARROW_STATE;
        if (state.intValue() == START_POINT_CHANGING_ADD)
            return START_POINT_CHANGING;
        if (state.intValue() == END_POINT_CHANGING_ADD)
            return END_POINT_CHANGING;
        return state;
    }

    private MovingArea getCurrentMovingArea() {
        if (panel == null)
            return null;
        return (MovingArea) panel.getFramework().get(CURRENT_MOVING_AREA);
    }

    private class Cross {
        Function a;
        Function b;
        boolean res;
    }

    public void setExernalReferenceAddingState() {
        setState(EXTERNAL_REFERENCE_ADDING_STATE);
    }

    public void setDataStoreAddingState() {
        setState(DATA_STORE_ADDING_STATE);
    }

    public Rectangle getIBounds(FRectangle bounds) {
        return new Rectangle(getIntOrdinate(bounds.getX()),
                getIntOrdinate(bounds.getY()),
                getIntOrdinate(bounds.getWidth()),
                getIntOrdinate(bounds.getHeight()));
    }

    public void renameObject() {
        if (activeObject != null)
            if (activeObject instanceof MovingText) {
                ((MovingText) activeObject).edit();
            }
    }

    public boolean canRenameActive() {
        if (activeObject instanceof DFDObject) {
            long link = ((DFDObject) activeObject).getFunction().getLink();
            return dataPlugin.findRowByGlobalId(link) != null;
        } else
            return true;
    }

    public void initClientHight() {
        CLIENT_HEIGHT = getWidth(MOVING_AREA_HEIGHT) - TOP_PART_A
                - BOTTOM_PART_A;
    }

    public void setDFDRoleAddingState() {
        setState(DFDS_ROLE_ADDING_STATE);

    }

    public List<DFDSRole> getDFDSRoles(DFDSFunction dfdsFunction) {
        List<DFDSRole> res = new ArrayList<DFDSRole>();
        for (MovingText mt : panels)
            if (mt instanceof DFDSRole) {
                DFDSRole role = (DFDSRole) mt;
                Row owner = role.getFunction().getOwner();
                if (owner != null
                        && owner.getElement().getId() == dfdsFunction
                        .getFunction().getElement().getId())
                    res.add(role);
            }
        Collections.sort(res);
        return res;
    }

    public List<Function> getRoles(DFDSFunction dfdsFunction) {
        List<Function> res = new ArrayList<Function>();
        for (MovingText mt : panels)
            if (mt instanceof DFDSRole) {
                DFDSRole role = (DFDSRole) mt;
                Row owner = role.getFunction().getOwner();
                if (owner != null
                        && owner.getElement().getId() == dfdsFunction
                        .getFunction().getElement().getId())
                    res.add(role.getFunction());
            }
        Collections.sort(res, new Comparator<Function>() {

            @Override
            public int compare(Function o1, Function o2) {
                FRectangle my = o1.getBounds();
                FRectangle o = o2.getBounds();

                if (my.getX() < o.getX())
                    return -1;
                if (my.getX() > o.getX())
                    return 1;
                return 0;
            }
        });
        return res;
    }

    public DFDSFunction findDFDSFunction(FRectangle bounds) {
        for (MovingText mt : panels)
            if (mt instanceof DFDSFunction) {
                DFDSFunction fun = (DFDSFunction) mt;
                if (fun.getBounds().contains(bounds))
                    return fun;
            }
        return null;
    }

    public DFDSFunction findDFDSFunction(Row function) {
        for (MovingText mt : panels)
            if (mt instanceof DFDSFunction) {
                DFDSFunction fun = (DFDSFunction) mt;
                if (fun.getFunction().equals(function))
                    return fun;
            }
        return null;
    }

    public Object getPrevActiveObject() {
        return prevActiveObject;
    }

    public void addToSelection(MouseSelection mouseSelection) {
        mouseSelection.clear();
        double x = mouseSelection.x;
        double y = mouseSelection.y;
        double w = Math.abs(mouseSelection.width);
        double h = Math.abs(mouseSelection.height);
        if (mouseSelection.width < 0)
            x += mouseSelection.width;

        if (mouseSelection.height < 0)
            y += mouseSelection.height;

        Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w, h);

        for (MovingText text : panels) {
            if (rect.contains(getBounds(text.getBounds())))
                mouseSelection.add(text);
        }
    }

    public MouseSelection getMouseSelection() {
        return mouseSelection;
    }

    public void paste(UserTemplate template) {
        if (template == null)
            return;
        startUserTransaction();
        template.createChilds(activeFunction, dataPlugin);
        refactor.setUndoPoint();
    }

    public void copy() {
        FunctionBeansClipboard.setCopy(activeFunction, dataPlugin,
                getRefactor(), mouseSelection.getFunctions());
    }

    public void paste() {
        paste(FunctionBeansClipboard.getTemplate());
    }

    public void cut() {
        FunctionBeansClipboard.setCopy(activeFunction, dataPlugin,
                getRefactor(), mouseSelection.getFunctions());
        removeActiveObject();
    }

    public void createBlocks(String[] names) {
        double fromX = 20;
        double fromY = 20;

        double x = fromX;
        double y = fromY;

        Attribute attribute = null;

        for (String name : names) {
            Function function = createFunctionalObject(x, y,
                    Function.TYPE_FUNCTION, activeFunction);

            x += 110;
            if (x + 110 + 20 > CLIENT_WIDTH) {
                x = fromX;
                y += 60;
            }
            if (attribute == null)
                attribute = getDataPlugin().getEngine()
                        .getAttribute(
                                getDataPlugin()
                                        .getEngine()
                                        .getQualifier(
                                                ((NFunction) function)
                                                        .getQualifierId())
                                        .getAttributeForName());

            Object object;
            if (attribute.getAttributeType().getTypeName().equals("DFDSName")) {
                DFDSName dfdsName = new DFDSName();
                dfdsName.setShortName(name);
                dfdsName.setLongName("");
                object = dfdsName;
            } else
                object = name;

            ((NFunction) function).setAttribute(attribute, object);
        }
    }

    public void createBlocksFromLines() {

        final JTextArea textArea = new JTextArea();

        BaseDialog dialog = new BaseDialog(getPanel().getFramework()
                .getMainFrame()) {
            @Override
            protected void onOk() {
                if (textArea.getText().trim().length() > 0) {
                    StringTokenizer st = new StringTokenizer(
                            textArea.getText(), "\n");
                    List<String> strings = new ArrayList<String>();

                    while (st.hasMoreElements()) {
                        String s = st.nextToken().trim();
                        if (s.length() > 0)
                            strings.add(s);
                    }
                    startUserTransaction();
                    createBlocks(strings.toArray(new String[strings.size()]));
                    commitUserTransaction();
                    super.onOk();
                }
            }
        };

        dialog.setMainPane(new JScrollPane(textArea));

        ResourceLoader.setJComponentsText(dialog);

        dialog.setBounds(0, 0, 800, 600);
        dialog.setLocationRelativeTo(null);

        dialog.setVisible(true);
    }

    public void visualOptions() {
        VisualOptionsDialog dialog = new VisualOptionsDialog(
                panel.getFramework(), mouseSelection);
        dialog.setVisible(true);
        repaintAsync();
    }

    /**
     * Метод, який додає рівень до активного функціонального блоку на моделі,
     * контекст функціонального блоку заміняється на новий функціональний блок з
     * контекстом, який мав активний функціональний блок.
     */

    public synchronized void addLevel() {
        final Function f = ((MovingFunction) getActiveObject()).getFunction();
        if (f.isLocked()) {
            JOptionPane.showMessageDialog(this,
                    ResourceLoader.getString("MovingArea.functionIsLocked"));
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                ResourceLoader.getString("MovingArea.addingLevelWarning"),
                ResourceLoader.getString("warning"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        f.lock();
        final Vector<Row> childs = dataPlugin.getChilds(f, true);
        final Function n = (Function) dataPlugin.createRow(f, true);
        n.setSectorData(f.getSectorData());
        n.setBounds(f.getBounds());
        n.setBackground(f.getBackground());
        n.setForeground(f.getForeground());
        n.setName(f.getName());
        n.setDecompositionType(f.getDecompositionType());
        n.setType(f.getType());
        f.setName(f.getName() + " ("
                + ResourceLoader.getString("MovingArea.context") + ")");
        n.setFont(f.getFont());
        f.setSectorData(new byte[0]);
        final SectorRefactor r = new SectorRefactor(this);
        // n.setBounds(new FRectangle(getDoubleWidth() / 2
        // - n.getBounds().getWidth() / 2, getDoubleHeight() / 2
        // - n.getBounds().getHeight() / 2, n.getBounds().getWidth(), n
        // .getBounds().getHeight()));
        for (int i = 0; i < childs.size(); i++) {
            final Function c = (Function) childs.get(i);
            c.setParentRow(n);
        }
        Vector srs = new Vector(f.getSectors());
        for (int i = 0; i < srs.size(); i++) {
            final Sector sector = (Sector) srs.get(i);
            sector.setFunction(n);
            // System.out.println(sector.getFunction());
        }
        final Function p = (Function) f.getParent();
        srs = p.getSectors();

        final int[] cnts = new int[4];
        final double[] poses = new double[4];
        final double[] hs = new double[4];

        for (int i = 0; i < cnts.length; i++)
            cnts[i] = 0;

        for (int i = 0; i < srs.size(); i++) {
            final Sector sector = (Sector) srs.get(i);
            if (f.equals(sector.getStart().getFunction())) {
                cnts[MovingPanel.RIGHT]++;
            }
            if (f.equals(sector.getEnd().getFunction())) {
                cnts[sector.getEnd().getFunctionType()]++;
            }
        }

        for (int i = 0; i < cnts.length; i++) {
            if (cnts[i] > 0) {
                if (i == MovingPanel.LEFT || i == MovingPanel.RIGHT)
                    hs[i] = n.getBounds().getHeight() / cnts[i];
                else
                    hs[i] = n.getBounds().getWidth() / cnts[i];
            }
            poses[i] = hs[i] / 2.0d;
        }

        for (int i = 0; i < srs.size(); i++) {
            final Sector sector = (Sector) srs.get(i);
            if (f.equals(sector.getStart().getFunction())) {
                final Crosspoint c = sector.getStart().getCrosspoint();
                final Sector[] ss = c.getOppozite(sector);
                Sector so = null;
                for (final Sector element : ss) {
                    so = element;
                }
                final Sector s = dataPlugin.createSector();
                ((AbstractSector) s).setThisStream(SectorRefactor.cloneStream(
                        sector.getStream(), dataPlugin, s));
                s.setVisualAttributes(sector.getVisualAttributes());
                s.setFunction(f);

                final Ordinate x1 = new Ordinate(Ordinate.TYPE_X);
                final Ordinate x2 = new Ordinate(Ordinate.TYPE_X);
                x1.setPosition(n.getBounds().getX() + n.getBounds().getWidth());
                x2.setPosition(getDoubleWidth() - PART_SPACE);
                final Ordinate y = new Ordinate(Ordinate.TYPE_Y);
                y.setPosition(n.getBounds().getY() + poses[MovingPanel.RIGHT]);
                poses[MovingPanel.RIGHT] += hs[MovingPanel.RIGHT];
                final Point p1 = new Point(x1, y);
                final Point p2 = new Point(x2, y);
                final PaintSector ps = new PaintSector(s, p1, p2, this);
                r.addSector(ps);

                final Crosspoint cp = dataPlugin.createCrosspoint();
                if (so != null) {
                    so.getEnd().setCrosspointA(cp);
                    so.getEnd().commit();
                }
                s.getEnd().setCrosspointA(c);
                s.getStart().setCrosspointA(cp);
                s.getStart().setFunctionA(n);
                s.getStart().setFunctionTypeA(MovingPanel.RIGHT);
                s.getEnd().setBorderTypeA(MovingPanel.RIGHT);
                s.getStart().commit();
                s.getEnd().commit();
            }
            if (f.equals(sector.getEnd().getFunction())) {
                final int et = sector.getEnd().getFunctionType();
                final Crosspoint c = sector.getEnd().getCrosspoint();
                final Sector[] ss = c.getOppozite(sector);
                Sector so = null;
                for (final Sector element : ss) {
                    so = element;
                    so.setFunction(n);
                }
                final Sector s = dataPlugin.createSector();

                ((AbstractSector) s).setThisStream(SectorRefactor.cloneStream(
                        sector.getStream(), dataPlugin, s));
                s.setVisualAttributes(sector.getVisualAttributes());

                s.setFunction(f);

                if (et == MovingPanel.LEFT) {
                    final Ordinate x1 = new Ordinate(Ordinate.TYPE_X);
                    final Ordinate x2 = new Ordinate(Ordinate.TYPE_X);
                    x2.setPosition(n.getBounds().getX());
                    x1.setPosition(PART_SPACE);
                    final Ordinate y = new Ordinate(Ordinate.TYPE_Y);
                    y.setPosition(n.getBounds().getY() + poses[et]);
                    final Point p1 = new Point(x1, y);
                    final Point p2 = new Point(x2, y);
                    final PaintSector ps = new PaintSector(s, p1, p2, this);
                    r.addSector(ps);
                } else if (et == MovingPanel.TOP) {
                    final Ordinate y1 = new Ordinate(Ordinate.TYPE_Y);
                    final Ordinate y2 = new Ordinate(Ordinate.TYPE_Y);
                    y2.setPosition(n.getBounds().getY());
                    y1.setPosition(PART_SPACE);
                    final Ordinate x = new Ordinate(Ordinate.TYPE_X);
                    x.setPosition(n.getBounds().getX() + poses[et]);
                    final Point p1 = new Point(x, y1);
                    final Point p2 = new Point(x, y2);
                    final PaintSector ps = new PaintSector(s, p1, p2, this);
                    r.addSector(ps);
                } else if (et == MovingPanel.BOTTOM) {
                    final Ordinate y1 = new Ordinate(Ordinate.TYPE_Y);
                    final Ordinate y2 = new Ordinate(Ordinate.TYPE_Y);
                    y2.setPosition(n.getBounds().getY()
                            + n.getBounds().getHeight());
                    y1.setPosition(PART_SPACE + getDoubleHeight());
                    final Ordinate x = new Ordinate(Ordinate.TYPE_X);
                    x.setPosition(n.getBounds().getX() + poses[et]);
                    final Point p1 = new Point(x, y1);
                    final Point p2 = new Point(x, y2);
                    final PaintSector ps = new PaintSector(s, p1, p2, this);
                    r.addSector(ps);
                }

                poses[et] += hs[et];

                final Crosspoint cp = dataPlugin.createCrosspoint();
                if (so != null) {
                    so.getStart().setCrosspointA(cp);
                    so.setFunction(n);
                    so.getStart().commit();
                }
                s.getStart().setCrosspointA(c);
                s.getEnd().setCrosspointA(cp);
                s.getEnd().setFunctionA(n);
                s.getEnd().setFunctionTypeA(et);
                s.getStart().setBorderTypeA(et);
                s.getStart().commit();
                s.getEnd().commit();
            }
        }
        r.saveToFunction(f);
        if (panel != null)
            panel.getFrame().propertyChange(
                    MChangeListener.RELOAD_FUNCTION_IN_TREE,
                    dataPlugin.getBaseFunction());
        ((NFunction) f).clearSectorsBuffer();
        f.unlock();
    }

    public void createLevel() {
        if (JOptionPane.showConfirmDialog(this,
                ResourceLoader.getString("MovingArea.addingLevelWarning"),
                ResourceLoader.getString("warning"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;

        final SectorRefactor r = new SectorRefactor(this);
        r.loadFromFunction(activeFunction, false);
        startUserTransaction();
        Function f = activeFunction;
        final Vector<Row> childs = dataPlugin.getChilds(f, true);
        NFunction n = (NFunction) dataPlugin.createRow(f, true);

        n.setSectorData(f.getSectorData());
        n.setBounds(f.getBounds());
        n.setBackground(f.getBackground());
        n.setForeground(f.getForeground());
        n.setName(f.getName());
        n.setDecompositionType(f.getDecompositionType());
        n.setType(f.getType());
        // f.setName(f.getName() + " ("
        // + ResourceLoader.getString("MovingArea.context") + ")");
        n.setFont(f.getFont());
        f.setSectorData(new byte[0]);
        for (int i = 0; i < childs.size(); i++) {
            final Function c = (Function) childs.get(i);
            c.setParentRow(n);
        }
        for (int i = 0; i < r.getSectorsCount(); i++)
            r.getSector(i).getSector().setFunction(n);

        ((NFunction) f).clearSectorsBuffer();

        commitUserTransaction();
    }

    public Cursor getVisualCopyCursor() {
        return visualCopyCursor;
    }

    public void setVisualCopyCursor(Cursor visualCopyCursor) {
        this.visualCopyCursor = visualCopyCursor;
    }

    static class VisualCopyOptions {
        boolean copyFont = true;
        boolean copyBK = true;
        boolean copyFK = true;
        boolean copySize = true;
    }

    private static VisualCopyOptions copyOptions = new VisualCopyOptions();

    public void DFDSRoleCopyVisual() {
        final VisualPanelCopyOptions panel = new VisualPanelCopyOptions();

        panel.setCopyBackground(copyOptions.copyBK);
        panel.setCopyForeground(copyOptions.copyFK);
        panel.setCopySize(copyOptions.copySize);
        panel.setCopyFont(copyOptions.copyFont);

        BaseDialog dialog = new BaseDialog(this.panel.getFramework()
                .getMainFrame()) {
            @Override
            protected void onOk() {
                copyOptions.copyBK = panel.isCopyBackground();
                copyOptions.copyFK = panel.isCopyForeground();

                copyOptions.copySize = panel.isCopySize();
                copyOptions.copyFont = panel.isCopyFont();

                copyDFDSVisual();
                super.onOk();
            }
        };

        dialog.setTitle("dfds_role_copy_visual");

        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(panel, BorderLayout.NORTH);

        dialog.setMainPane(panel2);

        ResourceLoader.setJComponentsText(dialog);

        dialog.setLocationRelativeTo(this.panel.getFramework().getMainFrame());

        Options.loadOptions("dfdsRoleCopy", dialog);
        dialog.setVisible(true);
        Options.saveOptions("dfdsRoleCopy", dialog);
    }

    protected void copyDFDSVisual() {
        startUserTransaction();
        DFDSRole role = (DFDSRole) activeObject;
        VisualPanelImpl panelImpl = new VisualPanelImpl(role);

        Stream stream = role.getStream();
        Row[] oRows = stream.getAdded();

        for (Row row : dataPlugin.getRecChilds(dataPlugin.getBaseFunction(),
                true))
            if (row instanceof Function) {
                final Function f = (Function) row;
                if (f.getType() == Function.TYPE_DFDS_ROLE
                        && f.getElement().getId() != role.getFunction()
                        .getElement().getId()) {
                    long l = f.getLink();
                    if (l >= 0) {
                        Stream s = (Stream) dataPlugin.findRowByGlobalId(l);
                        if (s != null && s.getAdded() != null) {
                            Row[] rows = s.getAdded();
                            boolean b = false;
                            for (Row row2 : rows)
                                if (row2 != null) {
                                    if (b)
                                        break;
                                    for (Row row3 : oRows)
                                        if (row3.equals(row2)) {
                                            panelImpl.copyTo(
                                                    new VisualPanel() {

                                                        @Override
                                                        public void setForegroundA(
                                                                Color foreground) {
                                                            f.setForeground(foreground);
                                                        }

                                                        @Override
                                                        public void setFontA(
                                                                Font font) {
                                                            f.setFont(font);
                                                        }

                                                        @Override
                                                        public void setBoundsA(
                                                                FRectangle bounds) {
                                                            f.setBounds(bounds);
                                                        }

                                                        @Override
                                                        public void setBackgroundA(
                                                                Color background) {
                                                            f.setBackground(background);
                                                        }

                                                        @Override
                                                        public Color getForegroundA() {
                                                            return f.getForeground();
                                                        }

                                                        @Override
                                                        public Font getFontA() {
                                                            return f.getFont();
                                                        }

                                                        @Override
                                                        public FRectangle getBoundsA() {
                                                            return f.getBounds();
                                                        }

                                                        @Override
                                                        public Color getBackgroundA() {
                                                            return f.getBackground();
                                                        }
                                                    }, copyOptions.copyBK,
                                                    copyOptions.copyFK,
                                                    copyOptions.copyFont,
                                                    copyOptions.copySize);

                                            b = true;
                                            break;
                                        }
                                }
                        }
                    }
                }
            }
        commitUserTransaction();
    }

    public boolean isMousePinWidthCtrl() {
        return mousePinWidthCtrl;
    }

    public int getActiveMovingTextIndex() {
        return activeMovingTextIndex;
    }

    public void joinArrows() {
        List<Row> rows = mouseSelection.getSelectedArrowsAddedRows();
        Row[] array = rows.toArray(new Row[rows.size()]);
        startUserTransaction();
        List<MovingLabel> pss = new ArrayList<MovingLabel>();

        for (int i = 1; i < mouseSelection.getLabels().size(); i++)
            pss.add(mouseSelection.getLabels().get(i));
        mouseSelection.getLabels().removeAll(pss);

        for (MovingLabel ml : pss)
            ml.getSector().remove();

        PaintSector sector = mouseSelection.getLabels().get(0).getSector();
        Stream stream = sector.getStream();
        if (stream == null) {
            stream = (Stream) dataPlugin.createRow(dataPlugin.getBaseStream(),
                    true);
            stream.setRows(array);
            sector.setStream(stream, ReplaceStreamType.SIMPLE);
        }
        sector.setRows(array);
        sector.createTexts();
        HashSet<PaintSector> hashSet = new HashSet();
        sector.getConnectedSector(hashSet);
        for (PaintSector sp : hashSet)
            PaintSector.save(sp, new DataLoader.MemoryData(),
                    dataPlugin.getEngine());
        getRefactor().setUndoPoint();
    }

    public void arrowCopyVisual() {
        startUserTransaction();
        PaintSector.Pin pin = (PaintSector.Pin) activeObject;

        Stream stream = pin.getSector().getStream();
        Row[] oRows = stream.getAdded();
        byte[] bs = pin.getSector().getSector().getVisualAttributes();

        for (Sector f : dataPlugin.getAllSectors())
            if (f.getStream() != null && f.getStream().getAdded() != null) {
                Row[] rows = f.getStream().getAdded();
                boolean b = false;
                for (Row row2 : rows)
                    if (row2 != null) {
                        if (b)
                            break;
                        for (Row row3 : oRows)
                            if (row3.equals(row2)) {
                                f.setVisualAttributes(bs);
                                b = true;
                                break;
                            }
                    }
            }
        refactor.setUndoPoint();
    }

    public boolean isNativePaint() {
        return textPaintCache.isNativePaint();
    }

    public void setNativePaint(boolean nativePaint) {
        textPaintCache.setNativePaint(nativePaint);
    }

    public boolean isShowNet() {
        return showNet;
    }

    public void setbImage(BufferedImage bImage) {
        this.bImage = bImage;
    }
}
