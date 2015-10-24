package com.ramussoft.pb.print.web;

import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import com.dsoft.pb.types.FRectangle;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.elements.PaintSector.Pin;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingLabel;
import com.ramussoft.pb.print.PIDEF0painter;
import com.ramussoft.pb.types.GlobalId;

public class HTMLHelper {

    private static final int IMAGE_WIDTH = 1024;

    private static final int IMAGE_HEIGHT = 768;

    private DataPlugin dataPlugin;

    public HTMLHelper(DataPlugin dataPlugin) {
        this.dataPlugin = dataPlugin;
    }

	/*
     *
	 * private static final int IMAGE_WIDTH = 850; private static final int
	 * IMAGE_HEIGHT = 738;
	 */

    public String getDiagram(final String sId, final String functionController,
                             final String clasificatorController) {
        if (sId == null)
            return null;
        final Row r = dataPlugin.findRowByGlobalId(GlobalId.convert(sId));
        if (r instanceof Function) {
            final Function function = (Function) r;
            final int imageWidth = IMAGE_WIDTH;
            final int imageHeight = IMAGE_HEIGHT;
            final DiagramHolder htmlStream = new DiagramHolder();
            htmlStream.println("<img border=0 width=" + imageWidth + " height="
                    + imageHeight + " src=\"" + functionController + "idef0/"
                    + function.getGlobalId().toString() + "\" useMap=#M"
                    + function.getGlobalId().toString() + ">");

            htmlStream.println("<map name=M"
                    + function.getGlobalId().toString() + ">");
            final Vector childs = dataPlugin.getChilds(function, true);
            final MovingArea area = PIDEF0painter.createMovingArea(
                    new Dimension(imageWidth, imageHeight), dataPlugin);
            final SectorRefactor refactor = area.getRefactor();
            for (int i = 0; i < childs.size(); i++) {
                final Function fun = (Function) childs.get(i);
                String where;
                if (fun.isLeaf())
                    where = clasificatorController;
                else
                    where = functionController + "index/";
                htmlStream.print("<area shape=RECT coords="
                        + getAreaCoords(fun.getBounds(), area) + " href=\""
                        + where + fun.getGlobalId().toString() + "\"");
                htmlStream.println(">");
            }

            refactor.loadFromFunction(function, false);
            final int sc = refactor.getSectorsCount();

            for (int i = 0; i < sc; i++) {
                final PaintSector sector = refactor.getSector(i);
                final Stream stream = sector.getStream();

                final MovingLabel text = refactor.getSector(i).getText();
                if (text != null && stream != null) {
                    htmlStream.print("<area shape=RECT coords="
                            + getAreaCoords(text.getBounds(), area)
                            + " href=\"" + clasificatorController
                            + stream.getGlobalId().toString() + "\"");
                    htmlStream.println(">");
                }
                final int l = sector.getPinCount();
                for (int j = 0; j < l; j++)
                    if (stream != null) {
                        final Pin pin = sector.getPin(j);
                        htmlStream.print("<area shape=RECT coords="
                                + getPinCoords(pin, area) + " href=\""
                                + clasificatorController
                                + stream.getGlobalId().toString() + "\"");
                        htmlStream.println(">");
                    }
            }
            htmlStream.println("<map>");
            return htmlStream.toString();
        }
        return null;
    }

    private String getAreaCoords(final FRectangle bounds, final MovingArea area) {
        return area.getIntOrdinate(bounds.getLocation().getX())
                + ","
                + (area.getIntOrdinate(bounds.getLocation().getY()) + area
                .getIntOrdinate(area.TOP_PART_A))
                + ","
                + area.getIntOrdinate(bounds.getRight())
                + ","
                + (area.getIntOrdinate(bounds.getBottom()) + area
                .getIntOrdinate(area.TOP_PART_A));
    }

    private String getPinCoords(final Pin pin, final MovingArea area) {
        final double d = 2;
        final FRectangle bounds = new FRectangle();
        if (pin.getType() == PaintSector.PIN_TYPE_X) {
            double x1 = pin.getStart().getX();
            double x2 = pin.getEnd().getX();
            final double y = pin.getEnd().getY();
            if (x2 < x1) {
                final double x = x1;
                x1 = x2;
                x2 = x;
            }
            bounds.setX(x1);
            bounds.setWidth(x2 - x1);
            bounds.setY(y - d);
            bounds.setHeight(d * 2);
        } else {
            double y1 = pin.getStart().getY();
            double y2 = pin.getEnd().getY();
            final double x = pin.getEnd().getX();
            if (y2 < y1) {
                final double y = y1;
                y1 = y2;
                y2 = y;
            }
            bounds.setY(y1);
            bounds.setHeight(y2 - y1);
            bounds.setX(x - d);
            bounds.setWidth(d * 2);
        }
        return getAreaCoords(bounds, area);
    }

    public byte[] getDiagramPicture(final String id) {
        final Row r = dataPlugin.findRowByGlobalId(GlobalId.convert(id));
        if (r instanceof Function) {
            final Function f = (Function) r;
            final PIDEF0painter painter = new PIDEF0painter(f, new Dimension(
                    IMAGE_WIDTH, IMAGE_HEIGHT), dataPlugin);
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                painter.writeToStream(stream, PIDEF0painter.PNG_FORMAT);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            return stream.toByteArray();
        }
        return new byte[]{};
    }
}
