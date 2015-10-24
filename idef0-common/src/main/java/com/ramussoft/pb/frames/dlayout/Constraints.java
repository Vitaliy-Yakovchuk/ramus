/*
 *  DockingConstraints.java
 *  2004-01-02
 */

package com.ramussoft.pb.frames.dlayout;

public class Constraints {
    public static final int MAX = ToolBarLayout.MAX;
    public static final int NORTH = ToolBarLayout.NORTH;
    public static final int SOUTH = ToolBarLayout.SOUTH;
    public static final int EAST = ToolBarLayout.EAST;
    public static final int WEST = ToolBarLayout.WEST;

    private int ourDockIndex = MAX;
    private int ourRowIndex = 0;
    private int ourDockEdge = NORTH;

    /**
     * Creates a DockingConstraints object with a default edge of NORTH, row of
     * 0, and index of MAX.
     */
    public Constraints() {

    }

    /**
     * Creates a DockingConstraints object at the specified edge with a default
     * row of 0 and index of MAX.
     */
    public Constraints(final int edge) {
        setEdge(edge);
    }

    /**
     * Creates a DockingConstraints object at the specified edge and index with
     * a default row of 0.
     */
    public Constraints(final int edge, final int index) {
        setEdge(edge);
        setIndex(index);
    }

    /**
     * Creates a DockingConstraints object at the specified edge, row, and
     * index.
     */
    public Constraints(final int edge, final int row, final int index) {
        setEdge(edge);
        setRow(row);
        setIndex(index);
    }

    /**
     * Sets the edge at which the toolbar is docked.
     */
    void setEdge(final int edge) {
        if (edge == NORTH || edge == SOUTH || edge == EAST || edge == WEST)
            ourDockEdge = edge;
    }

    /**
     * Sets the index of the row within the DockBoundary in which the toolbar is
     * docked.
     */
    void setRow(final int row) {
        if (row < 0)
            ourRowIndex = 0;
        else
            ourRowIndex = row;
    }

    /**
     * Sets the index within the row in which the toolbar is docked.
     */
    void setIndex(final int index) {
        if (index < 0)
            ourDockIndex = 0;
        else
            ourDockIndex = index;
    }

    /**
     * Returns an int representing the edge in which the toolbar is docked.
     */
    public int getEdge() {
        return ourDockEdge;
    }

    /**
     * Returns an int representing the row index within the DockBoundary in
     * which the toolbar is docked.
     */
    public int getRow() {
        return ourRowIndex;
    }

    /**
     * Returns an int representing the index within the row in which the toolbar
     * is docked.
     */
    public int getIndex() {
        return ourDockIndex;
    }

}
