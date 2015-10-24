package com.ramussoft.pb.master.model;

import java.util.ArrayList;

public abstract class AbstractMasterModel implements MasterModel {

    public String[] getKeys() {
        final ArrayList<String> res = new ArrayList<String>();
        final int l = getPanelCount();
        for (int i = 0; i < l; i++) {
            final Properties ps = getPanel(i);
            final int j = ps.getPropertyCount();
            for (int k = 0; k < j; k++) {
                final Property p = ps.getProperty(k);
                res.add(p.getKey());
            }
        }
        return res.toArray(new String[res.size()]);
    }

    public Property getProperty(final String key) {
        final int l = getPanelCount();
        for (int i = 0; i < l; i++) {
            final Properties ps = getPanel(i);
            final int j = ps.getPropertyCount();
            for (int k = 0; k < j; k++) {
                final Property p = ps.getProperty(k);
                if (key.equals(p.getKey()))
                    return p;
            }
        }
        return null;
    }

}
