package com.ramussoft.pb.frames;

import static com.ramussoft.gui.common.GUIFramework.BUTTON_GROUP;
import static com.ramussoft.gui.common.GUIFramework.BUTTON_GROUP_ADDED;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.pb.frames.components.SerialCheker;
import com.ramussoft.pb.frames.docking.MChangeListener;
import com.ramussoft.pb.frames.docking.ViewPanel;
import com.ramussoft.pb.frames.setup.ViewOptionsDialog;
import com.ramussoft.pb.idef.elements.DemoChecker;
import com.ramussoft.pb.idef.frames.IDEFPanel;
import com.ramussoft.pb.idef.frames.UserTemplatesDialog;
import com.ramussoft.pb.idef.frames.IDEFPanel.Zoom;
import com.ramussoft.pb.types.GlobalId;

/**
 * Головне вікно програми.
 *
 * @author Яковчук В.В.
 */

public class MainFrame implements ActionListener {

    public static Image mainIcon = Toolkit.getDefaultToolkit().getImage(
            MainFrame.class.getResource("/images/main.gif"));

    public ImageIcon clasificatorIcon = new ImageIcon(getClass().getResource(
            "/images/clasificatorIcon.png"));

    static public final int MAIN_MODEL = GlobalId.GLOBAL_RESERVED + 1;

    public final String FILE_EX = getFileEx(); // @jve:decl-index=0:

    public static final String NEW_PROJECT = "new_project"; // @jve:decl-index=0:

    public static final String OPEN_PROJECT = "open"; // @jve:decl-index=0:

    public static final String SAVE_PROJECT = "save"; // @jve:decl-index=0:

    private static final String SAVE_PROJECT_AS = "save_as"; // @jve:decl-index=0:

    public static final String PAGE_SETUP = "page_setup"; // @jve:decl-index=0:

    public static final String PRINT_PREVIEW = "page_preview"; // @jve:decl-index=0:

    public static final String PRINT = "print"; // @jve:decl-index=0:

    public static final String EXIT = "exit"; // @jve:decl-index=0:

    public static final String OPEN_WEB_SERVER = "open_web_server"; // @jve:decl-index=0:

    public static final String EXPORT_REPORT_TO_XML = "export_report_to_xml"; // @jve:decl-index=0:

    public static final String IMPORT_REPORT_FROM_XML = "import_report_from_xml"; // @jve:decl-index=0:

    public static final String USER_TEMPLATES = "User.Templates";

    public static final String ADD = "add"; // @jve:decl-index=0:

    public static final String EDIT = "edit_active"; // @jve:decl-index=0:

    public static final String BRAKE_DFDSROLE_CONNECTION = "break_dfdsrole_connection"; // @jve:decl-index=0:

    public static final String INSERT = "insert_rows"; // @jve:decl-index=0:

    public static final String ADD_CHILD = "add_child"; // @jve:decl-index=0:

    public static final String LEVEL_UP = "level_up"; // @jve:decl-index=0:

    public static final String LEVEL_DOWN = "level_down"; // @jve:decl-index=0:

    public static final String CREATE_LEVEL = "create_level";

	/*
     * public static final String MOVE_ROW_UP = "moveRowUp"; //
	 * 
	 * @jve:decl-index=0:
	 * 
	 * public static final String MOVE_ROW_DOWN = "moveRowDown"; //
	 * 
	 * @jve:decl-index=0:
	 */

    public static final String VISUAL_OPTIONS = "VisualPanel.options";

    public static final String REMOVE = "delete_row"; // @jve:decl-index=0:

    public static final String JOIN_ROWS = "joinRows";

    public static final String JOIN_ARROWS = "JoinArrows";

    public static final String CURSOR_TOOL = "cursor_tool"; // @jve:decl-index=0:

    public static final String MOVE_UP = "MoveElementUp";

    public static final String MOVE_DOWN = "MoveElementDown";

    private final JComboBox zoomComboBox = new JComboBox();

    public static final String GENARATE = "GanarateReport";

    public static final String FUNCTION_TOOL = "function_block_tool";

    public static final String EXTERNAL_REFERENCE_TOOL = "external_reference_tool";

    public static final String DFDS_ROLE_TOOL = "dfds_role_tool";

    public static final String DFDS_ROLE_COPY_VISUAL = "dfds_role_copy_visual";

    public static final String ARROW_COPY_VISUAL = "arrow_copy_visual";

    public static final String DATA_STORE_TOOL = "data_store_tool";

    public static final String ARROW_TOOL = "arrow_tool"; // @jve:decl-index=0:

    public static final String TILDA_TOOL = "squiggle_tool"; // @jve:decl-index=0:

    public static final String TEXT_TOOL = "text_tool"; // @jve:decl-index=0:

    public static final String GO_TO_PARENT = "go_to_parent_diagram"; // @jve:decl-index=0:

    public static final String GO_TO_CHILD = "go_to_child_diagram"; // @jve:decl-index=0:

    public static final String PROJECT_OPTIONS = "project options"; // @jve:decl-index=0:

    public static final String RUN_JS_SCRIPT = "run_script";

    public static final String RELOAD_SAVE = "reload_save"; // @jve:decl-index=0:

    public static final String CONTEXT = "help.help";

    public static final String EXPORT_REPORT_TO_HTML = "export_to_html";

    public static final String PROGRAM_OPTIONS = "program_options";

    public static final String SELECT_CLEAN = "select_all_not_connecting";

    public static final String CUT = "cut";

    public static final String COPY = "copy";

    public static final String PASTE = "paste";

    public static final String MOVE_ROWS = "moveRows";

    public static final String IDEF0_UNDO = "iUndo";

    public static final String IDEF0_REDO = "iRedo";

    public static final String IDEF0_NET = "idef0Net";

    public static final String RENAME = "Action.Rename";

    // IDEF0 commands

    public static final String CENTER_ALL_SECTORS = "center_all_added_sectors";

    public static final String ADD_MODEL_TO_TEMPLATE = "add_diagram_template";

    public static final String SET_LOOK_FOR_CHILDRENS = "set_all_look_properties";

    public static final String SET_LOOK_FOR_CHILDRENS_FONT = "set_font_look_properties";

    public static final String SET_LOOK_FOR_CHILDRENS_BACKGROUND = "set_background_look_properties";

    public static final String SET_LOOK_FOR_CHILDRENS_FOREGROUND = "set_foreground_look_properties";

    public static final String TUNNEL_ARROW = "tunnel_arrow";

    public static final String SET_TRANSPARENT_ARROW_TEXT = "transparentText";

    public static final String SET_ARROW_TILDA = "tilda";

    public static final String FUNCTION_TYPE = "functional_block_type";

    public static final String FUNCTION_TYPE_KOMPLEX = "process_komplex";

    public static final String FUNCTION_TYPE_PROCESS = "process";

    public static final String FUNCTION_TYPE_PROCESS_PART = "process_part";

    public static final String FUNCTION_TYPE_OPERATION = "operation";

    public static final String FUNCTION_TYPE_ACTION = "action";

    public static final String CENTER_ADDED_SECTORS = "center_added_sectors";

    public static final String FUNCTION_MOVE = "IDEF0Panel.moveFunction";

    public static final String FUNCTION_ADD_LEVEL = "IDEF0Panel.addLevel";

    public static final String FUNCTION_REMOVE_LEVEL = "IDEF0Panel.removeLevel";

    public static final String OPEN_TAB = "open_tab";

    public static final String OPEN_IN_INNER_TAB = "open_in_inner_tab";

    public static final String CREATE_PARALEL = "createParalel";

    public static final String LOAD_FROM_PARALEL = "loadFromParalel";

    public static final String GO_NEXT = "goNext"; // @jve:decl-index=0:

    public static final String GO_BACK = "goBack"; // @jve:decl-index=0:

    public static final String GO_TO_URL = "goToUrl"; // @jve:decl-index=0:

    public static final String GO_HOME = "goHome"; // @jve:decl-index=0:

    public static final String SHOW_STREAMS = "Menu.Streams";

    public static final String MODEL_PROPETIES = "ModelProperties";

    public static final String DIAGRAM_PROPETIES = "DiagramProperties";

    public static final String EXPORT_TO_IMAGES = "ExportToImages";

    private static final String[] ALLWAYS_ENABLE_ACTIONS = getAlwayEnabled();

    private static int modelId = MAIN_MODEL;

    protected String fileName = null;

    protected String activeWorkspace; // @jve:decl-index=0:

    private final Hashtable<String, Action> actions = new Hashtable<String, Action>();

    private final Vector<ActionListener> listeners = new Vector<ActionListener>(); // @jve:decl-index=0:

    private final Vector<com.ramussoft.pb.frames.docking.MChangeListener> propertyListeners = new Vector<com.ramussoft.pb.frames.docking.MChangeListener>(); // @jve:decl-index=0:

    private ViewPanel activeView = null;

    private final Helper helper = new Helper(getHSName());

    private final Group workspaceGroup = new Group(); // @jve:decl-index=0:

    private final ButtonGroup idef0StateGroup = new ButtonGroup(); // @jve:decl-index=0:

    protected JToolBar editToolBar = null;

    protected JToolBar idef0StateToolBar = null;

    protected JMenuBar jJMenuBar = null;

    protected JMenu jMenu = null;

    protected JMenu networkMenu = null;

    private JMenuItem jMenuItem = null;

    private JMenuItem jMenuItem1 = null;

    private JMenuItem jMenuItem2 = null;

    private JMenuItem jMenuItem3 = null;

    private JMenuItem jMenuItem4 = null;

    private JMenuItem jMenuItem5 = null;

    private JMenuItem jMenuItem6 = null;

    private JMenuItem jMenuItem7 = null;

    private JMenuItem jMenuItemStartWebServer = null;

    private JMenuItem jMenuItemExportReportToHTML = null;

    private JMenu jMenu2 = null;

    private JMenuItem jMenuItemAdd = null;

    private JMenuItem jMenuItemEdit = null;

    private JMenuItem jMenuItemInsert = null;

    private JMenuItem jMenuItemRemove = null;

    private JMenuItem jMenuItemAddChild = null;

    private JMenuItem jMenuItemLevelUp = null;

    private JMenuItem jMenuItemLevelDown = null;

    private UserTemplatesDialog userTemplatesDialog = null;

    protected JFileChooser chooser = null;

    private static Vector<String> workspaceCommands;

    private ArrayList<JToggleButton> dfdButtons = new ArrayList<JToggleButton>();

    private ArrayList<JToggleButton> dfdsButtons = new ArrayList<JToggleButton>();

    private GUIFramework framework;

    protected String getFileEx() {
        return ".rsf";
    }

    protected String getHSName() {
        return "BuilderHelp.hs";
    }

    public JFileChooser getChooser() {
        if (chooser == null) {
            chooser = new JFileChooser();
            chooser.addChoosableFileFilter(odzFilter);
            chooser.addChoosableFileFilter(new PCBFilter() {

                @Override
                protected String getFE() {
                    return ".pgf";
                }

                @Override
                public String getDescription() {
                    return ResourceLoader.getString("PG.File");
                }
            });
            chooser.setFileFilter(odzFilter);
            final String path = Options.getString("lastFile");
            if (path != null)
                chooser.setSelectedFile(new File(path));
        }
        return chooser;
    }

    private static String[] getAlwayEnabled() {

        final String[] b = new String[]{NEW_PROJECT, OPEN_PROJECT,
                SAVE_PROJECT, SAVE_PROJECT_AS, EXIT, OPEN_WEB_SERVER,
                PROJECT_OPTIONS, RUN_JS_SCRIPT, CONTEXT, PROGRAM_OPTIONS,
                SHOW_STREAMS, USER_TEMPLATES};

        final Vector<String> s = new Vector<String>();
        for (final String st : b)
            s.add(st);

        // final Plugin[] plugins = Main.dataPlugin.getPlugins();
        if (workspaceCommands == null) {
            workspaceCommands = new Vector<String>();
        }

		/*
         * for (final Plugin p : plugins) { String[] wks = p.getWorkspaces();
		 * for (final String st : wks) { s.add(st); if (add)
		 * workspaceCommands.add(st); } wks = p.getAlwaysEnabled(); for (final
		 * String st : wks) s.add(st); }
		 */
        final String[] res = new String[s.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = s.get(i);
        return res;
    }

    /**
     * Метод знаходить Action, за його назвою.
     *
     * @param name Назва команди.
     * @return Налаштування для команди.
     */

    public Action findAction(final String name) {
        return actions.get(name);
    }

    public Action createAction(final String name, final String label,
                               final String describe, final Icon icon, final KeyStroke stroke) {
        final AbstractAction action = new AbstractAction(label, icon) {

            public void actionPerformed(ActionEvent e) {
                MainFrame.this.actionPerformed(e);
            }

        };
        actions.put(name, action);
        action.putValue(Action.ACTION_COMMAND_KEY, name);
        if (describe != null)
            action.putValue(Action.SHORT_DESCRIPTION, describe);
        if (stroke != null)
            action.putValue(Action.ACCELERATOR_KEY, stroke);

        return action;
    }

    public void addMChangeListener(
            final com.ramussoft.pb.frames.docking.MChangeListener listener) {
        synchronized (this) {
            propertyListeners.add(listener);
        }
    }

    public void removeMChangeListener(
            final com.ramussoft.pb.frames.docking.MChangeListener listener) {
        synchronized (this) {
            propertyListeners.remove(listener);
        }
    }

    /**
     * Метод, який викликає аналогічний метод для всіх лістенерів.
     *
     * @param propertyName
     * @param newObject
     */

    public void propertyChange(final String propertyName, final Object newObject) {
        for (int i = 0; i < propertyListeners.size(); i++)
            propertyListeners.get(i).propertyChange(propertyName, newObject);
    }

    protected void createActions() {
        createAction(NEW_PROJECT, ResourceLoader.getString(NEW_PROJECT),
                ResourceLoader.getString(NEW_PROJECT), new ImageIcon(getClass()
                        .getResource("/images/new.png")),
                KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        createAction(OPEN_PROJECT, ResourceLoader.getString(OPEN_PROJECT),
                ResourceLoader.getString(OPEN_PROJECT), new ImageIcon(
                        getClass().getResource("/images/open.png")),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        createAction(SAVE_PROJECT, ResourceLoader.getString(SAVE_PROJECT),
                ResourceLoader.getString(SAVE_PROJECT), new ImageIcon(
                        getClass().getResource("/images/save.png")),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        createAction(SAVE_PROJECT_AS,
                ResourceLoader.getString(SAVE_PROJECT_AS),
                ResourceLoader.getString(SAVE_PROJECT_AS), null, null);

        createAction(EXIT, ResourceLoader.getString(EXIT),
                ResourceLoader.getString(EXIT), null,
                KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));

        createAction(OPEN_WEB_SERVER,
                ResourceLoader.getString(OPEN_WEB_SERVER),
                ResourceLoader.getString(OPEN_WEB_SERVER), null, null);

        createAction(ADD, ResourceLoader.getString(ADD),
                ResourceLoader.getString(ADD), new ImageIcon(getClass()
                        .getResource("/images/add.png")),
                KeyStroke.getKeyStroke(KeyEvent.VK_ADD, ActionEvent.CTRL_MASK));

        createAction(REMOVE, ResourceLoader.getString(REMOVE),
                ResourceLoader.getString(REMOVE), new ImageIcon(getClass()
                        .getResource("/images/remove.png")),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

        createAction(EDIT, ResourceLoader.getString(EDIT),
                ResourceLoader.getString(EDIT), new ImageIcon(getClass()
                        .getResource("/images/edit.png")), null);

        createAction(BRAKE_DFDSROLE_CONNECTION);
        createAction(INSERT, ResourceLoader.getString(INSERT),
                ResourceLoader.getString(INSERT), new ImageIcon(getClass()
                        .getResource("/images/insert.png")),
                KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK));
        createAction(ADD_CHILD, ResourceLoader.getString(ADD_CHILD),
                ResourceLoader.getString(ADD_CHILD), new ImageIcon(getClass()
                        .getResource("/images/add_child.png")),
                KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        createAction(LEVEL_UP, ResourceLoader.getString(LEVEL_UP),
                ResourceLoader.getString(LEVEL_UP), new ImageIcon(getClass()
                        .getResource("/images/left.png")),
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.CTRL_MASK));

        createAction(LEVEL_DOWN, ResourceLoader.getString(LEVEL_DOWN),
                ResourceLoader.getString(LEVEL_DOWN), new ImageIcon(getClass()
                        .getResource("/images/right.png")),
                KeyStroke
                        .getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.CTRL_MASK));
        createAction(ADD_MODEL_TO_TEMPLATE);

        createAction(USER_TEMPLATES);

        createAction(MODEL_PROPETIES);

        createAction(DIAGRAM_PROPETIES);

        createAction(EXPORT_TO_IMAGES);

        createAction(VISUAL_OPTIONS);

        createAction(CREATE_LEVEL);

		/*
		 * createAction(MOVE_ROW_UP, ResourceLoader.getString(MOVE_ROW_UP),
		 * ResourceLoader.getString(MOVE_ROW_UP), new ImageIcon(getClass()
		 * .getResource("/images/top.png")), KeyStroke
		 * .getKeyStroke(KeyEvent.VK_UP, ActionEvent.CTRL_MASK));
		 * 
		 * createAction(MOVE_ROW_DOWN, ResourceLoader.getString(MOVE_ROW_DOWN),
		 * ResourceLoader.getString(MOVE_ROW_DOWN), new ImageIcon(
		 * getClass().getResource("/images/bottom.png")),
		 * KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.CTRL_MASK));
		 */

        createAction(SELECT_CLEAN, ResourceLoader.getString(SELECT_CLEAN),
                ResourceLoader.getString(SELECT_CLEAN), new ImageIcon(
                        getClass().getResource("/images/sel_ather.png")), null);

        createAction(CUT, ResourceLoader.getString(CUT),
                ResourceLoader.getString(CUT), new ImageIcon(getClass()
                        .getResource("/images/cut.png")),
                KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));

        createAction(COPY, ResourceLoader.getString(COPY),
                ResourceLoader.getString(COPY), new ImageIcon(getClass()
                        .getResource("/images/copy.png")),
                KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));

        createAction(PASTE, ResourceLoader.getString(PASTE),
                ResourceLoader.getString(PASTE), new ImageIcon(getClass()
                        .getResource("/images/paste.png")),
                KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));

        createAction(MOVE_ROWS);

        createAction(JOIN_ROWS);

        // --------IDEF0-------------

        createAction(IDEF0_UNDO,
                new ImageIcon(getClass().getResource("/images/iUndo.png")),
                KeyStroke
                        .getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));

        createAction(IDEF0_REDO,
                new ImageIcon(getClass().getResource("/images/iRedo.png")),
                KeyStroke
                        .getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));

        createAction(FUNCTION_TOOL, ResourceLoader.getString(FUNCTION_TOOL),
                ResourceLoader.getString(FUNCTION_TOOL), new ImageIcon(
                        getClass().getResource("/images/block.gif")),
                KeyStroke.getKeyStroke(KeyEvent.VK_X, 0));

        createAction(CURSOR_TOOL, ResourceLoader.getString(CURSOR_TOOL),
                ResourceLoader.getString(CURSOR_TOOL), new ImageIcon(getClass()
                        .getResource("/images/cursor.gif")),
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0));

		/*
		 * createAction(FUNCTION_TOOL, ResourceLoader.getString(FUNCTION_TOOL),
		 * ResourceLoader.getString(FUNCTION_TOOL), new ImageIcon(
		 * getClass().getResource("/images/block.gif")), KeyStroke
		 * .getKeyStroke(KeyEvent.VK_X, 0));
		 */

        createAction(ARROW_TOOL, ResourceLoader.getString(ARROW_TOOL),
                ResourceLoader.getString(ARROW_TOOL), new ImageIcon(getClass()
                        .getResource("/images/arrow.gif")),
                KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));

        createAction(TILDA_TOOL, ResourceLoader.getString(TILDA_TOOL),
                ResourceLoader.getString(TILDA_TOOL), new ImageIcon(getClass()
                        .getResource("/images/tilda.gif")),
                KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));

        createAction(TEXT_TOOL, ResourceLoader.getString(TEXT_TOOL),
                ResourceLoader.getString(TEXT_TOOL), new ImageIcon(getClass()
                        .getResource("/images/text.gif")),
                KeyStroke.getKeyStroke(KeyEvent.VK_B, 0));

        createAction(
                EXTERNAL_REFERENCE_TOOL,
                ResourceLoader.getString(EXTERNAL_REFERENCE_TOOL),
                ResourceLoader.getString(EXTERNAL_REFERENCE_TOOL),
                new ImageIcon(getClass().getResource(
                        "/images/external-reference.gif")), null);

        createAction(DFDS_ROLE_TOOL, ResourceLoader.getString(DFDS_ROLE_TOOL),
                ResourceLoader.getString(DFDS_ROLE_TOOL), new ImageIcon(
                        getClass().getResource("/images/role.png")), null);

        createAction(DFDS_ROLE_COPY_VISUAL);

        createAction(ARROW_COPY_VISUAL);

        createAction(DATA_STORE_TOOL,
                ResourceLoader.getString(DATA_STORE_TOOL),
                ResourceLoader.getString(DATA_STORE_TOOL), new ImageIcon(
                        getClass().getResource("/images/data-store.gif")), null);

        createAction(GO_TO_PARENT, ResourceLoader.getString(GO_TO_PARENT),
                ResourceLoader.getString(GO_TO_PARENT), new ImageIcon(
                        getClass().getResource("/images/up.gif")), null);

        createAction(GO_TO_CHILD, ResourceLoader.getString(GO_TO_CHILD),
                ResourceLoader.getString(GO_TO_CHILD), new ImageIcon(getClass()
                        .getResource("/images/down.gif")), null);

        createAction(RELOAD_SAVE, ResourceLoader.getString(RELOAD_SAVE),
                ResourceLoader.getString(RELOAD_SAVE), new ImageIcon(getClass()
                        .getResource("/images/refresh.png")), null);

        createAction(IDEF0_NET, ResourceLoader.getString(IDEF0_NET),
                ResourceLoader.getString(IDEF0_NET), new ImageIcon(getClass()
                        .getResource("/images/net.png")),
                KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));

        createAction(CENTER_ALL_SECTORS,
                ResourceLoader.getString(CENTER_ALL_SECTORS),
                ResourceLoader.getString(CENTER_ALL_SECTORS), null,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));

        createAction(SET_LOOK_FOR_CHILDRENS);
        createAction(SET_LOOK_FOR_CHILDRENS_BACKGROUND);
        createAction(SET_LOOK_FOR_CHILDRENS_FONT);
        createAction(SET_LOOK_FOR_CHILDRENS_FOREGROUND);

        createAction(RENAME, null, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));

        createAction(TUNNEL_ARROW);

        createAction(SET_TRANSPARENT_ARROW_TEXT);

        createAction(SET_ARROW_TILDA);

        createAction(FUNCTION_TYPE);

        createAction(FUNCTION_TYPE_KOMPLEX);

        createAction(FUNCTION_TYPE_PROCESS);

        createAction(FUNCTION_TYPE_PROCESS_PART);

        createAction(FUNCTION_TYPE_OPERATION);

        createAction(FUNCTION_TYPE_ACTION);

        createAction(CENTER_ADDED_SECTORS);

        createAction(FUNCTION_MOVE);

        createAction(FUNCTION_ADD_LEVEL);

        createAction(FUNCTION_REMOVE_LEVEL);

        createAction(OPEN_TAB);

        createAction(OPEN_IN_INNER_TAB);

        createAction(CREATE_PARALEL);

        createAction(LOAD_FROM_PARALEL);

        createAction(SHOW_STREAMS);

        createAction(JOIN_ARROWS);

        // --------------create workspace actions--------------

        // --------------ather-------------
        createAction(PROJECT_OPTIONS,
                ResourceLoader.getString(PROJECT_OPTIONS),
                ResourceLoader.getString(PROJECT_OPTIONS), null, null);

        createAction(RUN_JS_SCRIPT, ResourceLoader.getString(RUN_JS_SCRIPT),
                ResourceLoader.getString(RUN_JS_SCRIPT), null, null);

        createAction(PROGRAM_OPTIONS,
                ResourceLoader.getString(PROGRAM_OPTIONS),
                ResourceLoader.getString(PROGRAM_OPTIONS), null, null);
        createAction(EXPORT_REPORT_TO_HTML,
                ResourceLoader.getString(EXPORT_REPORT_TO_HTML),
                ResourceLoader.getString(EXPORT_REPORT_TO_HTML), null, null);
        // --------------report tool bar--

        createAction(MOVE_UP, ResourceLoader.getString(MOVE_UP),
                ResourceLoader.getString(MOVE_UP), new ImageIcon(getClass()
                        .getResource("/images/top.png")), null);

        createAction(MOVE_DOWN, ResourceLoader.getString(MOVE_DOWN),
                ResourceLoader.getString(MOVE_DOWN), new ImageIcon(getClass()
                        .getResource("/images/bottom.png")), null);

        createAction(GENARATE, ResourceLoader.getString(GENARATE),
                ResourceLoader.getString(GENARATE), new ImageIcon(getClass()
                        .getResource("/images/build.png")), null);

        createAction(IMPORT_REPORT_FROM_XML,
                ResourceLoader.getString(IMPORT_REPORT_FROM_XML),
                ResourceLoader.getString(IMPORT_REPORT_FROM_XML),
                new ImageIcon(getClass().getResource("/images/import.png")),
                null);
        createAction(EXPORT_REPORT_TO_XML,
                ResourceLoader.getString(EXPORT_REPORT_TO_XML),
                ResourceLoader.getString(EXPORT_REPORT_TO_XML), new ImageIcon(
                        getClass().getResource("/images/export.png")), null);

        // -------------help------------

        createAction(CONTEXT, ResourceLoader.getString(CONTEXT),
                ResourceLoader.getString(CONTEXT), null,
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
    }

    public void createAction(final String name) {
        createAction(name, ResourceLoader.getString(name), null, null, null);
    }

    public Action createAction(final String actionName, final ImageIcon icon,
                               final KeyStroke keyStroke) {
        return createAction(actionName, ResourceLoader.getString(actionName),
                ResourceLoader.getString(actionName), icon, keyStroke);

    }

    /**
     * This is the default constructor
     */
    public MainFrame() {
        super();
        createActions();
        createConnections();
    }

    public void setEngine(final GUIFramework framework) {
        this.framework = framework;
        if (Metadata.DEMO) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    String serial = Options.getString("SERIAL");
                    if ((serial == null) || (!new SerialCheker().check(serial))) {
                        framework.getEngine().addElementListener(null,
                                new DemoChecker(framework));
                        Metadata.DEMO_REGISTERED = false;
                    } else
                        Metadata.DEMO_REGISTERED = true;
                }
            });
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
        if (Metadata.LOCAL_VERSION_DISABLE) {
            if (framework.getEngine().getDeligate() != null) {
                ext(framework);
            }
        }
    }

    private void ext(final GUIFramework framework) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(60000 * 10);
                } catch (InterruptedException e) {
                }
                framework.getEngine().addElementListener(null,
                        new ElementAdapter() {
                            public void elementCreated(
                                    com.ramussoft.common.event.ElementEvent event) {
                                if (Math.random() < 0.1) {
                                    try {
                                        ((FileIEngineImpl) framework
                                                .getEngine().getDeligate())
                                                .close();
                                    } catch (Exception e) {
                                    }
                                }
                            }

                            ;
                        });
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    protected boolean isAllwayEnable(final Action action) {
        final String cmd = (String) action.getValue(Action.ACTION_COMMAND_KEY);
        for (final String s : ALLWAYS_ENABLE_ACTIONS) {
            if (s.equals(cmd))
                return true;
        }
        return false;
    }

    /**
     * Мотод створює під’єднання до кнопок для вікон перегляду.
     */

    private void createConnections() {
        setEnableActions(new String[0]);
        actionPerformed(new ActionEvent(this, 0, NEW_PROJECT));
        createToolBars();
    }

    public void setActiveView(final ViewPanel view) {
        final String[] actions = (view != null) ? view.getEnableActions()
                : new String[]{};
        setEnableActions(actions);
        activeView = view;
        boolean idef0Panel = view instanceof IDEFPanel;
        zoomComboBox.setEnabled(idef0Panel);
        if (idef0Panel) {

        }
    }

    public UserTemplatesDialog getUserTemplatesDialog() {
        if (userTemplatesDialog == null) {
            userTemplatesDialog = new UserTemplatesDialog(
                    framework.getMainFrame());
        }
        return userTemplatesDialog;
    }

    private int updatec = 0;

    private void setEnableActions(final String[] actions) {
        synchronized (this) {
            updatec++;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    synchronized (MainFrame.this) {
                        updatec--;
                        if (updatec == 0)
                            setEnableActionsX(actions);
                    }

                }
            });
        }
    }

    private void setEnableActionsX(final String[] actions) {
        final Collection<Action> c = this.actions.values();
        final Iterator<Action> it = c.iterator();
        while (it.hasNext()) {
            final Action action = it.next();
            if (!isAllwayEnable(action)) {
                boolean b = false;
                for (final String s : actions) {
                    if (s.equals(action.getValue(Action.ACTION_COMMAND_KEY)))
                        b = true;
                }
                if (b) {
                    if (!action.isEnabled())
                        action.setEnabled(true);
                } else {
                    if (action.isEnabled())
                        action.setEnabled(false);
                }
            }
        }
    }

    protected String getProgramName() {
        return "Ramus";
    }

    public class Group {

        private final Vector<AbstractButton> buttons = new Vector<AbstractButton>();

        public Group() {
            super();
            addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    select(e.getActionCommand());
                }

            });
        }

        public void add(final AbstractButton b) {
            buttons.add(b);
        }

        public void select(String cmd) {
            if (cmd == null)
                return;
            for (int i = 0; i < buttons.size(); i++) {
                final AbstractButton b = buttons.get(i);

                if (cmd.equals(b.getActionCommand())) {
                    for (int j = 0; j < buttons.size(); j++) {
                        final AbstractButton b1 = buttons.get(j);
                        if (!cmd.equals(b1.getActionCommand()))
                            buttons.get(j).setSelected(false);
                        else
                            buttons.get(j).setSelected(true);
                    }
                    break;
                }
            }
        }
    }

    ;

    protected void desableFocus(final JToolBar bar) {
        for (int i = 0; i < bar.getComponentCount(); i++) {
            bar.getComponent(i).setFocusable(false);
        }
    }

    protected void createToolBars() {
        final Object[] editBar = new Object[]{findAction(CUT),
                findAction(COPY), findAction(PASTE), null, findAction(ADD),
                findAction(INSERT), findAction(ADD_CHILD),
                findAction(LEVEL_UP), findAction(LEVEL_DOWN), findAction(EDIT),
                findAction(REMOVE), findAction(SELECT_CLEAN)};

        final Action[] idef0Bar = new Action[]{findAction(CURSOR_TOOL),
                findAction(FUNCTION_TOOL), findAction(ARROW_TOOL),
                findAction(TILDA_TOOL), findAction(TEXT_TOOL),
                findAction(DFDS_ROLE_TOOL),
                findAction(EXTERNAL_REFERENCE_TOOL),
                findAction(DATA_STORE_TOOL), findAction(IDEF0_NET)};

        editToolBar = createToolBar(editBar);
        idef0StateToolBar = new JToolBar();

        idef0StateToolBar.setRollover(true);
        idef0StateToolBar.setFloatable(true);

		/*
		 * idef0StateToolBar.add(findAction(IDEF0_UNDO)).setFocusable(false);
		 * idef0StateToolBar.add(findAction(IDEF0_REDO)).setFocusable(false);
		 * idef0StateToolBar.addSeparator();
		 */

        for (final Action action : idef0Bar) {
            final JToggleButton button = new JToggleButton(action);
            button.setText(null);
            idef0StateToolBar.add(button);
            if (action != idef0Bar[idef0Bar.length - 1]) {
                idef0StateGroup.add(button);
                action.putValue(BUTTON_GROUP, idef0StateGroup);
                action.putValue(BUTTON_GROUP_ADDED, Boolean.TRUE);
                if ((action == idef0Bar[idef0Bar.length - 2])
                        || (action == idef0Bar[idef0Bar.length - 3]))
                    dfdButtons.add(button);

                if (action == idef0Bar[idef0Bar.length - 4])
                    dfdsButtons.add(button);
            }

            button.setFocusable(false);
            action.putValue(Action.SELECTED_KEY, Boolean.FALSE);
        }

        findAction(CURSOR_TOOL).putValue(Action.SELECTED_KEY, Boolean.TRUE);

        idef0StateToolBar.add(findAction(GO_TO_PARENT));
        idef0StateToolBar.add(findAction(GO_TO_CHILD));
        addMoreIDEF0Commants();
        final JPanel zoom = new JPanel();
        zoom.setLayout(new BorderLayout());

        zoomComboBox.addItem(IDEFPanel.createZoom(0.25));
        zoomComboBox.addItem(IDEFPanel.createZoom(0.5));
        zoomComboBox.addItem(IDEFPanel.createZoom(1.0));
        zoomComboBox.addItem(IDEFPanel.createZoom(1.5));
        zoomComboBox.addItem(IDEFPanel.createZoom(2.0));
        zoomComboBox.addItem(IDEFPanel.createZoom(3.0));
        zoomComboBox.addItem(IDEFPanel.createZoom(IDEFPanel.FIT_ALL));
        zoomComboBox.addItem(IDEFPanel.createZoom(IDEFPanel.FIT_WIDTH));
        zoomComboBox.addItem(IDEFPanel.createZoom(IDEFPanel.FIT_HEIGHT));
        zoomComboBox.setEditable(true);
        zoom.add(zoomComboBox, BorderLayout.CENTER);
        zoomComboBox.setEnabled(false);
        final JPanel zLabel = new JPanel();
        zLabel.setLayout(new BorderLayout());
        final JPanel tmpPanel = new JPanel();
        tmpPanel.setLayout(new FlowLayout());
        zLabel.add(tmpPanel, BorderLayout.CENTER);
        zLabel.add(new JLabel(ResourceLoader.getString("zoom:")),
                BorderLayout.WEST);

        zoom.add(zLabel, BorderLayout.WEST);

        zoomComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                Object item = zoomComboBox.getSelectedItem();
                if (item instanceof String) {
                    String s = (String) item;
                    StringBuilder sb = new StringBuilder();
                    for (char c : s.toCharArray())
                        if (Character.isDigit(c) || c == '.')
                            sb.append(c);
                        else if (c == ',')
                            sb.append('.');

                    double zoom2 = Double.parseDouble(sb.toString()) / 100d;
                    if (zoom2 < 0.07 || zoom2 > 20)
                        return;
                    item = new Zoom(zoom2);
                    zoomComboBox.setSelectedItem(item);
                }
                zoomComboBox.setFocusable(false);
                zoomComboBox.setFocusable(true);
                final Zoom zoom = (Zoom) item;
                if (activeView instanceof IDEFPanel)
                    zoom.doZoom((IDEFPanel) activeView);
                int selectedIndex = zoomComboBox.getSelectedIndex();
                Options.setInteger(IDEFPanel.IDEF0_ZOOM, selectedIndex);
                if (selectedIndex < 0)
                    Options.setDouble("IDEF0D_ZOOM_DOUBLE", zoom.getZoom());
            }

        });

        int selectedIndex = Options.getInteger(IDEFPanel.IDEF0_ZOOM, 6);
        if (selectedIndex >= 0)
            zoomComboBox.setSelectedIndex(selectedIndex);
        else
            zoomComboBox.setSelectedItem(new Zoom(Options.getDouble(
                    "IDEF0D_ZOOM_DOUBLE", 1)));

        idef0StateToolBar.addSeparator();
        idef0StateToolBar.add(zoom);

        desableFocus(editToolBar);
        desableFocus(idef0StateToolBar);
        // pane.add(ToolBarLayout.north, fileToolBar);
        // pane.add(ToolBarLayout.north, idef0StateToolBar);
        // pane.add(ToolBarLayout.north, reportToolBar);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                actionPerformed(new ActionEvent(this, 0, CURSOR_TOOL));
            }
        });

    }

    protected void addMoreIDEF0Commants() {

    }

    private JToolBar createToolBar(final Object[] os) {
        final JToolBar res = new JToolBar();
        for (final Object o : os) {
            if (o == null)
                res.addSeparator();
            else
                res.add((Action) o);
        }
        desableFocus(res);
        return res;
    }

    protected class PCBFilter extends FileFilter {

        public boolean accept(final String file) {
            if (file.length() < 4)
                return false;
            return file.substring(file.length() - 4, file.length())
                    .toLowerCase().equals(getFE());
        }

        protected String getFE() {
            return FILE_EX;
        }

        public boolean accept(final File file) {
            if (file.isDirectory())
                return true;
            return accept(file.getName());

        }

        public String getDescription() {
            return ResourceLoader.getString("odz_files");
        }

    }

    ;

    protected FileFilter odzFilter = createFilter();

    private JMenu jMenuIDEF0 = null;

    private JMenuItem jMenuItemCursor = null;

    private JMenuItem jMenuItemFunction = null;

    private JMenuItem jMenuItemArrow = null;

    private JMenuItem jMenuItemTilda = null;

    private JMenuItem jMenuItemText = null;

    private JMenuItem jMenuItemGoToParent = null;

    private JMenuItem jMenuItemGoToChild = null;

    private JMenu jMenuWindows = null;

    private ViewOptionsDialog viewOptions = null;

    protected FileFilter createFilter() {
        return new PCBFilter();
    }

    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if (EXIT.equals(cmd))
            exit();
        else if (CONTEXT.equals(cmd))
            helper.showHelpContext(e);
        else if (PROGRAM_OPTIONS.equals(cmd))
            getViewOptions().showModal();
        else if (USER_TEMPLATES.equals(cmd)) {
            getUserTemplatesDialog().setVisible(true);
        }

        if (activeView != null)
            activeView.actionPerformed(e);
        for (int i = 0; i < listeners.size(); i++)
            listeners.get(i).actionPerformed(e);
    }

    public void actionPerformed(final String cmd) {
        final Action c = findAction(cmd);
        if (c != null && c.isEnabled()) {
            c.actionPerformed(new ActionEvent(this, -1, cmd));
        }
    }

    protected ViewOptionsDialog getViewOptions() {
        if (viewOptions == null) {
            viewOptions = new ViewOptionsDialog(framework.getMainFrame());
        }
        return viewOptions;
    }

    protected void exit() {
        actionPerformed(new ActionEvent(this, 0, RELOAD_SAVE));
        if (activeWorkspace != null)
            Options.setString("WORKSPACE", activeWorkspace);
        Options.save();
        System.exit(0);
    }

    public void addActionListener(final ActionListener listener) {
        synchronized (this) {
            listeners.add(listener);
        }
    }

    public void removeActionListener(final ActionListener listener) {
        synchronized (this) {
            listeners.remove(listener);
        }
    }

    /**
     * This method initializes jJMenuBar
     *
     * @return javax.swing.JMenuBar
     */
    protected JMenuBar getJJMenuBar() {
        if (jJMenuBar == null) {
            jJMenuBar = new JMenuBar();
            jJMenuBar.add(getJFileMenu());
            jJMenuBar.add(getNetworMenu());
            jJMenuBar.add(getJMenuTools());
            jJMenuBar.add(getJMenuWindows());
            jJMenuBar.add(new JPanel());
            jJMenuBar.add(getJMenuHelp());
            MnemonicFactory.setMnemonics(jJMenuBar);
        }
        return jJMenuBar;
    }

    /**
     * This method initializes jMenu
     *
     * @return javax.swing.JMenu
     */
    protected JMenu getJFileMenu() {
        if (jMenu == null) {
            jMenu = new JMenu();
            jMenu.setText(ResourceLoader.getString("file"));
            jMenu.add(getJNewProjectMenuItem());
            jMenu.addSeparator();
            jMenu.add(getJOpenProjectMenuItem());
            jMenu.addSeparator();
            jMenu.add(getJMenuImport());
            jMenu.addSeparator();
            jMenu.add(getJSevaProjectMenuItem());
            jMenu.add(getJSaveProjectAsMenuItem());
            jMenu.addSeparator();
            jMenu.add(getJMenuExport());
            jMenu.addSeparator();
            jMenu.add(getJPageSetupMenuItem());
            jMenu.add(getJPagePreviewMenuItem());
            jMenu.add(getJPrintMenuItem());
            jMenu.addSeparator();

            jMenu.add(getJExitMenuItem());
        }
        return jMenu;
    }

    /**
     * This method initializes jMenu1
     *
     * @return javax.swing.JMenu
     */
    protected JMenu getNetworMenu() {
        if (networkMenu == null) {
            networkMenu = new JMenu();
            networkMenu.setText(ResourceLoader.getString("network"));
            networkMenu.add(getJMenuItemStartWebServer());
        }
        return networkMenu;
    }

    /**
     * This method initializes jMenuItem
     *
     * @return javax.swing.JMenuItem
     */
    protected JMenuItem getJNewProjectMenuItem() {
        if (jMenuItem == null) {
            jMenuItem = new JMenuItem();
            jMenuItem.setAction(findAction(NEW_PROJECT));
        }
        return jMenuItem;
    }

    /**
     * This method initializes jMenuItem1
     *
     * @return javax.swing.JMenuItem
     */
    protected JMenuItem getJOpenProjectMenuItem() {
        if (jMenuItem1 == null) {
            jMenuItem1 = new JMenuItem();
            jMenuItem1.setAction(findAction(OPEN_PROJECT));
        }
        return jMenuItem1;
    }

    /**
     * This method initializes jMenuItem2
     *
     * @return javax.swing.JMenuItem
     */
    protected JMenuItem getJSevaProjectMenuItem() {
        if (jMenuItem2 == null) {
            jMenuItem2 = new JMenuItem();
            jMenuItem2.setAction(findAction(SAVE_PROJECT));
        }
        return jMenuItem2;
    }

    /**
     * This method initializes jMenuItem3
     *
     * @return javax.swing.JMenuItem
     */
    protected JMenuItem getJSaveProjectAsMenuItem() {
        if (jMenuItem3 == null) {
            jMenuItem3 = new JMenuItem();
            jMenuItem3.setAction(findAction(SAVE_PROJECT_AS));
        }
        return jMenuItem3;
    }

    /**
     * This method initializes jMenuItem4
     *
     * @return javax.swing.JMenuItem
     */
    protected JMenuItem getJPageSetupMenuItem() {
        if (jMenuItem4 == null) {
            jMenuItem4 = new JMenuItem();
            jMenuItem4.setAction(findAction(PAGE_SETUP));
        }
        return jMenuItem4;
    }

    /**
     * This method initializes jMenuItem5
     *
     * @return javax.swing.JMenuItem
     */
    protected JMenuItem getJPagePreviewMenuItem() {
        if (jMenuItem5 == null) {
            jMenuItem5 = new JMenuItem();
            jMenuItem5.setAction(findAction(PRINT_PREVIEW));
        }
        return jMenuItem5;
    }

    /**
     * This method initializes jMenuItem6
     *
     * @return javax.swing.JMenuItem
     */
    protected JMenuItem getJPrintMenuItem() {
        if (jMenuItem6 == null) {
            jMenuItem6 = new JMenuItem();
            jMenuItem6.setAction(findAction(PRINT));
        }
        return jMenuItem6;
    }

    /**
     * This method initializes jMenuItem7
     *
     * @return javax.swing.JMenuItem
     */
    protected JMenuItem getJExitMenuItem() {
        if (jMenuItem7 == null) {
            jMenuItem7 = new JMenuItem();
            jMenuItem7.setAction(findAction(EXIT));
        }
        return jMenuItem7;
    }

    /**
     * This method initializes jMenuItemStartWebServer
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemStartWebServer() {
        if (jMenuItemStartWebServer == null) {
            jMenuItemStartWebServer = new JMenuItem();
            jMenuItemStartWebServer.setAction(findAction(OPEN_WEB_SERVER));
        }
        return jMenuItemStartWebServer;
    }

    /**
     * This method initializes jMenu2
     *
     * @return javax.swing.JMenu
     */
    protected JMenu getJMenu2() {
        if (jMenu2 == null) {
            jMenu2 = new JMenu();
            jMenu2.setText(ResourceLoader.getString("edit"));
            jMenu2.add(getJMenuItemAdd());
            jMenu2.add(getJMenuItemInsert());
            jMenu2.add(getJMenuItemAddChild());
            jMenu2.add(getJMenuItemLevelUp());
            jMenu2.add(getJMenuItemLevelDown());
            jMenu2.add(getJMenuItemRemove());
            jMenu2.addSeparator();
            jMenu2.add(getJMenuItemRowsJoin());
            jMenu2.addSeparator();
            jMenu2.add(getJMenuItemEdit());
        }
        return jMenu2;
    }

    /**
     * This method initializes jMenuItemAdd
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemAdd() {
        if (jMenuItemAdd == null) {
            jMenuItemAdd = new JMenuItem();
            jMenuItemAdd.setAction(findAction(ADD));
        }
        return jMenuItemAdd;
    }

    /**
     * This method initializes jMenuItemEdit
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemEdit() {
        if (jMenuItemEdit == null) {
            jMenuItemEdit = new JMenuItem();
            jMenuItemEdit.setAction(findAction(EDIT));
        }
        return jMenuItemEdit;
    }

    /**
     * This method initializes jMenuItemInsert
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemInsert() {
        if (jMenuItemInsert == null) {
            jMenuItemInsert = new JMenuItem();
            jMenuItemInsert.setAction(findAction(INSERT));
        }
        return jMenuItemInsert;
    }

    /**
     * This method initializes jMenuItemRemove
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemRemove() {
        if (jMenuItemRemove == null) {
            jMenuItemRemove = new JMenuItem();
            jMenuItemRemove.setAction(findAction(REMOVE));
        }
        return jMenuItemRemove;
    }

    /**
     * This method initializes jMenuItemAddChild
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemAddChild() {
        if (jMenuItemAddChild == null) {
            jMenuItemAddChild = new JMenuItem();
            jMenuItemAddChild.setAction(findAction(ADD_CHILD));
        }
        return jMenuItemAddChild;
    }

    /**
     * This method initializes jMenuItemLevelUp
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemLevelUp() {
        if (jMenuItemLevelUp == null) {
            jMenuItemLevelUp = new JMenuItem();
            jMenuItemLevelUp.setAction(findAction(LEVEL_UP));
        }
        return jMenuItemLevelUp;
    }

    /**
     * This method initializes jMenuItemLevelDown
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemLevelDown() {
        if (jMenuItemLevelDown == null) {
            jMenuItemLevelDown = new JMenuItem();
            jMenuItemLevelDown.setAction(findAction(LEVEL_DOWN));
        }
        return jMenuItemLevelDown;
    }

    public static int getModelId() {
        return modelId;
    }

    /**
     * This method initializes jMenuIDEF0
     *
     * @return javax.swing.JMenu
     */
    private JMenu getJMenuIDEF0() {
        if (jMenuIDEF0 == null) {
            jMenuIDEF0 = new JMenu();
            jMenuIDEF0.setText(ResourceLoader.getString("idf0_state"));
            jMenuIDEF0.add(getJMenuItemCursor());
            jMenuIDEF0.add(getJMenuItemFunction());
            jMenuIDEF0.add(getJMenuItemArrow());
            jMenuIDEF0.add(getJMenuItemTilda());
            // idef0StateGroup.add(getJNewProjectMenuItem());
            jMenuIDEF0.add(getJMenuItemText());

            jMenuIDEF0.add(getJMenuItemText());
            final JCheckBoxMenuItem item = new JCheckBoxMenuItem(
                    findAction(IDEF0_NET));
            jMenuIDEF0.add(item);
            jMenuIDEF0.add(getJMenuItemGoToParent());
            jMenuIDEF0.add(getJMenuItemGoToChild());
        }
        return jMenuIDEF0;
    }

    /**
     * This method initializes jMenuItemCursor
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemCursor() {
        if (jMenuItemCursor == null) {
            jMenuItemCursor = new JRadioButtonMenuItem();
            jMenuItemCursor.setAction(findAction(CURSOR_TOOL));
            jMenuItemCursor.setSelected(true);
        }
        return jMenuItemCursor;
    }

    /**
     * This method initializes jMenuItemFunction
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemFunction() {
        if (jMenuItemFunction == null) {
            jMenuItemFunction = new JRadioButtonMenuItem();
            jMenuItemFunction.setAction(findAction(FUNCTION_TOOL));
        }
        return jMenuItemFunction;
    }

    /**
     * This method initializes jMenuItemArrow
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemArrow() {
        if (jMenuItemArrow == null) {
            jMenuItemArrow = new JRadioButtonMenuItem();
            jMenuItemArrow.setAction(findAction(ARROW_TOOL));
        }
        return jMenuItemArrow;
    }

    /**
     * This method initializes jMenuItemTilda
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemTilda() {
        if (jMenuItemTilda == null) {
            jMenuItemTilda = new JRadioButtonMenuItem();
            jMenuItemTilda.setAction(findAction(TILDA_TOOL));
        }
        return jMenuItemTilda;
    }

    /**
     * This method initializes jMenuItemText
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemText() {
        if (jMenuItemText == null) {
            jMenuItemText = new JRadioButtonMenuItem();
            jMenuItemText.setAction(findAction(TEXT_TOOL));
        }
        return jMenuItemText;
    }

    /**
     * This method initializes jMenuItemGoToParent
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemGoToParent() {
        if (jMenuItemGoToParent == null) {
            jMenuItemGoToParent = new JMenuItem();
            jMenuItemGoToParent.setAction(findAction(GO_TO_PARENT));
        }
        return jMenuItemGoToParent;
    }

    /**
     * This method initializes jMenuItemGoToChild
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemGoToChild() {
        if (jMenuItemGoToChild == null) {
            jMenuItemGoToChild = new JMenuItem();
            jMenuItemGoToChild.setAction(findAction(GO_TO_CHILD));
        }
        return jMenuItemGoToChild;
    }

    /**
     * This method initializes jMenuWindows
     *
     * @return javax.swing.JMenu
     */
    protected JMenu getJMenuWindows() {
        if (jMenuWindows == null) {
            jMenuWindows = new JMenu();
            jMenuWindows.setText(ResourceLoader.getString("workspace"));

            for (int i = 0; i < workspaceCommands.size(); i++) {
                final JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                        findAction(workspaceCommands.get(i)));
                jMenuWindows.add(item);
                workspaceGroup.add(item);
            }

            jMenuWindows.addSeparator();
            createWindowItems();
        }
        return jMenuWindows;
    }

    private void createWindowItems() {

        final JMenu windows = createWindowsMenu();
        jMenuWindows.add(windows);
        jMenuWindows.addSeparator();
    }

    protected JMenu createWindowsMenu() {
        final JMenu windows = new JMenu(
                ResourceLoader.getString("MainMenu.windows"));

        return windows;
    }

    protected JMenu jMenuHelp = null;

    private JMenuItem jMenuItemContext = null;

    private JPopupMenu jPopupMenuRows = null;

    private JMenu jMenu5 = null;

    private JMenu jMenuTools = null;

    private JMenuItem jMenuItemLookForChilds = null;

    private JMenuItem jMenuItemOpenTab = null;

    private JMenuItem jMenuItemOpenTabNA = null;

    private JMenu jMenuFunctionType = null;

    private JMenuItem jMenuItemCenterAdded = null;

    private JMenuItem jMenuItemMoveFunction = null;

    private JMenuItem jMenuItemAddLevel = null;

    private JMenuItem jMenuItemRemoveLevel = null;

    private JMenuItem jMenuItemCreateParalel = null;

    private JMenuItem jMenuItemLoadFromParalel = null;

    private JCheckBoxMenuItem jCheckBoxMenuItemShowTilda = null;

    private JCheckBoxMenuItem jCheckBoxMenuItemTransparentText = null;

    private JMenuItem jMenuItemTunnelArrow = null;

    private JRadioButtonMenuItem jMenuItemAction = null;

    private JRadioButtonMenuItem jMenuItemOperation = null;

    private JRadioButtonMenuItem jMenuItemProcesspart = null;

    private JRadioButtonMenuItem jMenuItemProcess = null;

    private JRadioButtonMenuItem jMenuItemKomplex = null;

    private JMenuItem jMenuItemRowsJoin = null;

    protected JMenu jMenuExport = null;

    protected JMenu jMenuImport = null;

    /**
     * This method initializes jMenuHelp
     *
     * @return javax.swing.JMenu
     */
    protected JMenu getJMenuHelp() {
        if (jMenuHelp == null) {
            jMenuHelp = new JMenu();
            jMenuHelp.setText(ResourceLoader.getString("help"));
            jMenuHelp.add(getJMenuItemContext());
        }
        return jMenuHelp;
    }

    /**
     * This method initializes jMenuItemContext
     *
     * @return javax.swing.JMenuItem
     */
    protected JMenuItem getJMenuItemContext() {
        if (jMenuItemContext == null) {
            jMenuItemContext = new JMenuItem();
            jMenuItemContext.setAction(findAction(CONTEXT));
        }
        return jMenuItemContext;
    }

    /**
     * This method initializes jPopupMenuRows
     *
     * @return javax.swing.JPopupMenu
     */
    public JPopupMenu getJPopupMenuRows() {
        if (jPopupMenuRows == null) {
            jPopupMenuRows = new JPopupMenu();
            jPopupMenuRows.add(findAction(CUT));
            jPopupMenuRows.add(findAction(COPY));
            jPopupMenuRows.add(findAction(PASTE));
            jPopupMenuRows.addSeparator();
            jPopupMenuRows.add(findAction(MOVE_ROWS));
            jPopupMenuRows.add(findAction(JOIN_ROWS));
            jPopupMenuRows.addSeparator();
            jPopupMenuRows.add(findAction(EDIT));
        }
        return jPopupMenuRows;
    }

    public Group getWorkspaceGroup() {
        return workspaceGroup;
    }

    protected JMenuItem getJMenuItemExportReportToHTML() {
        if (jMenuItemExportReportToHTML == null) {
            jMenuItemExportReportToHTML = new JMenuItem();
            jMenuItemExportReportToHTML
                    .setAction(findAction(EXPORT_REPORT_TO_HTML));
        }
        return jMenuItemExportReportToHTML;
    }

    public void refreshActions(final ViewPanel panel) {
        if (activeView == null)
            setEnableActions(new String[]{});
        else
            setEnableActions(activeView.getEnableActions());
    }

    /**
     * This method initializes jMenu5
     *
     * @return javax.swing.JMenu
     */
    protected JMenu getJMenuIDEF0Edit() {
        if (jMenu5 == null) {
            jMenu5 = new JMenu();
            jMenu5.setText(ResourceLoader.getString("idf0_editor"));
            jMenu5.add(getJMenuIDEF0());
            jMenu5.addSeparator();
            jMenu5.add(getJMenuItemLookForChilds());
            jMenu5.addSeparator();
            jMenu5.add(getJMenuItemOpenTab());
            jMenu5.add(getJMenuItemOpenTabNA());
            jMenu5.addSeparator();
            jMenu5.add(getJMenuFunctionType());
            jMenu5.addSeparator();
            jMenu5.add(getJMenuItemCenterAdded());
            jMenu5.add(findAction(CENTER_ALL_SECTORS));
            jMenu5.addSeparator();
            jMenu5.add(getJMenuItemMoveFunction());
            jMenu5.add(getJMenuItemAddLevel());
            jMenu5.add(getJMenuItemRemoveLevel());
            jMenu5.addSeparator();
            jMenu5.add(getJMenuItemCreateParalel());
            jMenu5.add(getJMenuItemLoadFromParalel());
            jMenu5.addSeparator();
            jMenu5.add(getJCheckBoxMenuItemShowTilda());
            jMenu5.add(getJCheckBoxMenuItemTransparentText());
            jMenu5.addSeparator();
            jMenu5.add(getJMenuItemTunnelArrow());
        }
        return jMenu5;
    }

    /**
     * This method initializes jMenuItemLookForChilds
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemLookForChilds() {
        if (jMenuItemLookForChilds == null) {
            jMenuItemLookForChilds = new JMenuItem(
                    findAction(SET_LOOK_FOR_CHILDRENS));
        }
        return jMenuItemLookForChilds;
    }

    /**
     * This method initializes jMenuItemOpenTab
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemOpenTab() {
        if (jMenuItemOpenTab == null) {
            jMenuItemOpenTab = new JMenuItem(findAction(OPEN_TAB));
        }
        return jMenuItemOpenTab;
    }

    /**
     * This method initializes jMenuItemOpenTabNA
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemOpenTabNA() {
        if (jMenuItemOpenTabNA == null) {
            jMenuItemOpenTabNA = new JMenuItem(findAction(OPEN_IN_INNER_TAB));
        }
        return jMenuItemOpenTabNA;
    }

    /**
     * This method initializes jMenuFunctionType
     *
     * @return javax.swing.JMenu
     */
    private JMenu getJMenuFunctionType() {
        if (jMenuFunctionType == null) {
            jMenuFunctionType = new JMenu(findAction(FUNCTION_TYPE));
            jMenuFunctionType.add(getJMenuItemAction());
            jMenuFunctionType.add(getJMenuItemOperation());
            jMenuFunctionType.add(getJMenuItemProcessPart());
            jMenuFunctionType.add(getJMenuItemProcess());
            jMenuFunctionType.add(getJMenuItemKomplex());
            final ButtonGroup bg = new ButtonGroup();
            bg.add(getJMenuItemAction());
            bg.add(getJMenuItemOperation());
            bg.add(getJMenuItemProcessPart());
            bg.add(getJMenuItemProcess());
            bg.add(getJMenuItemKomplex());
        }
        return jMenuFunctionType;
    }

    /**
     * This method initializes jMenuItemCenterAdded
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemCenterAdded() {
        if (jMenuItemCenterAdded == null) {
            jMenuItemCenterAdded = new JMenuItem(
                    findAction(CENTER_ADDED_SECTORS));
        }
        return jMenuItemCenterAdded;
    }

    /**
     * This method initializes jMenuItemMoveFunction
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemMoveFunction() {
        if (jMenuItemMoveFunction == null) {
            jMenuItemMoveFunction = new JMenuItem(findAction(FUNCTION_MOVE));
        }
        return jMenuItemMoveFunction;
    }

    /**
     * This method initializes jMenuItemAddLevel
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemAddLevel() {
        if (jMenuItemAddLevel == null) {
            jMenuItemAddLevel = new JMenuItem(findAction(FUNCTION_ADD_LEVEL));
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
            jMenuItemRemoveLevel = new JMenuItem(
                    findAction(FUNCTION_REMOVE_LEVEL));
        }
        return jMenuItemRemoveLevel;
    }

    /**
     * This method initializes jMenuItemCreateParalel
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemCreateParalel() {
        if (jMenuItemCreateParalel == null) {
            jMenuItemCreateParalel = new JMenuItem(findAction(CREATE_PARALEL));
        }
        return jMenuItemCreateParalel;
    }

    /**
     * This method initializes jMenuItemLoadFromParalel
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemLoadFromParalel() {
        if (jMenuItemLoadFromParalel == null) {
            jMenuItemLoadFromParalel = new JMenuItem(
                    findAction(LOAD_FROM_PARALEL));
        }
        return jMenuItemLoadFromParalel;
    }

    /**
     * This method initializes jCheckBoxMenuItemShowTilda
     *
     * @return javax.swing.JCheckBoxMenuItem
     */
    public JCheckBoxMenuItem getJCheckBoxMenuItemShowTilda() {
        if (jCheckBoxMenuItemShowTilda == null) {
            jCheckBoxMenuItemShowTilda = new JCheckBoxMenuItem(
                    findAction(SET_ARROW_TILDA));
        }
        return jCheckBoxMenuItemShowTilda;
    }

    /**
     * This method initializes jCheckBoxMenuItemTransparentText
     *
     * @return javax.swing.JCheckBoxMenuItem
     */
    public JCheckBoxMenuItem getJCheckBoxMenuItemTransparentText() {
        if (jCheckBoxMenuItemTransparentText == null) {
            jCheckBoxMenuItemTransparentText = new JCheckBoxMenuItem(
                    findAction(SET_TRANSPARENT_ARROW_TEXT));
        }
        return jCheckBoxMenuItemTransparentText;
    }

    /**
     * This method initializes jMenuItemTunnelArrow
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemTunnelArrow() {
        if (jMenuItemTunnelArrow == null) {
            jMenuItemTunnelArrow = new JMenuItem(findAction(TUNNEL_ARROW));
        }
        return jMenuItemTunnelArrow;
    }

    /**
     * This method initializes jMenuItemAction
     *
     * @return javax.swing.JMenuItem
     */
    public JRadioButtonMenuItem getJMenuItemAction() {
        if (jMenuItemAction == null) {
            jMenuItemAction = new JRadioButtonMenuItem(
                    findAction(FUNCTION_TYPE_ACTION));
        }
        return jMenuItemAction;
    }

    /**
     * This method initializes jMenuItemOperation
     *
     * @return javax.swing.JMenuItem
     */
    public JRadioButtonMenuItem getJMenuItemOperation() {
        if (jMenuItemOperation == null) {
            jMenuItemOperation = new JRadioButtonMenuItem(
                    findAction(FUNCTION_TYPE_OPERATION));
        }
        return jMenuItemOperation;
    }

    /**
     * This method initializes jMenuItemProcesspart
     *
     * @return javax.swing.JMenuItem
     */
    public JRadioButtonMenuItem getJMenuItemProcessPart() {
        if (jMenuItemProcesspart == null) {
            jMenuItemProcesspart = new JRadioButtonMenuItem(
                    findAction(FUNCTION_TYPE_PROCESS_PART));
        }
        return jMenuItemProcesspart;
    }

    /**
     * This method initializes jMenuItemProcess
     *
     * @return javax.swing.JMenuItem
     */
    public JRadioButtonMenuItem getJMenuItemProcess() {
        if (jMenuItemProcess == null) {
            jMenuItemProcess = new JRadioButtonMenuItem(
                    findAction(FUNCTION_TYPE_PROCESS));
        }
        return jMenuItemProcess;
    }

    /**
     * This method initializes jMenuItemKomplex
     *
     * @return javax.swing.JMenuItem
     */
    public JRadioButtonMenuItem getJMenuItemKomplex() {
        if (jMenuItemKomplex == null) {
            jMenuItemKomplex = new JRadioButtonMenuItem(
                    findAction(FUNCTION_TYPE_KOMPLEX));
        }
        return jMenuItemKomplex;
    }

    public void saveParams() {
        actionPerformed(new ActionEvent(this, 0, RELOAD_SAVE));
        propertyChange(MChangeListener.FILE_SAVE, null);
        actionPerformed(new ActionEvent(this, 0, RELOAD_SAVE));
    }

    public void closeConnection() {
        propertyChange(MChangeListener.CLOSE_CONNECTION, null);
    }

    /**
     * This method initializes jMenuItemRowsJoin
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItemRowsJoin() {
        if (jMenuItemRowsJoin == null) {
            jMenuItemRowsJoin = new JMenuItem();
            jMenuItemRowsJoin.setAction(findAction(JOIN_ROWS));
        }
        return jMenuItemRowsJoin;
    }

    /**
     * This method initializes jMenuExport
     *
     * @return javax.swing.JMenu
     */
    protected JMenu getJMenuExport() {
        if (jMenuExport == null) {
            jMenuExport = new JMenu();
            jMenuExport.setText(ResourceLoader.getString("export_data_to"));
            jMenuExport.add(getJMenuItemExportReportToHTML());

        }
        return jMenuExport;
    }

    /**
     * This method initializes jMenuImport
     *
     * @return javax.swing.JMenu
     */
    public JMenu getJMenuImport() {
        if (jMenuImport == null) {
            jMenuImport = new JMenu();
            jMenuImport.setText(ResourceLoader.getString("import_data_from"));

        }
        return jMenuImport;
    }

    public static void addRightActivator(final JComponent component,
                                         final JPopupMenu popupMenu) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            component.requestFocus();
                        }
                    });

                    popupMenu.show(component, e.getX(), e.getY());
                }
            }

        });
    }

    private JMenu getJMenuTools() {
        if (jMenuTools == null) {
            jMenuTools = new JMenu(ResourceLoader.getString("Tools"));
            jMenuTools.add(findAction(ADD_MODEL_TO_TEMPLATE));
            jMenuTools.add(findAction(USER_TEMPLATES));
            jMenuTools.addSeparator();
            jMenuTools.add(findAction(SHOW_STREAMS));
            jMenuTools.addSeparator();
            jMenuTools.add(findAction(RUN_JS_SCRIPT));
            jMenuTools.addSeparator();
            jMenuTools.add(findAction(PROJECT_OPTIONS));
            jMenuTools.addSeparator();
            jMenuTools.add(findAction(PROGRAM_OPTIONS));
        }
        return jMenuTools;
    }

    public Hashtable<String, Action> getActions() {
        return actions;
    }

    public JToolBar getIdef0StateToolBar() {
        return idef0StateToolBar;
    }

    public Zoom getActiveZoom() {
        return (Zoom) zoomComboBox.getSelectedItem();
    }

    public void setActiveZoom(Zoom zoom) {
        zoomComboBox.setSelectedItem(zoom);
    }

    public ArrayList<JToggleButton> getDfdButtons() {
        return dfdButtons;
    }

    public ArrayList<JToggleButton> getDfdsButtons() {
        return dfdsButtons;
    }

    public ViewPanel getActiveView() {
        return activeView;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
