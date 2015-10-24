package com.ramussoft.core.attribute.simple;

import java.io.Serializable;
import java.util.Arrays;

public class HTMLPage implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8018771078927850706L;

    private String path;

    private byte[] data;

    private transient boolean empty = false;

    public HTMLPage(byte[] data, String path) {
        this.path = path;
        this.data = data;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(data);
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof HTMLPage))
            return false;
        HTMLPage other = (HTMLPage) obj;
        if (!Arrays.equals(data, other.data))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }

    /**
     * @param empty the empty to set
     */
    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    /**
     * @return the empty
     */
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public String toString() {
        if (data != null)
            return new String(data);
        return super.toString();
    }
}
