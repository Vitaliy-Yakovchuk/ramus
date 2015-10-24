package com.ramussoft.idef0;

import static com.ramussoft.idef0.IDEF0ViewPlugin.ACTIVE_DIAGRAM;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Element;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowMover;
import com.ramussoft.gui.StandardFilePlugin;
import com.ramussoft.gui.common.AbstractView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.common.event.ViewTitleEvent;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.frames.MainFrame;
import com.ramussoft.pb.idef.frames.IDEFPanel;
import com.ramussoft.pb.idef.visual.VisualPanel;
import com.ramussoft.pb.idef.visual.VisualPanelImpl;
import com.ramussoft.pb.idef.visual.event.ActiveFunctionEvent;
import com.ramussoft.pb.idef.visual.event.ActiveFunctionListener;

public class IDEF0TabView extends AbstractView implements TabView {

    private IDEFPanel panel;

    private DataPlugin dataPlugin;

    private MainFrame frame;

    private AccessRules rules;

    private long functionId;

    public static final String CLOSE = "close_function";

    public static final String UPDATE_SIZES = "update_sizes";

    public static final String DISABLE_SILENT_REFRESH = "disable_silent_refresh";

    private Object refreshingLock = new Object();

    private boolean refreshing;

    private boolean disableSilentRefresh;

    private VisualPanelImpl visualCopy;

    private VisualPanel activaPanel;

    private JPanel basePanel;

    private AbstractAction createBlocksFromLinesAction = new AbstractAction(
            "Action.CreateFromText") {

        {
            putValue(ACTION_COMMAND_KEY, "Action.CreateFromText");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/add.png")));
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            panel.getMovingArea().createBlocksFromLines();
        }
    };

    private AbstractAction handAction = new AbstractAction() {

        {
            putValue(ACTION_COMMAND_KEY, "Action.HandCopy");
            putValue(SELECTED_KEY, Boolean.FALSE);
            setEnabled(false);
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource("/images/dropper.png")));

            addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (Action.SELECTED_KEY.equals(evt.getPropertyName())) {
                        if ((Boolean) evt.getNewValue()) {
                            Toolkit toolkit = Toolkit.getDefaultToolkit();
                            Image image = toolkit.getImage(getClass()
                                    .getResource("/images/pen-cursor.png"));
                            java.awt.Point point = new java.awt.Point(0, 15);
                            panel.getMovingArea().setVisualCopyCursor(
                                    toolkit.createCustomCursor(image, point,
                                            "CopyVisualCursor"));
                        } else {
                            panel.getMovingArea().setVisualCopyCursor(null);
                        }
                    }
                }
            });
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if ((Boolean) getValue(SELECTED_KEY))
                visualCopy = new VisualPanelImpl(activaPanel);

        }
    };

    private ElementAttributeListener reloadMovingArea = new ElementAttributeListener() {
        @Override
        public void attributeChanged(AttributeEvent event) {
            if ((event.getAttribute().getName()
                    .equals(IDEF0Plugin.F_PROJECT_PREFERENCES))
                    || (event.getAttribute().getName()
                    .equals(IDEF0Plugin.F_CREATE_DATE))
                    || (event.getAttribute().getName()
                    .equals(IDEF0Plugin.F_REV_DATE))
                    || (event.getAttribute().getName()
                    .equals(IDEF0Plugin.F_AUTHOR))) {
                Row row = (Row) dataPlugin.findRowByGlobalId(event
                        .getElement().getId());
                if ((row == null) || (row.getParent() == null))
                    return;
                if (panel.getMovingArea().getActiveFunction() == null)
                    return;
                if (panel.getMovingArea().getActiveFunction().getElement()
                        .getId() == row.getElementId()) {
                    refresh();
                }
            } else if (event.getAttribute().equals(
                    IDEF0Plugin.getFunctionVisualDataAttribute(event
                            .getEngine()))) {
                if (panel.getMovingArea().getActiveFunction().getElement()
                        .getId() == event.getElement().getId()) {
                    refresh();
                    panel.repaint();
                }
            } else {
                String name = event.getAttribute().getName();
                if ((name.equals(IDEF0Plugin.F_BOUNDS))
                        || (name.equals(IDEF0Plugin.F_BACKGROUND))
                        || (name.equals(IDEF0Plugin.F_FONT))
                        || (name.equals(IDEF0Plugin.F_FOREGROUND))
                        || (name.equals(IDEF0Plugin.F_BACKGROUND))) {
                    Row row = (Row) dataPlugin.findRowByGlobalId(event
                            .getElement().getId());
                    if ((row == null) || (row.getParent() == null))
                        return;
                    if (panel.getMovingArea().getActiveFunction() == null)
                        return;
                    if (panel.getMovingArea().getActiveFunction().getElement()
                            .getId() == row.getParent().getElementId()) {
                        refresh();
                    }
                } else if (event.getAttribute().getId() == dataPlugin
                        .getBaseFunctionQualifier().getAttributeForName()) {
                    long id = getFunctionId();
                    if (id == event.getElement().getId())
                        refreshTitle();
                    else {
                        Row row = (Row) dataPlugin.findRowByGlobalId(event
                                .getElement().getId());
                        if ((row.getParent() != null)
                                && (panel.getMovingArea().getActiveFunction()
                                .getElement().getId() == row
                                .getParent().getElementId())) {
                            refresh();
                        }
                    }
                }
            }
        }

    };

    private QualifierListener qualifierListener = new QualifierAdapter() {
        @Override
        public void qualifierUpdated(QualifierEvent event) {
            if (event.getOldQualifier().getAttributeForName() != event
                    .getNewQualifier().getAttributeForName())
                refresh();
        }
    };

    private void refresh() {
        synchronized (refreshingLock) {
            if (refreshing)
                return;
            if (disableSilentRefresh)
                return;
            refreshing = true;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (refreshingLock) {
                    refreshing = false;
                }
                panel.getMovingArea().silentRefresh(
                        panel.getMovingArea().getActiveFunction());
            }

            ;
        });
    }

    private ElementAdapter elementAdapter = new ElementAdapter() {
        @Override
        public void elementCreated(ElementEvent event) {
            update(event.getNewElement());
        }

        public void beforeElementDeleted(ElementEvent event) {
            update(event.getOldElement());
        }

        private void update(Element element) {
            Function f = panel.getMovingArea().getActiveFunction();
            Vector<com.ramussoft.pb.Row> v = dataPlugin.getChilds(f, true);
            for (com.ramussoft.pb.Row r : v) {
                if (r.getElement().getId() == element.getId())
                    refresh();
            }

        }

    };

    private ActionListener fullRefreshAction = new ActionListener() {
        @Override
        public void onAction(ActionEvent event) {
            long id = ((Row) panel.getMovingArea().getActiveFunction())
                    .getElementId();
            if (dataPlugin.getBaseFunction().equals(
                    panel.getMovingArea().getActiveFunction()))
                id = -1;
            dataPlugin.refresh(framework);
            Function f = (Function) dataPlugin.findRowByGlobalId(id);
            if (f == null)
                close();
            else
                panel.getMovingArea().setActiveFunction(
                        (id < 0) ? dataPlugin.getBaseFunction() : f);
        }
    };

    private ActionListener closeAction = new ActionListener() {
        @Override
        public void onAction(ActionEvent event) {
            long id = ((Row) panel.getMovingArea().getActiveFunction())
                    .getElementId();
            if (dataPlugin.getBaseFunction().equals(
                    panel.getMovingArea().getActiveFunction()))
                id = -1;
            if (event.getValue().equals(id))
                close();
        }
    };

    private ActionListener setSilentRefreshAction = new ActionListener() {
        @Override
        public void onAction(ActionEvent event) {
            synchronized (refreshingLock) {
                disableSilentRefresh = (Boolean) event.getValue();
            }
        }
    };

    private ActionListener updateSizeAction = new ActionListener() {
        @Override
        public void onAction(ActionEvent event) {
            panel.getMovingArea().setActiveFunction(
                    panel.getMovingArea().getActiveFunction());
        }
    };

    public IDEF0TabView(GUIFramework framework, DataPlugin dataPlugin,
                        long functionId, MainFrame frame, AccessRules rules) {
        super(framework);
        this.dataPlugin = dataPlugin;
        this.frame = frame;
        this.rules = rules;
        this.functionId = functionId;
        framework.addActionListener(Commands.FULL_REFRESH, fullRefreshAction);
        framework.addActionListener(CLOSE, closeAction);
        framework.addActionListener(UPDATE_SIZES, updateSizeAction);
        framework.addActionListener(DISABLE_SILENT_REFRESH,
                setSilentRefreshAction);
        Qualifier qualifier = dataPlugin.getBaseFunctionQualifier();
        dataPlugin.getEngine().addElementAttributeListener(qualifier,
                reloadMovingArea);
        dataPlugin.getEngine().addElementAttributeListener(
                IDEF0Plugin.getBaseFunctions(dataPlugin.getEngine()),
                reloadMovingArea);
        dataPlugin.getEngine().addElementListener(qualifier, elementAdapter);
        dataPlugin.getEngine().addElementListener(
                IDEF0Plugin.getBaseFunctions(dataPlugin.getEngine()),
                elementAdapter);
        dataPlugin.getEngine().addQualifierListener(qualifierListener);

        if (Metadata.EDUCATIONAL) {
            if (!RowMover.isEducational()) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (Math.random() < 0.3d)
                            System.exit(25);
                    }
                });
            }
        }
    }

    @Override
    public String getTitle() {
        if (isBaseFunctionSelected())
            return dataPlugin.getBaseFunctionQualifier().getName();
        return panel.getMovingArea().getActiveFunction().getName();
    }

    @Override
    public JComponent createComponent() {
        basePanel = new JPanel(new BorderLayout());
        createPanel();
        return basePanel;
    }

    private void createPanel() {
        panel = new IDEFPanel(frame, dataPlugin, framework, rules, this) {
            @Override
            protected void openTab(Function function) {
                long id = -1;
                if (!dataPlugin.getBaseFunction().equals(function))
                    id = function.getElement().getId();
                OpenDiagram openDiagram = new OpenDiagram(
                        dataPlugin.getBaseFunctionQualifier(), id);
                framework.propertyChanged(IDEF0ViewPlugin.OPEN_DIAGRAM,
                        openDiagram);
            }

            @Override
            protected void setActiveObject(Object activeObject) {
                super.setActiveObject(activeObject);
                if (activeObject instanceof VisualPanel)
                    activaPanel = (VisualPanel) activeObject;

                if ((Boolean) handAction.getValue(AbstractAction.SELECTED_KEY)) {
                    if (activeObject instanceof VisualPanel) {
                        panel.getMovingArea().startUserTransaction();
                        VisualPanel vpanel = (VisualPanel) activeObject;
                        visualCopy.copyTo(vpanel);
                        panel.getMovingArea().commitUserTransaction();
                    }
                } else {
                    if (activeObject instanceof VisualPanel) {
                        handAction.setEnabled(true);
                    } else
                        handAction.setEnabled(false);
                }
            }
        };
        Function function = null;

        if (functionId >= 0) {
            function = (Function) dataPlugin.findRowByGlobalId(functionId);
        }

        if (function == null)
            function = dataPlugin.getBaseFunction();
        panel.getMovingArea().setActiveFunction(function);
        panel.getMovingArea().addActiveFunctionListener(
                new ActiveFunctionListener() {
                    @Override
                    public void activeFunctionChanged(ActiveFunctionEvent event) {
                        refreshTitle();
                        framework.propertyChanged(ACTIVE_DIAGRAM,
                                getOpenAction());
                    }
                });
        frame.getActiveZoom().doZoom(panel);
        basePanel.add(panel, BorderLayout.CENTER);
    }

    private void refreshTitle() {
        ViewTitleEvent titleEvent = new ViewTitleEvent(IDEF0TabView.this,
                getTitle());
        titleChanged(titleEvent);
    }

    @Override
    public Action[] getActions() {
        return new Action[]{handAction, createBlocksFromLinesAction};
    }

    @Override
    public void focusGained() {
        frame.setActiveView(panel);
        frame.setActiveZoom(panel.getActiveZoom());
        framework.propertyChanged(ACTIVE_DIAGRAM, getOpenAction());
    }

    @Override
    public void focusLost() {
        frame.setActiveView(null);
        framework.propertyChanged(ACTIVE_DIAGRAM);
    }

    @Override
    public void close() {
        super.close();
        framework
                .removeActionListener(Commands.FULL_REFRESH, fullRefreshAction);
        framework.removeActionListener(CLOSE, closeAction);
        framework.removeActionListener(UPDATE_SIZES, updateSizeAction);
        framework.removeActionListener(DISABLE_SILENT_REFRESH,
                setSilentRefreshAction);
        Qualifier qualifier = dataPlugin.getBaseFunctionQualifier();
        dataPlugin.getEngine().removeElementAttributeListener(qualifier,
                reloadMovingArea);
        dataPlugin.getEngine().removeElementAttributeListener(
                IDEF0Plugin.getBaseFunctions(dataPlugin.getEngine()),
                reloadMovingArea);
        dataPlugin.getEngine().removeElementListener(qualifier, elementAdapter);
        dataPlugin.getEngine().removeElementListener(
                IDEF0Plugin.getBaseFunctions(dataPlugin.getEngine()),
                elementAdapter);
        dataPlugin.getEngine().removeQualifierListener(qualifierListener);
        panel.close();
    }

    @Override
    public ActionEvent getOpenAction() {
        long id = getFunctionId();
        OpenDiagram openDiagram = new OpenDiagram(
                dataPlugin.getBaseFunctionQualifier(), id);
        return new ActionEvent(IDEF0ViewPlugin.OPEN_DIAGRAM, openDiagram);
    }

    private long getFunctionId() {
        Function function = panel.getMovingArea().getActiveFunction();
        long id = -1;
        if (!function.equals(dataPlugin.getBaseFunction()))
            id = function.getElement().getId();
        return id;
    }

    public boolean isBaseFunctionSelected() {
        return panel.getMovingArea().getActiveFunction()
                .equals(dataPlugin.getBaseFunction());
    }

    public boolean isSelectedElementId(Element oldElement) {
        return oldElement.getId() == getFunctionId();
    }

    @Override
    public void onAction(ActionEvent event) {
        java.awt.event.ActionEvent event2 = null;
        if (event.getKey().equals(StandardFilePlugin.ACTION_PAGE_SETUP))
            event2 = new java.awt.event.ActionEvent(panel, 0,
                    MainFrame.PAGE_SETUP);
        else if (event.getKey().equals(StandardFilePlugin.ACTION_PRINT))
            event2 = new java.awt.event.ActionEvent(panel, 0, MainFrame.PRINT);
        if (event.getKey().equals(StandardFilePlugin.ACTION_PRINT_PREVIEW))
            event2 = new java.awt.event.ActionEvent(panel, 0,
                    MainFrame.PRINT_PREVIEW);
        panel.actionPerformed(event2);
    }

    @Override
    public String[] getGlobalActions() {
        return new String[]{StandardFilePlugin.ACTION_PRINT,
                StandardFilePlugin.ACTION_PAGE_SETUP,
                StandardFilePlugin.ACTION_PRINT_PREVIEW};
    }

    public AbstractAction getHandAction() {
        return handAction;
    }
}
