/*
 * Created on 28/8/2005
 */
package com.ramussoft.pb.data;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Comparator;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.data.negine.NRow;

/**
 * @author ZDD
 */

public class RowNameComparator<T> implements Comparator<T> {
    public static final RuleBasedCollator StringCollator = (RuleBasedCollator) Collator
            .getInstance(ResourceLoader.getLocale());

    public static RuleBasedCollator getRowSorter() {
        return StringCollator;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(final Object arg0, final Object arg1) {
        return StringCollator.compare(arg0.toString(), arg1.toString());
    }

    public static Comparator<Row> nameLevelComparator = new Comparator<Row>() {

        private RowNameComparator comparator = new RowNameComparator();

        public int compare(Row o1, Row o2) {
            DataPlugin p = ((NRow) o1).getDataPlugin();
            int level1 = p.getLevel(o1);
            int level2 = p.getLevel(o2);
            while (level1 < level2) {
                level2--;
                o2 = o2.getParentRow();
            }
            while (level2 < level1) {
                level1--;
                o1 = o1.getParentRow();
            }
            while (o1.getParentRow() != null) {
                if (o1.getParentRow().equals(o2.getParentRow()))
                    return comparator.compare(o1, o2);
                o1 = o1.getParentRow();
                o2 = o2.getParentRow();
            }
            return comparator.compare(o1, o2);
        }

    };
}
