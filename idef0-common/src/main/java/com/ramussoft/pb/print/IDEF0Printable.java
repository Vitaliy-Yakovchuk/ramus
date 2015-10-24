package com.ramussoft.pb.print;

import java.awt.Dimension;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.common.print.AbstractRamusPrintable;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.idef.frames.ExportToImagesDialog;

/**
 * Клас призначений для друку діаграм IDEF0
 *
 * @author ZDD
 */

public class IDEF0Printable extends AbstractRamusPrintable {

    private Function[] functions = null;

    private PIDEF0painter[] painters = null;

    private DataPlugin dataPlugin;

    private final GUIFramework framework;

    private static PageFormat staticPageFormat;

    private boolean nativeTextPaint;

    public IDEF0Printable(DataPlugin dataPlugin, GUIFramework framework) {
        super();
        if (staticPageFormat == null) {
            staticPageFormat = new PageFormat();
            staticPageFormat.setOrientation(PageFormat.LANDSCAPE);
            staticPageFormat = Options.getPageFormat("idef0-page-format",
                    staticPageFormat);
        }
        this.pageFormat = staticPageFormat;
        this.dataPlugin = dataPlugin;
        this.framework = framework;
        final Vector<Function> res = new Vector<Function>();
        final Function base = dataPlugin.getBaseFunction();
        if (base.isHaveRealChilds())
            res.add(base);

        final Vector<Row> rec = dataPlugin.getRecChilds(base, true);
        for (int i = 0; i < rec.size(); i++)
            if (((Function) rec.get(i)).isHaveRealChilds())
                res.add((Function) rec.get(i));
        setPrintFunctions(res);
    }

    private void setPrintFunctions(final Vector<Function> res) {
        functions = new Function[res.size()];
        for (int i = 0; i < functions.length; i++)
            functions[i] = res.get(i);
        painters = new PIDEF0painter[functions.length];
    }

    public void setPrintFunctions(Function[] functions) {
        this.functions = functions;
        painters = new PIDEF0painter[functions.length];
    }

    @Override
    public String getJobName() {
        return ResourceLoader.getString("IDF0_model");
    }

    public int getPageCount() {
        int res = 0;
        for (int index = 0; index < painters.length; index++) {
            PIDEF0painter painter = getPainter(index);
            res += painter.hPageCount;
        }
        if (getPageFormat().getOrientation() == PageFormat.PORTRAIT)
            res = (int) Math.ceil((double) res / 2.d);
        return res;
    }

    private PIDEF0painter getPainter(final int index) {
        if (painters[index] == null) {
            final PageFormat pf = getPageFormat();
            int imageableWidth = (int) pf.getImageableWidth();

            String size = functions[index].getPageSize();
            int h;
            int pc = 1;
            if (size != null && (h = size.indexOf('x')) >= 0)
                pc = Integer.parseInt(size.substring(h + 1));

            imageableWidth *= pc;
            painters[index] = new PIDEF0painter(
                    functions[index],
                    new Dimension(imageableWidth, (int) pf.getImageableHeight()),
                    dataPlugin, pc);
            if (nativeTextPaint)
                painters[index].getMovingArea().setNativePaint(true);
        }
        return painters[index];
    }

    public void print(final JFrame frame, DataPlugin dataPlugin) {
        final PrintDialog printDialog = new PrintDialog(frame, dataPlugin,
                framework);
        printDialog.showModal(this);
    }

    @Override
    public void print(Graphics2D g, int nx) {
        PageFormat pf = getPageFormat(getPageFormat(), nx);

        int n = 0;

        if (getPageFormat().getOrientation() == PageFormat.PORTRAIT) {
            AffineTransform at = g.getTransform();
            n = 0;
            PIDEF0painter painter = getPainter(n);
            int part = 0;

            for (int i = 0; i < nx; i++) {
                part++;
                if (part >= painter.hPageCount) {
                    part = 0;
                    n++;
                    painter = getPainter(n);
                }
                part++;
                if (part >= painter.hPageCount) {
                    part = 0;
                    n++;
                    painter = getPainter(n);
                }
            }

            painter.paintNonstandard(g, pf.getImageableX(), pf.getImageableY(),
                    part, painter.hPageCount);
            g.setTransform(at);
            part++;
            if (part >= painter.hPageCount) {
                part = 0;
                n++;
            }
            int index = n;
            if (index < functions.length)
                getPainter(index).paintNonstandard(g, pf.getImageableX(),
                        pf.getImageableY() + pf.getImageableHeight() / 2d,
                        part, getPainter(index).hPageCount);
        } else {
            n = 0;
            PIDEF0painter painter = getPainter(n);
            int part = 0;

            for (int i = 0; i < nx; i++) {
                part++;
                if (part >= painter.hPageCount) {
                    part = 0;
                    n++;
                    painter = getPainter(n);
                }
            }
            painter.paintNonstandard(g, pf.getImageableX(), pf.getImageableY(),
                    part, painter.hPageCount);
        }
    }

    @Override
    public Action[] getActions(final GUIFramework framework) {
        return new Action[]{new AbstractAction(
                ResourceLoader.getString("ExportToImages")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                new ExportToImagesDialog(framework.getMainFrame(), dataPlugin)
                        .setVisible(true);
            }
        }};
    }

    @Override
    protected String getJobKey() {
        return "idef0";
    }

    @Override
    public void setPageFormat(PageFormat pageFormat) {
        super.setPageFormat(pageFormat);
        if (!staticPageFormat.equals(pageFormat)) {
            Arrays.fill(painters, null);
            staticPageFormat = pageFormat;
            Options.setPageFormat("idef0-page-format", staticPageFormat);
        }
    }

    public DataPlugin getDataPlugin() {
        return dataPlugin;
    }

    public boolean isNativeTextPaint() {
        return nativeTextPaint;
    }

    public void setNativeTextPaint(boolean nativeTextPaint) {
        this.nativeTextPaint = nativeTextPaint;
        if (painters != null)
            for (PIDEF0painter painter : painters)
                if (painter != null)
                    painter.getMovingArea().setNativePaint(nativeTextPaint);
    }

}
