/*
 * Created on 13/8/2005
 */
package com.dsoft.pb.idef.elements;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.ramussoft.idef0.attribute.ReaderPersistent;

/**
 * @author ZDD
 */
public class Readed implements Comparable<Readed>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4879506586523059231L;

    private ReaderPersistent deligate = new ReaderPersistent();

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat();

    static {
        dateFormat.applyPattern("dd.MM.yyyy");
    }

    public Readed() {
    }

    public Readed(ReaderPersistent reader) {
        this.deligate = reader;
    }

    /**
     * @return Returns the reader.
     */
    public String getReader() {
        return deligate.getReader();
    }

    /**
     * @param reader The reader to set.
     */
    public void setReader(final String reader) {
        this.deligate.setReader(reader);
    }

    /**
     * @return Returns the date.
     */
    public String getDate() {
        return dateFormat.format(deligate.getDate());
    }

    /**
     * @param date The date to set.
     * @throws ParseException
     */
    public void setDate(final String date) throws ParseException {
        this.deligate.setDate(new Timestamp(dateFormat.parse(date).getTime()));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final Readed r) {
        return deligate.getDate().compareTo(r.deligate.getDate());
    }

    public void setDeligate(ReaderPersistent deligate) {
        this.deligate = deligate;
    }

    public ReaderPersistent getDeligate() {
        return deligate;
    }
}
