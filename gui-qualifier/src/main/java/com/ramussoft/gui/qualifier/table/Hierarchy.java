package com.ramussoft.gui.qualifier.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.ramussoft.common.Attribute;
import com.ramussoft.gui.common.GUIFramework;

import static com.ramussoft.gui.qualifier.table.TableViewProperties.toLongs;

public class Hierarchy {

    private boolean showBaseHierarchy = false;

    private static final String SHOW_BASE_HIERARCHY = "showBaseHierarchy";

    private static final String HIERARCHICAL_ATTRIBUTES = "hierarchicalAttributes";

    /**
     * Hierarchical attributes;
     */

    private long[] hAttributes = new long[]{};

    public boolean isShowBaseHierarchy() {
        return showBaseHierarchy;
    }

    public void setShowBaseHierarchy(boolean showBaseHierarchy) {
        this.showBaseHierarchy = showBaseHierarchy;
    }

    /**
     * Set hierarchical attributes, if <code>null</code>, then application will
     * use base hierarchy.
     */

    public void setAttributes(Attribute[] attributes) {
        if (attributes == null) {
            throw new NullPointerException("attributes can not be null");
        } else {
            hAttributes = new long[attributes.length];
            for (int i = 0; i < attributes.length; i++) {
                hAttributes[i] = attributes[i].getId();
            }
        }
    }

    /**
     * Return hierarchical attributes.
     *
     * @param list All attributes.
     * @return Hierarchical attributes, in the same order.
     */

    public Attribute[] getAttributes(List<Attribute> list) {
        List<Attribute> l = new ArrayList<Attribute>(list.size());
        for (long id : hAttributes) {
            Attribute a = findAttribute(list, id);
            if (a != null)
                l.add(a);
        }
        return l.toArray(new Attribute[l.size()]);
    }

    private Attribute findAttribute(List<Attribute> list, long id) {
        for (Attribute attribute : list)
            if (attribute.getId() == id)
                return attribute;
        return null;
    }

    public void loadHierarchy(Properties properties, int index) {
        String hString = properties.getProperty(HIERARCHICAL_ATTRIBUTES + "_"
                + index);
        if (hString == null)
            hAttributes = new long[]{};
        else {
            hAttributes = toLongs(hString);
        }
        String sbh = properties.getProperty(SHOW_BASE_HIERARCHY + "_" + index);
        if (sbh == null)
            showBaseHierarchy = false;
        else
            showBaseHierarchy = Boolean.parseBoolean(sbh);
    }

    public void storeHierarchy(Properties properties, int index) {
        properties.put(HIERARCHICAL_ATTRIBUTES + "_" + index,
                TableViewProperties.toString(hAttributes));
        properties.setProperty(SHOW_BASE_HIERARCHY + "_" + index, Boolean
                .toString(showBaseHierarchy));
    }

    public String toString(List<Attribute> list, GUIFramework framework) {
        Attribute[] attributes = getAttributes(list);
        StringBuffer sb = new StringBuffer("(");
        for (Attribute attribute : attributes) {
            if ((attributes.length > 0) && (attribute != attributes[0])) {
                sb.append("->");
            }
            String name = framework.getSystemAttributeName(attribute);
            if (name == null)
                name = attribute.getName();
            sb.append(name);
        }
        sb.append(")");
        return sb.toString();
    }
}
