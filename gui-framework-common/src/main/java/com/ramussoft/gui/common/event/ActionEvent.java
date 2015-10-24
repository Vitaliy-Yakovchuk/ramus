package com.ramussoft.gui.common.event;

import java.io.Serializable;
import java.util.Arrays;

public class ActionEvent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2789520189179043655L;

    public static final String OPEN_STATIC_VIEW = "OpenStaticView";

    private String key;

    private Object value;

    private transient Object metadata;

    public ActionEvent(String key, Object value, Object metadat) {
        this.key = key;
        this.value = value;
        this.metadata = metadat;
    }

    public ActionEvent(String key, Object value) {
        this(key, value, null);
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int kh = 0;
        if (key != null)
            kh = key.hashCode();
        int vh = 0;
        if (value != null)
            vh = value.hashCode();

        return Arrays.hashCode(new int[]{kh, vh});
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActionEvent) {
            ActionEvent ae = (ActionEvent) obj;
            return equals(key, ae.key) && equals(value, ae.value);
        }
        return super.equals(obj);
    }

    private boolean equals(Object a, Object b) {
        if (a == null)
            return b == null;
        return a.equals(b);
    }

    /**
     * @return the metadata
     */
    public Object getMetadata() {
        return metadata;
    }
}
