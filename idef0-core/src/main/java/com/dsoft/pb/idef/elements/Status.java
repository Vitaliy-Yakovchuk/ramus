/*
 * Created on 13/8/2005
 */
package com.dsoft.pb.idef.elements;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * @author ZDD
 */
public class Status implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4283592396980908690L;

    public final static int WORKING = 0;

    public final static int DRAFT = 1;

    public final static int RECOMENDED = 2;

    public final static int PUBLICATION = 3;

    public final static int OTHER = 4;

    public static final String[] STATUS_NAMES = {"working", "draft",
            "recomended", "publication", "other"};

    private int type = WORKING;

    private String otherName = "";

    public Status() {
        super();
    }

    public Status(final int type, final String otherName) {
        super();
        this.type = type;
        this.otherName = otherName;
    }

    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(final int type) {
        this.type = type;
    }

    /**
     * @return Returns the otherName.
     */
    public String getAtherName() {
        return otherName;
    }

    /**
     * @param otherName The otherName to set.
     */
    public void setOtherName(final String otherName) {
        if (otherName == null)
            this.otherName = "";
        else
            this.otherName = otherName;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((otherName == null) ? 0 : otherName.hashCode());
        result = prime * result + type;
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Status))
            return false;
        Status other = (Status) obj;
        if (otherName == null) {
            if (other.otherName != null)
                return false;
        } else if (!otherName.equals(other.otherName))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Type: {0}, ON: {1}", type, otherName);
    }

    public static int typeOf(String type) {
        for (int i = 0; i < STATUS_NAMES.length; i++) {
            if (STATUS_NAMES[i].equalsIgnoreCase(type))
                return i;
        }
        return -1;
    }
}
