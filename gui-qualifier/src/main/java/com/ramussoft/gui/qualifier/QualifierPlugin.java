package com.ramussoft.gui.qualifier;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.core.attribute.standard.AutochangePlugin;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.attribute.AttributeEditorView;
import com.ramussoft.gui.attribute.AttributePullView;
import com.ramussoft.gui.attribute.ElementAttributesEditor;
import com.ramussoft.gui.attribute.TextAttributePlugin;
import com.ramussoft.gui.branches.BranchView;
import com.ramussoft.gui.common.AbstractView;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.TabbedView;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.common.event.TabbedEvent;
import com.ramussoft.gui.common.event.ViewTitleEvent;
import com.ramussoft.gui.common.prefrence.AbstractPreferences;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.common.prefrence.Preferences;
import com.ramussoft.gui.qualifier.table.SelectableTableView;
import com.ramussoft.gui.qualifier.table.TabbedTableView;
import com.ramussoft.gui.qualifier.table.TableTabView;
import com.ramussoft.gui.spell.Language;
import com.ramussoft.gui.spell.SpellFactory;

public class QualifierPlugin extends AbstractViewPlugin implements Commands {

    public static final String OPEN_QUALIFIER = "OpenQualifier";

    private Engine engine;

    private AccessRules accessor;

    @Override
    public String getName() {
        return "Qualifiers";
    }

    @Override
    public TabbedView[] getTabbedViews() {
        return new TabbedView[]{new TabbedTableView(framework)};
    }

    public QualifierPlugin() {
    }

    @Override
    public UniqueView[] getUniqueViews() {
        if (Metadata.EDUCATIONAL)
            return new UniqueView[]{
                    new QualifierView(framework, engine, accessor),
                    new AttributePullView(framework),
                    new AttributeEditorView(framework, engine, accessor)};
        else
            return new UniqueView[]{
                    new QualifierView(framework, engine, accessor),
                    new AttributePullView(framework),
                    new AttributeEditorView(framework, engine, accessor),
                    new ElementAttributesEditor(framework),
                    new BranchView(framework)};
    }

    public static class Data {
        public AbstractView view;

        public QualifierListener listener;

        public Qualifier qualifier;
    }

    @Override
    public void setFramework(final GUIFramework framework) {
        super.setFramework(framework);
        this.engine = framework.getEngine();
        this.accessor = framework.getAccessRules();
        framework.addActionListener(OPEN_QUALIFIER, new ActionListener() {

            @Override
            public void onAction(final ActionEvent event) {

                if (framework.openView(event))
                    return;

                final Data data = new Data();

                data.qualifier = (Qualifier) event.getValue();

                if (data.qualifier != null) {
                    data.qualifier = engine.getQualifier(data.qualifier.getId());
                }

                if (data.qualifier == null)
                    return;

                data.listener = new QualifierAdapter() {
                    @Override
                    public void qualifierUpdated(QualifierEvent event) {
                        if (event.getNewQualifier().equals(data.qualifier)) {
                            ViewTitleEvent e = new ViewTitleEvent(data.view,
                                    event.getNewQualifier().getName());
                            data.view.titleChanged(e);
                        }
                    }

                    @Override
                    public void qualifierDeleted(QualifierEvent event) {
                        if (event.getOldQualifier().equals(data.qualifier)) {
                            data.view.close();
                        }
                    }
                };

                engine.addQualifierListener(data.listener);

                data.view = new TableTabView(framework, engine, accessor,
                        data.qualifier) {

                    @Override
                    public void close() {
                        super.close();
                        engine.removeQualifierListener(data.listener);
                        TabbedEvent tEvent = new TabbedEvent("TabbedTableView",
                                this);
                        tabRemoved(tEvent);
                    }

                    @Override
                    public String getTitle() {
                        return qualifier.getName();
                    }

                    @Override
                    public ActionEvent getOpenAction() {
                        return new ActionEvent(OPEN_QUALIFIER, data.qualifier);
                    }
                };
                TabbedEvent tEvent = new TabbedEvent(
                        TabbedTableView.MAIN_TABBED_VIEW, (TabView) data.view);
                tabCreated(tEvent);
            }
        });

        framework.setSystemAttributeName(
                StandardAttributesPlugin.getAttributeNameAttribute(engine),
                GlobalResourcesManager.getString("AttributeName"));
        framework.setSystemAttributeName(
                StandardAttributesPlugin.getAttributeTypeNameAttribute(engine),
                GlobalResourcesManager.getString("AttributeTypeName"));
    }

    @Override
    public Preferences[] getProjectPreferences() {
        return new Preferences[]{new AbstractPreferences() {

            class Button extends JRadioButton {

                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                private Language language;

                public Button(Language language) {
                    this.language = language;
                }

                public Language getLanguage() {
                    return language;
                }

            }

            ;

            private JPanel component;

            private Button[] buttons;

            private JCheckBox box;

            {
                Properties ps = engine
                        .getProperties(TextAttributePlugin.USER_GUI_SPELL_PROPERTIES);
                Language language = getCurrentLanguage(ps);
                Language[] languages = SpellFactory.getLanguages();
                box = new JCheckBox(
                        GlobalResourcesManager.getString("Editor.SpellCheck"));
                box.setSelected(Options.getBoolean(
                        TextAttributePlugin.SPELL_CHECK, true, ps));
                JPanel languagesPanel = new JPanel();
                languagesPanel.setLayout(new BoxLayout(languagesPanel,
                        BoxLayout.Y_AXIS));
                buttons = new Button[languages.length];
                ButtonGroup group = new ButtonGroup();
                for (int i = 0; i < languages.length; i++) {
                    Button button = new Button(languages[i]);
                    button.setText(languages[i].getLocalizedName());
                    languagesPanel.add(button);
                    buttons[i] = button;
                    if (languages[i].equals(language))
                        button.setSelected(true);
                    group.add(button);
                }
                JScrollPane pane = new JScrollPane();
                pane.setViewportView(languagesPanel);

                component = new JPanel(new BorderLayout());
                component.add(pane, BorderLayout.CENTER);
                component.add(box, BorderLayout.NORTH);
            }

            @Override
            public JComponent createComponent() {
                return component;
            }

            @Override
            public String getTitle() {
                return GlobalResourcesManager
                        .getString("Preferences.SpellChecking");
            }

            @Override
            public boolean save(JDialog dialog) {
                Properties ps = engine
                        .getProperties(TextAttributePlugin.USER_GUI_SPELL_PROPERTIES);
                Options.setBoolean(TextAttributePlugin.SPELL_CHECK,
                        box.isSelected(), ps);
                String l = "";

                for (Button button : buttons)
                    if (button.isSelected()) {
                        l = button.getLanguage().getName();
                        break;
                    }

                Options.setString(TextAttributePlugin.LANGUAGE, l, ps);
                engine.setProperties(
                        TextAttributePlugin.USER_GUI_SPELL_PROPERTIES, ps);
                return true;
            }

            private Language getCurrentLanguage(Properties ps) {
                String langName = Options.getString(
                        TextAttributePlugin.LANGUAGE, "", ps);
                Language language;
                if ((langName == null) || (langName.equals("")))
                    language = SpellFactory.getDefaultLanguage();
                else
                    language = SpellFactory.findLanguage(langName);
                return language;
            }
        },

                new AbstractPreferences() {

                    SelectableTableView tableView;

                    JComboBox attributeForName = new JComboBox();

                    private JPanel panel;

                    {
                        tableView = new SelectableTableView(framework, engine,
                                accessor,
                                StandardAttributesPlugin.getAttributesQualifier(engine)) {
                            @Override
                            protected Attribute[] getAttributes() {
                                return new Attribute[]{
                                        StandardAttributesPlugin
                                                .getAttributeNameAttribute(engine),
                                        StandardAttributesPlugin
                                                .getAttributeTypeNameAttribute(engine)};
                            }
                        };

                        double[][] size = {
                                {5, TableLayout.MINIMUM, 5, TableLayout.FILL, 5},
                                {5, TableLayout.MINIMUM, 5}};

                        JPanel attrForName = new JPanel(new TableLayout(size));

                        attrForName.add(
                                new JLabel(GlobalResourcesManager
                                        .getString("Qualifier.AttributeForName")),
                                "1, 1");
                        attrForName.add(attributeForName, "3, 1");

                        panel = new JPanel(new BorderLayout());
                        panel.add(tableView.createComponent(), BorderLayout.CENTER);
                        panel.add(attrForName, BorderLayout.SOUTH);

                        Properties ps = engine
                                .getProperties(AutochangePlugin.AUTO_ADD_ATTRIBUTES);
                        String ids = ps
                                .getProperty(AutochangePlugin.AUTO_ADD_ATTRIBUTE_IDS);
                        if (ids == null)
                            ids = "";

                        String aForName = ps
                                .getProperty(AutochangePlugin.ATTRIBUTE_FOR_NAME);

                        StringTokenizer st = new StringTokenizer(ids, " ,");
                        List<Row> rows = tableView.getComponent().getRowSet()
                                .getAllRows();
                        attributeForName.addItem(null);
                        while (st.hasMoreElements()) {
                            String s = st.nextToken();
                            Long long1 = new Long(s);
                            Row row = findRow(long1, rows);
                            if (row != null) {
                                tableView.setSelectedRow(row, true);
                                attributeForName.addItem(row);
                                if (s.equals(aForName))
                                    attributeForName.setSelectedItem(row);
                            }
                        }

                        attributeForName.addPopupMenuListener(new PopupMenuListener() {

                            @Override
                            public void popupMenuCanceled(PopupMenuEvent e) {
                            }

                            @Override
                            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                            }

                            @Override
                            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                                attributeForName.removeAllItems();
                                List<Row> list = tableView.getSelectedRows();
                                for (Row p : list) {
                                    Attribute a = StandardAttributesPlugin
                                            .getAttribute(engine, p.getElement());
                                    if (StandardAttributesPlugin.isNameType(a
                                            .getAttributeType()))
                                        attributeForName.addItem(p);
                                }

                                JComboBox box = (JComboBox) e.getSource();
                                Object comp = box.getUI().getAccessibleChild(box, 0);
                                if (!(comp instanceof JPopupMenu))
                                    return;
                                JComponent scrollPane = (JComponent) ((JPopupMenu) comp)
                                        .getComponent(0);
                                Dimension size = new Dimension();
                                size.width = box.getPreferredSize().width;
                                size.height = scrollPane.getPreferredSize().height;
                                scrollPane.setPreferredSize(size);
                            }

                        });
                    }

                    @Override
                    public JComponent createComponent() {
                        return panel;
                    }

                    private Row findRow(Long long1, List<Row> rows) {
                        Attribute attr = engine.getAttribute(long1.longValue());
                        if (attr != null) {
                            for (Row row : rows) {
                                Long long2 = StandardAttributesPlugin.getAttributeId(
                                        engine, row.getElement());

                                if (long2 != null) {
                                    if (attr.getId() == long2.longValue())
                                        return row;

                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    public String getTitle() {
                        return GlobalResourcesManager
                                .getString("Preferences.AutoaddAttributes");
                    }

                    @Override
                    public boolean save(JDialog dialog) {
                        List<Row> list = tableView.getSelectedRows();
                        StringBuffer sb = new StringBuffer();
                        for (Row row : list) {
                            Long id = StandardAttributesPlugin.getAttributeId(engine,
                                    row.getElement());
                            if (id != null)
                                sb.append(id + " ");
                        }
                        Properties properties = engine
                                .getProperties(AutochangePlugin.AUTO_ADD_ATTRIBUTES);
                        properties.setProperty(AutochangePlugin.AUTO_ADD_ATTRIBUTE_IDS,
                                sb.toString());
                        Row row = (Row) attributeForName.getSelectedItem();
                        if (row != null)
                            properties.setProperty(AutochangePlugin.ATTRIBUTE_FOR_NAME,
                                    Long.toString(StandardAttributesPlugin
                                            .getAttributeId(engine, row.getElement())));

                        engine.setProperties(AutochangePlugin.AUTO_ADD_ATTRIBUTES,
                                properties);
                        return true;
                    }

                    @Override
                    public void close() {
                        tableView.close();
                    }

                }};
    }

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor qualifiersAttributes = new ActionDescriptor();

        Action action = new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = -5496907849883803302L;

            {
                putValue(ACTION_COMMAND_KEY,
                        "Action.QualifiersQualifierAttributes");
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                QualifiersQualifierAttributesDialog dialog = new QualifiersQualifierAttributesDialog(
                        framework);
                dialog.pack();
                dialog.setMinimumSize(new Dimension(400, 300));
                dialog.setLocationRelativeTo(null);
                Options.loadOptions(dialog);
                dialog.setVisible(true);
                Options.saveOptions(dialog);
                dialog.close();
            }
        };

        qualifiersAttributes.setAction(action);

        qualifiersAttributes.setMenu("Tools");

        return new ActionDescriptor[]{qualifiersAttributes};
    }
}
