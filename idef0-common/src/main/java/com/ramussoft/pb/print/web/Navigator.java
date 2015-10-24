package com.ramussoft.pb.print.web;

import java.awt.BorderLayout;
import java.awt.Desktop;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.StandardFilePlugin;
import com.ramussoft.gui.common.AbstractUniqueView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.print.HTMLPrintable;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.data.AbstractDataPlugin;
import com.ramussoft.pb.frames.MainFrame;
import com.ramussoft.pb.frames.docking.ViewPanel;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.Request;
import com.ramussoft.web.Servlet;

public class Navigator extends AbstractUniqueView implements UniqueView {

    private JTextPane pane = new JTextPane() {
        @Override
        public void setPage(URL page) throws IOException {
            final URLConnection conn = page.openConnection();
            String field = conn.getHeaderField("Ramus-content-disposition");
            if (field != null) {
                try {
                    String[] options = {
                            GlobalResourcesManager
                                    .getString("FileEditor.SaveAs"),
                            GlobalResourcesManager.getString("FileEditor.Open"),
                            GlobalResourcesManager.getString("cancel")};
                    String fileName = new String(toBytes(field), "UTF-8");

                    int end = Math.max(fileName.lastIndexOf('\\'), fileName
                            .lastIndexOf('/'));
                    if ((end > 0) && (end + 2 < fileName.length())) {
                        fileName = fileName.substring(end + 1);
                    }

                    int result = JOptionPane.showOptionDialog(framework
                                    .getMainFrame(), ResourceLoader
                                    .getString("OpenAttachment.message"),
                            ResourceLoader.getString("OpenAttachment.title"),
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options,
                            options[0]);
                    if (result == 0) {
                        JFileChooser chooser = getSaveDialog();
                        chooser.setSelectedFile(new File(fileName));
                        InputStream in = conn.getInputStream();
                        if (JFileChooser.APPROVE_OPTION == chooser
                                .showSaveDialog(framework.getMainFrame())) {
                            FileOutputStream fos = new FileOutputStream(chooser
                                    .getSelectedFile());
                            AbstractDataPlugin.copyStream(in, fos);
                            fos.close();
                        }
                        in.close();
                    } else if (result == 1) {
                        String tmp = System.getProperty("java.io.tmpdir");
                        if (!tmp.endsWith(File.separator))
                            tmp += File.separator;
                        String prefix = "";
                        File file;
                        int id = 0;
                        while ((file = new File(tmp + prefix + fileName))
                                .exists()) {
                            id++;
                            prefix = "(" + id + ") ";
                        }
                        FileOutputStream fos = new FileOutputStream(file);
                        InputStream in = conn.getInputStream();
                        AbstractDataPlugin.copyStream(in, fos);
                        fos.close();
                        in.close();
                        file.deleteOnExit();
                        Desktop d = Desktop.getDesktop();
                        if (d != null) {
                            d.open(file);
                        }
                    }
                } catch (IOException e) {
                    JOptionPane
                            .showMessageDialog(null, e.getLocalizedMessage());
                }

                return;
            }

            URL loaded = getPage();

            // reset scrollbar
            if (!page.equals(loaded) && page.getRef() == null) {
                scrollRectToVisible(new Rectangle(0, 0, 1, 1));
            }
            boolean reloaded = false;
            if ((loaded == null) || !loaded.sameFile(page)) {
                // different url or POST method, load the new content

                // Either we do not have POST data, or should submit the data
                // synchronously.
                InputStream in = getStream(page);
                if (getEditorKit() != null) {
                    Document doc = initializeModel(getEditorKit(), page);

                    read(in, doc);
                    setDocument(doc);
                    reloaded = true;
                }

            }
            final String reference = page.getRef();
            if (reference != null) {
                if (!reloaded) {
                    scrollToReference(reference);
                } else {
                    // Have to scroll after painted.
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            scrollToReference(reference);
                        }
                    });
                }
                getDocument().putProperty(Document.StreamDescriptionProperty,
                        page);
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        scrollRectToVisible(new Rectangle());
                    }
                });
            }
            firePropertyChange("page", loaded, page);

            if (page != null) {
                location = page.toString();
                updateHistory();
            }
        }

        protected InputStream getStream(URL page) throws IOException {
            return super.getStream(page);
        }

        ;

        private Document initializeModel(EditorKit kit, URL page) {
            Document doc = kit.createDefaultDocument();

            if (doc.getProperty(Document.StreamDescriptionProperty) == null) {
                doc.putProperty(Document.StreamDescriptionProperty, page);
            }
            return doc;
        }

        protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                                            int condition, boolean pressed) {
            if (pressed) {
                if ((e.getKeyCode() == KeyEvent.VK_DOWN)
                        || (e.getKeyCode() == KeyEvent.VK_SPACE)) {
                    ActionEvent event = new ActionEvent(scrollPane
                            .getVerticalScrollBar(), -1,
                            "positiveUnitIncrement");
                    Action action = scrollPane.getVerticalScrollBar()
                            .getActionMap().get("positiveUnitIncrement");
                    if (action != null) {
                        action.actionPerformed(event);
                        return false;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    ActionEvent event = new ActionEvent(scrollPane
                            .getVerticalScrollBar(), -1,
                            "negativeUnitIncrement");
                    Action action = scrollPane.getVerticalScrollBar()
                            .getActionMap().get("negativeUnitIncrement");
                    if (action != null) {
                        action.actionPerformed(event);
                        return false;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    ActionEvent event = new ActionEvent(scrollPane
                            .getVerticalScrollBar(), -1,
                            "negativeUnitIncrement");
                    if (goBack.isEnabled())
                        goBack.actionPerformed(event);
                }
            }
            return super.processKeyBinding(ks, e, condition, pressed);
        }

        ;

        protected EditorKit createDefaultEditorKit() {
            return new HTMLEditorKit();
        }

        ;
    };

    private String location;

    private Engine engine;

    private AccessRules rules;

    private HTTPServer server;

    private MainFrame frame;

    private ViewPanel panel;

    private DataPlugin dataPlugin;

    private JScrollPane scrollPane;

    private History history = new History();

    private ExportToHTMLAction exportToHTMLAction = new ExportToHTMLAction();

    private JFileChooser saveDialog = null;

    private Action goHome = new AbstractAction() {

        {
            putValue(ACTION_COMMAND_KEY, "toStart");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/images/go_home.png")));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
                    KeyEvent.ALT_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (server == null) {
                getServer();
            } else {
                location = "http://127.0.0.1:"
                        + getServer().getServer().getLocalPort();
                openLocation();
            }
        }

    };

    private Action goBack = new AbstractAction() {

        {
            putValue(ACTION_COMMAND_KEY, "GoBack");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/images/go_back.png")));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
                    KeyEvent.ALT_MASK));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            location = history.goBack();
            openLocation();
        }

    };

    private Action goForward = new AbstractAction() {

        {
            putValue(ACTION_COMMAND_KEY, "GoForward");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/images/go_forward.png")));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
                    KeyEvent.ALT_MASK));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            location = history.goForward();
            openLocation();
        }

    };

    public Navigator(GUIFramework framework, Engine engine, AccessRules rules,
                     MainFrame mainFrame) {
        super(framework);
        this.engine = engine;
        this.rules = rules;
        this.frame = mainFrame;
        panel = new ViewPanel(frame) {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String cmd = e.getActionCommand();
                if (MainFrame.PRINT.equals(cmd)) {
                    final String add;
                    if (location.indexOf('?') >= 0) {
                        add = "&printVersion=true";
                    } else {
                        if (location.endsWith("/")) {
                            add = "?printVersion=true";
                        } else {
                            add = "/?printVersion=true";
                        }
                    }

                    final HTMLPrintable htmlPrintable = createHTMPPrintable();
                    try {
                        htmlPrintable.loadPage(location + add,
                                new ActionListener() {

                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        try {
                                            htmlPrintable
                                                    .print(Navigator.this.framework);
                                        } catch (PrinterException e1) {
                                            e1.printStackTrace();
                                            JOptionPane.showMessageDialog(
                                                    Navigator.this.framework
                                                            .getMainFrame(),
                                                    e1.getLocalizedMessage());
                                        }

                                    }
                                });
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(Navigator.this.framework
                                .getMainFrame(), e1.getLocalizedMessage());
                    }

                } else if (MainFrame.PAGE_SETUP.equals(cmd)) {
                    final HTMLPrintable htmlPrintable = createHTMPPrintable();
                    htmlPrintable.pageSetup(Navigator.this.framework);
                } else if (MainFrame.PRINT_PREVIEW.equals(cmd)) {

                    final String add;
                    if (location.indexOf('?') >= 0) {
                        add = "&printVersion=true";
                    } else {
                        if (location.endsWith("/")) {
                            add = "?printVersion=true";
                        } else {
                            add = "/?printVersion=true";
                        }
                    }

                    final HTMLPrintable htmlPrintable = createHTMPPrintable();
                    try {
                        htmlPrintable.loadPage(location + add,
                                new ActionListener() {

                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        Navigator.this.framework
                                                .printPreview(htmlPrintable);

                                    }
                                });
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(Navigator.this.framework
                                .getMainFrame(), e1.getLocalizedMessage());
                    }

                }

            }

            private HTMLPrintable createHTMPPrintable() {
                HTMLPrintable htmlPrintable = new HTMLPrintable();
                return htmlPrintable;
            }

            @Override
            public String[] getEnableActions() {
                if (exportToHTMLAction.isEnabled())
                    return new String[]{MainFrame.PRINT,
                            MainFrame.PRINT_PREVIEW, MainFrame.PAGE_SETUP};
                return new String[]{};
            }

            @Override
            public String getTitleKey() {
                return "NoTitle";
            }

        };
    }

    protected void updateHistory() {
        history.update(location);
        goBack.setEnabled(history.canGonBack());
        goForward.setEnabled(history.canGoForward());
    }

    @Override
    public void focusGained() {
        frame.setActiveView(panel);
    }

    @Override
    public void focusLost() {
        frame.setActiveView(null);
    }

    @Override
    public JComponent createComponent() {
        pane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == EventType.ACTIVATED) {
                    location = e.getURL().toExternalForm();
                    openLocation();
                }
            }
        });

        pane.setEditable(false);

        scrollPane = new JScrollPane();
        scrollPane.setViewportView(this.pane);
        return scrollPane;
    }

    private void openLocation() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (server == null) {
                        getServer();
                    } else {
                        if (location != null)
                            pane.setPage(location);
                    }

                } catch (IOException e) {
                    JOptionPane.showMessageDialog(framework.getMainFrame(), e
                            .getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        });
        if (!exportToHTMLAction.isEnabled()) {
            exportToHTMLAction.setEnabled(true);
            framework.updateViewActions();
        }
    }

    @Override
    public Action[] getActions() {
        Action refresh = new AbstractAction() {

            {
                putValue(ACTION_COMMAND_KEY, "refresh_current_page");
                putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                        "/com/ramussoft/gui/refresh.png")));
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R,
                        KeyEvent.CTRL_MASK));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                pane.getDocument().putProperty(
                        Document.StreamDescriptionProperty, null);
                openLocation();
            }
        };
        return new Action[]{goBack, goForward, goHome, refresh, null,
                exportToHTMLAction};
    }

    @Override
    public String getDefaultWorkspace() {
        return "ModelNavigator";
    }

    @Override
    public String getId() {
        return "ModelNavigator";
    }

    public void dispose() {
        super.close();
        if (server != null) {
            server.interrupt();
            try {
                server.getServer().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            server = null;
        }
        frame = null;
        pane.setText("");
        pane = null;
        panel = null;
        dataPlugin = null;
        scrollPane = null;
        history = null;
    }

    /**
     * @return the server
     */
    public HTTPServer getServer() {
        if (server == null) {
            server = new HTTPServer("0", getDataPlugin(), framework) {
                @Override
                protected Servlet getServlet(Request request) {
                    return new HTTPParser(dataPlugin, framework) {
                        protected synchronized void printStartD()
                                throws IOException {
                            if (printVersion)
                                return;
                            htmlStream.println("<table width=100%>");
                            htmlStream.println("<tr>");
                            htmlStream
                                    .println("<td valign=top bgcolor=#EEEEFF width=20%>");
                            htmlStream.println("<table>");
                            printClasificatorsList();
                            printModelsList();
                            printMatrixProjectionsList();
                            printReportsList();
                            htmlStream.println("</table>");
                            htmlStream.println("</td>");
                            htmlStream.println("<td valign=top align=left>");
                        }
                    };
                }

                @Override
                protected void serverStarted() {
                    location = "http://127.0.0.1:" + getServer().getLocalPort();
                    openLocation();
                }
            };
        }
        return server;
    }

    /**
     * @return the dataPlugin
     */
    public DataPlugin getDataPlugin() {
        if (dataPlugin == null) {
            dataPlugin = NDataPluginFactory.getDataPlugin(null, engine, rules);
        }
        return dataPlugin;
    }

    protected class ExportToHTMLAction extends AbstractAction {

        public ExportToHTMLAction() {
            putValue(ACTION_COMMAND_KEY, "Action.ExportToHTML");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/print/html.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final String add;
            if (location.indexOf('?') >= 0) {
                add = "&printVersion=true";
            } else {
                if (location.endsWith("/")) {
                    add = "?printVersion=true";
                } else {
                    add = "/?printVersion=true";
                }
            }

            final HTMLPrintable htmlPrintable = new HTMLPrintable();
            try {
                htmlPrintable.loadPage(location + add, null);
                htmlPrintable.createExportToHTMLAction(framework)
                        .actionPerformed(e);
            } catch (IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(Navigator.this.framework
                        .getMainFrame(), e1.getLocalizedMessage());
            }
        }

    }

    @Override
    public void onAction(com.ramussoft.gui.common.event.ActionEvent event) {
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
        if (exportToHTMLAction.isEnabled())
            return new String[]{StandardFilePlugin.ACTION_PRINT,
                    StandardFilePlugin.ACTION_PAGE_SETUP,
                    StandardFilePlugin.ACTION_PRINT_PREVIEW};
        return super.getGlobalActions();
    }

    private byte[] toBytes(String value) {
        if (value.length() == 0)
            return new byte[]{};
        byte[] bs = new byte[value.length() / 2];
        int len = value.length();
        for (int i = 0; i < len; i += 2) {
            int val;
            char c = value.charAt(i);
            if (c >= 'A')
                val = 16 * (c - 'A' + 10);
            else
                val = 16 * (c - '0');
            c = value.charAt(i + 1);
            if (c >= 'A')
                val += (c - 'A' + 10);
            else
                val += (c - '0');
            bs[i / 2] = (byte) (val - 128);
        }
        return bs;
    }

    public JFileChooser getSaveDialog() {
        if (saveDialog == null) {
            saveDialog = new JFileChooser() {
                @Override
                public void approveSelection() {
                    if (getSelectedFile().exists()) {
                        if (JOptionPane.showConfirmDialog(framework
                                        .getMainFrame(), GlobalResourcesManager
                                        .getString("File.Exists"), UIManager
                                        .getString("OptionPane.messageDialogTitle"),
                                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                            return;
                    }
                    super.approveSelection();
                }
            };
        }
        return saveDialog;
    }

    @Override
    public String getDefaultPosition() {
        return BorderLayout.CENTER;
    }
}
