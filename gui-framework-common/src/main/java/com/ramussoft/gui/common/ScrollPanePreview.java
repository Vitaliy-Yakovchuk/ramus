package com.ramussoft.gui.common;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public class ScrollPanePreview extends JComponent {
    /**
     *
     */
    private static final long serialVersionUID = -7989475676016126751L;
    // static final fields
    private static final double MAX_SIZE = 200;

    private static final Icon LAUNCH_SELECTOR_ICON = new Icon() {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color tmpColor = g.getColor();
            g.setColor(Color.BLACK);
            g.drawRect(2, 2, 10, 10);
            g.drawRect(4, 5, 6, 4);
            g.setColor(tmpColor);
        }

        public int getIconWidth() {
            return 15;
        }

        public int getIconHeight() {
            return 15;
        }
    };
    private static final String COMPONENT_ORIENTATION = "componentOrientation";

    // member fields
    private LayoutManager theFormerLayoutManager;
    private JScrollPane theScrollPane;
    private JComponent theComponent;
    private JPopupMenu thePopupMenu;
    private JComponent theButton;
    private BufferedImage theImage;
    private Rectangle theStartRectangle;
    private Rectangle theRectangle;
    private Point theStartPoint;
    private double theScale;
    private PropertyChangeListener theComponentOrientationListener;
    private ContainerAdapter theViewPortViewListener;

    /**
     * Installs a ScrollPaneSelector to the given JScrollPane instance.
     *
     * @param aScrollPane
     */
    public synchronized static void install(
            JScrollPane aScrollPane) {
        if (aScrollPane == null)
            return;
        ScrollPanePreview scrollPaneSelector = new ScrollPanePreview();
        scrollPaneSelector.installOnScrollPane(aScrollPane);
    }

    /**
     * Removes the ScrollPaneSelector from the given JScrollPane.
     *
     * @param aScrollPane
     */
    public synchronized static void uninstall(
            JScrollPane aScrollPane) {
        if (aScrollPane == null)
            return;
/*		ScrollPanePreview scrollPaneSelector = theInstalledScrollPaneSelectors
                .get(aScrollPane);
		if (scrollPaneSelector == null)
			return;
		scrollPaneSelector.uninstallFromScrollPane();
		theInstalledScrollPaneSelectors.remove(aScrollPane);*/
    }

    // -- Constructor ------
    private ScrollPanePreview() {
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        theScrollPane = null;
        theImage = null;
        theStartRectangle = null;
        theRectangle = null;
        theStartPoint = null;
        theScale = 0.0;
        theButton = new JLabel(LAUNCH_SELECTOR_ICON);
        MouseInputListener mil = new MouseInputAdapter() {
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                SwingUtilities.convertPointToScreen(p, theButton);
                display(p);
            }

            public void mouseReleased(MouseEvent e) {
                if (theStartPoint != null) {
                    Point newPoint = e.getPoint();
                    SwingUtilities.convertPointToScreen(newPoint, (Component) e
                            .getSource());
                    int deltaX = (int) ((newPoint.x - theStartPoint.x) / theScale);
                    int deltaY = (int) ((newPoint.y - theStartPoint.y) / theScale);
                    scroll(deltaX, deltaY);
                }
                theStartPoint = null;
                theStartRectangle = theRectangle;
            }

            public void mouseDragged(MouseEvent e) {
                if (theStartPoint == null)
                    return;
                Point newPoint = e.getPoint();
                SwingUtilities.convertPointToScreen(newPoint, (Component) e
                        .getSource());
                moveRectangle(newPoint.x - theStartPoint.x, newPoint.y
                        - theStartPoint.y);
            }
        };
        theButton.addMouseListener(mil);
        theButton.addMouseMotionListener(mil);
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        thePopupMenu = new JPopupMenu();
        thePopupMenu.setLayout(new BorderLayout());
        thePopupMenu.add(this, BorderLayout.CENTER);
        theComponentOrientationListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (theScrollPane == null)
                    return;
                theScrollPane.setCorner(JScrollPane.LOWER_LEADING_CORNER, null);
                theScrollPane.setCorner(JScrollPane.LOWER_TRAILING_CORNER,
                        theButton);
            }
        };
        theViewPortViewListener = new ContainerAdapter() {
            public void componentAdded(ContainerEvent e) {
                if (thePopupMenu.isVisible())
                    thePopupMenu.setVisible(false);
                Component comp = theScrollPane.getViewport().getView();
                theComponent = (comp instanceof JComponent) ? (JComponent) comp
                        : null;
            }
        };
    }

    // -- JComponent overriden methods ------
    public Dimension getPreferredSize() {
        if (theImage == null || theRectangle == null)
            return new Dimension();
        Insets insets = getInsets();
        return new Dimension(theImage.getWidth(null) + insets.left
                + insets.right, theImage.getHeight(null) + insets.top
                + insets.bottom);
    }

    protected void paintComponent(Graphics g1D) {
        if (theImage == null || theRectangle == null)
            return;
        Graphics2D g = (Graphics2D) g1D;
        Insets insets = getInsets();
        int xOffset = insets.left;
        int yOffset = insets.top;
        int availableWidth = getWidth() - insets.left - insets.right;
        int availableHeight = getHeight() - insets.top - insets.bottom;
        g.drawImage(theImage, xOffset, yOffset, null);
        Color tmpColor = g.getColor();
        Area area = new Area(new Rectangle(xOffset, yOffset, availableWidth,
                availableHeight));
        area.subtract(new Area(theRectangle));
        g.setColor(new Color(200, 200, 200, 128));
        g.fill(area);
        g.setColor(Color.BLACK);
        g.draw(theRectangle);
        g.setColor(tmpColor);
    }

    // -- Private methods ------
    private void installOnScrollPane(JScrollPane aScrollPane) {
        if (theScrollPane != null)
            uninstallFromScrollPane();
        theScrollPane = aScrollPane;
        theFormerLayoutManager = theScrollPane.getLayout();
        theScrollPane.setLayout(new ScrollPanePreviewLayout());
        theScrollPane.addPropertyChangeListener(COMPONENT_ORIENTATION,
                theComponentOrientationListener);
        theScrollPane.getViewport().addContainerListener(
                theViewPortViewListener);
        theScrollPane.setCorner(JScrollPane.LOWER_TRAILING_CORNER, theButton);
        Component comp = theScrollPane.getViewport().getView();
        theComponent = (comp instanceof JComponent) ? (JComponent) comp : null;
    }

    private void uninstallFromScrollPane() {
        if (theScrollPane == null)
            return;
        if (thePopupMenu.isVisible())
            thePopupMenu.setVisible(false);
        theScrollPane.setCorner(JScrollPane.LOWER_TRAILING_CORNER, null);
        theScrollPane.removePropertyChangeListener(COMPONENT_ORIENTATION,
                theComponentOrientationListener);
        theScrollPane.getViewport().removeContainerListener(
                theViewPortViewListener);
        theScrollPane.setLayout(theFormerLayoutManager);
        theScrollPane = null;
    }

    private void display(Point aPointOnScreen) {
        if (theComponent == null) return;
        double compWidth = theComponent.getWidth();
        double compHeight = theComponent.getHeight();
        double scaleX = MAX_SIZE / compWidth;
        double scaleY = MAX_SIZE / compHeight;
        theScale = Math.min(scaleX, scaleY);
        int scaledWidth = (int) (theComponent.getWidth() * theScale);
        int scaledHeight = (int) (theComponent.getHeight() * theScale);
        theImage = new BufferedImage(scaledWidth,
                scaledHeight,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = theImage.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


        g.scale(theScale, theScale);
        theComponent.paint(g);
        theStartRectangle = theComponent.getVisibleRect();
        Insets insets = getInsets();
        theStartRectangle.x = (int) (theScale * theStartRectangle.x + insets.left);
        theStartRectangle.y = (int) (theScale * theStartRectangle.y + insets.right);
        theStartRectangle.width *= theScale;
        theStartRectangle.height *= theScale;
        theRectangle = theStartRectangle;

        Dimension pref = thePopupMenu.getPreferredSize();
        Point buttonLocation = theButton.getLocationOnScreen();
        Point popupLocation = new Point((theButton.getWidth() - pref.width) / 2,
                (theButton.getHeight() - pref.height) / 2);
        Point centerPoint = new Point(buttonLocation.x + popupLocation.x + theRectangle.x + theRectangle.width / 2,
                buttonLocation.y + popupLocation.y + theRectangle.y + theRectangle.height / 2);
        boolean movedByRobot = false;
        try {
            // Attempt to move the mouse pointer to the center of the selector's rectangle.
            new Robot().mouseMove(centerPoint.x, centerPoint.y);
            movedByRobot = true;
            theStartPoint = centerPoint;
        } catch (Exception e) {
            // Since we cannot move the cursor, we'll move the popup instead.
            theStartPoint = aPointOnScreen;
            e.printStackTrace();
            popupLocation.x += theStartPoint.x - centerPoint.x;
            popupLocation.y += theStartPoint.y - centerPoint.y;
        }
        thePopupMenu.show(theButton, popupLocation.x, popupLocation.y);
        if (movedByRobot) {
            Point newPopupLocationOnScreen = thePopupMenu.getLocationOnScreen();
            Point expectedPopupLocationOnScreen = new Point(buttonLocation);
            expectedPopupLocationOnScreen.translate(popupLocation.x, popupLocation.y);
            if (!newPopupLocationOnScreen.equals(expectedPopupLocationOnScreen)) {
                // popup not at expected place, have to move mouseCursors
                Point newCenterPoint = new Point(centerPoint.x + (newPopupLocationOnScreen.x - expectedPopupLocationOnScreen.x), centerPoint.y + (newPopupLocationOnScreen.y - expectedPopupLocationOnScreen.y));
                try {
                    new Robot().mouseMove(newCenterPoint.x, newCenterPoint.y);
                    movedByRobot = true;
                    theStartPoint = newCenterPoint;
                } catch (AWTException e) {
                    // we can not do anything anymore, position of cursor is now wrong
                }
            }
        }
    }


    private void moveRectangle(int aDeltaX, int aDeltaY) {
        if (theStartRectangle == null)
            return;

        Insets insets = getInsets();
        Rectangle newRect = new Rectangle(theStartRectangle);
        newRect.x += aDeltaX;
        newRect.y += aDeltaY;
        newRect.x = Math.min(Math.max(newRect.x, insets.left), getWidth()
                - insets.right - newRect.width);
        newRect.y = Math.min(Math.max(newRect.y, insets.right), getHeight()
                - insets.bottom - newRect.height);
        Rectangle clip = new Rectangle();
        Rectangle.union(theRectangle, newRect, clip);
        clip.grow(2, 2);
        theRectangle = newRect;
        paintImmediately(clip);
    }

    private void scroll(int aDeltaX, int aDeltaY) {
        if (theComponent == null)
            return;
        Rectangle rect = theComponent.getVisibleRect();
        rect.x += aDeltaX;
        rect.y += aDeltaY;
        theComponent.scrollRectToVisible(rect);
        thePopupMenu.setVisible(false);
    }
}