package com.ramussoft.report.editor;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.ramussoft.common.Element;
import com.ramussoft.common.Qualifier;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.gui.qualifier.table.SelectableTableView;
import com.ramussoft.report.Query;
import com.ramussoft.report.ReportQuery;
import com.ramussoft.report.ReportResourceManager;

public class QueryView extends SubView {

    /**
     *
     */
    private static final long serialVersionUID = -1133616669364615282L;

    private ReportQuery reportQuery;

    private SelectableTableView selectableTableView = null;

    private GUIFramework framework;

    public QueryView(GUIFramework framework) {
        this.framework = framework;
        this.reportQuery = (ReportQuery) framework.getEngine();
    }

    @Override
    public String getTitle() {
        return ReportResourceManager.getString("QueryView.title");
    }

    @Override
    public Action[] getActions() {
        if (selectableTableView != null)
            return selectableTableView.getActions();
        return new Action[]{};
    }

    public void setQueryForReport(Element element) {
        Qualifier qualifier = reportQuery.getHTMLReportQuery(element);
        if (qualifier == null) {
            this.removeAll();
            if (selectableTableView != null) {
                selectableTableView.close();
                selectableTableView = null;
            }
            this.add(new JLabel(ReportResourceManager
                            .getString("QueryView.emptyQueryText")),
                    BorderLayout.CENTER);
        } else {
            if ((selectableTableView == null)
                    || (!selectableTableView.getQualifier().equals(qualifier))) {
                this.removeAll();
                if (selectableTableView != null)
                    selectableTableView.close();
                selectableTableView = new SelectableTableView(framework,
                        qualifier);
                JComponent createComponent = selectableTableView
                        .createComponent();
                selectableTableView.getComponent().getModel().checkAll();
                this.add(createComponent, BorderLayout.CENTER);
                selectableTableView.setSelectType(SelectType.CHECK);
            }
        }
    }

    public Query getQuery() {
        if (selectableTableView == null)
            return null;
        Query query = new Query(new HashMap<String, String>(0));

        List<Row> rows = selectableTableView.getSelectedRows();
        ArrayList<Element> elements = new ArrayList<Element>(rows.size());

        for (Row row : rows)
            elements.add(row.getElement());
        query.setElements(elements);
        return query;
    }

    public void close() {
        if (selectableTableView == null)
            return;
        selectableTableView.close();
        selectableTableView = null;
    }
}
