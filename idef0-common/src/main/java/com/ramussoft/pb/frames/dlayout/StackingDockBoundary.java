/*
 *  StackingDockBoundary.java
 *  2004-01-02
 */

package com.ramussoft.pb.frames.dlayout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JToolBar;

/**
 * A DockBoundary that allows stacking toolbars into rows (or columns). Rows are
 * NOT automatically wrapped when the toolbars don't fit.
 *
 * @author Christopher Bach
 */
// Package access only...
class StackingDockBoundary extends DockBoundary {

    private final ArrayList ourDockSlivers = new ArrayList();

    /**
     * Creates a StackingDockBoundary for the specified edge.
     */
    public StackingDockBoundary(final int edge) {
        super(edge);
    }

    /**
     * Creates a StackingDockBoundary for the specified edge with the provided
     * spacing.
     */
    public StackingDockBoundary(final int edge, final int spacing) {
        super(edge, spacing);
    }

    /**
     * Implementation of the abstract superclass method, returns the index at
     * which the toolbar should be added when dropped at the specified point.
     */
    @Override
    public int getDockIndex(final Point p) {
        for (int i = 0; i < ourDockSlivers.size(); i++) {
            final DockSliver sliver = (DockSliver) ourDockSlivers.get(i);
            if (sliver.contains(p))
                return sliver.getDockIndex(p);
        }

        return ToolBarLayout.MAX;
    }

    /**
     * Implementation of the absract superclass method, returns the index of the
     * specified toolbar within this boundary, or -1 if the toolbar is not
     * present.
     */
    @Override
    public int getDockIndex(final JToolBar toolbar) {
        final DockSliver sliver = getDockSliver(toolbar);
        if (sliver == null)
            return -1;
        else
            return sliver.getDockIndex(toolbar);
    }

    /**
     * Implementation of the abstract superclass method, returns the row index
     * at which the toolbar should be added when dropped at the specified point.
     */
    @Override
    public int getRowIndex(final Point p) {
        for (int i = 0; i < ourDockSlivers.size(); i++) {
            final DockSliver sliver = (DockSliver) ourDockSlivers.get(i);
            if (sliver.contains(p))
                return i;
        }

        return ToolBarLayout.MAX;
    }

    /**
     * Implementation of the abstract superclass method, returns the row index
     * of the specified toolbar in this boundary, or -1 if the toolbar is not
     * present.
     */
    @Override
    public int getRowIndex(final JToolBar toolbar) {
        for (int i = 0; i < ourDockSlivers.size(); i++) {
            final DockSliver sliver = (DockSliver) ourDockSlivers.get(i);
            if (sliver.containsToolBar(toolbar))
                return i;
        }

        return -1;
    }

    /**
     * Implementation of the abstract superclass method, lays out the registered
     * toolbars and calculates the depth of this boundary. When this method is
     * called, the depth of the boundary is assumed to be 0 and is not
     * determined until the validation completes. For convenience, the
     * subcomponents are arranged in a top-down or left-to-right fashion
     * relative to the origin of this boundary, which presumably lies at the
     * parent container's edge. For the south and east boundaries, this puts the
     * bounds of the subcomponents outside the bounds of the parent container.
     * To compensate, these subcomponents are reflected about a line passing
     * through the boundary's origin as they are placed.
     */
    @Override
    public void validate() {
        final int spacing = getSpacing();
        final int orientation = getOrientation();
        final int edge = getEdge();

        int length = 0;
        if (orientation == ToolBarLayout.HORIZONTAL)
            length = width;
        else
            length = height;

        int totalDepth = 0;

        for (int i = 0; i < ourDockSlivers.size(); i++) {
            final DockSliver sliver = (DockSliver) ourDockSlivers.get(i);

            if (totalDepth != 0)
                totalDepth += spacing;

            if (orientation == ToolBarLayout.HORIZONTAL)
                sliver.setPosition(x, y + totalDepth, length);
            else
                sliver.setPosition(x + totalDepth, y, length);

            if (edge == ToolBarLayout.EAST || edge == ToolBarLayout.SOUTH) {
                final Rectangle r = sliver.getBounds();

                if (orientation == ToolBarLayout.HORIZONTAL)
                    mirrorBounds(r, y);
                else
                    mirrorBounds(r, x);

                sliver.setBounds(r);
                sliver.validate();
            }

            totalDepth += sliver.getDepth();
        }

        setDepth(totalDepth);

    }

    // Override superclass methods to provide additional behavior

    /**
     * Inserts the specified toolbar into this boundary at the provided indices.
     */
    @Override
    protected void toolBarAdded(final JToolBar toolbar, final int rowIndex, final int index) {
        getDockSliver(rowIndex).addToolBar(toolbar, index);
    }

    /**
     * Removes the specified toolbar from this boundary.
     */
    @Override
    protected void toolBarRemoved(final JToolBar toolbar) {
        final DockSliver sliver = getDockSliver(toolbar);
        if (sliver != null) {
            sliver.removeToolBar(toolbar);
            if (sliver.getToolBarCount() == 0)
                ourDockSlivers.remove(sliver);
        }
    }

    /**
     * Returns a DockSliver for the specified row. If none exists at this index,
     * a new one is created and inserted.
     */
    private DockSliver getDockSliver(final int row) {
        if (row < 0) {
            final DockSliver sliver = new DockSliver();
            ourDockSlivers.add(0, sliver);
            return sliver;
        } else if (row >= ourDockSlivers.size()) {
            final DockSliver sliver = new DockSliver();
            ourDockSlivers.add(sliver);
            return sliver;
        } else
            return (DockSliver) ourDockSlivers.get(row);
    }

    /**
     * Returns the DockSliver containing the specified toolbar or null if no
     * DockSliver contains this toolbar.
     */
    private DockSliver getDockSliver(final JToolBar toolbar) {
        for (int i = 0; i < ourDockSlivers.size(); i++) {
            final DockSliver sliver = (DockSliver) ourDockSlivers.get(i);
            if (sliver.containsToolBar(toolbar))
                return sliver;
        }

        return null;
    }

    /**
     * Inner class defining a row or "sliver" of toolbars stacked within this
     * boundary.
     */
    private class DockSliver extends Rectangle {

        private final ArrayList myToolBars = new ArrayList();

        public DockSliver() {

        }

        public void addToolBar(final JToolBar toolbar, final int index) {
            if (index < 0)
                myToolBars.add(0, toolbar);
            else if (index >= myToolBars.size())
                myToolBars.add(toolbar);
            else
                myToolBars.add(index, toolbar);
        }

        public void removeToolBar(final JToolBar toolbar) {
            myToolBars.remove(toolbar);
        }

        public int getToolBarCount() {
            return myToolBars.size();
        }

        public boolean containsToolBar(final JToolBar toolbar) {
            return myToolBars.contains(toolbar);
        }

        public void setPosition(final int x, final int y, final int length) {
            setLocation(x, y);
            if (getOrientation() == ToolBarLayout.HORIZONTAL)
                width = length;
            else
                height = length;
            validate();
        }

        public void validate() {
            int pos = 0;
            int base = 0;
            final int orient = getOrientation();
            final int space = getSpacing();
            int length = 0;

            if (orient == ToolBarLayout.HORIZONTAL) {
                pos = x;
                base = x;
                length = width;
                height = getPreferredDepth();
            } else {
                pos = y;
                base = y;
                length = height;
                width = getPreferredDepth();
            }

            for (int i = 0; i < myToolBars.size(); i++) {
                final JToolBar toolbar = (JToolBar) myToolBars.get(i);
                int barLength = getPreferredToolBarLength(toolbar);
                if (pos + barLength > length + base)
                    barLength = base + length - pos;

                if (orient == ToolBarLayout.HORIZONTAL) {
                    toolbar.setBounds(pos, y, barLength, height);
                } else
                    toolbar.setBounds(x, pos, width, barLength);

                pos += barLength + space;
            }
        }

        public int getPreferredDepth() {
            int depth = 0;

            for (int i = 0; i < myToolBars.size(); i++) {
                final JToolBar toolbar = (JToolBar) myToolBars.get(i);
                final int barDepth = getPreferredToolBarDepth(toolbar);
                depth = Math.max(depth, barDepth);

            }

            return depth;
        }

        public int getDepth() {
            if (getOrientation() == ToolBarLayout.HORIZONTAL)
                return height;
            else
                return width;
        }

        public int getDockIndex(final Point p) {
            for (int i = 0; i < myToolBars.size(); i++) {
                final JToolBar toolbar = (JToolBar) myToolBars.get(i);
                if (toolbar.getBounds().contains(p))
                    return i;
            }

            return ToolBarLayout.MAX;
        }

        public int getDockIndex(final JToolBar toolbar) {
            return myToolBars.indexOf(toolbar);
        }
    }

}