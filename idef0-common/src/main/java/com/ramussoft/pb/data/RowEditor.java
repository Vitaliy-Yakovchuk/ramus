/*
 * Created on 27/8/2005
 */
package com.ramussoft.pb.data;

import com.ramussoft.pb.Row;

/**
 * @author ZDD
 */
public interface RowEditor {
    public void addChild();

    public void insertRow();

    public Row addRow();

    public void removeRows();

    public void moveLeft();

    public void moveRight();
}
