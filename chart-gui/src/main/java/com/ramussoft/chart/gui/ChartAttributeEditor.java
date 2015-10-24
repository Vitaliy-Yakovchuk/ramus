package com.ramussoft.chart.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.ramussoft.chart.core.ChartPlugin;
import com.ramussoft.common.Attribute;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.gui.qualifier.table.SelectableTableView;

public class ChartAttributeEditor extends AbstractAttributeEditor {

    private Long link;

    private SelectableTableView view;

    private GUIFramework framework;

    public ChartAttributeEditor(GUIFramework framework, Long link) {
        this.framework = framework;
        this.link = link;
    }

    @Override
    public JComponent getComponent() {
        view = new SelectableTableView(framework, ChartPlugin
                .getCharts(framework.getEngine())) {
            @Override
            protected Attribute[] getAttributes() {
                return new Attribute[]{StandardAttributesPlugin
                        .getAttributeNameAttribute(engine)};
            }
        };

        JComponent component = view.createComponent();
        view.setSelectType(SelectType.RADIO);

        if (link != null) {
            List<Long> rows = new ArrayList<Long>(1);
            rows.add(link);
            view.selectRows(rows);
        }

        return component;
    }

    @Override
    public Object getValue() {
        List<Row> rows = view.getSelectedRows();
        if (rows.size() > 0) {
            link = rows.get(0).getElementId();
        } else
            link = null;
        return link;
    }

    @Override
    public Object setValue(Object value) {
        link = (Long) value;
        if (view != null) {
            view.clearSelection();
            if (link != null) {
                List<Long> rows = new ArrayList<Long>(1);
                rows.add(link);
                view.selectRows(rows);
            }
        }
        return link;
    }

    @Override
    public void close() {
        super.close();
        try {
            view.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
