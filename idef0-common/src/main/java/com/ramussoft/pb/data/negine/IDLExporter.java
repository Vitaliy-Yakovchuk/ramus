package com.ramussoft.pb.data.negine;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.dsoft.pb.idef.elements.ProjectOptions;
import com.dsoft.pb.idef.elements.Status;
import com.dsoft.pb.types.FloatPoint;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingFunction;
import com.ramussoft.pb.idef.visual.MovingLabel;
import com.ramussoft.pb.print.PStringBounder;

public class IDLExporter extends IDL {

    private NFunction base;

    private IDLWriter writer;

    private Hashtable<Function, FunctionMeta> meta = new Hashtable<Function, FunctionMeta>();

    private class FunctionMeta {
        ArrayList<Sector> cs = new ArrayList<Sector>();
        ArrayList<Sector> os = new ArrayList<Sector>();
        ArrayList<Sector> is = new ArrayList<Sector>();
        ArrayList<Sector> ms = new ArrayList<Sector>();

        public ArrayList<Sector> getSectors(int functionType) {
            switch (functionType) {
                case MovingFunction.LEFT:
                    return is;
                case MovingFunction.RIGHT:
                    return os;
                case MovingFunction.TOP:
                    return cs;
                case MovingFunction.BOTTOM:
                    return ms;
            }
            return null;
        }

        public void addSector(Sector sector, int functionType) {
            getSectors(functionType).add(sector);
        }

        public String getTypeName(int box, int functionType) {
            String name;
            switch (functionType) {
                case MovingFunction.LEFT:
                    name = "I";
                    break;
                case MovingFunction.RIGHT:
                    name = "O";
                    break;
                case MovingFunction.TOP:
                    name = "C";
                    break;
                default:
                    name = "M";
                    break;
            }

            return "BOX " + box + name + (getSectors(functionType).size());
        }

        public String getBorderTypeName(Sector sector, int borderType) {
            String name;
            switch (borderType) {
                case MovingFunction.LEFT:
                    name = "I";
                    break;
                case MovingFunction.RIGHT:
                    name = "O";
                    break;
                case MovingFunction.TOP:
                    name = "C";
                    break;
                default:
                    name = "M";
                    break;
            }

            return "BORDER " + name
                    + (getSectors(borderType).indexOf(sector) + 1);
        }

    }

    ;

    public IDLExporter(NFunction base, NDataPlugin dataPlugin, String encoding) {
        super(dataPlugin, encoding);
        this.base = base;
    }

    public void export(OutputStream out) throws IOException {

        loadFonts();

        writer = new IDLWriter(out, encoding);
        writer.p1("KIT");
        writer.right();
        writer.p1("IDL VERSION 1.2.8");
        writer.p2("TITLE {0}", base.getName());
        final ProjectOptions projectOptions = base.getProjectOptions();
        writer.p2("AUTHOR {0}", projectOptions.getProjectAutor());
        writer.p1("CREATION DATE " + format.format(new Date()));
        writer.p2("PROJECT NAME {0}", projectOptions.getProjectName());
        writer.println();

        writer.println("MODEL {0}", q(base.getName()));

        writer.right();

        writer.println("{LWI");

        printFonts();
        writer.println("D 0 0 0 0 0 1 0 0 2");
        writer.println("G 0 1");
        writer.println("T "
                + q(Status.STATUS_NAMES[base.getStatus().getType()]
                .toUpperCase()));
        writer.println("R 77");
        writer.println("M 0 }");
        writer.left();
        writer.println(" ;");
        writer.right();

        writer.p2("AUTHOR {0}", projectOptions.getProjectAutor());
        writer.p2("PROJECT NAME {0}", projectOptions.getProjectName());

        rec(new FunctionCallback() {
            @Override
            public void call(Function f) throws IOException {
                String code = MovingFunction.getIDEF0Kod((com.ramussoft.database.common.Row) f);
                writer.p1("DIAGRAM GRAPHIC " + code);
                writer.right();
                writer.p1("CREATION DATE "
                        + format.format(projectOptions.getDateCreateDate()));
                writer.p1("REVISION DATE "
                        + format.format(projectOptions.getDateChangeDate()));
                writer.p2("TITLE {0}", f.getName());
                writer.p2("STATUS "
                        + Status.STATUS_NAMES[f.getStatus().getType()]
                        .toUpperCase());
                writer.println();
                for (int i = 0; i < f.getChildCount(); i++) {
                    Function child = (Function) f.getChildAt(i);
                    int j = i;
                    if (!f.equals(base))
                        j++;
                    writer.p1("BOX " + j);
                    writer.right();
                    int textColot = toByteTextIndex(child.getForeground());
                    int bkColot = toByteIndex(child.getBackground());
                    writer.p2("NAME {0}",
                            "{LWI I " + uniqueFonts.indexOf(child.getFont())
                                    + " " + textColot + " " + bkColot + " "
                                    + textColot + "}" + child.getName());

                    String lb = toCoortinate(child.getBounds().getLeft(), child
                            .getBounds().getBottom());
                    String rt = toCoortinate(child.getBounds().getRight(),
                            child.getBounds().getTop());

                    writer.p1("BOX COORDINATES " + lb + " " + rt);
                    writer.p1("DETAIL REFERENCE N "
                            + MovingFunction
                            .getIDEF0Kod((com.ramussoft.database.common.Row) child));
                    writer.left();
                    writer.p1("ENDBOX");
                }

                printSegments(f);

                writer.left();
                writer.p1("ENDDIAGRAM");
            }
        }, true);
        writer.left();
        writer.p1("ENDMODEL");
        writer.left();
        writer.p1("ENDKIT");
    }

    public static String q(String string) {
        return "'" + toCanonocal(string) + "'";
    }

    private static String toCanonocal(String string) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == '\n')
                sb.append("<CR>");
            else if (c == '\\')
                sb.append("\\\\");
            else if (c == '\'')
                sb.append("\\\'");
            else
                sb.append(c);
        }
        return sb.toString();
    }

    protected void printSegments(Function f) throws IOException {
        MovingArea movingArea = new MovingArea(dataPlugin, f);
        movingArea.setActiveFunction(f);
        SectorRefactor sr = movingArea.getRefactor();
        for (int i = 0; i < sr.getSectorsCount(); i++) {
            PaintSector ps = sr.getSector(i);
            writer.p1("ARROWSEG " + (i + 1));
            writer.right();

            writer.p1("SOURCE "
                    + getName(ps.getSector().getStart(), f, ps.getSector(), sr,
                    ps.getPoint(0)));

            StringBuffer path = new StringBuffer();
            path.append("PATH ");

            for (int j = 0; j < ps.getPointCount(); j++) {
                Point point = ps.getPoint(j);
                path.append(toCoortinate(point.getX(), point.getY()));
            }

            writer.p1(path.toString());

            String s = ps.getAlternativeText();
            if ("".equals(s))
                s = ps.getSector().getName();
            if ((s != null) && (s.length() > 0)) {
                MovingLabel text = ps.getText();
                if (text != null) {
                    final PStringBounder nb = new PStringBounder(null);
                    nb.setFont(text.getFont());
                    final PStringBounder.Tokanizer tokanizer = nb.getTokanizer(
                            s, text.getBounds().getWidth(), 0);

                    StringBuffer sb = new StringBuffer();
                    if (tokanizer.hasMoreData())
                        sb.append(tokanizer.getNext());
                    while (tokanizer.hasMoreData()) {
                        sb.append("<CR>");
                        sb.append(tokanizer.getNext());
                    }

                    writer.p2("LABEL {0}",
                            "{LWI I 0 " + toByteTextIndex(text.getColor())
                                    + " " + toByteTextIndex(text.getColor())
                                    + " }" + sb.toString());
                    writer.p1("LABEL COORDINATES "
                            + toCoortinate(ps.getText().getBounds().getX(), ps
                            .getText().getBounds().getCenter().getY()));
                    if (ps.isShowTilda()) {
                        writer.p1("SQUIGGLE COORDINATES "
                                + toCoortinate(ps.getTildaOPoint()) + " "
                                + toCoortinate(ps.getTildaPoint()));
                    }
                } else
                    writer.p2("LABEL {0}", s);
            }

            writer.p1("SINK "
                    + getName(ps.getSector().getEnd(), f, ps.getSector(), sr,
                    ps.getPoint(ps.getPointCount() - 1)));
            writer.left();
            writer.p1("ENDSEG");
        }
    }

    private int toByteTextIndex(Color color) {
        int res = toByteIndex(color);
        if (res == 255)
            return 6;
        if (res == 7)
            return 255;
        return res;
    }

    private String toCoortinate(FloatPoint location) {
        return toCoortinate(location.getX(), location.getY());
    }

    private String getName(NSectorBorder border, Function parent,
                           Sector sector, SectorRefactor sr, Point coordinate) {
        if (border.getBorderType() >= 0) {
            if (parent.equals(base))
                return "BORDER";

            Sector[] oppozite = border.getCrosspoint().getOppozite(sector);
            if (oppozite.length == 0) {
                if (border.getTunnelType() == Crosspoint.TUNNEL_HARD)
                    return "TUNNEL  {LWI Q}BORDER";
                else
                    return "TUNNEL BORDER";
            }

            return getMeta(parent).getBorderTypeName(oppozite[0],
                    border.getBorderType())
                    + " " + toCoortinate(coordinate.getX(), coordinate.getY());
        } else if (border.getFunctionType() >= 0) {
            Function function = border.getFunction();
            if (function == null)
                return "BORDER";// probably will never happen
            int box = parent.getIndex(function);
            if (!parent.equals(base))
                box++;
            getMeta(function).addSector(sector, border.getFunctionType());
            return getMeta(function).getTypeName(box, border.getFunctionType());
        } else {

            if (border.getCrosspoint() == null) {
                return "TUNNEL {LWI Q} BORDER";
            } else {

                StringBuffer sb = new StringBuffer();
                if (border.getCrosspoint().getIns().length == 1)
                    sb.append("BRANCH");
                else
                    sb.append("JOIN");

                for (Sector s : border.getCrosspoint().getOppozite(sector)) {
                    int index = sr.getPaintSectorIndex(s) + 1;
                    sb.append(' ');
                    sb.append(index);
                }
                return sb.toString();
            }
        }
    }

    private String toCoortinate(double x, double y) {
        final double w = WIDTH;
        final double h = HEIGHT;
        String p1 = numberFormat.format((float) (x / w) + X_ADD);
        String p2 = numberFormat.format((float) (y / h) + Y_ADD);
        String string = "("
                + p1
                + (((p1.indexOf(',') >= 0) || (p2.indexOf(',') >= 0)) ? ";"
                : ",") + p2 + ")";

        return string;
    }

    private void printFonts() throws IOException {
        for (int i = 0; i < uniqueFonts.size(); i++) {
            printFont(i);
        }
    }

    private void printFont(int i) throws IOException {
        Font font = uniqueFonts.get(i);
        writer.println("F {0} {1} 0 0 0 400", i, toIDLSize(font.getSize()));
        writer.right();
        writer.println("{0} 0 {1} 204 3 2 1 34",
                ((font.isItalic()) ? "1" : "0"), ((font.isBold()) ? "1" : "0"));
        writer.println("{0} {1}", toSize(font), q(font.getName()));
        writer.left();
    }

    private Object toSize(Font font) {
        double size = font.getSize();
        size *= 0.8;
        return ((int) size) * 10;
    }

    private Object toIDLSize(int size) {
        return "-" + size;
    }

    private void loadFonts() throws IOException {
        addFont(new Font("Arial", 0, 13));
        addFont(new Font("Arial", 0, 24));
        addFont(new Font("Courier New", 0, 11));
        rec(new FunctionCallback() {
            @Override
            public void call(Function f) {
                addFont(f.getFont());
                SectorRefactor sr = new SectorRefactor(new MovingArea(
                        dataPlugin));
                sr.loadFromFunction(f, false);
                for (int i = 0; i < sr.getSectorsCount(); i++) {
                    PaintSector ps = sr.getSector(i);
                    addFont(ps.getFont());
                }
            }
        }, false);
    }

    private void rec(FunctionCallback callback, boolean onlyModels)
            throws IOException {
        Vector<Row> v = dataPlugin.getRecChilds(base, true);
        callback.call(base);
        for (Row row : v) {
            if ((row.getChildCount() == 0) && (!onlyModels))
                callback.call((Function) row);
            else if (row.getChildCount() > 0)
                callback.call((Function) row);
        }
    }

    private interface FunctionCallback {
        void call(Function function) throws IOException;
    }

    private int toByteIndex(Color color) {
        int res = 0;
        double diff = diff(color, COLORS[0]);
        for (int i = 1; i < COLORS.length; i++) {
            double d = diff(color, COLORS[i]);
            if (d < diff) {
                diff = d;
                res = i;
            }
        }
        if (res == 6)
            return 255;
        return res;
    }

    private double diff(Color a, Color b) {
        return s(a.getRed() - b.getRed()) + s(a.getBlue() - b.getBlue())
                + s(a.getGreen() - b.getGreen());
    }

    private double s(int i) {
        return i * i;
    }

    private FunctionMeta getMeta(Function function) {
        FunctionMeta meta = this.meta.get(function);
        if (meta == null) {
            meta = new FunctionMeta();
            this.meta.put(function, meta);
        }
        return meta;
    }

}
