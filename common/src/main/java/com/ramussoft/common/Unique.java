package com.ramussoft.common;

import java.io.Serializable;

public abstract class Unique implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = -276356678594726065L;

    protected long id;

    /**
     * Getter for unique id of the object (qualifier, attribute, element, etc...).
     *
     * @return
     */

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Unique) {
            return ((Unique) obj).id == this.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

}
