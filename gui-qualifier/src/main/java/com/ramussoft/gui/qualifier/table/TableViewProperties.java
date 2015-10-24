package com.ramussoft.gui.qualifier.table;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.prefrence.Options;

public class TableViewProperties {

    private static final String USER_GUI_TABLE_VIEW = "/user/gui/table/view/";

    private static final String ACTIVE_HIERARCHY = "activeHierarchy";

    private static final String HIDE_ATTRIBUTES = "hideAttributes";

    private static final String HIERARCHY_COUNT = "hierarchyCount";

    private static final String SELECTED_ROWS = "selectedRows";

    private static final String EXPANDED_ROWS = "expandedRows";

    private static final String SHOW_FIND_PANEL = "showFindPanel";

    private static final String ATTRIBUTES_ORDER = "attributesOrder";

    private static final String SEL_RECT = "selRect";

    private Rectangle viewRect = null;

    private int[] selectedRows = new int[]{};

    private int[] expanedRows = new int[]{};

    private long[] hideAttributes;

    private Hierarchy[] hierarchies = new Hierarchy[]{};

    private int activeHierarchy = -1;

    private boolean showFindPanel;

    private String hideAttributesString;

    public static String toString(long[] a) {
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    public static long[] toLongs(String string) {
        if (string == null)
            return null;
        StringTokenizer st = new StringTokenizer(string, "[], ");
        List<Long> list = new ArrayList<Long>();
        while (st.hasMoreTokens()) {
            list.add(Long.parseLong(st.nextToken()));
        }
        long[] res = new long[list.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = list.get(i);
        return res;
    }

    public static String toString(int[] a) {
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    public static int[] toInts(String string) {
        StringTokenizer st = new StringTokenizer(string, "[], ");
        List<Integer> list = new ArrayList<Integer>();
        while (st.hasMoreTokens()) {
            list.add(Integer.parseInt(st.nextToken()));
        }
        int[] res = new int[list.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = list.get(i);
        return res;
    }

    public void store(Engine engine, TableView tableView) {
        try {
            Attribute[] attributes = tableView.getAttributes();
            if (attributes.length == 0)// In case if qualifier removing by undo
                return;
            Properties properties = new Properties();
            showFindPanel = tableView.getComponent().isShowFindPanel();
            properties.setProperty(HIDE_ATTRIBUTES, toString(hideAttributes));
            Options.saveOptions(tableView.getComponent(), properties);

            properties.setProperty(HIERARCHY_COUNT,
                    Integer.toString(getHierarchies().length));

            for (int index = 0; index < getHierarchies().length; index++)
                getHierarchies()[index].storeHierarchy(properties, index);

            properties.put(ACTIVE_HIERARCHY,
                    Integer.toString(getActiveHierarchy()));

            RowTreeTable table = tableView.getComponent().getTable();
            this.selectedRows = table.getSelectedRows();
            this.expanedRows = table.getExpandedRows();

            this.viewRect = tableView.getComponent().getPane().getViewport()
                    .getViewRect();

            properties.put(SELECTED_ROWS, toString(selectedRows));

            properties.put(EXPANDED_ROWS, toString(expanedRows));

            long attrs[] = new long[table.getColumnCount()];
            for (int i = 0; i < attrs.length; i++) {
                attrs[i] = tableView.getAttributes()[table
                        .convertColumnIndexToModel(i)].getId();
            }

            properties.put(ATTRIBUTES_ORDER, toString(attrs));

            if (viewRect != null)
                Options.setRectangle(SEL_RECT, viewRect, properties);

            Options.setBoolean(SHOW_FIND_PANEL, showFindPanel, properties);

            String propertiesName = getPropertiesName(tableView);
            engine.setProperties(USER_GUI_TABLE_VIEW
                    + propertiesName, properties);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                engine.deleteStream(USER_GUI_TABLE_VIEW
                        + getPropertiesName(tableView));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private String getPropertiesName(TableView tableView) {
        return tableView.getClass().getName() + "."
                + tableView.getQualifier().getId()
                + tableView.getPropertiesPrefix() + ".xml";
    }

    public void load(Engine engine, TableView tableView) {
        Properties properties = getProperties(engine, tableView);
        hideAttributesString = properties.getProperty(HIDE_ATTRIBUTES);
        if (hideAttributesString == null)
            hideAttributes = new long[]{};
        else
            hideAttributes = toLongs(hideAttributesString);

        String sCount = properties.getProperty(HIERARCHY_COUNT);
        if (sCount == null)
            setHierarchies(new Hierarchy[]{});
        else {
            setHierarchies(new Hierarchy[Integer.parseInt(sCount)]);
            for (int index = 0; index < getHierarchies().length; index++) {
                getHierarchies()[index] = new Hierarchy();
                getHierarchies()[index].loadHierarchy(properties, index);
            }
        }

        String string = properties.getProperty(ACTIVE_HIERARCHY);
        if (string == null)
            setActiveHierarchy(-1);
        else
            setActiveHierarchy(Integer.parseInt(string));

        string = properties.getProperty(EXPANDED_ROWS);
        if (string != null)
            expanedRows = toInts(string);
        string = properties.getProperty(SELECTED_ROWS);
        if (string != null)
            selectedRows = toInts(string);

        viewRect = Options.getRectangle(SEL_RECT, viewRect, properties);

        showFindPanel = Options.getBoolean(SHOW_FIND_PANEL, false, properties);
    }

    private Properties getProperties(Engine engine, TableView tableView) {
        return engine.getProperties(USER_GUI_TABLE_VIEW
                + getPropertiesName(tableView));
    }

    /**
     * @param hideAttributes the hideAttributes to set
     */
    public void setHideAttributes(long[] hideAttributes) {
        this.hideAttributes = hideAttributes;
    }

    /**
     * @return the hideAttributes
     */
    public long[] getHideAttributes() {
        return hideAttributes;
    }

    public boolean isPresent(Attribute attribute) {
        for (long l : hideAttributes) {
            if (attribute.getId() == l)
                return true;
        }
        return false;
    }

    public void setComponentLook(final Engine engine, final TableView tableView) {
        RowTreeTableComponent component = tableView.getComponent();
        Options.loadOptions(component, getProperties(engine, tableView));

        RowTreeTable table = component.getTable();
        if (expanedRows != null)
            table.setExpandedRows(expanedRows);
        if (selectedRows != null)
            table.setSelectedRows(selectedRows, false);
        if (viewRect != null) {
            Rectangle c = component.getPane().getViewport().getViewRect();
            Rectangle r = viewRect;
            component
                    .getPane()
                    .getViewport()
                    .scrollRectToVisible(
                            new Rectangle(r.x - c.x, r.y - c.y, r.width,
                                    r.height));
        }

        tableView.getComponent().setShowFindPanel(showFindPanel);
    }

    /**
     * @param activeHierarchy the activeHierarcgy to set
     */
    public void setActiveHierarchy(int activeHierarchy) {
        this.activeHierarchy = activeHierarchy;
    }

    /**
     * @return the activeHierarcgy
     */
    public int getActiveHierarchy() {
        return activeHierarchy;
    }

    /**
     * @param hierarchies the hierarchies to set
     */
    public void setHierarchies(Hierarchy[] hierarchies) {
        this.hierarchies = hierarchies;
    }

    /**
     * @return the hierarchies
     */
    public Hierarchy[] getHierarchies() {
        return hierarchies;
    }

    /**
     * @param expanedRows the expanedRows to set
     */
    public void setExpanedRows(int[] expanedRows) {
        this.expanedRows = expanedRows;
    }

    /**
     * @return the expanedRows
     */
    public int[] getExpanedRows() {
        return expanedRows;
    }

    /**
     * @param selectedRows the selectedRows to set
     */
    public void setSelectedRows(int[] selectedRows) {
        this.selectedRows = selectedRows;
    }

    /**
     * @return the selectedRows
     */
    public int[] getSelectedRows() {
        return selectedRows;
    }

    /**
     * @param viewRect the viewRect to set
     */
    public void setViewRect(Rectangle viewRect) {
        this.viewRect = viewRect;
    }

    /**
     * @return the viewRect
     */
    public Rectangle getViewRect() {
        return viewRect;
    }

    /**
     * @param showFindPanel the showFindPanel to set
     */
    public void setShowFindPanel(boolean showFindPanel) {
        this.showFindPanel = showFindPanel;
    }

    /**
     * @return the showFindPanel
     */
    public boolean isShowFindPanel() {
        return showFindPanel;
    }

    public String getHideAttributesString() {
        return hideAttributesString;
    }
}
