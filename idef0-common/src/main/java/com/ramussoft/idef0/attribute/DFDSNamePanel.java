package com.ramussoft.idef0.attribute;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.undo.UndoManager;

import org.dts.spell.swing.JTextComponentSpellChecker;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.spell.Language;
import com.ramussoft.gui.spell.SpellFactory;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.idef0.attribute.ArrowLinksPanel.ItemHolder;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.data.negine.NFunction;

import static com.ramussoft.gui.attribute.TextAttributePlugin.*;

public class DFDSNamePanel extends JPanel {

    private JTextArea textArea;

    protected JTextComponentSpellChecker checker;

    private Engine engine;

    private Element element;

    private DataPlugin dataPlugin;

    private ArrowLinksPanel panel;

    private UndoManager undoManager;

    /**
     * Create the panel.
     */
    public DFDSNamePanel(Engine engine, Element element) {
        super(new BorderLayout());
        this.engine = engine;
        this.element = element;
        dataPlugin = NDataPluginFactory.getExistingDataPlugin(engine);
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setComponentPopupMenu(createSelectLanguageMenu());

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (undoManager == null)
                    return;
                if (e.isControlDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_Z)
                        if (undoManager.canUndo())
                            undoManager.undo();
                    if (e.getKeyCode() == KeyEvent.VK_Y)
                        if (undoManager.canRedo())
                            undoManager.redo();
                }
            }
        });

        if (dataPlugin != null) {
            Row row = dataPlugin.findRowByGlobalId(element.getId());
            if (row instanceof Function) {
                Function function = (Function) row;
                panel = new ArrowLinksPanel(function);
                JSplitPane splitPane = new JSplitPane();
                add(splitPane, BorderLayout.CENTER);
                splitPane.setLeftComponent(new JScrollPane(textArea));
                splitPane.setRightComponent(panel);
                createChecker();
                return;
            }
        }

        add(new JScrollPane(textArea), BorderLayout.CENTER);

        createChecker();
    }

    public void setDFDSName(DFDSName name) {
        if (name == null) {
            textArea.setText("");
            return;
        }

        {
            StringBuilder sb = new StringBuilder();
            if (name.getShortNameSource() != null)
                sb.append(name.getShortNameSource());
            sb.append('\n');
            if (name.getLongNameSource() != null)
                sb.append(name.getLongNameSource());
            if (sb.length() > 1) {
                textArea.setText(localize(sb.toString()));
            } else {
                sb = new StringBuilder();
                if (name.getShortName() != null)
                    sb.append(name.getShortName());
                sb.append('\n');
                if (name.getLongName() != null)
                    sb.append(name.getLongName());

                textArea.setText(sb.toString());
            }
        }
        if (undoManager != null) {
            textArea.getDocument().removeUndoableEditListener(undoManager);
            undoManager = null;
        }
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);
    }

    public DFDSName getDFDSName() {
        DFDSName name = new DFDSName();
        String text = delocalize(textArea.getText());
        int index = text.indexOf('\n');
        if (index < 0) {
            name.setShortNameSource(text);
            name.setLongNameSource("");
        } else {
            name.setShortNameSource(text.substring(0, index));
            name.setLongNameSource(text.substring(index + 1));
        }
        if (dataPlugin == null) {
            name.setShortName(name.getShortNameSource());
            name.setLongName(name.getLongNameSource());
        } else
            compile(name);
        return name;
    }

    private String toLowLevel(String source) {
        StringBuilder sb = new StringBuilder();
        if (panel != null) {
            for (int i = 0; i < source.length(); i++) {
                char ch = source.charAt(i);
                if (ch == '\\') {
                    String code = "\\";
                    for (i++; i < source.length(); i++) {
                        ch = source.charAt(i);
                        if (Character.isDigit(ch) || ch == '.')
                            code += ch;
                        else
                            break;
                    }
                    if (code.endsWith(".")) {
                        code = code.substring(0, code.length() - 1);
                        i--;
                    }
                    ItemHolder ih = panel.getItemHolder(code);
                    if (ih != null) {
                        sb.append(ih.getLowLevelCode());
                    } else
                        sb.append(code);
                    i--;
                } else
                    sb.append(ch);
            }
        }
        return sb.toString();
    }

    private String toHiLevel(String source) {
        StringBuilder sb = new StringBuilder();
        if (panel != null) {
            for (int i = 0; i < source.length(); i++) {
                char ch = source.charAt(i);
                if (ch == '\\') {
                    String code = "\\";
                    for (i++; i < source.length(); i++) {
                        ch = source.charAt(i);
                        if (Character.isDigit(ch) || ch == '.')
                            code += ch;
                        else
                            break;
                    }
                    if (code.endsWith(".")) {
                        code = code.substring(0, code.length() - 1);
                        i--;
                    }
                    ItemHolder ih = panel.getItemHolderByLowLevelCode(code);
                    if (ih != null) {
                        sb.append(ih.getCode());
                    } else
                        sb.append(code);
                    i--;
                } else
                    sb.append(ch);
            }
        }
        return sb.toString();
    }

    private void compile(DFDSName name) {
        Row row = dataPlugin.findRowByGlobalId(element.getId());
        if (row instanceof Function) {
            Function function = (NFunction) row;
            dataPlugin.compileDFDSName(name, function);
        }
    }

    public void setTextArea(JTextArea textArea2) {
        removeAll();
        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(textArea2);
        add(BorderLayout.CENTER, scrollPane);
    }

    public JComponent getTextArea() {
        return textArea;
    }

    private JPopupMenu createSelectLanguageMenu() {
        JMenu lMenu = new JMenu(
                GlobalResourcesManager.getString("Editor.Language"));
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

        JCheckBoxMenuItem item = new JCheckBoxMenuItem(
                GlobalResourcesManager.getString("Editor.SpellCheck"));
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
        Properties ps = engine.getProperties(USER_GUI_SPELL_PROPERTIES);
        if (isSpellCheck(ps)) {
            Language language = getCurrentLanguage(ps);
            if (language != null) {
                checker = language.createTextComponentSpellChecker();
                checker.startRealtimeMarkErrors(textArea);
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

    private String localize(String from) {
        String role = ResourceLoader.getString("Key.Role");
        String term = ResourceLoader.getString("Key.Term");
        return toHiLevel(from.replace(DFDSNamePlugin.ROLE, role).replace(
                DFDSNamePlugin.TERM, term));
    }

    private String delocalize(String from) {
        String role = ResourceLoader.getString("Key.Role");
        String term = ResourceLoader.getString("Key.Term");
        return toLowLevel(from.replace(role, DFDSNamePlugin.ROLE).replace(term,
                DFDSNamePlugin.TERM));
    }
}
