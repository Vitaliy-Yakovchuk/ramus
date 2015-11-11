package com.ramussoft.pb.master;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.idef.elements.ProjectOptions;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.HierarchicalPersistent;
import com.ramussoft.core.attribute.standard.AutochangePlugin;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.IDEF0ViewPlugin;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.idef0.OpenDiagram;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.master.gui.DefaultPanelCreator;
import com.ramussoft.pb.master.gui.IPanel;
import com.ramussoft.pb.master.gui.MainPanel;
import com.ramussoft.pb.master.model.DefaultMasterModel;
import com.ramussoft.pb.master.model.DefaultProperties;
import com.ramussoft.pb.master.model.DefaultProperty;
import com.ramussoft.pb.master.model.MasterModel;
import com.ramussoft.pb.master.model.Properties;
import com.ramussoft.pb.master.model.Property;

public class NewProjectDialog extends JDialog {

    private static final String DEFINITION = "definition:";
    private static final String CLASIFICATOR1 = "clasificator1:";
    private static final String CLASIFICATOR2 = "clasificator2:";
    private static final String CLASIFICATOR3 = "clasificator3:";
    private static final String CLASIFICATOR4 = "clasificator4:";
    private static final String CLASIFICATOR5 = "clasificator5:";

    private static final String USED_AT = "used_at:";
    private static final String AUTOR = "autor:";
    private static final String PROJECT_NAME = "project_name:";
    private static final String MODEL_NAME = "model_name:";
    private MainPanel mainPanel = null;
    private DefaultMasterModel masterModel = null;
    private final JFrame frame;
    private JRadioButton idef0 = new JRadioButton(
            ResourceLoader.getString("IDEF0"));
    private JRadioButton dfd = new JRadioButton(ResourceLoader.getString("DFD"));

    private JRadioButton dfds = new JRadioButton(
            ResourceLoader.getString("DFDS"));

    private final Vector<String> ouners = new Vector<String>(0);

    private class Ouners extends JPanel implements IPanel {

        private String[] clasificators = new String[]{};

        private final AbstractTableModel model = new AbstractTableModel() {

            public int getColumnCount() {
                return 2;
            }

            public int getRowCount() {
                return clasificators.length;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                if (columnIndex == 0)
                    return clasificators[rowIndex];
                return ouners.indexOf(clasificators[rowIndex]) >= 0;
            }

            @Override
            public void setValueAt(Object value, int rowIndex, int columnIndex) {
                assert value instanceof Boolean;
                String cl = clasificators[rowIndex];
                if ((Boolean) value) {
                    if (ouners.indexOf(cl) < 0)
                        ouners.add(cl);
                } else {
                    ouners.remove(cl);
                }
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex != 0;
            }

            @Override
            public String getColumnName(int column) {
                if (column == 0) {
                    return ResourceLoader.getString("clasificator");
                }
                return ResourceLoader.getString("owner");
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0)
                    return super.getColumnClass(columnIndex);
                return Boolean.class;
            }
        };

        public Ouners() {
            setLayout(new BorderLayout());
            final JScrollPane pane = new JScrollPane();
            final JTable table = new JTable(model);
            pane.setViewportView(table);
            this.add(pane, BorderLayout.CENTER);
        }

        public void get() {
            clasificators = getClasifiactors();
            model.fireTableDataChanged();
        }

        public Component getComponent() {
            return this;
        }

        public String getDescribe() {
            return Factory.getString("ouners.Describe");
        }

        public void set() {
        }

    }

    ;

    private class PanelCreator extends DefaultPanelCreator {

        private final Ouners ouners = new Ouners();

        private boolean modelTypeAdded = false;

        public PanelCreator(final MasterModel masterModel) {
            super(masterModel);
        }

        @Override
        public int getPanelCount() {
            return super.getPanelCount() + 1;
        }

        @Override
        public IPanel getPanel(final int i) {
            if ((i == 0) && (!modelTypeAdded)) {
                ButtonGroup bg = new ButtonGroup();
                idef0.setSelected(true);
                bg.add(idef0);
                bg.add(dfd);
                bg.add(dfds);
                JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                jPanel.add(idef0);
                jPanel.add(dfd);
                jPanel.add(dfds);
                JPanel panel = (JPanel) super.getPanel(i);
                panel.add(jPanel, BorderLayout.SOUTH);
                modelTypeAdded = true;
            }
            if (super.getPanelCount() == i) {
                return ouners;
            }
            return super.getPanel(i);
        }

    }

    ;

    private String[] getClasifiactors() {
        final ArrayList<String> list = new ArrayList<String>(0);
        add(list, getValue(CLASIFICATOR1));
        add(list, getValue(CLASIFICATOR2));
        add(list, getValue(CLASIFICATOR3));
        add(list, getValue(CLASIFICATOR4));
        add(list, getValue(CLASIFICATOR5));
        return list.toArray(new String[list.size()]);
    }

    private void add(final ArrayList<String> list, final String value) {
        final String c = value.trim();
        if (!"".equals(c)) {
            if (list.indexOf(c) < 0)
                list.add(c);
        }

    }

    public NewProjectDialog(final JFrame frame) {
        super(frame, true);
        this.frame = frame;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(MessageFormat.format(Factory.getString("Master"),
                new Object[]{ResourceLoader.getString("project options")}));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainPanel.cancel();
            }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public void showModal(final Engine engine, final AccessRules accessRules,
                          final GUIFramework framework) {
        ((Journaled) engine).setNoUndoPoint();
        final Properties name_author = new DefaultProperties(new Property[]{
                new DefaultProperty(AUTOR, Property.TEXT_FIELD),
                new DefaultProperty(PROJECT_NAME, Property.TEXT_FIELD),
                new DefaultProperty(MODEL_NAME, Property.TEXT_FIELD)},
                Factory.getString("name_author.Describe"));

        final Properties used_at = new DefaultProperties(
                new Property[]{new DefaultProperty(USED_AT,
                        Property.TEXT_FIELD)},
                Factory.getString("used_at.Describe"));

        final Properties def = new DefaultProperties(
                new Property[]{new DefaultProperty(DEFINITION, Property.TEXT)},
                Factory.getString("describe.Describe"));

        final Properties clasificators = new DefaultProperties(new Property[]{
                new DefaultProperty(CLASIFICATOR1, Property.TEXT_FIELD),
                new DefaultProperty(CLASIFICATOR2, Property.TEXT_FIELD),
                new DefaultProperty(CLASIFICATOR3, Property.TEXT_FIELD),
                new DefaultProperty(CLASIFICATOR4, Property.TEXT_FIELD),
                new DefaultProperty(CLASIFICATOR5, Property.TEXT_FIELD)},
                Factory.getString("Clasificators.Describe"));

        masterModel = new DefaultMasterModel(new Properties[]{name_author,
                used_at, def, clasificators});
        mainPanel = new MainPanel(new PanelCreator(masterModel)) {
            @Override
            public boolean cancel() {
                if (super.cancel()) {
                    NewProjectDialog.this.setVisible(false);
                    finish();
                    return true;
                }
                return false;
            }

            @Override
            protected void finish() {
                super.finish();
                ((Journaled) engine).startUserTransaction();
                final Qualifier baseFunction = engine.createQualifier();
                String modelName = getValue(MODEL_NAME);
                if ((modelName == null) || (modelName.length() == 0))
                    baseFunction.setName(ResourceLoader
                            .getString("base_function"));
                else
                    baseFunction.setName(modelName);
                Attribute name = engine.createAttribute(new AttributeType(
                        "Core", "Text", true));
                name.setName(ResourceLoader.getString("name"));

                engine.updateAttribute(name);

                java.util.Properties ps = engine
                        .getProperties(AutochangePlugin.AUTO_ADD_ATTRIBUTES);
                ps.setProperty(AutochangePlugin.AUTO_ADD_ATTRIBUTE_IDS,
                        Long.toString(name.getId()));
                ps.setProperty(AutochangePlugin.ATTRIBUTE_FOR_NAME,
                        Long.toString(name.getId()));
                engine.setProperties(AutochangePlugin.AUTO_ADD_ATTRIBUTES, ps);

                baseFunction.getAttributes().add(name);
                baseFunction.setAttributeForName(name.getId());

                IDEF0Plugin.installFunctionAttributes(baseFunction, engine);

                Element element = engine.createElement(IDEF0Plugin
                        .getModelTree(engine).getId());
                engine.setAttribute(element, StandardAttributesPlugin
                        .getAttributeQualifierId(engine), baseFunction.getId());
                HierarchicalPersistent hp = new HierarchicalPersistent();
                hp.setParentElementId(-1l);
                hp.setPreviousElementId(-1l);
                engine.setAttribute(element, StandardAttributesPlugin
                        .getAttributeNameAttribute(engine), baseFunction
                        .getName());
                engine.setAttribute(element, StandardAttributesPlugin
                        .getHierarchicalAttribute(engine), hp);

                DataPlugin dataPlugin = NDataPluginFactory.getDataPlugin(
                        baseFunction, engine, accessRules);
                if (dfd.isSelected())
                    dataPlugin.getBaseFunction().setDecompositionType(
                            MovingArea.DIAGRAM_TYPE_DFD);
                else if (dfds.isSelected())
                    dataPlugin.getBaseFunction().setDecompositionType(
                            MovingArea.DIAGRAM_TYPE_DFDS);
                else if (dfds.isSelected())
                    dataPlugin.getBaseFunction().setDecompositionType(
                            MovingArea.DIAGRAM_TYPE_DFDS);
                ProjectOptions projectOptions = new ProjectOptions();

                projectOptions.setProjectAutor(getValue(AUTOR));
                projectOptions.setProjectName(getValue(PROJECT_NAME));
                projectOptions.setDefinition(getValue(DEFINITION));
                projectOptions.setUsedAt(getValue(USED_AT));
                NewProjectDialog.this.setVisible(false);
                final String[] cls = getClasifiactors();
                final StringBuffer ounersIds = new StringBuffer();
                for (final String c : cls) {
                    Row row = dataPlugin.createRow(null, false);
                    Qualifier qualifier = engine
                            .getQualifier(StandardAttributesPlugin
                                    .getQualifierId(engine, row.getElement()
                                            .getId()));
                    qualifier.setName(c);
                    engine.updateQualifier(qualifier);
                    if (ouners.indexOf(c) >= 0) {
                        ounersIds.append(qualifier.getId());
                        ounersIds.append(' ');
                    }
                }
                dataPlugin.setProperty(DataPlugin.PROPERTY_OUNERS,
                        ounersIds.toString());
                dataPlugin.getBaseFunction().setProjectOptions(projectOptions);
                ((Journaled) engine).commitUserTransaction();
                ((Journaled) engine).setNoUndoPoint();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        framework.propertyChanged("ShowWorkspace",
                                "Workspace.IDEF0");
                        OpenDiagram open = new OpenDiagram(baseFunction, -1l);
                        framework.propertyChanged(IDEF0ViewPlugin.OPEN_DIAGRAM,
                                open);
                    }
                });
            }
        };
        setContentPane(mainPanel);
        pack();
        Dimension size = this.getSize();
        setMinimumSize(new Dimension(size.width, size.height + 30));
        setLocationRelativeTo(null);
        setVisible(true);
        frame.repaint();
    }

    private String getValue(final String key) {
        final Object value = masterModel.getProperty(key).getValue();
        return value == null ? "" : value.toString();
    }
}
