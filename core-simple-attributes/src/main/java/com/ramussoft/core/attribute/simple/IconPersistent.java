package com.ramussoft.core.attribute.simple;

import java.util.Arrays;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Binary;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;

@Table(name = "icons")
public class IconPersistent extends AbstractPersistent implements
        Comparable<IconPersistent> {

    /**
     *
     */
    private static final long serialVersionUID = -1884009152037943071L;

    private byte[] icon;

    private String name;

    /**
     * @param icon the icon to set
     */
    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    /**
     * @return the icon
     */
    @Binary(id = 2)
    public byte[] getIcon() {
        return icon;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    @Text(id = 3)
    public String getName() {
        return name;
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
        result = prime * result + Arrays.hashCode(icon);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (!(obj instanceof IconPersistent))
            return false;
        IconPersistent other = (IconPersistent) obj;
        if (!Arrays.equals(icon, other.icon))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int compareTo(IconPersistent o) {
        return name.compareTo(o.name);
    }

}
