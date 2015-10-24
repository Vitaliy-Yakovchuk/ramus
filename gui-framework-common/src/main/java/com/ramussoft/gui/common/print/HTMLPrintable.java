package com.ramussoft.gui.common.print;

import java.awt.Graphics;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.View;
import javax.swing.text.html.HTMLEditorKit;

import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

public class HTMLPrintable extends AbstractRamusPrintable implements Printable {

    private static final double SCALE = 0.70;

    private ByteArrayOutputStream outputStream = null;

    private final Vector<PagePos> pages = new Vector<PagePos>();

    private double pageStartY = 0;

    private double pageEndY = 0;

    private View rootView;

    private int currentPage;

    private double clientHeight = 0;

    protected JEditorPane pane;

    private double topHeight;

    private double bottomHeight;

    private String url;

    private class PagePos {

        public double pageStartY;

        public double pageEndY;

        public int currentPage;

        public PagePos() {
            super();
        }

        public PagePos(final double pageStartY, final double pageEndY,
                       final int currentPage) {
            this();
            this.pageStartY = pageStartY;
            this.pageEndY = pageEndY;
            this.currentPage = currentPage;
        }
    }

    public HTMLPrintable() {
    }

    public HTMLPrintable(String htmlText) {
        OutputStream stream = getOutputStream();
        try {
            stream.write(htmlText.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void initEditorPane() {
        if (pane != null)
            return;
        pane = new JEditorPane();
        HTMLEditorKit kit = new HTMLEditorKit() {
            /**
             *
             */
            private static final long serialVersionUID = -8040272164224951314L;

            @Override
            public Document createDefaultDocument() {
                Document document = super.createDefaultDocument();
                document.putProperty("IgnoreCharsetDirective", true);
                return document;
            }
        };

        pane.setEditorKit(kit);
        try {
            pane.read(new ByteArrayInputStream(outputStream.toByteArray()),
                    null);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPage(String url, final ActionListener listener)
            throws IOException {
        this.url = url;
        pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.addPropertyChangeListener("page", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                generate(0);
                if (listener != null)
                    listener.actionPerformed(new ActionEvent(
                            HTMLPrintable.this, 0, "PageLoaded"));
            }
        });
        pane.setPage(url);
    }

    public OutputStream getOutputStream() {
        outputStream = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                generate(0);
            }
        };
        return outputStream;
    }

    private void initRootView(final PageFormat pageFormat) {
        pane.setSize((int) (pageFormat.getImageableWidth() / SCALE),
                Integer.MAX_VALUE);
        pane.validate();
        rootView = pane.getUI().getRootView(pane);
        pages.clear();
        pageStartY = 0;
        pageEndY = 0;
        currentPage = -1;
        this.pageFormat = pageFormat;
        int pageIndex = 0;

        try {
            while (print(null, pageFormat, pageIndex) == PAGE_EXISTS)
                pageIndex++;
        } catch (final PrinterException e) {
            e.printStackTrace();
        }
    }

    protected void generate(final int fromPage) {
        pageFormat = getPageFormat();
        initEditorPane();
        initRootView(pageFormat);
    }

    @Override
    public PageFormat getPageFormat() {
        if (pageFormat == null) {
            pageFormat = Options.getPageFormat("html-page-format.xml",
                    new PageFormat());
        }
        return pageFormat;
    }

    @Override
    public void setPageFormat(PageFormat pageFormat) {
        super.setPageFormat(pageFormat);
        generate(0);
        Options.setPageFormat("html-page-format.xml", pageFormat);
    }

    public int getPageCount() {
        return pages.size() - 1;
    }

    public int print(final Graphics graphics, final PageFormat pageFormat,
                     final int pageIndex) throws PrinterException {
        final Graphics2D g = (Graphics2D) graphics;
        this.pageFormat = pageFormat;
        topHeight = 0;
        bottomHeight = 0;
        clientHeight = pageFormat.getImageableHeight() / SCALE
                - (topHeight + bottomHeight);

        if (pageIndex >= pages.size()) {
            final PagePos pos = new PagePos(pageStartY, pageEndY, currentPage);
            pages.add(pos);
        } else {
            final PagePos pos = pages.get(pageIndex);
            pageStartY = pos.pageStartY;
            pageEndY = pos.pageEndY;
            currentPage = pos.currentPage;
        }
        if (pageIndex > currentPage) {
            currentPage = pageIndex;
            pageStartY += pageEndY;
            pageEndY = clientHeight;
        }

        if (g != null) {
            g.translate(pageFormat.getImageableX(), pageFormat.getImageableY()
                    + topHeight * SCALE);
            g.scale(SCALE, SCALE);
        }
        final Rectangle allocation = new Rectangle(0, (int) -pageStartY,
                (int) pane.getMinimumSize().getWidth(), (int) pane
                .getPreferredSize().getHeight());
        if (printView(g, allocation, rootView)) {
            if (g != null) {
                g.scale(1 / SCALE, 1 / SCALE);
                g.translate(0, -topHeight * SCALE);
                g.scale(SCALE, SCALE);
            }
            return Printable.PAGE_EXISTS;
        }
        pageStartY = 0;
        pageEndY = 0;
        currentPage = -1;
        return NO_SUCH_PAGE;
    }

    private boolean printView(final Graphics2D graphics2D,
                              final Shape allocation, final View view) {
        boolean pageExists = false;
        final Rectangle clipRectangle = new Rectangle(0, 0, (int) (pageFormat
                .getImageableWidth() / SCALE), (int) clientHeight);
        Shape childAllocation;
        View childView;

        if (view.getViewCount() > 0) {
            for (int i = 0; i < view.getViewCount(); i++) {
                childAllocation = view.getChildAllocation(i, allocation);
                if (childAllocation != null) {
                    childView = view.getView(i);
                    if (printView(graphics2D, childAllocation, childView)) {
                        pageExists = true;
                    }
                }
            }
        } else {
            if (allocation.getBounds().getMaxY() >= clipRectangle.getY()) {

                if (allocation.getBounds().getHeight() > clipRectangle
                        .getHeight()
                        && allocation.intersects(clipRectangle)) {
                    paintView(graphics2D, view, allocation);
                    pageExists = true;
                } else {
                    if (allocation.getBounds().getY() >= clipRectangle.getY()) {
                        if (allocation.getBounds().getMaxY() <= clipRectangle
                                .getMaxY()) {
                            paintView(graphics2D, view, allocation);
                            pageExists = true;

                        } else {
                            if (allocation.getBounds().getY() < pageEndY) {
                                pageEndY = allocation.getBounds().getY();
                            }
                        }
                    }
                }
            }
        }
        return pageExists;
    }

    private void paintView(final Graphics2D graphics2D, final View view,
                           final Shape allocation) {
        if (graphics2D != null) {
            view.paint(graphics2D, allocation);
        }
    }

    @Override
    public void print(Graphics2D g, int pageIndex) throws PrinterException {
        print(g, getPageFormat(getPageFormat(), pageIndex), pageIndex);
    }

    @Override
    public Action[] getActions(GUIFramework framework) {
        Action[] actions2 = super.getActions(framework);
        Action[] actions = Arrays.copyOf(actions2, actions2.length + 1);
        actions[actions2.length] = createExportToHTMLAction(framework);
        return actions;
    }

    public Action createExportToHTMLAction(GUIFramework framework) {
        return new ExportToHTMLAction(framework);
    }

    @Override
    public Printable createPrintable() {
        return this;
    }

    protected class ExportToHTMLAction extends ExportAction {

        public ExportToHTMLAction(GUIFramework framework) {
            super(GlobalResourcesManager.getString("Action.ExportToHTML"),
                    framework);
            putValue(ACTION_COMMAND_KEY, "Action.ExportToHTML");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/print/html.png")));
            putValue(SHORT_DESCRIPTION, GlobalResourcesManager
                    .getString("Action.ExportToHTML"));
        }

        /**
         *
         */
        private static final long serialVersionUID = -2951543850803247975L;

        @Override
        protected void exportToFile(File file) throws Exception {
            FileOutputStream os = new FileOutputStream(file);
            if (outputStream != null)
                os.write(outputStream.toByteArray());
            else {
                URL url = new URL(HTMLPrintable.this.url);
                InputStream is = url.openStream();
                byte[] buff = new byte[1024];
                int r;
                while ((r = is.read(buff)) > 0) {
                    os.write(buff, 0, r);
                }
                is.close();
            }
            os.close();
        }

        @Override
        protected String getExtension() {
            return ".html";
        }

    }
}
