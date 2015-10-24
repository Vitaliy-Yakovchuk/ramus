/*
 *  ToolBarHandler.java
 *  2004-01-10
 */

package com.ramussoft.pb.frames.dlayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;

class Handler {

    private Constraints ourConstraints = new Constraints();

    private JToolBar ourToolBar = null;
    private ToolBarLayout ourDockLayout = null;
    private JDialog ourFloatFrame = null;
    private DraggingWindow ourDraggingWindow = null;

    private ToolBarDragListener ourDragListener = null;
    private UIChangeListener ourUIListener = null;

    private boolean ourToolBarIsDragging = false;
    private boolean ourToolBarShouldFloat = false;
    private static boolean ourVersionIsCompatible = false;

    public static final String TOOL_BAR_HANDLER_KEY = "ToolBarHandler";

    /**
     * Creates a ToolBarHandler for the specified toolbar to be docked within
     * the specified container.
     */
    public Handler(final JToolBar toolbar, final ToolBarLayout layout) {
        ourToolBar = toolbar;
        ourDockLayout = layout;

        // Initially the value of the static variable ourVersionIsCompatible is
        // false so that for compatible versions (1.3 and above) this check is
        // only performed once...
        if (!ourVersionIsCompatible) {
            final String specVersion = System
                    .getProperty("java.specification.version");
            try {
                final float ver = Float.parseFloat(specVersion);
                if (ver > 1.2f)
                    ourVersionIsCompatible = true;
            } catch (final Exception ex) {
                // Assume the version is not high enough,
                // leave ourVersionIsCompatible = false
            }
        }

        if (ourVersionIsCompatible) {
            ourDragListener = new ToolBarDragListener();
            ourUIListener = new UIChangeListener();
            installListeners();
        }
    }

    /**
     * Sets the DockingConstraints for this handler representing the edge and
     * indicies at which this handler's toolbar should be docked.
     */
    public void setConstraints(final Constraints constraints) {
        ourConstraints = constraints;
    }

    /**
     * Returns this handler's DockingConstraints representing the edge and
     * indicies at which this handler's toolbar should be docked.
     */
    public Constraints getConstraints() {
        return ourConstraints;
    }

    /**
     * Gets the edge in which the assocated toolbar should be docked.
     */
    public int getDockEdge() {
        return ourConstraints.getEdge();
    }

    /**
     * Sets the edge in which the associated toolbar should be docked.
     */
    public void setDockEdge(final int edge) {
        ourConstraints.setEdge(edge);

    }

    /**
     * Gets the index at which the associated toolbar should be docked within
     * its edge.
     */
    public int getDockIndex() {
        return ourConstraints.getIndex();
    }

    /**
     * Sets the index at which the associated toolbar should be docked within
     * its edge.
     */
    public void setDockIndex(final int index) {
        ourConstraints.setIndex(index);
    }

    /**
     * Gets the row index at which the associated toolbar should be docked
     * within its edge. Not all dock boundary styles will support control over
     * the row index.
     */
    public int getRowIndex() {
        return ourConstraints.getRow();
    }

    /**
     * Sets the row index at which the associated toolbar should be docked
     * within its edge. Not all dock boundary styles will support control over
     * the row index.
     */
    public void setRowIndex(final int index) {
        ourConstraints.setRow(index);
    }

    /**
     * Hides the associated toolbar by removing it from its dock or by closing
     * its client floating frame.
     */
    public void hideToolBar() {
        final Container target = ourDockLayout.getTargetContainer();
        target.remove(ourToolBar);
        final JDialog floatFrame = getFloatingFrame();
        if (floatFrame != null) {
            floatFrame.setVisible(false);
            floatFrame.getContentPane().remove(ourToolBar);
        }

        target.validate();
        target.repaint();
    }

    /**
     * Docks the associated toolbar at its last visible location.
     */
    public void dockToolBar() {
        dockToolBar(getDockEdge());
    }

    /**
     * Docks the associated toolbar at the specified edge.
     */
    public void dockToolBar(final int edge) {
        dockToolBar(edge, getRowIndex(), getDockIndex());
    }

    /**
     * Docks the associated toolbar at the secified edge and indicies.
     */
    public void dockToolBar(final int edge, final int row, final int index) {
        final Container target = ourDockLayout.getTargetContainer();
        if (target == null)
            return;

        target.remove(ourToolBar);
        final JDialog floatFrame = getFloatingFrame();
        if (floatFrame != null) {
            floatFrame.setVisible(false);
            floatFrame.getContentPane().remove(ourToolBar);
        }

        ourConstraints.setEdge(edge);
        ourConstraints.setRow(row);
        ourConstraints.setIndex(index);

        target.add(ourToolBar, ourConstraints);
        ourToolBarShouldFloat = false;

        target.validate();
        target.repaint();
    }

    /**
     * Floats the associated toolbar at its natural location.
     */
    public void floatToolBar() {
        final Point p = getFloatingLocation();
        floatToolBar(p.x, p.y);
    }

    /**
     * Floats the associated toolbar at the specified screen location.
     */
    public void floatToolBar(final int x, final int y) {
        floatToolBar(x, y, false);
    }

    /**
     * Floats the associated toolbar at the specified screen location,
     * optionally centering the floating frame on this point.
     */
    public void floatToolBar(int x, int y, final boolean center) {
        final JDialog floatFrame = getFloatingFrame();
        if (floatFrame == null)
            return;

        final Container target = ourDockLayout.getTargetContainer();
        if (target != null)
            target.remove(ourToolBar);
        floatFrame.setVisible(false);
        floatFrame.getContentPane().remove(ourToolBar);

        ourToolBar.setOrientation(ToolBarLayout.HORIZONTAL);
        floatFrame.getContentPane().add(ourToolBar, BorderLayout.CENTER);
        floatFrame.pack();

        if (center) {
            x -= floatFrame.getWidth() / 2;
            y -= floatFrame.getHeight() / 2;
        }

        // x and y are given relative to screen
        floatFrame.setLocation(x, y);
        floatFrame.setTitle(ourToolBar.getName());
        floatFrame.setVisible(true);

        ourToolBarShouldFloat = true;

        if (target != null) {
            target.validate();
            target.repaint();
        }
    }

    /**
     * Gets the screen location of the associated toolbar's floating frame.
     */
    public Point getFloatingLocation() {
        final JDialog floatFrame = getFloatingFrame();
        if (floatFrame != null)
            return floatFrame.getLocation();
        else
            return new Point(0, 0);
    }

    /**
     * Sets the screen location of the associated toolbar's floating frame.
     */
    public void setFloatingLocation(final int x, final int y) {
        final JDialog floatFrame = getFloatingFrame();
        if (floatFrame != null)
            floatFrame.setLocation(x, y);
    }

    /**
     * Determines whether or not the associated toolbar should be floated.
     */
    public boolean shouldFloat() {
        return ourToolBarShouldFloat;
    }

    /**
     * Stores whether or not the associated toolbar should be floated.
     */
    public void setShouldFloat(final boolean shouldFloat) {
        ourToolBarShouldFloat = shouldFloat;
    }

    /**
     * Gets the nearest dockable DockBoundary for the specified point, or
     * returns null if the point is not a dockable location.
     */
    private DockBoundary getDockableBoundary(final Point point) {
        return ourDockLayout.getDockableBoundary(point);
    }

    /**
     * Checks to see if the DockBoundary containing the provided toolbar wishes
     * to veto the drag operation at the provided point. Some DockBoundaries may
     * manipulate the positions of the toolbars without this handler having to
     * undock and redock the toolbar.
     */
    private boolean isDraggable(final Point point, final JToolBar toolbar) {
        if (toolbar == null)
            return false;

        final DockBoundary boundary = ourDockLayout.getBoundary(point);
        if (boundary == null)
            return true;

        else if (boundary.containsToolBar(toolbar))
            return boundary.isDraggablePoint(point, toolbar);

        else
            return true;
    }

    /**
     * Returns the client frame for supporting the floating toolbar.
     */
    private JDialog getFloatingFrame() {
        if (ourFloatFrame == null) {
            final Window w = SwingUtilities.getWindowAncestor(ourDockLayout
                    .getTargetContainer());

            if (w == null)
                return null;

            Frame fr = null;
            if (w instanceof Frame)
                fr = (Frame) w;
            ourFloatFrame = new JDialog(fr);
            ourFloatFrame
                    .setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            ourFloatFrame.addWindowListener(new FloatFrameCloseListener());
            ourFloatFrame.getContentPane().setLayout(new BorderLayout());
            ourFloatFrame.setTitle(ourToolBar.getName());
            ourFloatFrame.setResizable(false);
        }

        return ourFloatFrame;
    }

    /**
     * Returns a temporary window to display which represents the estimated
     * bounds of the toolbar while it is being dragged.
     */
    private DraggingWindow getDraggingWindow() {
        if (ourDraggingWindow == null) {
            final Window w = SwingUtilities.getWindowAncestor(ourDockLayout
                    .getTargetContainer());

            if (w != null)
                ourDraggingWindow = new DraggingWindow(w);
        }

        return ourDraggingWindow;
    }

    /**
     * Releases this handler's listeners from the associated toolbar.
     */
    public void uninstallListeners() {
        // May want to restore the original listeners that were
        // stripped from the toolbar when this handler was created.
        ourToolBar.removeMouseListener(ourDragListener);
        ourToolBar.removeMouseMotionListener(ourDragListener);
        ourToolBar.removePropertyChangeListener(ourUIListener);
    }

    /**
     * Strips off the UI's mouse listeners attached to the associated toolbar
     * and replaces them with this handler's listeners.
     */
    private void installListeners() {
        if (!ourVersionIsCompatible)
            return;

        ourToolBar.removePropertyChangeListener("UI", ourUIListener);

        // Uninstall the current ui, collect the remaining listeners
        // on the toolbar, and reinstall the ui...
        final ComponentUI ui = ourToolBar.getUI();
        ui.uninstallUI(ourToolBar);
        final java.util.List mList = Arrays.asList(ourToolBar
                .getListeners(MouseListener.class));

        final java.util.List mmList = Arrays.asList(ourToolBar
                .getListeners(MouseMotionListener.class));
        ui.installUI(ourToolBar);

        // ...then remove the listeners that were added by the ui...
        final MouseListener[] ml = ourToolBar
                .getListeners(MouseListener.class);
        final MouseMotionListener[] mml = ourToolBar
                .getListeners(MouseMotionListener.class);

        for (int i = 0; i < ml.length; i++) {
            if (!mList.contains(ml[i]))
                ourToolBar.removeMouseListener(ml[i]);
        }

        for (int i = 0; i < mml.length; i++) {
            if (!mmList.contains(mml[i]))
                ourToolBar.removeMouseMotionListener(mml[i]);
        }

        // ...and add our listeners to the toolbar.
        ourToolBar.addMouseListener(ourDragListener);
        ourToolBar.addMouseMotionListener(ourDragListener);
        ourToolBar.addPropertyChangeListener("UI", ourUIListener);
    }

    /**
     * Inner class that monitors the float frame for closing.
     */
    private class FloatFrameCloseListener extends WindowAdapter {
        @Override
        public void windowClosing(final WindowEvent we) {
            dockToolBar(getDockEdge(), getRowIndex(), getDockIndex());
        }
    }

    /**
     * Inner class that listens for changes in the UI on the associated toolbar.
     * Insures that when a new UI is installed, its listeners are replaced.
     */
    private class UIChangeListener implements PropertyChangeListener {
        public void propertyChange(final PropertyChangeEvent pce) {
            installListeners();
        }
    }

    /**
     * Inner class that replaces the associated toolbar's mouse listeners and
     * handles the drag and drop behavior.
     */
    private class ToolBarDragListener extends MouseInputAdapter {
        @Override
        public void mouseDragged(final MouseEvent me) {
            ourDraggingWindow = getDraggingWindow();
            if (ourDraggingWindow == null)
                return;

            // Only allow Button 1 to perform the drag...
            if ((me.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK) {
                hideDraggingWindow();
                return;
            }

            final Container target = ourDockLayout.getTargetContainer();
            if (target == null)
                return;

            Point p = me.getPoint();
            p = SwingUtilities.convertPoint(ourToolBar, p, target);

            // Make sure the DockBoundary containing this point
            // and this toolbar wishes to allow the drag operation
            // to commence or continue...
            if (!isDraggable(p, ourToolBar)) {
                hideDraggingWindow();
                return;
            }

            // Determine if this point lies within a
            // DockBoundary's dockable range...
            int orient = ToolBarLayout.HORIZONTAL;
            boolean dockable = false;

            if (!me.isControlDown()) {
                final DockBoundary dock = getDockableBoundary(p);

                if (dock != null) {
                    dockable = true;
                    orient = dock.getOrientation();
                }
            }

            // Present the dragging window at this point on the screen...
            SwingUtilities.convertPointToScreen(p, target);
            ourDraggingWindow.presentWindow(p, dockable, orient);
            ourToolBarIsDragging = true;

        }

        @Override
        public void mouseReleased(final MouseEvent me) {
            if (!ourToolBarIsDragging)
                return;

            if ((me.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK) {
                return;
            }

            hideDraggingWindow();

            final Container target = ourDockLayout.getTargetContainer();
            if (target == null)
                return;

            Point p = me.getPoint();
            p = SwingUtilities.convertPoint(ourToolBar, p, target);

            DockBoundary dock = null;

            if (!me.isControlDown()) {
                dock = getDockableBoundary(p);
                if (dock != null) {
                    setDockIndex(dock.getDockIndex(p));
                    setRowIndex(dock.getRowIndex(p));
                    setDockEdge(dock.getEdge());
                }
            }

            if (dock != null) {
                dockToolBar(getDockEdge(), getRowIndex(), getDockIndex());
            } else {
                SwingUtilities.convertPointToScreen(p, target);
                floatToolBar(p.x, p.y, true);
            }

        }

        private void hideDraggingWindow() {
            if (ourDraggingWindow != null) {
                ourDraggingWindow.hideWindow();
                ourToolBarIsDragging = false;
            }
        }

    }

    /**
     * Inner class that represents the bounds of the associated toolbar while
     * dragging.
     */
    private class DraggingWindow extends JWindow {
        private Border myFloatBorder = null;
        private Border myDockBorder = null;
        private final Color myFloatColor = null;
        private final Color myDockColor = null;
        private JPanel myContent = null;

        public DraggingWindow(final Window ancestor) {
            super(ancestor);

            Color myFloatColor = UIManager
                    .getColor("ToolBar.floatingForeground");
            Color myDockColor = UIManager.getColor("ToolBar.dockingForeground");
            if (myFloatColor == null)
                myFloatColor = Color.darkGray;
            if (myDockColor == null)
                myDockColor = Color.yellow;

            myFloatBorder = BorderFactory.createLineBorder(myFloatColor, 3);
            myDockBorder = BorderFactory.createLineBorder(myDockColor, 3);

            myContent = new JPanel();
            myContent.setOpaque(true);

            myFloatColor = UIManager.getColor("ToolBar.floatingBackground");
            myDockColor = UIManager.getColor("ToolBar.dockingBackground");
            if (myFloatColor == null)
                myFloatColor = myContent.getBackground();
            if (myDockColor == null)
                myDockColor = myContent.getBackground();

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(myContent, BorderLayout.CENTER);
        }

        // Present the window at the given point for the given dockability
        // at the given dock orientation...
        public void presentWindow(final Point screenPoint, final boolean dockable,
                                  final int dockOrientation) {
            setCentroidLocation(screenPoint);

            // int tbOrientation = ourToolBar.getOrientation();
            final int orientation = dockable ? dockOrientation
                    : ToolBarLayout.HORIZONTAL;

            setSize(getPreferredToolBarSize(orientation));

            myContent.setBorder(dockable ? myDockBorder : myFloatBorder);
            myContent.setBackground(dockable ? myDockColor : myFloatColor);

            setCentroidLocation(screenPoint);

            validate();
            if (!isVisible())
                setVisible(true);
        }

        public void hideWindow() {
            setVisible(false);
        }

        private void setCentroidLocation(final Point screenPoint) {
            setLocation(screenPoint.x - getWidth() / 2, screenPoint.y
                    - getHeight() / 2);
        }

        private Dimension getPreferredToolBarSize(final int orientation) {
            final Component[] comps = ourToolBar.getComponents();
            int w = 0, h = 0;
            for (final Component element : comps) {
                final Dimension d = element.getPreferredSize();
                if (orientation == ToolBarLayout.HORIZONTAL) {
                    w += d.width;
                    h = Math.max(h, d.height);
                } else {
                    w = Math.max(w, d.width);
                    h += d.height;
                }
            }

            return new Dimension(w, h);
        }

    }
}