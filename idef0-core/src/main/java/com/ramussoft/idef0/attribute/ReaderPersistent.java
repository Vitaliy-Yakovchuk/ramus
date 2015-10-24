package com.ramussoft.idef0.attribute;

import java.sql.Timestamp;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Date;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;

@Table(name = "readers")
public class ReaderPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = 7400684398309875280L;

    private Timestamp date = new Timestamp(System.currentTimeMillis());

    private String reader = "";

    /**
     * @param date the date to set
     */
    public void setDate(Timestamp date) {
        this.date = date;
    }

    /**
     * @return the date
     */
    @Date(id = 2, primary = true)
    public Timestamp getDate() {
        return date;
    }

    /**
     * @param reader the reader to set
     */
    public void setReader(String reader) {
        this.reader = reader;
    }

    /**
     * @return the reader
     */
    @Text(id = 3, primary = true)
    public String getReader() {
        return reader;
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
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((reader == null) ? 0 : reader.hashCode());
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
        if (!(obj instanceof ReaderPersistent))
            return false;
        ReaderPersistent other = (ReaderPersistent) obj;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (reader == null) {
            if (other.reader != null)
                return false;
        } else if (!reader.equals(other.reader))
            return false;
        return true;
    }

}
