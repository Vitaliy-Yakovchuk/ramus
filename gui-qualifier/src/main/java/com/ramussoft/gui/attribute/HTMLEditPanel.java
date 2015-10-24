package com.ramussoft.gui.attribute;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.dts.spell.swing.JTextComponentSpellChecker;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.HTMLPage;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.gui.GUIPatchFactory;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.spell.Language;
import com.ramussoft.gui.spell.SpellFactory;

import static com.ramussoft.gui.attribute.TextAttributePlugin.*;

public class HTMLEditPanel extends JPanel implements AttributeEditor {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private HTMLEditOptionDialog optionDialog = null;

    private String emptyValue;

    private HTMLPage page;

    private ArrayList<String> openFiles;

    class UndoAction extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(final ActionEvent e) {
            if (!undoMngr.canUndo())
                return;
            try {
                undoMngr.undo();
            } catch (final CannotUndoException ex) {
                ex.printStackTrace();
            }
            updateUndoState();
            redoAction.updateRedoState();
        }

        protected void updateUndoState() {
            setEnabled(undoMngr.canUndo());
        }
    }

    /**
     * Class for implementing Redo as an autonomous action
     */
    class RedoAction extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(final ActionEvent e) {
            if (!undoMngr.canRedo())
                return;
            try {
                undoMngr.redo();
            } catch (final CannotUndoException ex) {
                ex.printStackTrace();
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState() {
            setEnabled(undoMngr.canRedo());
        }
    }

    private JScrollPane jScrollPane = null;

    private JTextPane jTextPane = null;

    protected UndoManager undoMngr = new UndoManager();

    protected UndoAction undoAction = new UndoAction();

    protected RedoAction redoAction = new RedoAction(); // @jve:decl-index=0:

    private AbstractAction openInEditorAction = null;

    private AbstractAction setupEditorAction = null;

    private JFrame frame;

    private Engine engine;

    private AccessRules rules;

    private Element element;

    private Attribute attribute;

    private JTextComponentSpellChecker checker;

    private File tmpFile = null;

    /**
     * This is the default constructor
     */
    @SuppressWarnings({"unchecked"})
    public HTMLEditPanel(JFrame frame, Engine engine, AccessRules rules,
                         Element element, Attribute attribute) {
        super();
        this.frame = frame;
        this.engine = engine;
        this.rules = rules;
        this.element = element;
        this.attribute = attribute;
        openFiles = (ArrayList<String>) engine.getPluginProperty("Core",
                "HTMLEditoOpenFiles");
        if (openFiles == null) {
            openFiles = new ArrayList<String>();
            engine.setPluginProperty("Core", "HTMLEditoOpenFiles", openFiles);
        }
        initialize();
        if (!rules.canUpdateElement(element.getId(), attribute.getId())) {
            jTextPane.setEditable(false);
            jTextPane.setEnabled(false);
            if (openInEditorAction != null)
                openInEditorAction.setEnabled(false);
        }
        /*
         * page = (HTMLPage) engine.getAttribute(element, attribute);
		 * 
		 * boolean noError = true;
		 * 
		 * if (page != null) { noError = loadPageText(); }
		 */
        // if (noError) {
        jTextPane.setComponentPopupMenu(createSelectLanguageMenu());
        createChecker();
        // }
    }

    private boolean loadPageText() {
        boolean noError = true;
        try {
            tmpFile = File.createTempFile("index", ".html");

            FileOutputStream fos = new FileOutputStream(tmpFile);
            fos.write(page.getData());
            fos.close();

            jTextPane.setPage(tmpFile.toURI().toURL());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return noError;
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setLayout(new BorderLayout());
        this.setSize(300, 200);
        this.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
    }

    private HTMLEditOptionDialog getOptionDialog() {
        if (optionDialog == null) {
            optionDialog = new HTMLEditOptionDialog(frame);
        }
        return optionDialog;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextPane());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTextPane
     *
     * @return javax.swing.JTextPane
     */
    private JTextPane getJTextPane() {
        if (jTextPane == null) {
            jTextPane = new JTextPane() {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public void paste() {
                    final Clipboard clip = Toolkit.getDefaultToolkit()
                            .getSystemClipboard();
                    final Transferable t = clip.getContents(null);
                    Object o;
                    try {
                        o = t.getTransferData(DataFlavor.stringFlavor);
                        if (o != null)
                            replaceSelection((String) o);
                    } catch (final UnsupportedFlavorException e) {
                        e.printStackTrace();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            jTextPane.setContentType("text/html");
            jTextPane.getDocument().addUndoableEditListener(
                    new UndoableEditListener() {
                        public void undoableEditHappened(
                                final UndoableEditEvent uee) {
                            undoMngr.addEdit(uee.getEdit());
                            undoAction.updateUndoState();
                            redoAction.updateRedoState();
                        }
                    });
            jTextPane.getActionMap().put("undoEdit", undoAction);

            jTextPane.getActionMap().put("redoEdit", redoAction);

            emptyValue = jTextPane.getText();

        }
        final HTMLEditorKit kit = new HTMLEditorKit() {
            /**
             *
             */
            private static final long serialVersionUID = 802712872825556847L;

            @Override
            public Document createDefaultDocument() {
                AbstractDocument res = (AbstractDocument) super
                        .createDefaultDocument();
                res.setAsynchronousLoadPriority(-1);
                return res;
            }
        };

        jTextPane.setEditorKit(kit);
        GUIPatchFactory.patchHTMLTextPane(jTextPane);

        return jTextPane;
    }

    private void createHtmlEditor() {
        if (page == null) {
            String text = jTextPane.getText();
            try {
                page = new HTMLPage(text.getBytes("UTF8"),
                        (page != null) ? page.getPath() : null);
                ((Journaled) engine).startUserTransaction();
                engine.setAttribute(element, attribute, page);
                ((Journaled) engine).commitUserTransaction();
                HTMLPage page = (HTMLPage) engine.getAttribute(element, attribute);
                page.setEmpty(true);
                setValue(page);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (openEditor()) {
            updateEditing();
        }
    }

    private boolean openEditor() {
        final String html = Options.getString("HTML_EDITOR");
        if (html == null)
            return false;

        FileIEngineImpl impl = (FileIEngineImpl) engine.getDeligate();

        final ProcessBuilder builder = new ProcessBuilder(new String[]{html,
                impl.getFileForPath(page.getPath()).getAbsolutePath()});
        try {
            builder.start();
        } catch (final IOException e) {
            return false;
        }
        if (openFiles.indexOf(page.getPath()) < 0)
            openFiles.add(page.getPath());
        return true;
    }

    private void updateEditing() {
        jTextPane
                .setEditable((openFiles.indexOf(page.getPath()) < 0)
                        && (rules.canUpdateElement(element.getId(), attribute
                        .getId())));
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private AbstractAction getOpenInEditorAction() {
        if (openInEditorAction == null) {
            openInEditorAction = new AbstractAction() {

                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                {
                    putValue(TOOL_TIP_TEXT_KEY, GlobalResourcesManager
                            .getString("HTMLEditor.OpenInEditor"));
                    putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/open.png")));
                    putValue(ACTION_COMMAND_KEY, "HTMLEditor.OpenInEditor");
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    createHtmlEditor();
                }
            };
            setWEnable();

        }
        return openInEditorAction;
    }

    private void setWEnable() {
        final String html = Options.getString("HTML_EDITOR");
        openInEditorAction.setEnabled(html != null && new File(html).exists()
                && (engine.getDeligate() instanceof FileIEngineImpl));
    }

    /**
     * This method initializes jButton1
     *
     * @return javax.swing.JButton
     */
    private AbstractAction getSetupEditorAction() {
        if (setupEditorAction == null) {
            setupEditorAction = new AbstractAction() {

                /**
                 *
                 */
                private static final long serialVersionUID = 5355396413361705651L;

                {
                    putValue(TOOL_TIP_TEXT_KEY, GlobalResourcesManager
                            .getString("HTMLEditor.Options"));
                    putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/preferencies.png")));
                    putValue(ACTION_COMMAND_KEY, "HTMLEditor.Options");
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    editorOptions();
                }

            };
            setupEditorAction
                    .setEnabled((engine.getDeligate() instanceof FileIEngineImpl));
        }
        return setupEditorAction;
    }

    protected void editorOptions() {
        getOptionDialog().showModal();
        setWEnable();
    }

    @Override
    public void close() {
        if (checker != null) {
            checker.stopRealtimeMarkErrors();
            checker = null;
        }
        if (tmpFile != null) {
            tmpFile.delete();
        }
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public Object getValue() {
        String text = jTextPane.getText();
        if (emptyValue.equals(text)) {
            if ((page == null) || (page.isEmpty()))
                return null;
            return page;
        }
        HTMLPage p;
        try {
            p = new HTMLPage(text.getBytes("UTF8"), (page != null) ? page
                    .getPath() : null);
            return p;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object setValue(Object value) {
        page = (HTMLPage) value;
        if (value == null) {
            jTextPane.setText(emptyValue);
            GUIPatchFactory.patchHTMLTextPane(jTextPane);
            emptyValue = jTextPane.getText();
            return null;
        }
        updateEditing();
        if (engine.getDeligate() instanceof FileIEngineImpl) {
            FileIEngineImpl impl = (FileIEngineImpl) engine.getDeligate();
            try {
                jTextPane.getDocument().putProperty(
                        Document.StreamDescriptionProperty, null);
                jTextPane.setPage(impl.getFileForPath(page.getPath()).toURI()
                        .toURL());
                GUIPatchFactory.patchHTMLTextPane(jTextPane);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            loadPageText();

        }
        emptyValue = jTextPane.getText();
        return value;
    }

    @Override
    public Action[] getActions() {
        return new Action[]{getSetupEditorAction(), getOpenInEditorAction()};
    }

    private JPopupMenu createSelectLanguageMenu() {
        JMenu lMenu = new JMenu(GlobalResourcesManager
                .getString("Editor.Language"));
        Language[] languages = SpellFactory.getLanguages();
        final Properties ps = engine.getProperties(USER_GUI_SPELL_PROPERTIES);
        Language dLanguage = getCurrentLanguage(ps);

        ButtonGroup group = new ButtonGroup();

        for (Language l : languages) {
            final String ln = l.getName();
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                    new AbstractAction(l.getLocalizedName()) {
                        /**
                         *
                         */
                        private static final long serialVersionUID = -6106361308637383251L;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ps.setProperty(LANGUAGE, ln);
                            saveProperties(engine, ps);
                        }
                    });
            lMenu.add(item);
            if ((dLanguage != null) && (dLanguage.equals(l)))
                item.setSelected(true);
            group.add(item);
        }

        JPopupMenu menu = new JPopupMenu();

        JCheckBoxMenuItem item = new JCheckBoxMenuItem(GlobalResourcesManager
                .getString("Editor.SpellCheck"));
        item.setSelected(isSpellCheck(ps));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Options.setBoolean(SPELL_CHECK, !isSpellCheck(ps), ps);
                saveProperties(engine, ps);
            }

        });

        menu.add(item);
        menu.add(lMenu);
        return menu;
    }

    private void saveProperties(final Engine engine, final Properties ps) {
        engine.setProperties(USER_GUI_SPELL_PROPERTIES, ps);
        if (checker != null) {
            checker.stopRealtimeMarkErrors();
            checker = null;
        }
        createChecker();
    }

    private boolean isSpellCheck(final Properties ps) {
        return Options.getBoolean(SPELL_CHECK, true, ps);
    }

    private void createChecker() {
        if (checker != null) {
            checker.stopRealtimeMarkErrors();
            checker = null;
        }
        Properties ps = engine.getProperties(USER_GUI_SPELL_PROPERTIES);
        if (isSpellCheck(ps)) {
            Language language = getCurrentLanguage(ps);
            if (language != null) {
                checker = language.createTextComponentSpellChecker();
                checker.startRealtimeMarkErrors(jTextPane);
            }
        }
    }

    private Language getCurrentLanguage(Properties ps) {
        String langName = Options.getString(LANGUAGE, "", ps);
        Language language;
        if ((langName == null) || (langName.equals("")))
            language = SpellFactory.getDefaultLanguage();
        else
            language = SpellFactory.findLanguage(langName);
        return language;
    }

    @Override
    public boolean isAcceptable() {
        return true;
    }

    @Override
    public JComponent getLastComponent() {
        return jTextPane;
    }

    @Override
    public void apply(Engine engine, Element element, Attribute attribute,
                      Object value) {
        engine.setAttribute(element, attribute, getValue());
    }

    @Override
    public boolean canApply() {
        return true;
    }

    @Override
    public void showErrorMessage() {
    }

    @Override
    public boolean isSaveAnyway() {
        return false;
    }
}
