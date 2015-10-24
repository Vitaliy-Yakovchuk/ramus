package com.ramussoft.gui.qualifier;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.select.QualifierModel;
import com.ramussoft.gui.qualifier.select.QualifierTable;
import com.ramussoft.gui.qualifier.select.TableRowHeader;
import com.ramussoft.gui.qualifier.table.SelectType;

public class QualifierSelectComponent extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 356201446631585685L;

    private Qualifier[] qualifiers;

    private QualifierModel model;

    private Engine engine;

    public QualifierSelectComponent(Engine engine, boolean uniqueSelected,
                                    boolean all) {
        super(new BorderLayout());
        this.engine = engine;

        RowSet rs = new RowSet(engine, StandardAttributesPlugin
                .getQualifiersQualifier(engine),
                new Attribute[]{StandardAttributesPlugin
                        .getAttributeNameAttribute(engine)});

        List<Qualifier> qualifiers;
        if (all)
            qualifiers = engine.getQualifiers();
        else {
            qualifiers = new ArrayList<Qualifier>();
            List<Row> rows = rs.getAllRows();
            for (Row row : rows) {
                if (row.getChildCount() == 0) {
                    qualifiers.add(StandardAttributesPlugin.getQualifier(
                            engine, row.getElement()));
                }
            }
        }

        this.qualifiers = qualifiers.toArray(new Qualifier[qualifiers.size()]);

        Arrays.sort(this.qualifiers, new Comparator<Qualifier>() {

            private Collator collator = Collator.getInstance();

            @Override
            public int compare(Qualifier o1, Qualifier o2) {
                return collator.compare(o1.getName(), o2.getName());
            }
        });

        JScrollPane pane = new JScrollPane();
        QualifierTable table = new QualifierTable();

        model = new QualifierModel(rs.getRoot());

        table.setTreeTableModel(model);

        TableRowHeader view = new TableRowHeader(table, model);

        view.setGroupSelect(all);

        SelectType selectType = (uniqueSelected) ? SelectType.RADIO
                : SelectType.CHECK;
        view.setSelectType(selectType);
        model.setSelectType(selectType);

        pane.setRowHeaderView(view);

        Row root = rs.getRoot();
        ArrayList<Row> children = new ArrayList<Row>(root.getChildren());

        rs.close();

        root.setChildren(children);

        table.expandAll();

        pane.setViewportView(table);
        this.setPreferredSize(new Dimension(500, 300));
        this.add(pane, BorderLayout.CENTER);
    }

    public List<Qualifier> getSelected() {
        List<Qualifier> q = new ArrayList<Qualifier>();
        for (Row row : model.getSelected()) {
            q.add(StandardAttributesPlugin.getQualifier(engine, row
                    .getElement()));
        }
        return q;
    }

    public List<Qualifier> getAll() {
        List<Qualifier> list = new ArrayList<Qualifier>(qualifiers.length);

        for (Qualifier qualifier : qualifiers)
            list.add(qualifier);

        return list;
    }

    public void setSelected(Qualifier qualifier, boolean b) {
        for (int i = 0; i < qualifiers.length; i++) {
            if (qualifiers[i].equals(qualifier)) {
                model.setSelectedElement(StandardAttributesPlugin.getElement(
                        engine, qualifiers[i].getId()), true);
            }
        }
    }

    public List<Qualifier> showDialog(JFrame frame, JPanel jPanel,
                                      Object constraints) {
        final List<Qualifier> result = new ArrayList<Qualifier>();
        BaseDialog dialog = new BaseDialog(frame, true) {
            /**
             *
             */
            private static final long serialVersionUID = -2166014042175217388L;

            @Override
            protected void onOk() {
                result.addAll(getSelected());
                super.onOk();
            }
        };

        dialog.setTitle(GlobalResourcesManager.getString("QualifierView"));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(this, BorderLayout.CENTER);
        if (jPanel != null) {
            panel.add(jPanel, constraints);
        }
        dialog.setMainPane(panel);

        dialog.pack();

        dialog.setLocationRelativeTo(frame);
        dialog.setMinimumSize(dialog.getSize());

        Options.loadOptions(dialog);

        dialog.setVisible(true);
        Options.saveOptions(dialog);
        return result;
    }

    public List<Row> getSelectedRows() {
        return model.getSelected();
    }

}
