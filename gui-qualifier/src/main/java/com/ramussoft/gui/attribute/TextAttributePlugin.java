package com.ramussoft.gui.attribute;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.dts.spell.swing.JTextComponentSpellChecker;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.spell.Language;
import com.ramussoft.gui.spell.SpellFactory;

public class TextAttributePlugin extends AbstractAttributePlugin {

    public static final String SPELL_CHECK = "SPELL_CHECK";

    public static final String LANGUAGE = "LANGUAGE";

    public static final String USER_GUI_SPELL_PROPERTIES = "/user/gui/spell.xml";

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "Text", true);
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        JTextField textField = new JTextField();
        textField.setName("Table.editor");
        return new DefaultCellEditor(textField);
    }

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public AttributeEditor getAttributeEditor(final Engine engine,
                                              final AccessRules rules, final Element element,
                                              final Attribute attribute, AttributeEditor old) {
        if (old != null)
            old.close();
        return new AbstractAttributeEditor() {

            private JTextArea area = new JTextArea();

            protected JTextComponentSpellChecker checker;

            private Object value;

            private JPopupMenu createSelectLanguageMenu() {
                JMenu lMenu = new JMenu(GlobalResourcesManager
                        .getString("Editor.Language"));
                Language[] languages = SpellFactory.getLanguages();
                final Properties ps = engine
                        .getProperties(USER_GUI_SPELL_PROPERTIES);
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
                        checker.startRealtimeMarkErrors(area);
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
            public JComponent getComponent() {
                JScrollPane pane = new JScrollPane();
                pane.setViewportView(area);

                boolean canEdit = rules.canUpdateElement(element.getId(),
                        attribute.getId());
                area.setEditable(canEdit);
                area.setWrapStyleWord(true);
                area.setLineWrap(true);
                area.setComponentPopupMenu(createSelectLanguageMenu());
                createChecker();
                return pane;
            }

            @Override
            public Object getValue() {
                String res = area.getText();
                return ((res.equals("")) && (value == null)) ? null : res;
            }

            @Override
            public Object setValue(Object value) {
                this.value = value;
                area.setText((String) value);
                return value;
            }

            @Override
            public void close() {
                if (checker != null) {
                    checker.stopRealtimeMarkErrors();
                    checker = null;
                }
            }

            @Override
            public JComponent getLastComponent() {
                return area;
            }

        };
    }
}
