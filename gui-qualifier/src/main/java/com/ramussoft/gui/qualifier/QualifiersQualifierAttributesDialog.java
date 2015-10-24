package com.ramussoft.gui.qualifier;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.gui.qualifier.table.SelectableTableView;

public class QualifiersQualifierAttributesDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = -1551588850785262747L;

    private SelectableTableView view;

    private Engine engine;

    public QualifiersQualifierAttributesDialog(GUIFramework framework) {
        super(framework.getMainFrame(), true);

        setTitle(GlobalResourcesManager
                .getString("Action.QualifiersQualifierAttributes"));

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

        setMainPane(view.createComponent());

        view.getComponent().getModel().setEditable(false);
        view.getComponent().getTable().setComponentPopupMenu(null);

        view.setSelectType(SelectType.CHECK);

        Qualifier qualifier = engine.getQualifier(StandardAttributesPlugin
                .getQualifiersQualifier(engine).getId());

        List<Long> rows = new ArrayList<Long>();
        for (Attribute attribute : qualifier.getAttributes()) {
            rows.add(StandardAttributesPlugin.getElementForAttribute(engine,
                    attribute).getId());
        }

        view.selectRows(rows);
    }

    @Override
    protected void onOk() {
        Qualifier qualifier = StandardAttributesPlugin
                .getQualifiersQualifier(engine);

        qualifier.getAttributes().clear();

        for (Row row : view.getSelectedRows()) {
            qualifier.getAttributes().add(
                    engine.getAttribute(StandardAttributesPlugin
                            .getAttributeId(engine, row.getElement())));
        }

        ((Journaled) engine).startUserTransaction();
        engine.updateQualifier(qualifier);
        ((Journaled) engine).commitUserTransaction();
        super.onOk();
    }

    public void close() {
        view.close();
    }
}
