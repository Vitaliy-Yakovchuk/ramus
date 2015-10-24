/*
 *  WrappingDockBoundary.java
 *  2004-01-02
 */

package com.ramussoft.pb.frames.dlayout;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;

import javax.swing.JToolBar;

/**
 * A DockBoundary that automatically wraps the docked toolbars when they don't
 * all fit on the same row.
 *
 * @author Christopher Bach
 */
// package access only...
class DockWrapper extends DockBoundary {

    private boolean ourLayoutReflects = false;

    /**
     * Creates a WrappingDockBoundary for the specified edge.
     */
    public DockWrapper(final int edge) {
        super(edge);
        ourLayoutReflects = edge == ToolBarLayout.SOUTH || edge == ToolBarLayout.EAST;
    }

    /**
     * Creates a WrappingDockBoundary for the specified edge with the provided
     * spacing.
     */
    public DockWrapper(final int edge, final int spacing) {
        super(edge, spacing);
        ourLayoutReflects = edge == ToolBarLayout.SOUTH || edge == ToolBarLayout.EAST;
    }

    /**
     * Implementation of the abstract superclass method, returns the index at
     * which the toolbar should be added when dropped at the specified point.
     */
    @Override
    public int getDockIndex(final Point p) {
        final JToolBar[] toolbars = getToolBars();

        for (int i = 0; i < toolbars.length; i++) {
            if (toolbars[i].getBounds().contains(p)) {
                return i;
            }
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
        return Arrays.asList(getToolBars()).indexOf(toolbar);
    }

    /**
     * Implementation of the abstract superclass method, returns the row index
     * at which the toolbar should be added when dropped at the specified point.
     */
    @Override
    public int getRowIndex(final Point p) {
        return 0;
    }

    /**
     * Implementation of the abstract superclass method, returns the row index
     * of the specified toolbar in this boundary, or -1 if the toolbar is not
     * present.
     */
    @Override
    public int getRowIndex(final JToolBar toolbar) {
        if (Arrays.asList(getToolBars()).contains(toolbar))
            return 0;
        else
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

        int length = 0;
        if (orientation == ToolBarLayout.HORIZONTAL)
            length = width;
        else
            length = height;

        final JToolBar[] bars = getToolBars();
        final int barDepth = getPreferredDepth();
        int barLength = 0;
        int totalBarDepth = barDepth;
        int totalBarLength = 0;

        for (int i = 0; i < bars.length; i++) {
            final JToolBar toolbar = bars[i];

            barLength = getPreferredToolBarLength(toolbar);

            if (totalBarLength != 0)
                totalBarLength += spacing;

            setToolBarBounds(toolbar, totalBarLength, totalBarDepth - barDepth,
                    Math.min(barLength, length), barDepth);

            totalBarLength += barLength;

            if (totalBarLength > length && i > 0) {
                // Need to move the current toolbar to the
                // next row and start totalling the length anew.
                totalBarDepth += barDepth + spacing;
                totalBarLength = 0;
                setToolBarBounds(toolbar, totalBarLength, totalBarDepth
                        - barDepth, Math.min(barLength, length), barDepth);
                totalBarLength = barLength;
            }
        }

        setDepth(totalBarDepth);
    }

    /**
     * Returns the largest preferred height or width (depending on orientation)
     * of all of the associated toolbars.
     */
    private int getPreferredDepth() {
        int depth = 0;

        final JToolBar[] toolbars = super.getToolBars();

        for (final JToolBar toolbar : toolbars) {
            final Dimension d = toolbar.getPreferredSize();
            if (getOrientation() == ToolBarLayout.HORIZONTAL)
                depth = Math.max(depth, d.height);
            else
                depth = Math.max(depth, d.width);
        }

        return depth;
    }

    /**
     * Sets the bounds of the provided toolbar based on the provided bounds
     * parameters (given relative to this boundary's origin) accounting for the
     * default wrapping direction for this boundary.
     */
    private void setToolBarBounds(final JToolBar toolbar, final int lengthOffset,
                                  final int depthOffset, final int length, final int depth) {
        if (getOrientation() == ToolBarLayout.HORIZONTAL) {
            toolbar.setBounds(x + lengthOffset, y + depthOffset, length, depth);

            if (ourLayoutReflects) {
                final Rectangle r = toolbar.getBounds();
                mirrorBounds(r, y);
                toolbar.setBounds(r);
            }
        } else {
            toolbar.setBounds(x + depthOffset, y + lengthOffset, depth, length);

            if (ourLayoutReflects) {
                final Rectangle r = toolbar.getBounds();
                mirrorBounds(r, x);
                toolbar.setBounds(r);
            }
        }
    }

}