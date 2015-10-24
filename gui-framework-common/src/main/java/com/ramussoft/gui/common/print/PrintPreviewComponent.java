package com.ramussoft.gui.common.print;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.ramussoft.gui.common.AdditionalGUIPluginLoader;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.PrintPlugin;
import com.ramussoft.gui.common.prefrence.Options;

public abstract class PrintPreviewComponent extends JComponent {

    /**
     *
     */
    private static final long serialVersionUID = 6459969437762162708L;

    private static final int PREV_LAYOUT_GRID = 0;

    private static final int PREV_LAYOUT_COL = 1;

    private static final double W_SPACE = 40;

    private double zoom = 1d;

    private RamusPrintable printable;

    private int columnCount;

    private Page[] pages;

    private int rowCount;

    private double width;

    private double height;

    private double pageWidth;

    private double pageHeight;

    private JComboBox box;

    private int layout = Options.getInteger("PREVIW_LAYOUT", PREV_LAYOUT_GRID);

    private PrintAction printAction = new PrintAction();

    private PageSetupAction pageSetupAction = new PageSetupAction();

    private LayoutCol layoutColAction = new LayoutCol();

    private LayoutGrid layoutGridAction = new LayoutGrid();

    private GUIFramework framework;

    private boolean reverce = false;

    private JLabel pageOf;

    public PrintPreviewComponent(RamusPrintable printable, int columnCount,
                                 GUIFramework framework) {
        this.printable = printable;
        this.columnCount = columnCount;
        this.framework = framework;
        MouseWheelListener l = new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                    if (e.getModifiers() == KeyEvent.CTRL_MASK) {
                        double r = e.getWheelRotation();
                        double zoom = getZoom() - 0.2 * r;
                        setCurrentZoom(zoom);
                    } else {
                        Rectangle rect = getVisibleRect();
                        scrollRectToVisible(new Rectangle(rect.x, rect.y
                                + e.getWheelRotation() * 150, rect.width,
                                rect.height));
                    }
                }
            }
        };
        this.addMouseWheelListener(l);
        layout = Options.getInteger("PREVIW_LAYOUT", PREV_LAYOUT_GRID);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setCurrentZoom(Options.getDouble("PREV_ZOOM", 1d));
            }
        });
        setCurrentZoom(Options.getDouble("PREV_ZOOM", 1d));
    }

    protected void setCurrentZoom(double zoom) {
        if (zoom < 0.1)
            zoom = 0.1;
        if (zoom > 10)
            zoom = 10;
        if (layout == PREV_LAYOUT_GRID)
            setFitZoom(zoom, getAreaSize());
        else
            setup(1, zoom);
    }

    public void setup(int columnCount, double zoom) {
        this.columnCount = columnCount;
        this.setZoom(zoom);
        this.setSize();
        revalidate();
        repaint();
    }

    private void setSize() {
        int pageCount = printable.getPageCount();
        rowCount = (pageCount - 1) / columnCount + 1;
        pageWidth = 0;
        pageHeight = 0;
        pages = new Page[pageCount];
        PageFormat pageFormat = printable.getPageFormat();
        for (int i = 0; i < pageCount; i++) {
            pageFormat = printable.getPageFormat(pageFormat, i);
            double w = pageFormat.getWidth() + 1;
            double h = pageFormat.getHeight() + 1;
            double iW = pageFormat.getImageableWidth();
            double iH = pageFormat.getImageableHeight();
            double x = pageFormat.getImageableX();
            double y = pageFormat.getImageableY();

            reverce = (pageFormat.getOrientation() == PageFormat.REVERSE_LANDSCAPE);

			/*
             * if (pageFormat.getOrientation() == PageFormat.LANDSCAPE) { double
			 * t;
			 * 
			 * t = w; w = h; h = t;
			 * 
			 * t = iW; iW = iH; iH = t;
			 * 
			 * t = x; x = y; y = t; }
			 */

            Page page = new Page(w, h, x, y, iW, iH);

            if (pageWidth < w)
                pageWidth = w;
            if (pageHeight < h)
                pageHeight = h;
            pages[i] = page;
        }
        width = (columnCount - 1) * (pageWidth + W_SPACE / zoom) + pageWidth;
        height = rowCount * (pageHeight + W_SPACE / zoom);
        Dimension size = new Dimension((int) (width * getZoom()),
                (int) (height * getZoom()));
        this.setSize(size);
        this.setPreferredSize(size);
    }

    /**
     * @param columnCount the columnCount to set
     */
    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    /**
     * @return the columnCount
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * @return the printable
     */
    public RamusPrintable getPrintable() {
        return printable;
    }

    public Dimension2D getPageSize() {
        int pageCount = printable.getPageCount();
        double pageWidth = 0;
        double pageHeight = 0;
        PageFormat pageFormat = new PageFormat();
        for (int i = 0; i < pageCount; i++) {
            pageFormat = printable.getPageFormat(pageFormat, i);
            double w = pageFormat.getWidth();
            double h = pageFormat.getHeight();

            if (pageWidth < w)
                pageWidth = w;
            if (pageHeight < h)
                pageHeight = h;
        }

        final double fw = pageWidth;
        final double fh = pageHeight;
        return new Dimension2D() {

            @Override
            public void setSize(double width, double height) {
            }

            @Override
            public double getWidth() {
                return fw;
            }

            @Override
            public double getHeight() {
                return fh;
            }
        };
    }

    public void setFitZoom(Dimension size) {
        Dimension2D pageSize = getPageSize();
        int pageCount = printable.getPageCount();
        if (pageCount == 0)
            return;
        double xy = (pageSize.getWidth() + W_SPACE)
                * (pageSize.getHeight() + W_SPACE) * (pageCount + 1);
        double mxy = size.getWidth() * size.getHeight();
        double zoom = Math.sqrt(mxy / xy);
        int columnCount = (int) (size.getWidth() / ((pageSize.getWidth() + W_SPACE
                / zoom) * zoom));
        if (columnCount <= 0)
            columnCount = 1;
        if (columnCount > pageCount)
            columnCount = pageCount;
        setup(columnCount, zoom);
    }

    public void setFitZoom(double zoom, Dimension size) {
        if (zoom > 10)
            zoom = 10;
        if (zoom < 0.1)
            zoom = 0.1;
        int columnCount = 1;
        Dimension2D pageSize = getPageSize();
        while (((pageSize.getWidth() + W_SPACE / zoom) * columnCount + pageSize
                .getWidth()) * zoom < size.getWidth()) {
            columnCount++;
        }
        setup(columnCount, zoom);
    }

    public void setOnePageZoom(Dimension size) {
        Dimension2D pageSize = getPageSize();
        int pageCount = printable.getPageCount();
        if (pageCount == 0)
            return;
        double zoom = size.getWidth() / (pageSize.getWidth());
        int columnCount = 1;
        setup(columnCount, zoom);
    }

    @Override
    public void paint(Graphics gr) {
        super.paint(gr);
        Graphics2D g = (Graphics2D) gr;
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle rect = g.getClipBounds();
        int pageIndex = 0;
        int row = 0;
        int column = 0;

        Dimension size = getSize();

        if (width * getZoom() < size.getWidth()) {
            g.translate((size.getWidth() - width * getZoom()) / 2, 0);
        }

        AffineTransform at = g.getTransform();
        Font font = new Font("Dialog", Font.BOLD, 12);

        for (Page page : pages) {
            if (page.contains(rect, row, column)) {
                if (pageOf != null) {
                    String text = MessageFormat.format(
                            GlobalResourcesManager.getString("PageOf"),
                            pageIndex + 1, printable.getPageCount());
                    if (!text.equals(pageOf.getText()))
                        pageOf.setText(text);
                }

                g.setTransform(at);

                String pageNumber = "- " + (pageIndex + 1) + " -";
                g.setFont(font);
                Rectangle2D fontRect = g.getFontMetrics().getStringBounds(
                        pageNumber, g);
                double left = (pageWidth - fontRect.getWidth() - W_SPACE / zoom) / 2d;
                g.setColor(Color.black);
                g.drawString(
                        pageNumber,
                        (float) (left * zoom + column
                                * (pageWidth + W_SPACE / zoom) * zoom) + 5,
                        (float) ((pageHeight + W_SPACE / zoom) * (row + 1) * zoom) - 22);

                g.scale(getZoom(), getZoom());

                g.translate(column * (pageWidth + W_SPACE / zoom) + 1, row
                        * (pageHeight + W_SPACE / zoom));

                Rectangle2D r = new Rectangle2D.Double(0, 0, page.width,
                        page.height);

                if (reverce)
                    g.rotate(Math.PI, r.getCenterX(), r.getCenterY());

                g.setColor(Color.white);
                g.fill(r);
                g.setColor(Color.black);
                g.draw(r);

                r = new Rectangle2D.Double(page.x, page.y, page.imageableWidth,
                        page.imageableHeight);

                try {
                    g.translate(1, 1);
                    printable.print(g, pageIndex);
                } catch (PrinterException e) {
                    e.printStackTrace();
                }

            }
            pageIndex++;
            column++;
            if (column >= columnCount) {
                column = 0;
                row++;
            }
        }
    }

    public JToolBar createToolBar() {
        JToolBar bar = new JToolBar();

        JComboBox zoom = createZoomComboBox();

        for (Action action : getFileActions()) {
            if (action == null)
                bar.addSeparator();
            else
                bar.add(action).setFocusable(false);
        }
        bar.addSeparator();

        JToggleButton grid = new JToggleButton(layoutGridAction);
        grid.setText(null);
        grid.setFocusable(false);

        JToggleButton col = new JToggleButton(layoutColAction);
        col.setText(null);
        col.setFocusable(false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(col);
        bg.add(grid);

        bar.add(grid);
        bar.add(col);
        bar.addSeparator();
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(zoom);
        bar.add(panel);
        return bar;
    }

    private JComboBox createZoomComboBox() {
        box = new JComboBox();
        box.setEditable(true);
        box.setSelectedItem((int) (zoom * 100d) + " %");

        box.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String value = box.getSelectedItem().toString().trim();
                    if (value.endsWith("%")) {
                        value = value.substring(0, value.length() - 1).trim();
                    }
                    int zoom = Integer.parseInt(value);
                    setCurrentZoom(zoom / 100d);
                } catch (Exception ex) {
                }
            }
        });

        box.addItem("400 %");
        box.addItem("200 %");
        box.addItem("100 %");
        box.addItem("75 %");
        box.addItem("50 %");

        return box;
    }

    public abstract Dimension getAreaSize();

    public Action[] getFileActions() {
        Action[] actions = printable.getActions(framework);
        ArrayList<PrintPlugin> list = new ArrayList<PrintPlugin>();
        AdditionalGUIPluginLoader.loadPrintPlugins(list);
        Action[] res = new Action[actions.length + 3 + list.size()];
        res[0] = printAction;
        res[1] = pageSetupAction;
        for (int i = 0; i < list.size(); i++)
            res[i + 2] = list.get(i).getPrintAction(framework, printable);
        for (int i = 0; i < actions.length; i++)
            res[i + 3 + list.size()] = actions[i];
        return res;
    }

    public Action[] getViewActions() {
        return new Action[]{layoutGridAction, layoutColAction};
    }

    /**
     * @param zoom the zoom to set
     */
    public void setZoom(double zoom) {
        this.zoom = zoom;
        Options.setDouble("PREV_ZOOM", zoom);
        if (box != null)
            box.setSelectedItem((int) (zoom * 100d) + " %");
    }

    /**
     * @return the zoom
     */
    public double getZoom() {
        return zoom;
    }

    public void setPrevLayout(int layout) {
        this.layout = layout;
        Options.setInteger("PREVIW_LAYOUT", layout);
        setCurrentZoom(zoom);
    }

    private class PrintAction extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = 1988072066292669411L;

        public PrintAction() {
            super(GlobalResourcesManager.getString("Action.Print"));
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/print.png")));
            putValue(ACTION_COMMAND_KEY, "Action.Print");
            putValue(SHORT_DESCRIPTION,
                    GlobalResourcesManager.getString("Action.Print"));
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                printable.print(framework);
                setSize();
            } catch (PrinterException e1) {
                if (framework != null)
                    JOptionPane.showMessageDialog(framework.getMainFrame(),
                            e1.getLocalizedMessage());
                else
                    JOptionPane.showMessageDialog(null,
                            e1.getLocalizedMessage());
            }
        }
    }

    ;

    private class PageSetupAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 7009116808941760573L;

        public PageSetupAction() {
            super(GlobalResourcesManager.getString("Action.PageSetup"));
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/page-setup.png")));
            putValue(ACTION_COMMAND_KEY, "Action.PageSetup");
            putValue(SHORT_DESCRIPTION,
                    GlobalResourcesManager.getString("Action.PageSetup"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            printable.pageSetup(framework);
            setSize();
        }
    }

    ;

    private class LayoutCol extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5708876883973951411L;

        public LayoutCol() {
            super(GlobalResourcesManager
                    .getString("Action.PreviewColumnLayout"));
            putValue(
                    SMALL_ICON,
                    new ImageIcon(
                            getClass()
                                    .getResource(
                                            "/com/ramussoft/gui/print/preview/previewLayoutCol.png")));
            putValue(ACTION_COMMAND_KEY, "Action.PreviewColumnLayout");
            putValue(SHORT_DESCRIPTION,
                    GlobalResourcesManager
                            .getString("Action.PreviewColumnLayout"));
            putValue(SELECTED_KEY, layout == PREV_LAYOUT_COL);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setPrevLayout(PREV_LAYOUT_COL);
        }
    }

    ;

    private class LayoutGrid extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 7194193394639977134L;

        /**
         *
         */
        public LayoutGrid() {
            super(GlobalResourcesManager.getString("Action.PreviewGridLayout"));
            putValue(
                    SMALL_ICON,
                    new ImageIcon(
                            getClass()
                                    .getResource(
                                            "/com/ramussoft/gui/print/preview/previewLayoutGrid.png")));
            putValue(ACTION_COMMAND_KEY, "Action.PreviewGridLayout");
            putValue(SHORT_DESCRIPTION,
                    GlobalResourcesManager
                            .getString("Action.PreviewGridLayout"));
            putValue(SELECTED_KEY, layout == PREV_LAYOUT_GRID);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setPrevLayout(PREV_LAYOUT_GRID);
        }
    }

    ;

    private class Page {

        private double width;

        private double height;

        private double imageableWidth;

        private double imageableHeight;

        private double x;

        private double y;

        public Page(double width, double height, double x, double y,
                    double imageableWidth, double imageableHeight) {
            this.width = width;
            this.height = height;
            this.imageableWidth = imageableWidth;
            this.imageableHeight = imageableHeight;
            this.x = x;
            this.y = y;
        }

        public boolean contains(Rectangle rectangle, int row, int column) {
            if (rectangle == null)
                return true;

            double px = column * (pageWidth + W_SPACE / zoom) * getZoom();
            double py = row * (pageHeight + W_SPACE / zoom) * getZoom();
            double r = (width + W_SPACE / zoom) * getZoom() + px;
            double b = (height + W_SPACE / zoom) * getZoom() + py;
            double rx = rectangle.getX();
            double ry = rectangle.getY();

            double rr = rectangle.getMaxX();
            double rb = rectangle.getMaxY();
            if (((px <= rr) && (px >= rx)) || ((r <= rr) && (r >= rx))
                    || ((rr <= r) && (rr >= px)) || ((rx <= r) && (rx >= px))) {
                return (((py <= rb) && (py >= ry)) || ((b <= rb) && (b >= ry))
                        || ((rb <= b) && (rb >= py)) || ((ry <= b) && (ry >= py)));
            }
            return false;
        }
    }

    public Component createStatusBar() {
        JPanel panel = new JPanel(new FlowLayout());
        pageOf = new JLabel(MessageFormat.format(
                GlobalResourcesManager.getString("PageOf"), 1,
                printable.getPageCount()));
        panel.add(pageOf);
        return panel;
    }
}
