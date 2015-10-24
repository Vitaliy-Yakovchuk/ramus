package com.ramussoft.chart.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.ramussoft.chart.ElementSource;
import com.ramussoft.chart.QualifierSource;
import com.ramussoft.common.Qualifier;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.gui.qualifier.table.SelectableTableView;

public class QualifierSourceSelectPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -9175071460373752975L;

    private SelectableTableView view;

    private QualifierSource qualifierSource;

    public QualifierSourceSelectPanel(GUIFramework framework,
                                      Qualifier qualifier, QualifierSource qualifierSource, SelectType selectType) {
        super(new BorderLayout());
        this.qualifierSource = qualifierSource;
        view = new SelectableTableView(framework, qualifier) {

        };

        this.add(view.createComponent(), BorderLayout.CENTER);

        view.setSelectType(selectType);
        List<ElementSource> sources = qualifierSource.getElementSources();
        List<Long> rows = new ArrayList<Long>(sources.size());
        for (ElementSource source : sources) {
            rows.add(source.getElement().getId());
        }
        view.selectRows(rows);

        JToolBar toolBar = view.createToolBar();
        this.add(toolBar, BorderLayout.NORTH);
        toolBar.setFloatable(false);
    }

    public void close() {
        if (view != null) {
            view.close();
            view = null;
        }
    }

    public void save() {
        List<ElementSource> elementSources = qualifierSource
                .getElementSources();
        elementSources.clear();
        for (Row row : view.getSelectedRows()) {
            ElementSource e = qualifierSource.getChartSource()
                    .createElementSource();
            e.setElement(row.getElement());
            elementSources.add(e);
        }
        qualifierSource.getChartSource().fireAttributeListChanged();
    }

    public SelectableTableView getView() {
        return view;
    }

    public QualifierSource getQualifierSource() {
        return qualifierSource;
    }
}
