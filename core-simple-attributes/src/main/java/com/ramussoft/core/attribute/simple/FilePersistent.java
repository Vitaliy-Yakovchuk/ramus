package com.ramussoft.core.attribute.simple;

import java.sql.Timestamp;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Date;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;

@Table(name = "attached_files")
public class FilePersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = 8103813603911786492L;

    private byte[] data;

    private String path;

    private String name;

    private Timestamp lastModifiedTime;

    private Timestamp uploadTime;

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the path
     */
    @Text(id = 2)
    public String getPath() {
        return path;
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

    @Override
    public String toString() {
        return name;
    }

    /**
     * @param lastModifiedTime the lastModifiedTime to set
     */
    public void setLastModifiedTime(Timestamp lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * @return the lastModifiedTime
     */
    @Date(id = 4)
    public Timestamp getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * @param uploadTime the uploadTime to set
     */
    public void setUploadTime(Timestamp uploadTime) {
        this.uploadTime = uploadTime;
    }

    /**
     * @return the uploadTime
     */
    @com.ramussoft.common.persistent.Date(id = 5)
    public Timestamp getUploadTime() {
        return uploadTime;
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
        result = prime
                * result
                + ((lastModifiedTime == null) ? 0 : lastModifiedTime.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result
                + ((uploadTime == null) ? 0 : uploadTime.hashCode());
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
        if (!(obj instanceof FilePersistent))
            return false;
        FilePersistent other = (FilePersistent) obj;
        if (lastModifiedTime == null) {
            if (other.lastModifiedTime != null)
                return false;
        } else if (!lastModifiedTime.equals(other.lastModifiedTime))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (uploadTime == null) {
            if (other.uploadTime != null)
                return false;
        } else if (!uploadTime.equals(other.uploadTime))
            return false;
        return true;
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
}
