package com.ramussoft.pb.print.xml;

import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;

public class TreeMatrixNodeImpl implements TreeMatrixNode {

    private Row row;

    private Sector sector;

    public TreeMatrixNodeImpl(Row row, Sector sector) {
        this.row = row;
        this.sector = sector;
    }

    @Override
    public Row getRow() {
        return row;
    }

    @Override
    public Sector getSector() {
        return sector;
    }

}
