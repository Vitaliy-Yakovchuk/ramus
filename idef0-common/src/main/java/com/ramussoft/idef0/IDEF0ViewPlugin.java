package com.ramussoft.idef0;

import static com.ramussoft.pb.frames.MainFrame.ADD_MODEL_TO_TEMPLATE;

import static com.ramussoft.pb.frames.MainFrame.ARROW_TOOL;
import static com.ramussoft.pb.frames.MainFrame.CENTER_ALL_SECTORS;
import static com.ramussoft.pb.frames.MainFrame.CURSOR_TOOL;
import static com.ramussoft.pb.frames.MainFrame.DATA_STORE_TOOL;
import static com.ramussoft.pb.frames.MainFrame.DIAGRAM_PROPETIES;
import static com.ramussoft.pb.frames.MainFrame.EXPORT_TO_IMAGES;
import static com.ramussoft.pb.frames.MainFrame.EXTERNAL_REFERENCE_TOOL;
import static com.ramussoft.pb.frames.MainFrame.DFDS_ROLE_TOOL;
import static com.ramussoft.pb.frames.MainFrame.FUNCTION_TOOL;
import static com.ramussoft.pb.frames.MainFrame.GO_TO_CHILD;
import static com.ramussoft.pb.frames.MainFrame.GO_TO_PARENT;
import static com.ramussoft.pb.frames.MainFrame.IDEF0_NET;
import static com.ramussoft.pb.frames.MainFrame.MODEL_PROPETIES;
import static com.ramussoft.pb.frames.MainFrame.TEXT_TOOL;
import static com.ramussoft.pb.frames.MainFrame.TILDA_TOOL;
import static com.ramussoft.pb.frames.MainFrame.USER_TEMPLATES;
import static com.ramussoft.pb.frames.MainFrame.CREATE_LEVEL;

import com.ramussoft.gui.common.*;
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.Collator;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.MaskFormatter;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.common.event.CloseMainFrameAdapter;
import com.ramussoft.gui.common.event.TabbedEvent;
import com.ramussoft.gui.common.event.ViewTitleEvent;
import com.ramussoft.gui.common.prefrence.AbstractPreferences;
import com.ramussoft.gui.common.prefrence.Preferences;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.idef0.attribute.LineStyleAttributePlugin;
import com.ramussoft.idef0.attribute.SectorColorAttributePlugin;
import com.ramussoft.idef0.attribute.SectorFontAttributePlugin;
import com.ramussoft.idef0.attribute.StreamAttributePlugin;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.data.negine.NDataPlugin;
import com.ramussoft.pb.frames.MainFrame;
import com.ramussoft.pb.frames.components.SerialCheker;
import com.ramussoft.pb.frames.setup.OwnerClasificators;
import com.ramussoft.pb.frames.setup.ViewIDEF0PropertiosPanel;
import com.ramussoft.pb.idef.elements.DemoChecker;
import com.ramussoft.pb.master.NewProjectDialog;
import com.ramussoft.pb.print.web.HTTPServer;
import com.ramussoft.pb.print.web.Navigator;
import com.ramussoft.web.HTTPParser;

public class IDEF0ViewPlugin extends AbstractViewPlugin {

    public static final String OPEN_DIAGRAM = "OpenIDEF0Diagram";

    public static final String OPEN_STREAMS = "OpenIDEF0Streams";

    public static final String ACTIVE_DIAGRAM = "ActiveDiagram";

    private Engine engine;

    private AccessRules rules;

    private MainFrame frame;

    private HTTPServer server = null;

    private Navigator navigator;

    private class DiagramData {
        private OpenDiagram openDiagram;

        private QualifierListener listener;

        private ElementListener elementListener;

        private TabView view;
    }

    private class StreamsData {

        private TabView view;
    }

    public IDEF0ViewPlugin() {
        frame = new MainFrame();
    }

    @Override
    public String getString(String key) {
        String res = super.getString(key);
        if (res == null)
            res = ResourceLoader.getString(key);
        if (res == null) {
            try {
                res = HTTPParser.RES.getString(key);
            } catch (Exception e) {
            }
        }
        return res;
    }

    @Override
    public String getName() {
        return "IDEF0";
    }

    @Override
    public UniqueView[] getUniqueViews() {
        navigator = new Navigator(framework, engine, rules, frame);
        ArrayList<UniqueView> list = new ArrayList<UniqueView>();
        list.add(new ModelsView(framework, engine, rules));
        list.add(new RolesView(framework));
        if (!Metadata.EDUCATIONAL) {
            list.add(navigator);
        }
        return list.toArray(new UniqueView[list.size()]);
    }

    @Override
    public TabbedView[] getTabbedViews() {
        ArrayList<TabbedView> list = new ArrayList<TabbedView>();
        list.add(new IDEF0TabbedView(framework));
        return list.toArray(new TabbedView[list.size()]);
    }

    @Override
    public void setFramework(final GUIFramework framework) {
        super.setFramework(framework);
        this.engine = framework.getEngine();
        this.rules = framework.getAccessRules();

        this.frame.setEngine(framework);

        addOpenDiagramListener();
        addOpenStreamsViewListener();
        framework.addCloseMainFrameListener(new CloseMainFrameAdapter() {
            @Override
            public void closed() {
                Options.save();
                if (navigator != null)
                    navigator.dispose();

                if (engine.getPluginProperty("Core", "MainFrame") == framework
                        .getMainFrame()) {
                    NDataPlugin dataPlugin = (NDataPlugin) engine
                            .getPluginProperty("IDEF0", "DataPlugin");
                    if (dataPlugin != null) {
                        dataPlugin.clear();
                        dataPlugin.clearAll();
                    }
                }
                if (server != null) {
                    try {
                        server.getServer().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    server.interrupt();
                }
            }
        });
        framework.addActionListener("FileOpened", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                if (event.getValue() == null) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            NewProjectDialog dialog = new NewProjectDialog(
                                    framework.getMainFrame());
                            dialog.showModal(engine, rules, framework);
                        }
                    });
                }
            }
        });

        framework.addActionListener(Commands.FULL_REFRESH,
                new ActionListener() {
                    @Override
                    public void onAction(ActionEvent event) {
                        if (NDataPluginFactory.getExistingDataPlugin(engine) != null)
                            NDataPluginFactory.fullRefrash(framework);
                    }
                });

        framework.setSystemAttributeName(
                IDEF0Plugin.getBackgroundColorAttribute(engine),
                ResourceLoader.getString("bk_color"));
        framework.setSystemAttributeName(
                IDEF0Plugin.getForegroundColorAttribute(engine),
                ResourceLoader.getString("fg_color"));
        framework.setSystemAttributeName(IDEF0Plugin.getFontAttribute(engine),
                ResourceLoader.getString("font"));
        framework.setSystemAttributeName(
                IDEF0Plugin.getFunctionTypeAttribute(engine),
                ResourceLoader.getString("function_type"));
        framework.setSystemAttributeName(
                IDEF0Plugin.getFunctionOunerAttribute(engine),
                ResourceLoader.getString("select_owner"));

        StreamAttributePlugin plugin = new StreamAttributePlugin();
        plugin.setFramework(framework);
        framework.setSystemAttributePlugin(
                IDEF0Plugin.getStreamAttribute(engine), plugin);
        framework.setSystemAttributeName(
                IDEF0Plugin.getStreamAttribute(engine),
                ResourceLoader.getString("stream"));

        framework.setSystemAttributePlugin(
                IDEF0Plugin.getSectorBorderEndAttribute(engine),
                new SectorFontAttributePlugin());
        framework.setSystemAttributeName(
                IDEF0Plugin.getSectorBorderEndAttribute(engine),
                ResourceLoader.getString("font"));

        framework.setSystemAttributePlugin(
                IDEF0Plugin.getSectorBorderStartAttribute(engine),
                new SectorColorAttributePlugin());
        framework.setSystemAttributeName(
                IDEF0Plugin.getSectorBorderStartAttribute(engine),
                ResourceLoader.getString("color"));

        framework.setSystemAttributePlugin(
                IDEF0Plugin.getSectorFunctionAttribute(engine),
                new LineStyleAttributePlugin());
        framework.setSystemAttributeName(
                IDEF0Plugin.getSectorFunctionAttribute(engine),
                ResourceLoader.getString("arrow"));
    }

    private void addOpenStreamsViewListener() {
        framework.addActionListener(OPEN_STREAMS, new ActionListener() {

            @Override
            public void onAction(final ActionEvent event) {
                if (framework.openView(event))
                    return;

                final StreamsData data = new StreamsData();

                data.view = new StreamsTabView(framework) {

                    @Override
                    public void close() {
                        super.close();
                        TabbedEvent tEvent = new TabbedEvent(
                                IDEF0TabbedView.IDEF0_TAB_VIEW, this);
                        tabRemoved(tEvent);
                    }

                };
                TabbedEvent tEvent = new TabbedEvent(
                        IDEF0TabbedView.IDEF0_TAB_VIEW, (TabView) data.view);
                tabCreated(tEvent);
            }

        });
    }

    private void addOpenDiagramListener() {
        framework.addActionListener(OPEN_DIAGRAM, new ActionListener() {

            @Override
            public void onAction(final ActionEvent event) {
                if (framework.openView(event))
                    return;

                final DiagramData data = new DiagramData();

                data.openDiagram = (OpenDiagram) event.getValue();

                data.listener = new QualifierAdapter() {

                    @Override
                    public void qualifierDeleted(QualifierEvent event) {
                        if (event.getOldQualifier().equals(
                                data.openDiagram.getQualifier())) {
                            data.view.close();
                        }
                    }

                    @Override
                    public void qualifierUpdated(QualifierEvent event) {
                        if (data.openDiagram.getQualifier().equals(
                                event.getNewQualifier())) {
                            IDEF0TabView view = (IDEF0TabView) data.view;
                            if (view.isBaseFunctionSelected()) {
                                ViewTitleEvent event2 = new ViewTitleEvent(
                                        data.view, event.getNewQualifier()
                                        .getName());
                                view.titleChanged(event2);
                            }
                        }
                    }
                };

                data.elementListener = new ElementAdapter() {
                    @Override
                    public void elementDeleted(ElementEvent event) {
                        if (((IDEF0TabView) data.view)
                                .isSelectedElementId(event.getOldElement()))
                            data.view.close();
                    }
                };

                engine.addQualifierListener(data.listener);
                engine.addElementListener(data.openDiagram.getQualifier(),
                        data.elementListener);

                data.view = new IDEF0TabView(framework, NDataPluginFactory
                        .getDataPlugin(data.openDiagram.getQualifier(), engine,
                                rules), data.openDiagram.getFunctionId(),
                        frame, rules) {

                    @Override
                    public void close() {
                        super.close();
                        engine.removeQualifierListener(data.listener);
                        engine.removeElementListener(
                                data.openDiagram.getQualifier(),
                                data.elementListener);
                        TabbedEvent tEvent = new TabbedEvent(
                                IDEF0TabbedView.IDEF0_TAB_VIEW, this);
                        tabRemoved(tEvent);
                    }

                };
                TabbedEvent tEvent = new TabbedEvent(
                        IDEF0TabbedView.IDEF0_TAB_VIEW, (TabView) data.view);
                tabCreated(tEvent);
            }

        });
    }

    @Override
    public JToolBar[] getToolBars() {
        return new JToolBar[]{frame.getIdef0StateToolBar()};
    }

    @SuppressWarnings("unused")
    @Override
    public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor openStreans = new ActionDescriptor();
        openStreans.setActionLevel(ActionLevel.GLOBAL);
        openStreans.setMenu("Tools");
        openStreans.setAction(new AbstractAction() {

            {
                putValue(ACTION_COMMAND_KEY, "streams");
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                framework.propertyChanged(OPEN_STREAMS);
            }

        });

        ActionDescriptor runWebServer = new ActionDescriptor();
        runWebServer.setActionLevel(ActionLevel.GLOBAL);
        runWebServer.setMenu("Tools");
        runWebServer.setAction(new AbstractAction() {

            {
                putValue(ACTION_COMMAND_KEY, MainFrame.OPEN_WEB_SERVER);
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {

                if (server == null) {

                    server = new HTTPServer("0", NDataPluginFactory
                            .getDataPlugin(null, engine, rules), framework) {
                        @Override
                        protected void serverStarted() {
                            AbstractAttributePlugin.openUrl("http://127.0.0.1:"
                                    + getServer().getLocalPort(), framework);
                        }

                    };
                } else {
                    AbstractAttributePlugin.openUrl("http://127.0.0.1:"
                            + server.getServer().getLocalPort(), framework);
                }

            }

        });

        ActionDescriptor idef0Separator = new ActionDescriptor();
        idef0Separator.setMenu("IDEF0");

        ActionDescriptor loadModelsFromFile = new ActionDescriptor();

        loadModelsFromFile.setActionLevel(ActionLevel.GLOBAL);
        loadModelsFromFile.setMenu("IDEF0");
        loadModelsFromFile.setAction(new AbstractAction() {

            {
                putValue(ACTION_COMMAND_KEY, "loadModelsFromFile");
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                DataPlugin plugin = NDataPluginFactory.getDataPlugin(null,
                        engine, rules);
                try {
                    final JFileChooser chooser = frame.getChooser();
                    if (chooser.showOpenDialog(framework.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
                        plugin.loadFromParalel(chooser.getSelectedFile(),
                                framework);
                    }
                } catch (final IOException e) {
                    JOptionPane.showMessageDialog(framework.getMainFrame(),
                            e.getLocalizedMessage());
                }
            }
        });

        ActionDescriptor toolsSeparator = new ActionDescriptor();
        toolsSeparator.setMenu("Tools");

        if ((Metadata.DEMO)
                && (!new SerialCheker().check(Options.getString("SERIAL")))) {
            Metadata.DEMO_REGISTERED = false;
            if (Metadata.DEMO) {
                framework.addActionListener("MainFrameShown",
                        new ActionListener() {

                            @Override
                            public void onAction(ActionEvent event) {
                                String title = framework.getMainFrame()
                                        .getTitle();
                                if (!Metadata.DEMO_REGISTERED)
                                    title += " "
                                            + GlobalResourcesManager
                                            .getString("UnregisteredCopy");
                                else
                                    title += " "
                                            + MessageFormat.format(
                                            GlobalResourcesManager
                                                    .getString("RegisteredName"),
                                            Metadata.REGISTERED_FOR);
                                framework.getMainFrame().setTitle(title);
                            }
                        });
            }
            return new ActionDescriptor[]{runWebServer, openStreans,
                    findAction(CURSOR_TOOL), findAction(FUNCTION_TOOL),
                    findAction(ARROW_TOOL), findAction(TILDA_TOOL),
                    findAction(TEXT_TOOL), findAction(EXTERNAL_REFERENCE_TOOL),
                    findAction(DFDS_ROLE_TOOL), findAction(DATA_STORE_TOOL),
                    findAction(IDEF0_NET), findAction(GO_TO_PARENT),
                    findAction(GO_TO_CHILD), idef0Separator,
                    findAction(CENTER_ALL_SECTORS), idef0Separator,
                    loadModelsFromFile, toolsSeparator,
                    findAction(ADD_MODEL_TO_TEMPLATE, "Tools"),
                    findAction(USER_TEMPLATES, "Tools"), idef0Separator,
                    findAction(MODEL_PROPETIES), findAction(DIAGRAM_PROPETIES),
                    idef0Separator, findAction(EXPORT_TO_IMAGES),
                    createExportToIDL(), createImportFromIDL(),
                    createRegisterAction(), toolsSeparator,
                    findAction(CREATE_LEVEL)};
        } else {
            if (Metadata.DEMO) {
                framework.addActionListener("MainFrameShown",
                        new ActionListener() {

                            @Override
                            public void onAction(ActionEvent event) {
                                String title = framework.getMainFrame()
                                        .getTitle();
                                if (!Metadata.DEMO_REGISTERED)
                                    title += " "
                                            + GlobalResourcesManager
                                            .getString("UnregisteredCopy");
                                else
                                    title += " "
                                            + MessageFormat.format(
                                            GlobalResourcesManager
                                                    .getString("RegisteredName"),
                                            Metadata.REGISTERED_FOR);
                                framework.getMainFrame().setTitle(title);
                            }
                        });
            }
            ActionDescriptor editSeparator = new ActionDescriptor();
            editSeparator.setMenu("Edit");
            ActionDescriptor cut = new ActionDescriptor();
            cut.setMenu("Edit");
            cut.setAction(frame.findAction(MainFrame.CUT));
            ActionDescriptor copy = new ActionDescriptor();
            copy.setMenu("Edit");
            copy.setAction(frame.findAction(MainFrame.COPY));
            ActionDescriptor paste = new ActionDescriptor();
            paste.setMenu("Edit");
            paste.setAction(frame.findAction(MainFrame.PASTE));

            return new ActionDescriptor[]{runWebServer, openStreans,
                    findAction(CURSOR_TOOL), findAction(FUNCTION_TOOL),
                    findAction(ARROW_TOOL), findAction(TILDA_TOOL),
                    findAction(TEXT_TOOL), findAction(EXTERNAL_REFERENCE_TOOL),
                    findAction(DFDS_ROLE_TOOL), findAction(DATA_STORE_TOOL),
                    findAction(IDEF0_NET), findAction(GO_TO_PARENT),
                    findAction(GO_TO_CHILD), idef0Separator,
                    findAction(CENTER_ALL_SECTORS), idef0Separator,
                    loadModelsFromFile, toolsSeparator,
                    findAction(ADD_MODEL_TO_TEMPLATE, "Tools"),
                    findAction(USER_TEMPLATES, "Tools"), idef0Separator,
                    findAction(MODEL_PROPETIES), findAction(DIAGRAM_PROPETIES),
                    idef0Separator, findAction(EXPORT_TO_IMAGES),
                    createExportToIDL(), createImportFromIDL(), editSeparator,
                    cut, copy, paste};
        }
    }

    private ActionDescriptor createRegisterAction() {
        ActionDescriptor register = new ActionDescriptor();
        register.setMenu("Help");
        register.setAction(new AbstractAction() {

            {
                putValue(ACTION_COMMAND_KEY, "RegisterApplication");
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                JPanel panel = new JPanel(new BorderLayout());
                double[][] size = {
                        {5, TableLayout.FILL, TableLayout.MINIMUM,
                                TableLayout.FILL, TableLayout.MINIMUM,
                                TableLayout.FILL, TableLayout.MINIMUM,
                                TableLayout.FILL, TableLayout.MINIMUM,
                                TableLayout.FILL, 5},
                        {5, TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                                TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5}};

                JPanel center = new JPanel(new TableLayout(size));

                final JTextField regName = new JTextField();
                center.add(new JLabel(ResourceLoader.getString("RegName")),
                        "1,1,9,1");

                center.add(regName, "1,3,9,3");

                center.add(
                        new JLabel(ResourceLoader.getString("SerialNumber")),
                        "1,5,9,5");

                final JTextField[] fields = new JTextField[5];
                for (int i = 0; i < 5; i++) {
                    try {
                        fields[i] = new JFormattedTextField(new MaskFormatter(
                                "AAAAA"));
                    } catch (ParseException e1) {
                    }
                    fields[i].setPreferredSize(new Dimension(60, fields[i]
                            .getPreferredSize().height));
                    center.add(fields[i], (i * 2 + 1) + ", 7");
                    center.add(new JLabel("-"), (i * 2 + 2) + ", 7");

                    final Action oldPaste = fields[i].getActionMap().get(
                            "paste");

                    AbstractAction paste = new AbstractAction() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            String text = getClipboard();
                            if (text != null) {
                                String trim = text.trim();
                                if (trim.length() == 29) {
                                    String[] strings = trim.split("-");
                                    boolean ok = true;
                                    for (String s : strings) {
                                        if (s.length() != 5)
                                            ok = false;
                                        for (char c : s.toCharArray()) {
                                            if (!Character.isLetterOrDigit(c))
                                                ok = false;
                                        }
                                    }
                                    if (ok) {
                                        for (int i = 0; i < 5; i++) {
                                            fields[i].setText(strings[i]
                                                    .toUpperCase());
                                        }
                                        return;
                                    }
                                }
                            }
                            oldPaste.actionPerformed(e);
                        }
                    };
                    fields[i].getActionMap().put("paste-from-clipboard", paste);
                    fields[i].getActionMap().put("paste", paste);
                }

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        fields[0].requestFocus();
                    }
                });

                panel.add(center, BorderLayout.SOUTH);

                BaseDialog dialog = new BaseDialog(framework.getMainFrame(),
                        true) {

                    private int count = 0;

                    @Override
                    protected void onOk() {
                        final StringBuffer sb = new StringBuffer();
                        for (JTextField field : fields) {
                            sb.append(field.getText().trim());
                            sb.append("-");
                        }

                        String serial = sb.toString().substring(0,
                                sb.length() - 1);
                        if (new SerialCheker().check(serial)) {
                            Metadata.DEMO_REGISTERED = true;
                            com.ramussoft.gui.common.prefrence.Options
                                    .setString("REGISTERED_VERSION",
                                            Boolean.toString(true));
                            Options.setString("SERIAL", serial);

                            for (ElementListener el : framework.getEngine()
                                    .getElementListeners(null)) {
                                if (el instanceof DemoChecker) {
                                    DemoChecker dc = (DemoChecker) el;
                                    framework.getEngine()
                                            .removeElementListener(null, el);
                                    dc.getFramework().propertyChanged(
                                            "EnableSaveActions");
                                    String uc = " "
                                            + GlobalResourcesManager
                                            .getString("UnregisteredCopy");
                                    String title = framework.getMainFrame()
                                            .getTitle().replace(uc, "");

                                    framework.getMainFrame().setTitle(title);
                                }
                            }
                            com.ramussoft.gui.common.prefrence.Options
                                    .setString("REG_NAME", regName.getText());
                            Metadata.REGISTERED_FOR = regName.getText();
                            super.onOk();
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    JOptionPane.showMessageDialog(
                                            framework.getMainFrame(),
                                            MessageFormat.format(
                                                    ResourceLoader
                                                            .getString("SerialIsRight"),
                                                    Metadata.getApplicationName()));
                                }
                            });
                        } else {
                            try {
                                getOKButton().setEnabled(false);
                                Thread.sleep(3000 * count);
                                count++;
                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(framework
                                                .getMainFrame(), ResourceLoader
                                                .getString("SerialIsWrong"));
                                    }
                                });
                                getOKButton().setEnabled(true);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                dialog.setTitle(ResourceLoader.getString("RegisterApplication"));

                dialog.setMainPane(panel);

                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setResizable(false);
                dialog.setVisible(true);

            }
        });
        return register;
    }

    private static String getClipboard() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard()
                .getContents(null);

        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String text = (String) t
                        .getTransferData(DataFlavor.stringFlavor);
                return text;
            }
        } catch (UnsupportedFlavorException e) {
        } catch (IOException e) {
        }
        return null;
    }

    private ActionDescriptor createImportFromIDL() {
        ActionDescriptor descriptor = new ActionDescriptor();
        descriptor.setMenu("IDEF0");

        AbstractAction action = new AbstractAction() {

            {
                putValue(ACTION_COMMAND_KEY, "ImportFromIDL");
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                importFromIdl();
            }
        };

        descriptor.setAction(action);

        return descriptor;
    }

    private ActionDescriptor createExportToIDL() {
        ActionDescriptor descriptor = new ActionDescriptor();
        descriptor.setMenu("IDEF0");

        AbstractAction action = new AbstractAction() {

            {
                putValue(ACTION_COMMAND_KEY, "ExportToIDL");
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportToIdl();
            }
        };

        descriptor.setAction(action);

        return descriptor;
    }

    protected void importFromIdl() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isFile()) {
                    if (f.getName().toLowerCase().endsWith(".idl"))
                        return true;
                    return false;
                }
                return true;
            }

            @Override
            public String getDescription() {
                return "*.idl";
            }

        });
        if (fc.showOpenDialog(framework.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            try {

                ((Journaled) engine).startUserTransaction();
                Qualifier qualifier = engine.createQualifier();
                Attribute name = null;
                String aName = ResourceLoader.getString("name");
                for (Attribute a : engine.getAttributes()) {
                    if (a.getAttributeType().toString().equals("Core.Text")) {
                        if (aName.equals(a.getName()))
                            name = a;
                    }
                }
                if (name == null) {
                    name = engine.createAttribute(new AttributeType("Core",
                            "Text", true));
                    name.setName(aName);
                    engine.updateAttribute(name);
                }

                qualifier.getAttributes().add(name);
                qualifier.setAttributeForName(name.getId());

                IDEF0Plugin.installFunctionAttributes(qualifier, engine);
                DataPlugin plugin = NDataPluginFactory.getDataPlugin(qualifier,
                        engine, rules);

                File file = fc.getSelectedFile();
                plugin.importFromIDL(plugin, "cp1251",
                        new FileInputStream(file));

                ((Journaled) engine).commitUserTransaction();
                OpenDiagram openDiagram = new OpenDiagram(qualifier, -1l);
                framework.propertyChanged(OPEN_DIAGRAM, openDiagram);
            } catch (Exception e) {
                ((Journaled) engine).rollbackUserTransaction();
                e.printStackTrace();
                JOptionPane.showMessageDialog(framework.getMainFrame(),
                        "Формат выбранного вами файла IDL не поддерживается.");
            }
        }
    }

    protected void exportToIdl() {

        final JList list = new JList();

        final List<Qualifier> base = IDEF0Plugin.getBaseQualifiers(engine);
        Collections.sort(base, new Comparator<Qualifier>() {

            private Collator collator = Collator.getInstance();

            @Override
            public int compare(Qualifier o1, Qualifier o2) {
                return collator.compare(o1.getName(), o2.getName());
            }
        });
        list.setModel(new AbstractListModel() {

            @Override
            public Object getElementAt(int index) {
                return base.get(index);
            }

            @Override
            public int getSize() {
                return base.size();
            }

        });

        JScrollPane pane = new JScrollPane();
        pane.setViewportView(list);

        JFileChooser fc = new JFileChooser() {
            @Override
            public void approveSelection() {
                Qualifier result = (Qualifier) list.getSelectedValue();
                if (result == null) {
                    JOptionPane.showMessageDialog(this,
                            ResourceLoader.getString("select_model_first"));
                } else {
                    File file = getSelectedFile();
                    if (file.exists()) {
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
                    DataPlugin plugin = NDataPluginFactory.getDataPlugin(
                            result, engine, rules);
                    try {
                        if (!file.getName().toLowerCase().endsWith(".idl")) {
                            file = new File(file.getParentFile(),
                                    file.getName() + ".idl");
                        }
                        FileOutputStream fileOutputStream = new FileOutputStream(
                                file);
                        plugin.exportToIDL(plugin.getBaseFunction(),
                                fileOutputStream, "cp1251");
                        fileOutputStream.close();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(framework.getMainFrame(),
                                e.getLocalizedMessage());
                    }
                    super.approveSelection();
                }
            }
        };

        fc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isFile()) {
                    if (f.getName().toLowerCase().endsWith(".idl"))
                        return true;
                    return false;
                }
                return true;
            }

            @Override
            public String getDescription() {
                return "*.idl";
            }

        });

        JPanel bottom = new JPanel(new TableLayout(new double[][]{
                {5, TableLayout.FILL},
                {TableLayout.MINIMUM, 5, TableLayout.FILL}}));

        bottom.add(new JLabel(ResourceLoader.getString("Model")), "1, 0");
        bottom.add(pane, "1, 2");

        fc.setAccessory(bottom);

        fc.showSaveDialog(framework.getMainFrame());
    }

    private ActionDescriptor findAction(String action) {
        return findAction(action, "IDEF0");
    }

    private ActionDescriptor findAction(String actionString, String menu) {
        Action action = frame.findAction(actionString);
        ActionDescriptor descriptor = new ActionDescriptor();
        descriptor.setAction(action);
        descriptor.setMenu(menu);
        return descriptor;
    }

    @Override
    public Preferences[] getProjectPreferences() {
        List<Qualifier> list = IDEF0Plugin.getBaseQualifiers(engine);
        Preferences[] res = new Preferences[((list.size() > 0) ? 1 : 0)];
        final DataPlugin dataPlugin;
        if (list.size() == 0)
            dataPlugin = null;
        else
            dataPlugin = NDataPluginFactory.getDataPlugin(list.get(0), engine,
                    rules);
        if (res.length > 0)

            res[res.length - 1] = new AbstractPreferences() {

                private OwnerClasificators c = new OwnerClasificators(
                        dataPlugin);

                {
                    c.updateOuners();
                }

                @Override
                public JComponent createComponent() {
                    return c;
                }

                @Override
                public String getTitle() {
                    return ResourceLoader.getString("Owners.Clasificators");
                }

                @Override
                public boolean save(JDialog dialog) {
                    c.apply();
                    return true;
                }

            };
        return res;
    }

    @Override
    public Preferences[] getApplicationPreferences() {
        return new Preferences[]{new AbstractPreferences() {

            private ViewIDEF0PropertiosPanel panel = new ViewIDEF0PropertiosPanel();

            @Override
            public JComponent createComponent() {
                return new JScrollPane(panel);
            }

            @Override
            public String getTitle() {
                return ResourceLoader.getString("view_idf0_properties");
            }

            @Override
            public boolean save(JDialog dialog) {
                panel.saveOptions();
                return true;
            }

        }};
    }
}
