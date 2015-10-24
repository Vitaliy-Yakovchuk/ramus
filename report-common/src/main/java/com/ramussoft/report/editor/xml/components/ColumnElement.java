package com.ramussoft.report.editor.xml.components;

import java.awt.Graphics2D;

public abstract class ColumnElement extends Label {

    /**
     *
     */
    private static final long serialVersionUID = -7901597572059921632L;

    private TableColumn tableColumn;

    public void setTableColumn(TableColumn tableColumn) {
        this.tableColumn = tableColumn;
    }

    @Override
    protected void paintText(Graphics2D g) {
        g.translate(6, 0);
        label.paint(g);
        g.translate(-6, 0);
    }

    public TableColumn getTableColumn() {
        return tableColumn;
    }

    @Override
    public boolean isY() {
        return false;
    }

}
