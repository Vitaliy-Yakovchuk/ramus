/*
 * Created on 5/8/2005
 */
package com.ramussoft.pb.types;

import java.util.Comparator;

import com.ramussoft.pb.data.Indexed;

/**
 * @author ZDD
 */
public class GlobalIdComparator implements Comparator {

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(final Object arg0, final Object arg1) {
        return ((Indexed) arg0).getGlobalId().compareTo(
                ((Indexed) arg1).getGlobalId());
    }
}
