package com.ramussoft.chart.gui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import com.ramussoft.chart.ChartSource;
import com.ramussoft.chart.ElementSource;
import com.ramussoft.chart.FilterSource;
import com.ramussoft.chart.QualifierSource;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.QualifierPlugin;
import com.ramussoft.gui.qualifier.QualifierView;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.gui.qualifier.table.event.SelectionEvent;
import com.ramussoft.gui.qualifier.table.event.SelectionListener;

public class ChartSourceSelectPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1131957191718749092L;

    private static String SOURCE_TYPE = "source-type";

    private static String SOURCE_TYPE_ELEMENTS = "elements";

    private static String SOURCE_TYPE_TABLE = "table";

    private ChartSource chartSource;

    private QualifierView qualifierView;

    private QualifierSourceSelectPanel qualifierSelectPanel;

    private JSplitPane pane;

    private GUIFramework framework;

    private boolean res;

    private String sourceTypeElements = ChartResourceManager
            .getString("SourceType.elements");

    private String sourceTypeTable = ChartResourceManager
            .getString("SourceType.table");

    private JComboBox sourceType = new JComboBox();

    private JComboBox tableAttribute = new JComboBox();

    public ChartSourceSelectPanel(GUIFramework framework,
                                  ChartSource chartSource) {
        super(new BorderLayout());
        this.framework = framework;
        this.chartSource = chartSource;
        qualifierView = new QualifierView(framework) {
            @Override
            protected void addListeners() {
                table.addSelectionListener(new SelectionListener() {
                    @Override
                    public void changeSelection(SelectionEvent event) {
                        deleteRightPanel();
                        createRightPanel();
                    }
                });
                table.setEditIfNullEvent(false);
                table.getInputMap().put(
                        KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "EditCell");
                table.getActionMap().put("EditCell", new AbstractAction() {
                    /**
                     *
                     */
                    private static final long serialVersionUID = 3229634866196074563L;

                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        if ((table.getSelectedRow() >= 0)
                                && (table.getSelectedColumn() >= 0))
                            editTableField();
                    }
                });
            }
        };

        sourceType.addItem(sourceTypeElements);
        sourceType.addItem(sourceTypeTable);

        addTableAttributes();

        String type = chartSource.getProperty(SOURCE_TYPE);
        if (SOURCE_TYPE_TABLE.equals(type)) {
            sourceType.setSelectedItem(sourceTypeTable);
            try {
                changeSourceForTable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sourceType.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                updateEnables();
            }
        });

        updateEnables();

        pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setLeftComponent(qualifierView.createComponent());
        pane.setRightComponent(new JPanel());
        this.add(pane, BorderLayout.CENTER);
        this.add(createSourceTypePanel(), BorderLayout.SOUTH);
    }

    private void changeSourceForTable() {
        Engine engine = framework.getEngine();

        QualifierSource source = chartSource.getQualifierSources().get(0);
        FilterSource source2 = source.getFilterSources().get(0);
        Element element = engine.getElement(Long.parseLong(source2.getValue()));
        source.setQualifier(engine.getQualifier(element.getQualifierId()));
        source.getElementSources().clear();
        ElementSource source3 = chartSource.createElementSource();
        source3.setElement(element);
        source.getElementSources().add(source3);
        source.getFilterSources().clear();
    }

    private Component createSourceTypePanel() {
        double[][] size = {
                {5, TableLayout.MINIMUM, TableLayout.FILL,
                        TableLayout.MINIMUM, TableLayout.FILL, 5},
                {5, TableLayout.FILL, 5}};

        TableLayout layout = new TableLayout(size);

        layout.setHGap(5);
        layout.setVGap(5);

        JPanel panel = new JPanel(layout);

        panel.add(new JLabel(ChartResourceManager.getString("SourceType")),
                "1,1");
        panel.add(sourceType, "2,1");
        panel.add(new JLabel(ChartResourceManager
                .getString("SourceType.tableAttribute")), "3,1");
        panel.add(tableAttribute, "4,1");
        return panel;
    }

    private void addTableAttributes() {

        Attribute select = null;

        List<QualifierSource> sources = chartSource.getQualifierSources();
        if (sources.size() > 0) {
            QualifierSource source = sources.get(0);
            if (source.getQualifier().isSystem())
                select = StandardAttributesPlugin.getAttributeForTable(
                        framework.getEngine(), source.getQualifier());
        }

        for (Attribute attribute : framework.getEngine().getAttributes()) {
            if (attribute.getAttributeType().toString().equals("Core.Table")) {
                tableAttribute.addItem(attribute);
                if (attribute.equals(select))
                    tableAttribute.setSelectedItem(attribute);
            }
        }
    }

    private void updateEnables() {
        boolean table = sourceType.getSelectedItem().equals(sourceTypeTable);
        tableAttribute.setEnabled(table);
        if (qualifierSelectPanel != null) {
            if (table)
                qualifierSelectPanel.getView().setSelectType(SelectType.RADIO);
            else
                qualifierSelectPanel.getView().setSelectType(SelectType.CHECK);
            chartSource.getQualifierSources().clear();
        }
    }

    public void close() {
        if (qualifierView != null) {
            qualifierView.close();
            qualifierView = null;
        }

        if (qualifierSelectPanel != null) {
            qualifierSelectPanel.close();
            qualifierSelectPanel = null;
        }
        framework.setOpenDynamikViewEvent(null);
    }

    private void deleteRightPanel() {
        if (qualifierSelectPanel != null) {
            saveSelectedElements();
            qualifierSelectPanel.close();
            qualifierSelectPanel = null;
            framework.setOpenDynamikViewEvent(null);
        }
    }

    private void createRightPanel() {
        if (qualifierView == null)
            return;
        Qualifier qualifier = qualifierView.getSelectedQualifier();
        boolean b = qualifier != null;
        if (b) {
            framework.setOpenDynamikViewEvent(new ActionEvent(
                    QualifierPlugin.OPEN_QUALIFIER, qualifier));
            QualifierSource qualifierSource = null;
            for (QualifierSource s : chartSource.getQualifierSources()) {
                if (s.getQualifier().equals(qualifier)) {
                    qualifierSource = s;
                    break;
                }
            }

            if (qualifierSource == null) {
                qualifierSource = chartSource.createQualifierSource();
                qualifierSource.setQualifier(qualifier);
                chartSource.getQualifierSources().add(qualifierSource);
            }

            qualifierSelectPanel = new QualifierSourceSelectPanel(framework,
                    qualifier, qualifierSource, (sourceType.getSelectedItem()
                    .equals(sourceTypeElements)) ? SelectType.CHECK
                    : SelectType.RADIO);

            int dl = pane.getDividerLocation();
            pane.setRightComponent(qualifierSelectPanel);
            pane.revalidate();
            pane.repaint();
            pane.setDividerLocation(dl);
        }
    }

    public boolean showDialog() {
        res = false;
        BaseDialog dialog = new BaseDialog(framework.getMainFrame(), true) {
            /**
             *
             */
            private static final long serialVersionUID = 8037833645038528124L;

            @Override
            protected void onOk() {
                if (qualifierSelectPanel != null)
                    saveSelectedElements();
                close();
                res = true;
                super.onOk();
            }
        };
        dialog.setMainPane(this);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        Options.loadOptions(dialog);
        dialog.setVisible(true);
        return res;
    }

    public ChartSource getChartSource() {
        return chartSource;
    }

    public void save() {
        if (qualifierSelectPanel != null)
            saveSelectedElements();

        chartSource.setProperty(SOURCE_TYPE, (sourceType.getSelectedItem()
                .equals(sourceTypeElements)) ? SOURCE_TYPE_ELEMENTS
                : SOURCE_TYPE_TABLE);
        if (sourceType.getSelectedItem().equals(sourceTypeTable)) {
            if (chartSource.getQualifierSources().size() > 0) {
                QualifierSource source = chartSource.getQualifierSources().get(
                        0);
                if (source.getElementSources().size() > 0) {
                    ElementSource source2 = source.getElementSources().get(0);
                    source.getFilterSources().clear();
                    FilterSource filterSource = chartSource
                            .createFilterSource();
                    Attribute selectedItem = (Attribute) tableAttribute
                            .getSelectedItem();
                    if (selectedItem != null) {
                        filterSource.setAttribute(StandardAttributesPlugin
                                .getTableElementIdAttribute(framework
                                        .getEngine()));
                        filterSource.setValue(Long.toString(source2
                                .getElement().getId()));
                        source.getFilterSources().add(filterSource);
                        source.setQualifier(StandardAttributesPlugin
                                .getTableQualifierForAttribute(framework
                                        .getEngine(), selectedItem));
                        source
                                .setElementsLoadType(QualifierSource.ELEMENTS_LOAD_TYPE_ALL);
                    }
                }
            }
        } else {
            for (QualifierSource source : chartSource.getQualifierSources())
                source
                        .setElementsLoadType(QualifierSource.ELEMENTS_LOAD_TYPE_SELECTED);
        }
    }

    private void saveSelectedElements() {
        qualifierSelectPanel.save();
        QualifierSource source = qualifierSelectPanel.getQualifierSource();
        if (sourceType.getSelectedItem().equals(sourceTypeTable)) {
            if (source.getElementSources().size() > 0) {
                chartSource.getQualifierSources().clear();
                chartSource.getQualifierSources().add(source);
            }
        }
    }
}
