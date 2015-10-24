package com.ramussoft.pb.idef.frames;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.DataLoader;
import com.dsoft.utils.DataSaver;
import com.dsoft.utils.Options;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Qualifier;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.ScrollPanePreview;
import com.ramussoft.gui.common.View;
import com.ramussoft.gui.qualifier.table.FindPanel;
import com.ramussoft.idef0.ModelPropertiesDialog;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.IDEF0UndoMeneger;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.dfd.visual.DFDObjectDialog;
import com.ramussoft.pb.dfds.frames.DFDSRoleOptionsDialog;
import com.ramussoft.pb.dfds.visual.DFDSRole;
import com.ramussoft.pb.dmaster.Template;
import com.ramussoft.pb.frames.MainFrame;
import com.ramussoft.pb.frames.docking.MChangeListener;
import com.ramussoft.pb.frames.docking.ViewPanel;
import com.ramussoft.pb.frames.setup.OwnerClasificatorsDialog;
import com.ramussoft.pb.idef.elements.ArrowPainter;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingFunction;
import com.ramussoft.pb.idef.visual.MovingLabel;
import com.ramussoft.pb.idef.visual.MovingText;
import com.ramussoft.pb.print.IDEF0Printable;
import com.ramussoft.pb.print.PrintDialog;
import com.ramussoft.pb.types.GlobalId;

/**
 * @author ZDD
 */

public class IDEFPanel extends ViewPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     */

    public static final double DEFAULT_X = PaintSector.LINE_MIN_LENGTH * 2;

    /**
     * Координата y новоствореного функціонального блоку по-замовчуванню.
     */

    public static final double DEFAULT_Y = PaintSector.LINE_MIN_LENGTH * 2;

    /**
     * Ширина новоствореного функціонального блоку по-замовчуванню.
     */

    public static final double DEFAULT_WIDTH = PaintSector.LINE_MIN_LENGTH * 10;

    /**
     * Висота новоствореного функціонального блоку по-замовчуванню.
     */

    public static final double DEFAULT_HEIGHT = PaintSector.LINE_MIN_LENGTH * 7;

    public static final int FIT_ALL = 0;

    public static final int FIT_WIDTH = 1;

    public static final int FIT_HEIGHT = 2;

    private MainFrame frame;

    public static Color DEFAULT_BACKGROUND = Options.getColor(
            "DEFAULT_BACKGROUND", new java.awt.Color(255, 255, 235));

    private JScrollPane jScrollPane1 = null;

    private MovingArea movingArea = null;

    private JPanel jPanel2 = null;

    private JPanel jPanel3 = null;

    private Tab activeTab = null;

    private FunctionOptionsDialog functionOptions = null; // @jve:decl-index=0:visual-constraint="618,166"

    private JPanel jPanel5 = null;

    private ArrowOptionstDialog rowSelectDialog = null;

    private JPopupMenu arrowPopupMenu = null; // @jve:decl-index=0:visual-constraint="611,235"

    private JCheckBoxMenuItem jCheckBoxMenuItem = null;

    private JMenuItem jMenuItem = null;

    private JPopupMenu functionPopupMenu = null; // @jve:decl-index=0:visual-constraint="619,95"

    private JMenuItem jMenuItem1 = null;

    private boolean systemViewState = false;

    private boolean sys = false;

    private Zoom activeZoom = new Zoom(1.0d);

    private IDEF0UndoMeneger undoMeneger;

    private final JTabbedPane tabbedPane = new JTabbedPane();

    private final Object lock = new Object();

    private ContextMasterDialog contextMasterDialog = null;

    private OwnerClasificatorsDialog ownerClasificatorsDialog = null;

    private DataPlugin dataPlugin;

    private GUIFramework framework;

    private AccessRules accessRules;

    private boolean actionDisable = false;

    private FindPanel findPanel = new FindPanel() {
        public boolean findNext(String text, boolean wordsOrder) {
            return find(text, wordsOrder);
        }

        ;

        public boolean find(String text, boolean wordsOrder) {
            getMovingArea().textPaintCache.setSearchText(text);
            getMovingArea().setbImage(null);
            getMovingArea().repaintAsync();
            if (text == null || text.length() == 0)
                return true;
            return true;
        }

        ;
    };

    public static Zoom createZoom(final double zoom) {
        return new Zoom(zoom);
    }

    public static Zoom createZoom(final int zoomType) {
        return new Zoom(zoomType);
    }

    /**
     * Клас призначений для збереження інформації про маштаб.
     *
     * @author ZDD
     */

    public static class Zoom {
        private int fitType = -1;

        private double zoom;

        public Zoom(final double zoom) {
            super();
            this.zoom = zoom;
        }

        public Zoom(final int fitType) {
            super();
            this.fitType = fitType;
        }

        private double getWidthZoom(JScrollPane jScrollPane1,
                                    MovingArea movingArea) {
            final Rectangle r = jScrollPane1.getViewportBorderBounds();
            return (double) r.width / (double) movingArea.MOVING_AREA_WIDTH;
        }

        private double getHeightZoom(JScrollPane jScrollPane1,
                                     MovingArea movingArea) {
            final Rectangle r = jScrollPane1.getViewportBorderBounds();
            return (double) r.height / (double) movingArea.MOVING_AREA_HEIGHT;
        }

        public void doZoom(IDEFPanel panel) {
            panel.activeZoom = this;
            if (fitType >= 0) {
                switch (fitType) {
                    case FIT_ALL: {
                        zoom = getWidthZoom(panel.jScrollPane1,
                                panel.getMovingArea());
                        final double t = getHeightZoom(panel.jScrollPane1,
                                panel.getMovingArea());
                        if (t < zoom)
                            zoom = t;
                    }
                    break;
                    case FIT_HEIGHT:
                        zoom = getHeightZoom(panel.jScrollPane1,
                                panel.getMovingArea());
                        break;
                    case FIT_WIDTH:
                        zoom = getWidthZoom(panel.jScrollPane1,
                                panel.getMovingArea());
                        break;
                }
            }
            panel.getMovingArea().setZoom(zoom);
            panel.setMovingAreaSize(zoom);
        }

        @Override
        public String toString() {
            if (fitType == -1) {
                return (int) (zoom * 100) + " %";
            } else {
                switch (fitType) {
                    case FIT_ALL:
                        return ResourceLoader.getString("fit_all");
                    case FIT_HEIGHT:
                        return ResourceLoader.getString("fit_height");
                    case FIT_WIDTH:
                        return ResourceLoader.getString("fit_width");
                }
            }
            return super.toString();
        }

        public double getZoom() {
            return zoom;
        }
    }

    public void setSystemViewState(final boolean systemViewState) {
        this.systemViewState = systemViewState;
    }

    public void setMovingAreaSize(double zoom) {
        setMovingAreaSize(new Dimension(
                (int) (getMovingArea().MOVING_AREA_WIDTH * zoom),
                (int) (getMovingArea().MOVING_AREA_HEIGHT * zoom)));

    }

    public boolean isSystemViewState() {
        return systemViewState;
    }

    public boolean isSys() {
        return sys;
    }

    public void setSys(final boolean sys) {
        this.sys = sys;
    }

    @Override
    protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e,
                                        final int condition, final boolean pressed) {
        if (pressed && condition == 0)
            movingArea.processKeyBinding(e.getKeyCode());
        return super.processKeyBinding(ks, e, condition, pressed);
    }

    private void setMovingAreaSize(final java.awt.Dimension dimension) {
        getJPanel3().setPreferredSize(dimension);
        jTopPanel.setBounds(0, 0, dimension.width,
                movingArea.getIntOrdinate(movingArea.TOP_PART_A));
        movingArea.setBounds(0,
                movingArea.getIntOrdinate(movingArea.TOP_PART_A),
                dimension.width,
                movingArea.getIntOrdinate(movingArea.CLIENT_HEIGHT));
        jBottomPanel.setBounds(
                0,
                movingArea.getIntOrdinate(movingArea.TOP_PART_A
                        + movingArea.CLIENT_HEIGHT), dimension.width,
                movingArea.getIntOrdinate(movingArea.BOTTOM_PART_A));
        getJPanel5().doLayout();

        getJScrollPane1().revalidate();
    }

    public void up() {
        movingArea.up();
    }

    public void down() {
        movingArea.down();
    }

    public void remove() {
        final Object activeObject = getMovingArea().getActiveObject();
        if (activeObject == null) {
            if (getMovingArea().getMouseSelection() == null
                    || getMovingArea().getMouseSelection().getFunctions()
                    .size() == 0
                    || !getMovingArea().getMouseSelection().isRemoveable())
                return;
        }
        if (JOptionPane.showConfirmDialog(this, ResourceLoader
                        .getString("are_you_shour_want_remove_active_element"),
                ResourceLoader.getString("warning"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        movingArea.removeActiveObject();
    }

    /**
     * This method initializes jScrollPane1
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane1() {
        if (jScrollPane1 == null) {
            jScrollPane1 = new JScrollPane() {
                @Override
                protected void processMouseWheelEvent(MouseWheelEvent e) {
                    if (e.isControlDown()) {
                        Zoom zoom = frame.getActiveZoom();
                        int scroll = -e.getUnitsToScroll();
                        double zoom3 = zoom.getZoom() + (scroll * 5) / 100d;
                        if (zoom3 < 0.07 || zoom3 > 20)
                            return;
                        Zoom zoom2 = new Zoom(zoom3);
                        frame.setActiveZoom(zoom2);
                    } else if (e.isShiftDown()) {
                        Adjustable adj = jScrollPane1.getHorizontalScrollBar();
                        int scroll = e.getUnitsToScroll()
                                * adj.getBlockIncrement();
                        adj.setValue(adj.getValue() + scroll);
                    } else
                        super.processMouseWheelEvent(e);
                }
            };
            jScrollPane1.setViewportView(getJPanel5());
            jScrollPane1
                    .addComponentListener(new java.awt.event.ComponentAdapter() {
                        @Override
                        public void componentResized(
                                final java.awt.event.ComponentEvent e) {
                            activeZoom.doZoom(IDEFPanel.this);
                        }
                    });
            jScrollPane1.getVerticalScrollBar().setUnitIncrement(25);
            jScrollPane1.getHorizontalScrollBar().setUnitIncrement(25);
            ScrollPanePreview.install(jScrollPane1);
        }
        return jScrollPane1;
    }

    /**
     * This method initializes movingArea
     *
     * @return com.jason.clasificators.frames.idf.MovingArea
     */
    public MovingArea getMovingArea() {
        if (movingArea == null) {
            movingArea = new MovingArea(dataPlugin, this) {
                @Override
                public void setActiveObject(Object activeObject, boolean silent) {
                    super.setActiveObject(activeObject, silent);
                    if (!silent)
                        IDEFPanel.this.setActiveObject(activeObject);
                }
            };
            movingArea.setBackground(DEFAULT_BACKGROUND);
        }
        return movingArea;
    }

    protected void setActiveObject(Object activeObject) {

    }

    BoxLayout boxLayout = null;

    private BoxLayout getBoxLayout() {
        boxLayout = new BoxLayout(getJPanel2(), BoxLayout.X_AXIS);
        return boxLayout;
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            jPanel2 = new JPanel();
            jPanel2.setLayout(getBoxLayout());
        }
        return jPanel2;
    }

    private MChangeListener changeListener1 = new MChangeListener() {

        public void propertyChange(final String propertyName,
                                   final Object newObject) {
            if (FUNCTION_TREE_PANEL.equals(propertyName)) {
                getMovingArea().setActiveFunction((Function) newObject);
            } else if (MChangeListener.FILE_LOAD.equals(propertyName)) {
                activeZoom.doZoom(IDEFPanel.this);
            } else if (ACTIVATE_FUNCTION_OBJECT.equals(propertyName)) {
                getMovingArea().setSelectedFunction((Function) newObject);
            }
        }

    };

    private View view;

    /**
     * This is the default constructor
     */
    public IDEFPanel(final MainFrame frame, DataPlugin plugin,
                     GUIFramework framework, AccessRules accessRules, View view) {
        super(frame);
        this.dataPlugin = plugin;
        this.framework = framework;
        this.accessRules = accessRules;
        this.view = view;
        this.findPanel.getJButton1().setVisible(false);
        this.findPanel.getJCheckBox().setVisible(false);
        this.findPanel.getJTextField().addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                getView().focusLost();
            }

            @Override
            public void focusLost(FocusEvent e) {
                getView().focusGained();
            }
        });
        setLayout(new BorderLayout());
        setMainFrame(frame);
        frame.addMChangeListener(changeListener1);

        DEFAULT_BACKGROUND = Options.getColor("DEFAULT_BACKGROUND",
                new java.awt.Color(255, 255, 210));
        initialize();
        setFocusable(true);

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                "removeActive");
        getActionMap().put("removeActive", new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(final ActionEvent e) {
                final String[] aas = getEnableActions();
                for (final String aa : aas)
                    if (MainFrame.REMOVE.equals(aa)) {
                        remove();
                        return;
                    }
            }

        });

        AbstractAction action = new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(final ActionEvent e) {
                findPanel.setVisible(!findPanel.isVisible());
                if (findPanel.isVisible()) {
                    findPanel.getJTextField().requestFocus();
                } else {
                    findPanel.find("", false);
                    IDEFPanel.this.requestFocus();
                }
            }

        };
        getInputMap()
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                        java.awt.event.InputEvent.CTRL_DOWN_MASK), "findActive");
        getActionMap().put("findActive", action);

        findPanel
                .getJTextField()
                .getInputMap()
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                        java.awt.event.InputEvent.CTRL_DOWN_MASK), "findActive");
        findPanel.getJTextField().getActionMap().put("findActive", action);

        getMovingArea().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                IDEFPanel.this.requestFocus();
            }
        });
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setLayout(new BorderLayout());
        this.setSize(583, 272);
        getMovingArea();
        this.add(getJScrollPane1(), BorderLayout.CENTER);
        this.add(getJPanel(), BorderLayout.NORTH);
        findPanel.setVisible(false);
        this.add(findPanel, BorderLayout.SOUTH);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addChangeListener(new ChangeListener() {

            public void stateChanged(final ChangeEvent e) {
                int i = tabbedPane.indexOfComponent(getJScrollPane1());
                if (i >= 0)
                    tabbedPane.setComponentAt(i, new JLabel());
                i = tabbedPane.getSelectedIndex();
                if (i < 0)
                    activeTab = null;
                else if (i < tabbedPane.getTabCount()) {
                    final Tab tab = (Tab) tabbedPane.getTabComponentAt(i);
                    activeTab = tab;
                    if (tab != null) {
                        getMovingArea().setActiveFunction(tab.getFunction());
                        tab.getPanel().add(getJScrollPane1(),
                                BorderLayout.CENTER);
                    }
                }
            }

        });
    }

    /**
     * This method initializes jPanel3
     *
     * @return javax.swing.JPanel
     */
    public JPanel getJPanel3() {
        if (jPanel3 == null) {
            jPanel3 = new JPanel();
            jPanel3.setFont(new Font(Options
                    .getString("DEFAULT_FONT", "Dialog"), 0, 12));
            jPanel3.setLayout(null);
            jPanel3.setPreferredSize(new java.awt.Dimension(
                    movingArea.MOVING_AREA_WIDTH, movingArea.MOVING_AREA_HEIGHT));
            jPanel3.setName("jPanel3");
            jPanel3.add(getMovingArea(), java.awt.BorderLayout.CENTER);
            jPanel3.setBackground(getMovingArea().getBackground());

            jPanel3.add(getJTopPanel(), null);
            jPanel3.add(getJBottomPanel(), null);
        }
        return jPanel3;
    }

    public void paintIDF(final Graphics2D g) {
        getJPanel3().paint(g);
    }

    /**
     *
     */
    public void edit() {
        movingArea.editActive();
    }

    /**
     * This method initializes functionOptions
     *
     * @return com.jason.clasificators.frames.idf.FunctionOptions
     */
    public FunctionOptionsDialog getFunctionOptions() {
        synchronized (lock) {
            if (functionOptions == null) {
                functionOptions = new FunctionOptionsDialog(
                        framework.getMainFrame(), dataPlugin, framework);
            }
            return functionOptions;
        }
    }

    /**
     * This method initializes jPanel5
     *
     * @return javax.swing.JPanel
     */
    public JPanel getJPanel5() {
        if (jPanel5 == null) {
            jPanel5 = new JPanel();
            jPanel5.setLayout(new BorderLayout());
            jPanel5.add(getJPanel3(), BorderLayout.CENTER);
        }
        return jPanel5;
    }

    /**
     * This method initializes rowSelectDialog
     *
     * @return com.jason.clasificators.frames.idf.RowSelectDialog
     */
    public ArrowOptionstDialog getArrowOptionsDialog() {
        synchronized (lock) {
            if (rowSelectDialog == null) {
                rowSelectDialog = new ArrowOptionstDialog(
                        framework.getMainFrame(), dataPlugin, framework,
                        accessRules);
                rowSelectDialog.setSize(412, 298);
            }
            return rowSelectDialog;
        }
    }

    /**
     * This method initializes jPopupMenu
     *
     * @return javax.swing.JPopupMenu
     */
    public JPopupMenu getArrowPopupMenu() {
        if (arrowPopupMenu == null) {
            arrowPopupMenu = new JPopupMenu();
            arrowPopupMenu.add(getJMenuItem2());
            arrowPopupMenu.addSeparator();
            arrowPopupMenu.add(getJCheckBoxMenuItem());
            arrowPopupMenu.add(getJCheckBoxMenuItem1());
            // arrowPopupMenu.add(getJMenuItem8());
            arrowPopupMenu.addSeparator();
            arrowPopupMenu.add(getJMenuItem7());
            arrowPopupMenu.addSeparator();
            arrowPopupMenu.add(frame.findAction(MainFrame.JOIN_ARROWS));
            arrowPopupMenu.addSeparator();
            arrowPopupMenu.add(frame.findAction(MainFrame.ARROW_COPY_VISUAL));
            arrowPopupMenu.addSeparator();
            arrowPopupMenu.add(getJMenuItem());
        }
        return arrowPopupMenu;
    }

    /**
     * This method initializes jCheckBoxMenuItem
     *
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getJCheckBoxMenuItem() {
        if (jCheckBoxMenuItem == null) {
            jCheckBoxMenuItem = new JCheckBoxMenuItem();
            jCheckBoxMenuItem.setAction(frame
                    .findAction(MainFrame.SET_ARROW_TILDA));
        }
        return jCheckBoxMenuItem;
    }

    /**
     * This method initializes jMenuItem
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItem() {
        if (jMenuItem == null) {
            jMenuItem = new JMenuItem();
            jMenuItem.setAction(frame.findAction(MainFrame.EDIT));
        }
        return jMenuItem;
    }

    /**
     * This method initializes jPopupMenu
     *
     * @return javax.swing.JPopupMenu
     */
    public JPopupMenu getFunctionPopupMenu() {
        if (functionPopupMenu == null) {
            functionPopupMenu = new JPopupMenu();
            functionPopupMenu.add(getJMenuItem4());
            JMenu setLookMenu = new JMenu(
                    ResourceLoader
                            .getString("set_look_properties_for_childrens"));
            setLookMenu.add(frame
                    .findAction(MainFrame.SET_LOOK_FOR_CHILDRENS_FONT));
            setLookMenu.add(frame
                    .findAction(MainFrame.SET_LOOK_FOR_CHILDRENS_BACKGROUND));
            setLookMenu.add(frame
                    .findAction(MainFrame.SET_LOOK_FOR_CHILDRENS_FOREGROUND));
            setLookMenu.add(frame.findAction(MainFrame.SET_LOOK_FOR_CHILDRENS));
            functionPopupMenu.add(setLookMenu);
            functionPopupMenu.addSeparator();
            functionPopupMenu.add(frame.findAction(MainFrame.RENAME));
            functionPopupMenu.addSeparator();
            functionPopupMenu.add(getJMenuItemOpenTab());
            // functionPopupMenu.add(getJMenuItemOpenTabNotActive());
            functionPopupMenu.addSeparator();
            functionPopupMenu.add(getJMenu1());
            functionPopupMenu.add(getJMenu());
            functionPopupMenu.addSeparator();
            functionPopupMenu.add(getJMenuItem3());
            functionPopupMenu.addSeparator();
            // functionPopupMenu.add(getJMenuItemMoveFunction());
            functionPopupMenu.add(getJMenuItemAddLevel());
            functionPopupMenu.add(getJMenuItemRemoveLevel());
            functionPopupMenu.addSeparator();
            functionPopupMenu.add(frame.findAction(MainFrame.CUT));
            functionPopupMenu.add(frame.findAction(MainFrame.COPY));
            functionPopupMenu.add(frame.findAction(MainFrame.PASTE));

            functionPopupMenu.addSeparator();
            functionPopupMenu.add(getJMenuItemCreateParalel());
            functionPopupMenu.add(getJMenuLoadFromParalel());
            functionPopupMenu.addSeparator();
            functionPopupMenu.add(frame.findAction(MainFrame.VISUAL_OPTIONS));
            functionPopupMenu.addSeparator();
            functionPopupMenu.add(getJMenuItem1());
        }
        return functionPopupMenu;
    }

    /**
     * This method initializes jMenuItem1
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItem1() {
        if (jMenuItem1 == null) {
            jMenuItem1 = new JMenuItem();
            jMenuItem1.setAction(frame.findAction(MainFrame.EDIT));
        }
        return jMenuItem1;
    }

    private JMenuItem jMenuItem2 = null; // @jve:decl-index=0:

    // private JMenuItem jMenuItem3 = null; // @jve:decl-index=0:

    private JMenuItem jMenuItem4 = null; // @jve:decl-index=0:

    private JPopupMenu textPopupMenu = null;

    private JPopupMenu dfdsPopupMenu = null;

    private TextOptionsDialog textOptions = null; // @jve:decl-index=0:visual-constraint="425,323"

    private JCheckBoxMenuItem jCheckBoxMenuItem1 = null;

    private JMenuItem jMenuItem7 = null;

    private ArrowTunnelDialog arrowBorderDialog = null; // @jve:decl-index=0:visual-constraint="34,332"

    private JMenu jMenu = null;

    private JRadioButtonMenuItem jRadioButtonMenuItem = null;

    private JRadioButtonMenuItem jRadioButtonMenuItem1 = null;

    private JRadioButtonMenuItem jRadioButtonMenuItem2 = null;

    private JMenu jMenu1 = null;

    private JRadioButtonMenuItem jRadioButtonMenuItem3 = null;

    private JRadioButtonMenuItem jRadioButtonMenuItem4 = null;

    private JPanel jTopPanel = null;

    private JPanel jBottomPanel = null;

    private JMenuItem jMenuItem3 = null;

    // private JMenuItem jMenuItemMoveFunction = null;

    private JMenuItem jMenuItemAddLevel = null;

    private JMenuItem jMenuItemRemoveLevel = null;

    public static final String IDEF0_ZOOM = "IDEF0D_ZOOM"; // @jve:decl-index=0:

    private JMenuItem jMenuItemOpenTab = null;

    private JPanel jPanel = null;

    private JMenuItem jMenuItemCreateParalel = null;

    private JMenuItem jMenuLoadFromParalel = null;

    protected DiagramOptionsDialog diagramOptionsDialog;

    private DFDObjectDialog dfdObjectDialog = null;

    /**
     * @return
     */
    public JPanel getIDFArea() {
        return getJPanel3();
    }

    public void setModelColor(final Color f) {
        getMovingArea().setBackground(f);
        getIDFArea().setBackground(f);
        getJTopPanel().setBackground(f);
        getJBottomPanel().setBackground(f);
    }

    /**
     * This method initializes jButton9
     *
     * @return javax.swing.JButton
     */
    /**
     * This method initializes jMenuItem2
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItem2() {
        if (jMenuItem2 == null) {
            jMenuItem2 = new JMenuItem();
            jMenuItem2.setAction(frame.findAction(MainFrame.REMOVE));
        }
        return jMenuItem2;
    }

    /**
     * This method initializes jMenuItem4
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItem4() {
        if (jMenuItem4 == null) {
            jMenuItem4 = new JMenuItem();
            jMenuItem4.setAction(frame.findAction(MainFrame.REMOVE));
        }
        return jMenuItem4;
    }

    /**
     * This method initializes jPopupMenu
     *
     * @return javax.swing.JPopupMenu
     */
    public JPopupMenu getTextPopupMenu() {
        if (textPopupMenu == null) {
            textPopupMenu = new JPopupMenu();
            textPopupMenu.add(frame.findAction(MainFrame.REMOVE));
            textPopupMenu.addSeparator();
            textPopupMenu.add(frame.findAction(MainFrame.RENAME));
            textPopupMenu.addSeparator();
            textPopupMenu.add(frame.findAction(MainFrame.VISUAL_OPTIONS));
            textPopupMenu.addSeparator();
            textPopupMenu.add(frame.findAction(MainFrame.EDIT));
        }
        return textPopupMenu;
    }

    /**
     * This method initializes jPopupMenu
     *
     * @return javax.swing.JPopupMenu
     */
    public JPopupMenu getDFDSRolePopupMenu() {
        if (dfdsPopupMenu == null) {
            dfdsPopupMenu = new JPopupMenu();
            dfdsPopupMenu.add(frame.findAction(MainFrame.REMOVE));
            dfdsPopupMenu.addSeparator();
            dfdsPopupMenu.add(frame
                    .findAction(MainFrame.BRAKE_DFDSROLE_CONNECTION));
            dfdsPopupMenu.addSeparator();
            dfdsPopupMenu.add(frame.findAction(MainFrame.RENAME));
            dfdsPopupMenu.addSeparator();
            dfdsPopupMenu.add(frame.findAction(MainFrame.VISUAL_OPTIONS));
            dfdsPopupMenu.addSeparator();
            dfdsPopupMenu.add(frame.findAction(MainFrame.CUT));
            dfdsPopupMenu.add(frame.findAction(MainFrame.COPY));
            dfdsPopupMenu.add(frame.findAction(MainFrame.PASTE));
            dfdsPopupMenu.addSeparator();
            dfdsPopupMenu
                    .add(frame.findAction(MainFrame.DFDS_ROLE_COPY_VISUAL));
            dfdsPopupMenu.addSeparator();
            dfdsPopupMenu.add(frame.findAction(MainFrame.EDIT));
        }
        return dfdsPopupMenu;
    }

    /**
     * This method initializes textOptions
     *
     * @return com.jason.clasificators.frames.idf.TextOptions
     */
    public TextOptionsDialog getTextOptionsDialog() {
        synchronized (lock) {
            if (textOptions == null) {
                textOptions = new TextOptionsDialog();
            }
            return textOptions;
        }
    }

    /**
     * This method initializes jCheckBoxMenuItem1
     *
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getJCheckBoxMenuItem1() {
        if (jCheckBoxMenuItem1 == null) {
            jCheckBoxMenuItem1 = new JCheckBoxMenuItem();
            jCheckBoxMenuItem1.setAction(frame
                    .findAction(MainFrame.SET_TRANSPARENT_ARROW_TEXT));
        }
        return jCheckBoxMenuItem1;
    }

    /**
     *
     */
    public void pagePreview() {
        framework.printPreview(new IDEF0Printable(dataPlugin, framework));
    }

    /**
     * This method initializes jMenuItem7
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItem7() {
        if (jMenuItem7 == null) {
            jMenuItem7 = new JMenuItem();
            jMenuItem7.setAction(frame.findAction(MainFrame.TUNNEL_ARROW));
        }
        return jMenuItem7;
    }

    /**
     * This method initializes arrowBorderDialog
     *
     * @return com.jason.clasificators.frames.idf.ArrowBorderDialog
     */
    public ArrowTunnelDialog getArrowBorderDialog() {
        if (arrowBorderDialog == null) {
            arrowBorderDialog = new ArrowTunnelDialog(movingArea);
        }
        return arrowBorderDialog;
    }

    /**
     * This method initializes jMenu
     *
     * @return javax.swing.JMenu
     */
    private JMenu getJMenu() {
        if (jMenu == null) {
            jMenu = new JMenu();
            jMenu.setAction(frame.findAction(MainFrame.FUNCTION_TYPE));
            jMenu.add(getJRadioButtonMenuItemAction());
            jMenu.add(getJRadioButtonMenuItemOperation());
            jMenu.add(getJRadioButtonMenuItemProcessPart());
            jMenu.add(getJRadioButtonMenuItemProcess());
            jMenu.add(getJRadioButtonMenuItemKomplex());
            final ButtonGroup bg = new ButtonGroup();
            bg.add(getJRadioButtonMenuItemAction());
            bg.add(getJRadioButtonMenuItemOperation());
            bg.add(getJRadioButtonMenuItemProcessPart());
            bg.add(getJRadioButtonMenuItemProcess());
            bg.add(getJRadioButtonMenuItemKomplex());
        }
        return jMenu;
    }

    /**
     * This method initializes jRadioButtonMenuItem
     *
     * @return javax.swing.JRadioButtonMenuItem
     */
    private JRadioButtonMenuItem getJRadioButtonMenuItemAction() {
        if (jRadioButtonMenuItem == null) {
            jRadioButtonMenuItem = new JRadioButtonMenuItem();
            jRadioButtonMenuItem.setAction(frame
                    .findAction(MainFrame.FUNCTION_TYPE_ACTION));
        }
        return jRadioButtonMenuItem;
    }

    protected void setFunctionType(final int type) {
        movingArea.startUserTransaction();
        final Function function = ((MovingFunction) movingArea
                .getActiveObject()).getFunction();
        function.setType(type);
        switch (type) {
            case Function.TYPE_ACTION: {
                getJRadioButtonMenuItemAction().setSelected(true);
                frame.getJMenuItemAction().setSelected(true);
            }
            break;
            case Function.TYPE_OPERATION: {
                getJRadioButtonMenuItemOperation().setSelected(true);
                frame.getJMenuItemOperation().setSelected(true);
            }
            break;
            case Function.TYPE_PROCESS_PART: {
                getJRadioButtonMenuItemProcessPart().setSelected(true);
                frame.getJMenuItemProcessPart().setSelected(true);
            }
            break;
            case Function.TYPE_PROCESS: {
                getJRadioButtonMenuItemProcess().setSelected(true);
                frame.getJMenuItemProcess().setSelected(true);
            }
            break;
            case Function.TYPE_PROCESS_KOMPLEX: {
                getJRadioButtonMenuItemKomplex().setSelected(true);
                frame.getJMenuItemKomplex().setSelected(true);
            }
            break;
        }
        movingArea.commitUserTransaction();
    }

    /**
     * This method initializes jRadioButtonMenuItem1
     *
     * @return javax.swing.JRadioButtonMenuItem
     */
    private JRadioButtonMenuItem getJRadioButtonMenuItemOperation() {
        if (jRadioButtonMenuItem1 == null) {
            jRadioButtonMenuItem1 = new JRadioButtonMenuItem();
            jRadioButtonMenuItem1.setAction(frame
                    .findAction(MainFrame.FUNCTION_TYPE_OPERATION));
        }
        return jRadioButtonMenuItem1;
    }

    /**
     * This method initializes jRadioButtonMenuItem2
     *
     * @return javax.swing.JRadioButtonMenuItem
     */
    private JRadioButtonMenuItem getJRadioButtonMenuItemProcessPart() {
        if (jRadioButtonMenuItem2 == null) {
            jRadioButtonMenuItem2 = new JRadioButtonMenuItem();
            jRadioButtonMenuItem2.setAction(frame
                    .findAction(MainFrame.FUNCTION_TYPE_PROCESS_PART));
        }
        return jRadioButtonMenuItem2;
    }

    /**
     * This method initializes jMenu1
     *
     * @return javax.swing.JMenu
     */
    private JMenu getJMenu1() {
        if (jMenu1 == null) {
            jMenu1 = new JMenu();
            jMenu1.setText(ResourceLoader.getString("owner"));
        }
        return jMenu1;
    }

    /**
     * This method initializes jRadioButtonMenuItem3
     *
     * @return javax.swing.JRadioButtonMenuItem
     */
    private JRadioButtonMenuItem getJRadioButtonMenuItemProcess() {
        if (jRadioButtonMenuItem3 == null) {
            jRadioButtonMenuItem3 = new JRadioButtonMenuItem();
            jRadioButtonMenuItem3.setAction(frame
                    .findAction(MainFrame.FUNCTION_TYPE_PROCESS));
        }
        return jRadioButtonMenuItem3;
    }

    /**
     * This method initializes jRadioButtonMenuItem4
     *
     * @return javax.swing.JRadioButtonMenuItem
     */
    private JRadioButtonMenuItem getJRadioButtonMenuItemKomplex() {
        if (jRadioButtonMenuItem4 == null) {
            jRadioButtonMenuItem4 = new JRadioButtonMenuItem();
            jRadioButtonMenuItem4.setAction(frame
                    .findAction(MainFrame.FUNCTION_TYPE_KOMPLEX));
        }
        return jRadioButtonMenuItem4;
    }

    /**
     * This method initializes jTopPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJTopPanel() {
        if (jTopPanel == null) {
            jTopPanel = new JPanel() {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public void paint(final Graphics gr) {
                    final Graphics2D g = (Graphics2D) gr;
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getSize().width, getSize().height);
                    g.setColor(getForeground());

                    final ArrowPainter painter = new ArrowPainter(movingArea);
                    try {
                        painter.paintTop(g, movingArea
                                        .getIntOrdinate(movingArea.TOP_PART_A),
                                movingArea);
                    } catch (NullPointerException e) {

                    }
                }
            };
            jTopPanel.setBackground(getMovingArea().getBackground());
            jTopPanel.setBounds(new java.awt.Rectangle(5, 5, 10, 10));
            jTopPanel.setComponentPopupMenu(createTopPanelPopupMenu());
        }
        return jTopPanel;
    }

    private JPopupMenu createTopPanelPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(frame.findAction(MainFrame.MODEL_PROPETIES));
        menu.add(frame.findAction(MainFrame.DIAGRAM_PROPETIES));
        menu.add(frame.findAction(MainFrame.CREATE_LEVEL));
        return menu;
    }

    /**
     * This method initializes jBottomPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJBottomPanel() {
        if (jBottomPanel == null) {
            jBottomPanel = new JPanel() {

                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public void paint(final Graphics gr) {
                    final Graphics2D g = (Graphics2D) gr;
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getSize().width, getSize().height);
                    g.setColor(getForeground());

                    final ArrowPainter painter = new ArrowPainter(movingArea);
                    painter.paintBottom(g, getHeight(), movingArea);
                }
            };
            jBottomPanel.setBackground(getMovingArea().getBackground());
            jBottomPanel.setBounds(new java.awt.Rectangle(5, 20, 10, 10));
        }
        return jBottomPanel;
    }

    public void setFunctionMenu(final Function function) {

        getJMenu1().removeAll();
        final Row[] rows = function.getOwners();
        class OwnerSetter implements ActionListener {
            private final Function function;

            private final Row owner;

            public OwnerSetter(final Function function, final Row owner) {
                super();
                this.function = function;
                this.owner = owner;
            }

            public void actionPerformed(final ActionEvent e) {
                movingArea.startUserTransaction();
                function.setOwner(owner);
                movingArea.commitUserTransaction();
            }

        }
        boolean parentOuner = false;
        Row ouner = function.getOwner();
        Row pOuner = null;
        if (function.getParent() != null)
            pOuner = ((Function) function.getParentRow()).getOwner();
        parentOuner = ((ouner != null) && (ouner.equals(pOuner)));

        JRadioButtonMenuItem item;
        if (pOuner != null) {
            item = new JRadioButtonMenuItem("<html><body><i><b>"
                    + pOuner.getName() + "</b></i></body></html>");
        } else
            item = new JRadioButtonMenuItem("<html><body><i><b>"
                    + ResourceLoader.getString("owner_not_selected")
                    + "</b></i></body></html>");
        item.addActionListener(new OwnerSetter(function, null));
        if (ouner == null || parentOuner)
            item.setSelected(true);
        jMenu1.add(item);
        if (rows.length > 0)
            jMenu1.addSeparator();
        for (final Row element : rows)
            if (!element.equals(pOuner)) {
                item = new JRadioButtonMenuItem(element.getName());
                item.addActionListener(new OwnerSetter(function, element));
                if (ouner != null && ouner.equals(element))
                    item.setSelected(true);
                jMenu1.add(item);
            }
        jMenu1.addSeparator();
        final JMenuItem mi = new JMenuItem(
                ResourceLoader.getString("Owners.Clasificators"));
        mi.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                getOwnerClasificatorsDialog().showModal();
            }

        });
        jMenu1.add(mi);
    }

    /**
     * This method initializes jMenuItem3
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItem3() {
        if (jMenuItem3 == null) {
            jMenuItem3 = new JMenuItem();
            jMenuItem3.setAction(frame
                    .findAction(MainFrame.CENTER_ADDED_SECTORS));
        }
        return jMenuItem3;
    }

    public boolean isShowToolTips() {
        return false;
    }

    /**
     * This method initializes jMenuItemMoveFunction
     *
     * @return javax.swing.JMenuItem
     */
    /*
     * private JMenuItem getJMenuItemMoveFunction() { if (jMenuItemMoveFunction
	 * == null) { jMenuItemMoveFunction = new JMenuItem();
	 * jMenuItemMoveFunction.setAction(frame
	 * .findAction(MainFrame.FUNCTION_MOVE)); } return jMenuItemMoveFunction; }
	 */

    /**
     * This method initializes jMenuItemAddLevel
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemAddLevel() {
        if (jMenuItemAddLevel == null) {
            jMenuItemAddLevel = new JMenuItem();
            jMenuItemAddLevel.setAction(frame
                    .findAction(MainFrame.FUNCTION_ADD_LEVEL));
        }
        return jMenuItemAddLevel;
    }

    /**
     * This method initializes jMenuItemRemoveLevel
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemRemoveLevel() {
        if (jMenuItemRemoveLevel == null) {
            jMenuItemRemoveLevel = new JMenuItem();
            jMenuItemRemoveLevel.setAction(frame
                    .findAction(MainFrame.FUNCTION_REMOVE_LEVEL));
        }
        return jMenuItemRemoveLevel;
    }

    protected void removeLevel() {
        movingArea.removeLevel();
    }

    protected void addLevel() {
        movingArea.startUserTransaction();
        movingArea.addLevel();
        movingArea.commitUserTransaction();
    }

    private ActionListener actionListener = new ActionListener() {

        public void actionPerformed(final ActionEvent e) {
            final String propertyName = e.getActionCommand();
            if (MainFrame.CURSOR_TOOL.equals(propertyName)) {
                movingArea.setArrowState();
            } else if (MainFrame.FUNCTION_TOOL.equals(propertyName)) {
                movingArea.setFunctionAddingState();
            } else if (MainFrame.DATA_STORE_TOOL.equals(propertyName)) {
                movingArea.setDataStoreAddingState();
            } else if (MainFrame.EXTERNAL_REFERENCE_TOOL.equals(propertyName)) {
                movingArea.setExernalReferenceAddingState();
            } else if (MainFrame.DFDS_ROLE_TOOL.equals(propertyName)) {
                movingArea.setDFDRoleAddingState();
            } else if (MainFrame.ARROW_TOOL.equals(propertyName)) {
                movingArea.setArrowAddingState();
            } else if (MainFrame.TILDA_TOOL.equals(propertyName)) {
                movingArea.setTildaAddingState();
            } else if (MainFrame.TEXT_TOOL.equals(propertyName)) {
                movingArea.setTextAddingState();
            } else if (MainFrame.RELOAD_SAVE.equals(propertyName))
                movingArea.setActiveFunction(movingArea.getActiveFunction());
            else if (MainFrame.IDEF0_NET.equals(propertyName))
                movingArea.netAction();
            else if (MainFrame.RENAME.equals(propertyName))
                movingArea.renameObject();
        }

    };

    private DFDSRoleOptionsDialog dfdsRoleOptionsDialog;

    public void setMainFrame(final MainFrame frame) {
        this.frame = frame;

        frame.addActionListener(actionListener);
    }

    public void close() {
        frame.removeActionListener(actionListener);
        frame.removeMChangeListener(changeListener1);
        synchronized (lock) {
            if (rowSelectDialog != null) {
                rowSelectDialog.close();
                rowSelectDialog.dispose();
            }
            if (functionOptions != null)
                functionOptions.dispose();
            if (textOptions != null)
                textOptions.dispose();
            if (diagramOptionsDialog != null) {
                diagramOptionsDialog.dispose();
                diagramOptionsDialog = null;
            }
        }
        movingArea.setArrowState();
    }

    protected void closeTabs() {
        while (tabbedPane.getTabCount() > 0)
            closeTab((Tab) tabbedPane.getTabComponentAt(0));
    }

    public void cancelEditing() {
        synchronized (this) {
            if (!sys) {
                sys = true;
                frame.actionPerformed(new ActionEvent(this, 0,
                        MainFrame.CURSOR_TOOL));
                sys = false;
            }
        }
    }

    @Override
    public String[] getEnableActions() {
        if (actionDisable)
            return new String[]{};

        final Vector<String> res = new Vector<String>();
        res.add(MainFrame.PRINT_PREVIEW);
        res.add(MainFrame.PRINT);
        res.add(MainFrame.PAGE_SETUP);
        res.add(MainFrame.IDEF0_NET);
        res.add(MainFrame.GO_TO_PARENT);
        res.add(MainFrame.GO_TO_CHILD);
        res.add(MainFrame.EXPORT_TO_IMAGES);

        if (!isReadOnly()) {

            if (movingArea.getActiveFunction() != null
                    && movingArea.getActiveFunction().getChildCount() > 0) {
                res.add(MainFrame.CENTER_ALL_SECTORS);
            }
            if (undoMeneger != null) {
                if (undoMeneger.canUndo())
                    res.add(MainFrame.IDEF0_UNDO);
                if (undoMeneger.canRedo())
                    res.add(MainFrame.IDEF0_REDO);
            }
            res.add(MainFrame.ADD_MODEL_TO_TEMPLATE);

            res.add(MainFrame.MODEL_PROPETIES);
            res.add(MainFrame.DIAGRAM_PROPETIES);
            res.add(MainFrame.CREATE_LEVEL);

            res.add(MainFrame.CURSOR_TOOL);
            res.add(MainFrame.FUNCTION_TOOL);
            res.add(MainFrame.ARROW_TOOL);
            res.add(MainFrame.TILDA_TOOL);
            res.add(MainFrame.TEXT_TOOL);
            res.add(MainFrame.RELOAD_SAVE);
            if ((movingArea.getActiveFunction()).getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFD) {
                res.add(MainFrame.EXTERNAL_REFERENCE_TOOL);
                res.add(MainFrame.DATA_STORE_TOOL);
                for (AbstractButton ab : frame.getDfdsButtons()) {
                    if (ab.isSelected())
                        movingArea.cancelAdding();
                    ab.setVisible(false);
                }
                for (AbstractButton ab : frame.getDfdButtons())
                    ab.setVisible(true);
            } else if ((movingArea.getActiveFunction()).getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
                res.add(MainFrame.DFDS_ROLE_TOOL);
                for (AbstractButton ab : frame.getDfdButtons()) {
                    if (ab.isSelected())
                        movingArea.cancelAdding();
                    ab.setVisible(false);
                }
                for (AbstractButton ab : frame.getDfdsButtons())
                    ab.setVisible(true);
            } else {
                for (AbstractButton ab : frame.getDfdButtons()) {
                    if (ab.isSelected())
                        movingArea.cancelAdding();
                    ab.setVisible(false);
                }
                for (AbstractButton ab : frame.getDfdsButtons()) {
                    if (ab.isSelected())
                        movingArea.cancelAdding();
                    ab.setVisible(false);
                }
            }
            if (movingArea.getActiveObject() instanceof PaintSector.Pin) {
                res.add(MainFrame.SET_LOOK_FOR_CHILDRENS);
                res.add(MainFrame.SET_LOOK_FOR_CHILDRENS_BACKGROUND);
                res.add(MainFrame.SET_LOOK_FOR_CHILDRENS_FONT);
                res.add(MainFrame.SET_LOOK_FOR_CHILDRENS_FOREGROUND);
                res.add(MainFrame.REMOVE);
                final PaintSector.Pin pin = (PaintSector.Pin) movingArea
                        .getActiveObject();
                if (pin.getSector().getStream() != null
                        && pin.getSector().getStream().getAdded().length > 0)
                    res.add(MainFrame.ARROW_COPY_VISUAL);

                if (pin.getSector().getText() != null) {
                    res.add(MainFrame.SET_ARROW_TILDA);
                    getJCheckBoxMenuItem().setSelected(
                            pin.getSector().isShowTilda());
                    res.add(MainFrame.SET_TRANSPARENT_ARROW_TEXT);
                    getJCheckBoxMenuItem1().setSelected(
                            pin.getSector().isTransparent());
                }
                if (pin.getSector().isSelStart()
                        && pin.getSector().getStartTunnelType() != Crosspoint.TUNNEL_NONE
                        || pin.getSector().isSelEnd()
                        && pin.getSector().getEndTunnelType() != Crosspoint.TUNNEL_NONE)
                    res.add(MainFrame.TUNNEL_ARROW);
                res.add(MainFrame.EDIT);
            } else if (movingArea.getActiveObject() instanceof MovingFunction) {
                final Function function = ((MovingFunction) movingArea
                        .getActiveObject()).getFunction();
                if (function.isRemoveable())
                    res.add(MainFrame.REMOVE);
                addParalelActions(res, function);

                res.add(MainFrame.FUNCTION_TYPE);
                res.add(MainFrame.FUNCTION_TYPE_ACTION);
                res.add(MainFrame.FUNCTION_TYPE_OPERATION);
                res.add(MainFrame.FUNCTION_TYPE_KOMPLEX);
                res.add(MainFrame.FUNCTION_TYPE_PROCESS);
                res.add(MainFrame.FUNCTION_TYPE_PROCESS_PART);
                res.add(MainFrame.RENAME);
                res.add(MainFrame.COPY);
                res.add(MainFrame.CUT);
                switch (function.getType()) {
                    case Function.TYPE_ACTION: {
                        getJRadioButtonMenuItemAction().setSelected(true);
                        frame.getJMenuItemAction().setSelected(true);
                    }
                    break;
                    case Function.TYPE_OPERATION: {
                        getJRadioButtonMenuItemOperation().setSelected(true);
                        frame.getJMenuItemOperation().setSelected(true);
                    }
                    break;
                    case Function.TYPE_PROCESS_PART: {
                        getJRadioButtonMenuItemProcessPart().setSelected(true);
                        frame.getJMenuItemProcessPart().setSelected(true);
                    }
                    break;
                    case Function.TYPE_PROCESS: {
                        getJRadioButtonMenuItemProcess().setSelected(true);
                        frame.getJMenuItemProcess().setSelected(true);
                    }
                    break;
                    case Function.TYPE_PROCESS_KOMPLEX: {
                        getJRadioButtonMenuItemKomplex().setSelected(true);
                        frame.getJMenuItemKomplex().setSelected(true);
                    }
                    break;
                }

                res.add(MainFrame.CENTER_ADDED_SECTORS);

                final boolean b = !dataPlugin.getBaseFunction().equals(
                        function.getParentRow());
                if (b)
                    res.add(MainFrame.FUNCTION_MOVE);
                res.add(MainFrame.FUNCTION_ADD_LEVEL);
                res.add(MainFrame.FUNCTION_REMOVE_LEVEL);
                res.add(MainFrame.SET_LOOK_FOR_CHILDRENS);
                res.add(MainFrame.SET_LOOK_FOR_CHILDRENS_BACKGROUND);
                res.add(MainFrame.SET_LOOK_FOR_CHILDRENS_FONT);
                res.add(MainFrame.SET_LOOK_FOR_CHILDRENS_FOREGROUND);
                res.add(MainFrame.FUNCTION_ADD_LEVEL);
                if (function.getChildCount() > 0) {
                    res.add(MainFrame.OPEN_TAB);
                    res.add(MainFrame.OPEN_IN_INNER_TAB);
                }
                res.add(MainFrame.EDIT);
            } else if (movingArea.getActiveObject() instanceof MovingText) {
                if (!(movingArea.getActiveObject() instanceof MovingLabel))
                    res.add(MainFrame.EDIT);
                if (movingArea.canRenameActive())
                    res.add(MainFrame.RENAME);
                res.add(MainFrame.REMOVE);
                if (movingArea.getActiveObject() instanceof DFDSRole) {
                    if (((DFDSRole) movingArea.getActiveObject()).getFunction()
                            .getOwner() != null)
                        res.add(MainFrame.BRAKE_DFDSROLE_CONNECTION);
                    res.add(MainFrame.DFDS_ROLE_COPY_VISUAL);
                }
            } else
                res.add(MainFrame.PASTE);

            if (getMovingArea().getMouseSelection() != null) {
                if (getMovingArea().getMouseSelection().getFunctions().size() > 0) {
                    res.add(MainFrame.REMOVE);
                    res.add(MainFrame.COPY);
                    res.add(MainFrame.CUT);
                    res.add(MainFrame.VISUAL_OPTIONS);
                }
                if (getMovingArea().getMouseSelection().getLabels().size() > 1)
                    res.add(MainFrame.JOIN_ARROWS);
            }
        }

        return res.toArray(new String[res.size()]);
    }

    public boolean isReadOnly() {
        return dataPlugin.isReadOnly();
    }

    private void addParalelActions(final Vector<String> res,
                                   final Function function) {
        final boolean b = function.isHaveChilds();
        if (b)
            res.add(MainFrame.CREATE_PARALEL);
        res.add(MainFrame.LOAD_FROM_PARALEL);
    }

    /**
     * This method initializes jMenuItemOpenTab
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemOpenTab() {
        if (jMenuItemOpenTab == null) {
            jMenuItemOpenTab = new JMenuItem();
            jMenuItemOpenTab.setAction(frame.findAction(MainFrame.OPEN_TAB));
        }
        return jMenuItemOpenTab;
    }

    protected void openInnerTab(final Function function) {
        final Tab t = openTabNActive(function);
        if (t != null)
            t.select();
    }

    protected Tab openTabNActive(final Function function) {
        if (function.getChildCount() == 0) {
            final Template model = getContextMasterDialog().showModal();
            if (model != null) {
                model.createChilds(function, dataPlugin);
            }
        }
        if (function.getChildCount() == 0)
            return null;
        Tab x = null;
        if (tabbedPane.getTabCount() == 0) {
            x = createTab(movingArea.getActiveFunction());
        }
        final Tab tab = createTab(function);
        if (x != null)
            x.getPanel().add(getJScrollPane1(), BorderLayout.CENTER);
        return tab;
    }

    private Tab simpleCreateTab(final Function function) {
        if (function == null)
            return null;

        if (tabbedPane.getTabCount() == 0) {
            this.remove(getJScrollPane1());
            this.add(tabbedPane, BorderLayout.CENTER);
        }

        final Tab tab = new Tab(function, tabbedPane);
        tab.getCloseButton().addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                closeTab(tab);
            }

        });

        tabbedPane.addTab(tab.getText(), tab.getPanel());
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tab);
        // getJPanel2().add(tab);
        return tab;
    }

    private Tab createTab(final Function function) {
        final Tab res = simpleCreateTab(function);
        setTabs();
        return res;
    }

    protected void closeTab(final Tab tab) {
        final int i = tabbedPane.indexOfTabComponent(tab);
        if (i >= 0) {
            tabbedPane.remove(i);
            if (tabbedPane.getTabCount() == 1) {
                tabbedPane.remove(0);
                activeTab = null;
                this.remove(tabbedPane);
                this.add(getJScrollPane1(), BorderLayout.CENTER);
            }
        }
    }

    private void setTabs() {
		/*
		 * int l = tabbedPane.getTabCount(); for (int i = 0; i < l; i++) { Tab
		 * tab = (Tab) tabbedPane.getTabComponentAt(i); int j = i + 1; if (j <=
		 * 9) tab.setMnemonic(Integer.toString(j).charAt(0)); else if (j == 10)
		 * tab.setMnemonic(Integer.toString(0).charAt(0)); }
		 */

    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jPanel.add(getJPanel2(), BorderLayout.WEST);
            jPanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(final ComponentEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setTabs();
                        }
                    });
                }
            });
        }
        return jPanel;
    }

    public void setMovingActiveFunction(final Function activeFunction) {
        if (activeTab != null)
            activeTab.setFunction(activeFunction);

    }

    /**
     * This method initializes jMenuItemCreateParalel
     *
     * @return javax.swing.JMenuItem
     */
    protected JMenuItem getJMenuItemCreateParalel() {
        if (jMenuItemCreateParalel == null) {
            jMenuItemCreateParalel = new JMenuItem();
            jMenuItemCreateParalel.setAction(frame
                    .findAction(MainFrame.CREATE_PARALEL));
        }
        return jMenuItemCreateParalel;
    }

    protected void createParalel() {
        Function f = null;
        try {

            final JFileChooser chooser = new JFileChooser() {
                @Override
                public void approveSelection() {
                    if (getSelectedFile().exists()) {
                        if (JOptionPane
                                .showConfirmDialog(
                                        framework.getMainFrame(),
                                        GlobalResourcesManager
                                                .getString("File.Exists"),
                                        UIManager
                                                .getString("OptionPane.messageDialogTitle"),
                                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                            return;
                    }
                    super.approveSelection();
                }
            };
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                final CreateParalelDialog dialog = new CreateParalelDialog(
                        framework.getMainFrame());
                if (!dialog.showModal())
                    return;
                String fn = chooser.getSelectedFile().getAbsolutePath();
                if (!fn.substring(fn.length() - frame.FILE_EX.length())
                        .equalsIgnoreCase(frame.FILE_EX))
                    fn += frame.FILE_EX;
                f = ((MovingFunction) movingArea.getActiveObject())
                        .getFunction();
                frame.propertyChange(MChangeListener.FILE_SAVE, null);
                boolean clearFunctionalBlock = dialog.getCreateParalelModel()
                        .isClearFunctionalBlock();
                dataPlugin.createParalel(f, dialog.getCreateParalelModel()
                                .isCopyAllRows(), clearFunctionalBlock, new File(fn),
                        framework, movingArea.getDataPlugin());
                movingArea.repaint();
            }

        } catch (final IOException e) {
            JOptionPane.showMessageDialog(framework.getMainFrame(),
                    e.getLocalizedMessage());
        }
    }

    /**
     * This method initializes jMenuLoadFromParalel
     *
     * @return javax.swing.JMenu
     */
    protected JMenuItem getJMenuLoadFromParalel() {
        if (jMenuLoadFromParalel == null) {
            jMenuLoadFromParalel = new JMenuItem();
            jMenuLoadFromParalel.setAction(frame
                    .findAction(MainFrame.LOAD_FROM_PARALEL));

        }
        return jMenuLoadFromParalel;
    }

    protected void loadFromParalel() {
        try {
            final JFileChooser chooser = frame.getChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

                final Function f = ((MovingFunction) movingArea
                        .getActiveObject()).getFunction();

                frame.propertyChange(MChangeListener.FILE_SAVE, null);
                dataPlugin.loadFromParalel(dataPlugin, f,
                        chooser.getSelectedFile(), framework);
                frame.propertyChange(MChangeListener.FILE_LOAD, null);
            }
        } catch (final IOException e) {
            JOptionPane.showMessageDialog(framework.getMainFrame(),
                    e.getLocalizedMessage());
        }

    }

    @Override
    public MainFrame getFrame() {
        return frame;
    }

    public void savePosiotion() {
        final OutputStream stream = dataPlugin.setNamedData("system/idef0.pos");
        try {
            final int l = tabbedPane.getTabCount();
            DataSaver.saveInteger(stream, 0);// version
            DataSaver.saveInteger(stream, l);
            final int selectedButton = tabbedPane.getSelectedIndex();
            for (int i = 0; i < l; i++) {
                final Tab tab = (Tab) tabbedPane.getTabComponentAt(i);
                tab.getFunction().getGlobalId().saveToStream(stream);
            }
            if (l > 0)
                DataSaver.saveInteger(stream, selectedButton);
            else
                movingArea.getActiveFunction().getGlobalId()
                        .saveToStream(stream);
            stream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPosiotion() {
        final InputStream stream = dataPlugin.getNamedData("system/idef0.pos");
        closeTabs();
        movingArea.clear();

        if (stream == null) {
            movingArea.setActiveFunction(dataPlugin.getBaseFunction());
        } else {
            try {
                DataLoader.readInteger(stream);
                final int l = DataLoader.readInteger(stream);
                if (l < 0)
                    return;
                final Tab[] tabs = new Tab[l];
                for (int i = 0; i < l; i++) {
                    final GlobalId id = new GlobalId(stream);
                    tabs[i] = simpleCreateTab((Function) dataPlugin
                            .findRowByGlobalId(id));

                }
                if (l > 0) {
                    final int s = DataLoader.readInteger(stream);
                    tabs[s].getPanel().add(getJScrollPane1(),
                            BorderLayout.CENTER);
                    tabs[s].select();
                    movingArea.setActiveFunction(tabs[s].getFunction());
                } else {
                    final GlobalId id = new GlobalId(stream);
                    Function active = (Function) dataPlugin
                            .findRowByGlobalId(id);
                    if (active == null)
                        active = dataPlugin.getBaseFunction();
                    movingArea.setActiveFunction(active);
                }
                stream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        repaint();
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if (MainFrame.PRINT.equals(cmd)) {
            movingArea.setActiveFunction(movingArea.getActiveFunction());
            new IDEF0Printable(movingArea.dataPlugin, framework).print(
                    framework.getMainFrame(), dataPlugin);
        } else if (MainFrame.PAGE_SETUP.equals(cmd))
            new IDEF0Printable(movingArea.dataPlugin, framework)
                    .pageSetup(framework);
        else if (MainFrame.PRINT_PREVIEW.equals(cmd)) {
            movingArea.setActiveFunction(movingArea.getActiveFunction());
            final IDEF0Printable printable = new IDEF0Printable(dataPlugin,
                    framework);
            PrintDialog dialog = new PrintDialog(dataPlugin, framework) {
                @Override
                protected void onOk() {
                    printable.setPrintFunctions(panel.getSelectedFunctions());
                    setVisible(false);
                }
            };
            dialog.showModal(printable);
            framework.printPreview(printable);
        } else if (MainFrame.REMOVE.equals(cmd))
            remove();
        else if (MainFrame.EDIT.equals(cmd))
            edit();
        else if (MainFrame.FUNCTION_ADD_LEVEL.equals(cmd))
            addLevel();
        else if (MainFrame.FUNCTION_REMOVE_LEVEL.equals(cmd))
            removeLevel();
        else if (MainFrame.OPEN_IN_INNER_TAB.equals(cmd))
            openInnerTab();
        else if (MainFrame.OPEN_TAB.equals(cmd))
            openTab();
        else if (MainFrame.SET_TRANSPARENT_ARROW_TEXT.equals(cmd))
            movingArea.setTransparent();
        else if (MainFrame.SET_LOOK_FOR_CHILDRENS.equals(cmd))
            movingArea.setLookForChildrens();
        else if (MainFrame.SET_LOOK_FOR_CHILDRENS_BACKGROUND.equals(cmd))
            movingArea.setLookForChildrensBackground();
        else if (MainFrame.SET_LOOK_FOR_CHILDRENS_FONT.equals(cmd))
            movingArea.setLookForChildrensFont();
        else if (MainFrame.SET_LOOK_FOR_CHILDRENS_FOREGROUND.equals(cmd))
            movingArea.setLookForChildrensForeground();
        else if (MainFrame.TUNNEL_ARROW.equals(cmd))
            movingArea.tunelArrow();
        else if (MainFrame.FUNCTION_TYPE_KOMPLEX.equals(cmd))
            setFunctionType(Function.TYPE_PROCESS_KOMPLEX);
        else if (MainFrame.FUNCTION_TYPE_OPERATION.equals(cmd))
            setFunctionType(Function.TYPE_OPERATION);
        else if (MainFrame.FUNCTION_TYPE_PROCESS.equals(cmd))
            setFunctionType(Function.TYPE_PROCESS);
        else if (MainFrame.FUNCTION_TYPE_PROCESS_PART.equals(cmd))
            setFunctionType(Function.TYPE_PROCESS_PART);
        else if (MainFrame.FUNCTION_TYPE_ACTION.equals(cmd))
            setFunctionType(Function.TYPE_ACTION);
        else if (MainFrame.SET_TRANSPARENT_ARROW_TEXT.equals(cmd))
            movingArea.setTransparent();
        else if (MainFrame.CENTER_ADDED_SECTORS.equals(cmd))
            movingArea.centerAdderSectors();
        else if (MainFrame.TUNNEL_ARROW.equals(cmd))
            movingArea.tunelArrow();
        else if (MainFrame.LOAD_FROM_PARALEL.equals(cmd))
            loadFromParalel();
        else if (MainFrame.CREATE_PARALEL.equals(cmd))
            createParalel();
        else if (MainFrame.SET_ARROW_TILDA.equals(cmd))
            movingArea.setShowTilda();
        else if (MainFrame.ADD_MODEL_TO_TEMPLATE.equals(cmd))
            movingArea.addModelToTemplate();
        else if (MainFrame.CENTER_ALL_SECTORS.equals(cmd))
            movingArea.centerAllSectors();
        else if (MainFrame.GO_TO_PARENT.equals(cmd))
            movingArea.up();
        else if (MainFrame.GO_TO_CHILD.equals(cmd))
            movingArea.down();
        else if (MainFrame.MODEL_PROPETIES.equals(cmd))
            modelProperties();
        else if (MainFrame.DIAGRAM_PROPETIES.equals(cmd))
            diagramProperties();
        else if (MainFrame.BRAKE_DFDSROLE_CONNECTION.equals(cmd))
            braekDFDSRoleConnection();
        else if (MainFrame.EXPORT_TO_IMAGES.equals(cmd)) {
            new ExportToImagesDialog(framework.getMainFrame(), dataPlugin)
                    .setVisible(true);
        } else if (MainFrame.COPY.equals(cmd))
            movingArea.copy();
        else if (MainFrame.PASTE.equals(cmd))
            movingArea.paste();
        else if (MainFrame.CUT.equals(cmd))
            movingArea.cut();
        else if (MainFrame.VISUAL_OPTIONS.equals(cmd))
            movingArea.visualOptions();
        else if (MainFrame.CREATE_LEVEL.equals(cmd))
            movingArea.createLevel();
        else if (MainFrame.DFDS_ROLE_COPY_VISUAL.equals(cmd))
            movingArea.DFDSRoleCopyVisual();
        else if (MainFrame.JOIN_ARROWS.equals(cmd))
            movingArea.joinArrows();
        else if (MainFrame.ARROW_COPY_VISUAL.equals(cmd))
            movingArea.arrowCopyVisual();
    }

    private void braekDFDSRoleConnection() {
        movingArea.startUserTransaction();
        ((DFDSRole) movingArea.getActiveObject()).getFunction().setOwner(null);
        movingArea.commitUserTransaction();
    }

    private void openTab() {
        Function function = ((MovingFunction) movingArea.getActiveObject())
                .getFunction();
        openTab(function);
    }

    protected void openTab(Function function) {
        throw new RuntimeException("Method must be overrided");
    }

    private void openInnerTab() {
        openInnerTab(((MovingFunction) movingArea.getActiveObject())
                .getFunction());
    }

    @Override
    public String getTitleKey() {
        return IDEF0_EDITOR;
    }

    public ContextMasterDialog getContextMasterDialog() {
        if (contextMasterDialog == null) {
            contextMasterDialog = new ContextMasterDialog(
                    framework.getMainFrame());
        }
        return contextMasterDialog;
    }

    private OwnerClasificatorsDialog getOwnerClasificatorsDialog() {
        if (ownerClasificatorsDialog == null) {
            ownerClasificatorsDialog = new OwnerClasificatorsDialog(
                    framework.getMainFrame(), dataPlugin);
        }
        return ownerClasificatorsDialog;
    }

    public Zoom getActiveZoom() {
        return activeZoom;
    }

    public GUIFramework getFramework() {
        return framework;
    }

    public View getView() {
        return view;
    }

    private void modelProperties() {
        Qualifier qualifier = ((NFunction) movingArea.getActiveFunction())
                .getQualifier();
        if (qualifier.isSystem()) {
            qualifier = dataPlugin.getBaseFunctionQualifier();
        }
        ModelPropertiesDialog dialog = new ModelPropertiesDialog(framework,
                qualifier, dataPlugin.getEngine(), dataPlugin.getAccessRules());
        dialog.setVisible(true);
    }

    private void diagramProperties() {
        if (diagramOptionsDialog == null)
            diagramOptionsDialog = new DiagramOptionsDialog(framework);
        diagramOptionsDialog.showModal((NFunction) movingArea
                .getActiveFunction());
    }

    public DFDObjectDialog getDFDObjectOptionsDialog() {
        if (dfdObjectDialog == null) {
            dfdObjectDialog = new DFDObjectDialog(framework, dataPlugin);
        }
        return dfdObjectDialog;
    }

    /**
     * @param actionDisable the actionDisable to set
     */
    public void setActionDisable(boolean actionDisable) {
        this.actionDisable = actionDisable;
        frame.setActiveView(this);
    }

    /**
     * @return the actionDisable
     */
    public boolean isActionDisable() {
        return actionDisable;
    }

    public DFDSRoleOptionsDialog getDFDSRoleOptionsDialog() {
        if (dfdsRoleOptionsDialog == null) {
            dfdsRoleOptionsDialog = new DFDSRoleOptionsDialog(framework,
                    dataPlugin);
        }
        return dfdsRoleOptionsDialog;
    }

    public AbstractAction getHandAction() {
        return null;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
