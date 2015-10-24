package com.ramussoft.gui.attribute;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.gui.qualifier.table.SelectableTableView;

public class AttributesSelectPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 4105467091290068080L;

    private SelectableTableView view;

    private Engine engine;

    public AttributesSelectPanel(GUIFramework framework,
                                 List<Attribute> attributes) {
        super(new BorderLayout());
        engine = framework.getEngine();

        view = new SelectableTableView(framework, StandardAttributesPlugin
                .getAttributesQualifier(engine), true) {
            @Override
            protected Attribute[] getAttributes() {
                return new Attribute[]{
                        StandardAttributesPlugin
                                .getAttributeNameAttribute(engine),
                        StandardAttributesPlugin
                                .getAttributeTypeNameAttribute(engine)};
            }
        };

        this.add(view.createComponent(), BorderLayout.CENTER);

        view.getComponent().getModel().setEditable(false);
        view.getComponent().getTable().setComponentPopupMenu(null);

        view.setSelectType(SelectType.CHECK);

        List<Long> rows = new ArrayList<Long>();
        for (Attribute attribute : attributes) {
            rows.add(StandardAttributesPlugin.getElementForAttribute(engine,
                    attribute).getId());
        }

        view.selectRows(rows);
    }

    public void close() {
        view.close();
    }

    public List<Attribute> getAttributes() {
        List<Attribute> res = new ArrayList<Attribute>();
        for (Row row : view.getSelectedRows()) {
            res.add(engine.getAttribute(StandardAttributesPlugin
                    .getAttributeId(engine, row.getElement())));
        }
        return res;
    }
}
