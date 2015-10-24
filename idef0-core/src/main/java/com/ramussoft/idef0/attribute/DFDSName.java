package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;
import com.ramussoft.database.StringCollator;

@Table(name = "dfds_names")
public class DFDSName extends AbstractPersistent implements
        Comparable<DFDSName> {

    /**
     *
     */
    private static final long serialVersionUID = 6001579054998269411L;

    private String shortName;

    private String longName;

    private String shortNameSource;

    private String longNameSource;

    @Text(id = 2)
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Text(id = 3)
    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getShortNameSource() {
        return shortNameSource;
    }

    public void setShortNameSource(String shortNameSource) {
        this.shortNameSource = shortNameSource;
    }

    public String getLongNameSource() {
        return longNameSource;
    }

    public void setLongNameSource(String longNameSource) {
        this.longNameSource = longNameSource;
    }

    @Override
    public String toString() {
        return shortName == null ? "" : shortName;
    }

    @Override
    public int compareTo(DFDSName o) {
        return StringCollator.compare(this.toString(), o.toString());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((longName == null) ? 0 : longName.hashCode());
        result = prime * result
                + ((shortName == null) ? 0 : shortName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DFDSName other = (DFDSName) obj;
        if (longName == null) {
            if (other.longName != null)
                return false;
        } else if (!longName.equals(other.longName))
            return false;
        if (shortName == null) {
            if (other.shortName != null)
                return false;
        } else if (!shortName.equals(other.shortName))
            return false;
        return true;
    }
}
