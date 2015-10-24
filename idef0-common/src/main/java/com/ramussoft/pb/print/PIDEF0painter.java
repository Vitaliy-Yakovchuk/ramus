/*
 * Created on 31/8/2005
 */
package com.ramussoft.pb.print;

import java.awt.Color;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.dsoft.utils.Options;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.elements.ArrowPainter;
import com.ramussoft.pb.idef.frames.IDEFPanel;
import com.ramussoft.pb.idef.visual.MovingArea;

/**
 * Клас для малювання IDEF0 Діаграм.
 *
 * @author ZDD
 */
public class PIDEF0painter {

    public static final int BMP_FORMAT = 0;

    public static final int PNG_FORMAT = 1;

    public static final int JPEG_FORMAT = 2;

    public static final int SVG_FORMAT = 3;

    public static final int EMF_FORMAT = 4;

    private final Function function;

    private MovingArea movingArea;

    private Dimension size;

    private DataPlugin dataPlugin;

    public final int hPageCount;

    /**
     * @return Returns the size.
     */
    public Dimension getSize() {
        return size;
    }

    /**
     * @return Returns the function.
     */
    public Function getFunction() {
        return function;
    }

    /**
     * @return Returns the movingArea.
     */
    public MovingArea getMovingArea() {
        return movingArea;
    }

    public static MovingArea createMovingArea(final Dimension d,
                                              final DataPlugin dataPlugin) {
        final MovingArea movingArea = new MovingArea(dataPlugin);
        final double zh = (double) d.height
                / (double) movingArea.MOVING_AREA_HEIGHT;
        final double zw = (double) d.width
                / (double) movingArea.MOVING_AREA_WIDTH;
        final double zoom = zw < zh ? zw : zh;
        movingArea.setZoom(zoom);
        movingArea.setSize(
                movingArea.getIntOrdinate(movingArea.MOVING_AREA_WIDTH),
                movingArea.getIntOrdinate(movingArea.CLIENT_HEIGHT));
        return movingArea;
    }

    public static MovingArea createMovingArea(final Dimension d,
                                              final DataPlugin dataPlugin, Function activeFunction) {
        final MovingArea movingArea = new MovingArea(dataPlugin, activeFunction);
        final double zh = (double) d.height
                / (double) movingArea.MOVING_AREA_HEIGHT;
        final double zw = (double) d.width
                / (double) movingArea.MOVING_AREA_WIDTH;

        final double zoom = zw < zh ? zw : zh;
        movingArea.setZoom(zoom);
        movingArea.setSize(
                movingArea.getIntOrdinate(movingArea.MOVING_AREA_WIDTH),
                movingArea.getIntOrdinate(movingArea.CLIENT_HEIGHT));
        return movingArea;
    }

    public void prepare(Dimension d) {
        size = new Dimension(d.width - 1, d.height - 1);
        d = new Dimension(d.width - 3, d.height - 2);
        movingArea = createMovingArea(d, dataPlugin, function);

        movingArea.setActiveFunction(function);
        movingArea.setPrinting(true);
    }

    public PIDEF0painter(final Function function, final Dimension d,
                         final DataPlugin dataPlugin) {
        this(function, d, dataPlugin, 1);
    }

    public PIDEF0painter(final Function function, final Dimension d,
                         final DataPlugin dataPlugin, int hPageCount) {
        super();
        this.function = function;
        this.dataPlugin = dataPlugin;
        this.hPageCount = hPageCount;
        prepare(d);
    }

    public void paint(final Graphics2D g, final double x, final double y) {
        paint(g, x, y, false);
    }

    public void paint(final Graphics2D g, final double x, final double y,
                      final boolean fill) {
        paint(g, x, y, fill, 0, 1);
    }

    public void paint(final Graphics2D g, final double x, final double y,
                      final boolean fill, int partNumber, int hPageCount) {
        if (!function.isHaveChilds())
            return;

        final ArrowPainter painter = new ArrowPainter(movingArea);
        final int y1 = movingArea.getIntOrdinate(movingArea.TOP_PART_A);

        final int y2 = movingArea.getIntOrdinate(movingArea.CLIENT_HEIGHT);
        final int height = movingArea.getIntOrdinate(movingArea.BOTTOM_PART_A);
        final int x0 = (int) x + 1;
        final int y0 = (int) y + 1;

        if (fill) {
            final Color c = g.getColor();
            g.setColor(IDEFPanel.DEFAULT_BACKGROUND);
            g.fillRect(x0, y0,
                    movingArea.getIntOrdinate(movingArea.MOVING_AREA_WIDTH), y1
                            + y2 + height);
            g.setColor(c);
        }

        g.setStroke(ArrowPainter.THIN_STROKE);
        g.translate(x0, y0);
        g.setColor(Color.BLACK);
        painter.paintTop(
                g// (Graphics2D)g.create(x0,y0,size.width,y1)
                , movingArea.getIntOrdinate(movingArea.TOP_PART_A), movingArea,
                partNumber, hPageCount);
        g.translate(0, y1);
        movingArea.paint(g);// g.create(x0,y0+y1,size.width,y2));

        g.setStroke(ArrowPainter.THIN_STROKE);

        g.translate(0, y2);
        g.setColor(Color.BLACK);
        painter.paintBottom(g, height, movingArea, partNumber, hPageCount);
    }

    private void writePNG(final OutputStream stream) throws IOException {
        writeImage(stream, "png");
    }

    private void writeImage(final OutputStream stream, final String format)
            throws IOException {
        final int y1 = movingArea.getIntOrdinate(movingArea.TOP_PART_A);

        final int y2 = movingArea.getIntOrdinate(movingArea.CLIENT_HEIGHT);
        final int y3 = movingArea.getIntOrdinate(movingArea.BOTTOM_PART_A);

        int height = y1 + y2 + y3 + 1;
        final BufferedImage bi = new BufferedImage(size.width, height,
                BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = (Graphics2D) bi.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        g.setColor(Color.white);
        g.fillRect(0, 0, size.width, size.height);
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

        paint(g, 0, 0);
        ImageIO.write(bi, format, stream);
    }

    private void writeBMP(final OutputStream stream) throws IOException {
        writeImage(stream, "bmp");
    }

    private void writeJPEG(final OutputStream stream) throws IOException {
        writeImage(stream, "jpg");
    }

    /**
     * Медод записує малюнок у вихінжний потік.
     *
     * @param stream Потік, в який буде переданий малюнок.
     * @param format Формат малюнка (BMP_FORMAT, PNG_FORMAT, JPEG_FORMAT).
     * @throws IOException Само собою зрозуміло :).
     */

    public void writeToStream(final OutputStream stream, final int format)
            throws IOException {
        switch (format) {
            case BMP_FORMAT:
                writeBMP(stream);
                break;
            case PNG_FORMAT:
                writePNG(stream);
                break;
            case JPEG_FORMAT:
                writeJPEG(stream);
                break;
            case SVG_FORMAT:
                writeSVG(stream);
                break;
            case EMF_FORMAT:
                writeEMF(stream);
                break;
        }

    }

    private void writeSVG(OutputStream stream) throws IOException {
        DOMImplementation impl = GenericDOMImplementation
                .getDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        Document myFactory = impl.createDocument(svgNS, "svg", null);

        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(myFactory);
        ctx.setEmbeddedFontsOn(Options.getBoolean("EMBEDDED_SVG_FONTS", true));
        SVGGraphics2D svgGenerator = new SVGGraphics2D(ctx, Options.getBoolean(
                "EMBEDDED_SVG_FONTS", true));

        svgGenerator.setSVGCanvasSize(size);

        paint(svgGenerator, 0, 0);

        boolean useCSS = true;
        Writer out = new OutputStreamWriter(stream, "UTF-8");
        svgGenerator.stream(out, useCSS);

    }

    private void writeEMF(OutputStream stream) throws IOException {
        EMFGraphics2D g = new EMFGraphics2D(stream, size);
        g.startExport();
        paint(g, 0, 0);
        g.endExport();
    }

    public void paintNonstandard(Graphics2D g, double imageableX,
                                 double imageableY, int partNumber, int hPageCount) {
        double w = movingArea.getIDoubleOrdinate(movingArea.MOVING_AREA_WIDTH);
        w /= hPageCount;
        g.setClip(new Rectangle2D.Double(imageableX, imageableY, w, movingArea
                .getIDoubleOrdinate(movingArea.MOVING_AREA_HEIGHT)));
        double tx = -movingArea.getIDoubleOrdinate(movingArea.MOVING_AREA_WIDTH
                / hPageCount)
                * partNumber;
        double ty = 0;
        g.translate(tx, ty);
        paint(g, imageableX, imageableY, false, partNumber, hPageCount);
        g.translate(-tx, -ty);
        g.setClip(null);
    }
}
