package com.ramussoft.pb.data.negine;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import com.dsoft.pb.idef.elements.Status;
import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.ramussoft.common.Metadata;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.elements.ReplaceStreamType;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingFunction;
import com.ramussoft.pb.idef.visual.MovingLabel;

public class IDLImporter extends IDL {

    private static final String MODELC = "MODEL";

    private static final int KIT = 0;

    private static final int DIAGRAM_GRAPHIC = 3;

    private static final int BOX = 4;

    private static final int ARROWSEG = 5;

    private static final String TITLE = "TITLE";

    private static final String AUTHOR = "AUTHOR";

    private static final String PROJECT_NAME = "PROJECT NAME";

    private static final String F = "F";

    private static final String STATUS = "STATUS";

    private static final String LABEL = "LABEL";

    private static final String SOURCE = "SOURCE";

    private static final String LABAL_COORDINATES = "LABEL COORDINATES";

    private static final String SQUIGGLE_COORDINATES = "SQUIGGLE COORDINATES";

    private static final String SINK = "SINK";

    private static String BOXC = "BOX";

    private static String ENDBOX = "ENDBOX";

    private static String BOX_COORDINATES = "BOX COORDINATES";

    private static String NAME = "NAME";

    private static String DETAIL_REFERENCE = "DETAIL REFERENCE N";

    private static String PATH = "PATH";

    private static String DIAGRAM_GRAPHICC = "DIAGRAM GRAPHIC";

    private static String ENDDIAGRAM = "ENDDIAGRAM";

    private InputStreamReader reader;

    private char[] buff = new char[1024];

    private int cursor = 0;

    private int length = 0;

    private int position = KIT;

    private Function base;

    private Vector<Function> functions = new Vector<Function>();

    private Hashtable<String, Function> hash = new Hashtable<String, Function>();

    private Hashtable<Function, Hashtable<String, Crosspoint>> connections = new Hashtable<Function, Hashtable<String, Crosspoint>>();

    private Box box;

    private ArrayList<Box> boxes = new ArrayList<Box>();

    private Arrowseg seg;

    private ArrayList<Arrowseg> segments = new ArrayList<Arrowseg>();

    private DataPlugin plugin;

    private Hashtable<String, Stream> streams = new Hashtable<String, Stream>();

    private class Box {

        String name;

        String coordinates;

        String reference;

        Function function;

        public int index;
    }

    private class Arrowseg {
        String source;
        String path;
        String label;
        String coordinates;
        String squiggleCoordinates;
        String sink;
        PaintSector sector;
        int index;
        ArrayList<FloatPoint> pointsList;
        boolean seted = false;

        int getPinType(int pin) {
            FloatPoint start = pointsList.get(pin);
            FloatPoint end = pointsList.get(pin + 1);
            if (Math.abs(start.getX() - end.getX()) > Math.abs(start.getY()
                    - end.getY())) {
                return Ordinate.TYPE_X;
            }
            return Ordinate.TYPE_Y;
        }

        int getLastPinType() {
            return getPinType(pointsList.size() - 2);
        }

        int getFirstPinType() {
            return getPinType(0);
        }

        public String getText() {
            if (label == null)
                return null;
            int index = label.indexOf('}');
            if (index < 0)
                return label;
            return removeNs(label.substring(index + 1));
        }
    }

    public IDLImporter(NDataPlugin dataPlugin, Function base, String encoding,
                       DataPlugin plugin) {
        super(dataPlugin, encoding);
        this.base = base;
        this.plugin = plugin;
        this.hash.put("A-0", base);
    }

    public void importFromIDL(InputStream in) throws IOException {

        Vector<Row> streams = plugin.getRecChilds(plugin.getBaseStream(), true);

        for (Row r : streams) {
            Stream stream = (Stream) r;
            if (!stream.isEmptyName()) {
                this.streams.put(stream.getName(), stream);
            }
        }

        reader = new InputStreamReader(in, encoding);
        length = reader.read(buff);
        while (true) {
            String line = readLine();
            if (line == null)
                return;
            switch (position) {
                case KIT:
                    if (starts(MODELC, line)) {
                        String name = valueOf(line);
                        try {
                            base.setName(name);
                        } catch (Exception e) {
                        }
                        plugin.getBaseFunctionQualifier().setName(name);
                        plugin.getEngine().updateQualifier(plugin.getBaseFunctionQualifier());
                        loadFonts(line);
                    } else if (starts(PROJECT_NAME, line)) {
                        String name = valueOf(line);
                        plugin.getBaseFunction().getProjectOptions()
                                .setProjectName(name);
                    } else if (starts(AUTHOR, line)) {
                        String name = valueOf(line);
                        plugin.getBaseFunction().getProjectOptions()
                                .setProjectAutor(name);
                    } else if (starts(DIAGRAM_GRAPHICC, line)) {
                        position = DIAGRAM_GRAPHIC;
                        String name = line.substring(DIAGRAM_GRAPHICC.length() + 1);
                        Function f = getFunction(name);
                        functions.add(f);
                        boxes.clear();
                        segments.clear();
                    }
                    break;

                case DIAGRAM_GRAPHIC:
                    if (starts(TITLE, line)) {
                        String name = valueOf(line);
                        try {
                            getFunction().setName(name);
                        } catch (Exception e) {
                        }
                    } else if (starts(STATUS, line)) {
                        String name = line.substring(STATUS.length() + 1);
                        int typeOf = Status.typeOf(name);
                        Status status = getFunction().getStatus();
                        status.setType(typeOf);
                        if (typeOf < 0) {
                            status.setOtherName(name);
                            status.setType(Status.OTHER);
                        }
                        getFunction().setStatus(getFunction().getStatus());
                    } else if (starts(BOXC, line)) {
                        box = new Box();
                        box.index = Integer.parseInt(getLastWord(line));
                        boxes.add(box);
                        position = BOX;
                    } else if (starts(ENDDIAGRAM, line)) {
                        position = KIT;
                        createSegments();
                        functions.remove(functions.size() - 1);
                    } else if (starts("ARROWSEG", line)) {
                        position = ARROWSEG;
                        seg = new Arrowseg();
                        seg.index = Integer.parseInt(line.substring("ARROWSEG"
                                .length() + 1));
                        segments.add(seg);
                    }
                    break;
                case BOX:
                    if (starts(NAME, line)) {
                        String name = valueOf(line);
                        box.name = name;
                    } else if (starts(BOX_COORDINATES, line)) {
                        box.coordinates = line
                                .substring(BOX_COORDINATES.length() + 1);
                    } else if (starts(DETAIL_REFERENCE, line)) {
                        box.reference = line
                                .substring(DETAIL_REFERENCE.length() + 1);
                    } else if (starts(ENDBOX, line)) {
                        addBox();
                        position = DIAGRAM_GRAPHIC;
                    }
                    break;
                case ARROWSEG:
                    if (starts(SOURCE, line)) {
                        seg.source = line.substring(SOURCE.length() + 1);
                    } else if (starts(PATH, line)) {
                        seg.path = line.substring(PATH.length() + 1);
                    } else if (starts(LABAL_COORDINATES, line)) {
                        seg.coordinates = line
                                .substring(LABAL_COORDINATES.length() + 1);
                    } else if (starts(LABEL, line)) {
                        seg.label = valueOf(line);
                    } else if (starts(SQUIGGLE_COORDINATES, line)) {
                        seg.squiggleCoordinates = line
                                .substring(SQUIGGLE_COORDINATES.length() + 1);
                    } else if (starts(SINK, line)) {
                        seg.sink = line.substring(SINK.length() + 1);
                    } else if (starts("ENDSEG", line)) {
                        position = DIAGRAM_GRAPHIC;
                    }
                    break;
            }
        }
    }

    private void createSegments() {
        MovingArea area = new MovingArea(plugin, getFunction());
        area.setActiveFunction(getFunction());
        for (Arrowseg seg : segments)
            createSegment(seg, area);

        for (Arrowseg seg : segments)
            setSegment(seg, area);

        area.getRefactor().saveToFunction();
    }

    private void setSegment(Arrowseg seg, MovingArea area) {
        if (seg.seted)
            return;
        List<Arrowseg> source = loadConnected(seg.source);
        List<Arrowseg> sink = loadConnected(seg.sink);
        if (source != null) {

            for (Arrowseg s : source) {
                if (!s.seted)
                    setSegment(s, area);
            }
            Crosspoint c = null;

            for (Arrowseg s : source) {
                if (s.sector.getEnd() != null) {
                    c = (NCrosspoint) s.sector.getEnd();
                }
            }
            if (c == null)
                c = dataPlugin.createCrosspoint();
            seg.sector.getSector().getStart().setCrosspointA(c);
            seg.sector.getSector().getStart().commit();
            for (Arrowseg s : source) {
                if (s.sector.getEnd() == null) {
                    s.sector.getSector().getEnd().setCrosspointA(c);
                    s.sector.getSector().getEnd().commit();
                }
            }
        }

        PaintSector ps = seg.sector;
        Point[] points = new Point[seg.pointsList.size()];

        int type = seg.getFirstPinType();

        Ordinate x = null;

        Ordinate y = null;

        if (source != null) {
            for (Arrowseg arrowseg : source) {
                int sType = arrowseg.getLastPinType();
                if (type == sType) {
                    if (arrowseg.sector.getLastPin().getType() == Ordinate.TYPE_X) {
                        y = arrowseg.sector.getLastPin().getOrdinate();
                        x = arrowseg.sector.getLastPin().getPOrdinate();
                    } else {
                        x = arrowseg.sector.getLastPin().getOrdinate();
                        y = arrowseg.sector.getLastPin().getPOrdinate();

                    }
                } else {
                    if (arrowseg.sector.getLastPin().getType() == Ordinate.TYPE_Y) {
                        x = arrowseg.sector.getLastPin().getOrdinate();
                        y = arrowseg.sector.getLastPin().getPOrdinate();
                    } else {
                        y = arrowseg.sector.getLastPin().getOrdinate();
                        x = arrowseg.sector.getLastPin().getPOrdinate();
                    }
                }
            }
        }

        if (x == null) {
            x = new Ordinate(Ordinate.TYPE_X);
            x.setPosition(seg.pointsList.get(0).getX());
        }
        if (y == null) {
            y = new Ordinate(Ordinate.TYPE_Y);
            y.setPosition(seg.pointsList.get(0).getY());
        }
        points[0] = new Point(x, y);
        for (int i = 1; i < seg.pointsList.size(); i++) {
            FloatPoint point = seg.pointsList.get(i);
            type = seg.getPinType(i - 1);

            if (type == Ordinate.TYPE_X) {
                x = new Ordinate(Ordinate.TYPE_X);
                x.setPosition(point.getX());
            } else {
                y = new Ordinate(Ordinate.TYPE_Y);
                y.setPosition(point.getY());
            }

            if ((i == seg.pointsList.size() - 1) && (sink != null)
                    && (sink.size() == 1)) {
                Arrowseg s = sink.get(0);
                List<Arrowseg> list = loadConnected(s.source);
                for (Arrowseg arrowseg : list) {
                    if (arrowseg.seted) {
                        Point point2 = arrowseg.sector.getLastPin().getEnd();
                        Ordinate xo = point2.getXOrdinate();
                        for (Point point3 : xo.getPoints()) {
                            point3.setXOrdinate(x);
                        }
                        Ordinate yo = point2.getYOrdinate();
                        for (Point point3 : yo.getPoints()) {
                            point3.setYOrdinate(y);
                        }
                    }
                }
            }

            points[i] = new Point(x, y);
        }

        ps.setPoints(points);

        if (source == null)
            initCrosspoint(seg.sector.getSector().getStart(), seg.source, area,
                    seg.sector, true, seg);

        if (sink == null) {
            initCrosspoint(seg.sector.getSector().getEnd(), seg.sink, area,
                    seg.sector, false, seg);
        }

        seg.seted = true;
    }

    private void initCrosspoint(NSectorBorder border, String sBorder,
                                MovingArea area, PaintSector ps, boolean start, Arrowseg arrowseg) {
        if (sBorder.indexOf("BOX") >= 0) {

            String string = getLastWord(sBorder);
            Box box = getBox(string);

            int type = getType(string);
            Crosspoint cp = dataPlugin.createCrosspoint();
            border.setCrosspointA(cp);
            border.setFunctionTypeA(type);
            border.setFunctionA(box.function);

            border.commit();

            int i = 0;
            while (Character.isDigit(string.charAt(i)))
                i++;
            if (box.reference == null) {
                area.getRefactor().createSectorOnIn(ps, start);
            } else {
                Hashtable<String, Crosspoint> h = getConnections(box.function);
                h.put(string.substring(i), cp);
            }
        } else {
            int pos = sBorder.indexOf("BORDER");
            StringTokenizer st = new StringTokenizer(sBorder.substring(pos));
            st.nextToken();
            int type = -1;
            if (st.hasMoreElements()) {
                String string = st.nextToken();
                type = getType(string);
                Hashtable<String, Crosspoint> h = getConnections(getFunction());
                Crosspoint cp = h.get(string);

                if (cp == null)
                    cp = dataPlugin.createCrosspoint();
                border.setCrosspointA(cp);
                border.setBorderTypeA(type);

                border.commit();
            } else {
                if (start) {

                    String sink = arrowseg.sink;
                    int index = sink.indexOf("BORDER");
                    if (index >= 0) {
                        StringTokenizer s = new StringTokenizer(
                                sink.substring(index));
                        s.nextToken();
                        if (s.hasMoreTokens())
                            return;
                    }

                    type = ps.getPin(0).getWayType();
                } else {
                    if (ps.getSector().getStart().getBorderType() >= 0)
                        return;
                    type = ps.getLastPin().getWayType();
                }
                Crosspoint cp = dataPlugin.createCrosspoint();
                border.setCrosspointA(cp);
                border.setBorderTypeA(type);
            }
        }

    }

    private int getType(String string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (!Character.isDigit(c)) {
                if (c == 'I')
                    return MovingFunction.LEFT;
                if (c == 'O')
                    return MovingFunction.RIGHT;
                if (c == 'C')
                    return MovingFunction.TOP;
                if (c == 'M')
                    return MovingFunction.BOTTOM;
            }
        }
        return 0;
    }

    private Box getBox(String string) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isDigit(c))
                sb.append(c);
            else
                break;
        }
        return getBox(Integer.parseInt(sb.toString()));
    }

    private String getLastWord(String string) {
        String res = null;
        StringTokenizer st = new StringTokenizer(string);
        while (st.hasMoreTokens())
            res = st.nextToken();
        return res;
    }

    private List<Arrowseg> loadConnected(String source2) {
        List<Arrowseg> source;
        if (starts("BRANCH", source2)) {
            source = new ArrayList<Arrowseg>();
            StringTokenizer st = new StringTokenizer(source2.substring("BRANCH"
                    .length() + 1), " ");
            source = new ArrayList<Arrowseg>();
            while (st.hasMoreTokens()) {
                source.add(getSegment(Integer.parseInt(st.nextToken())));
            }
        } else if (starts("JOIN", source2)) {
            source = new ArrayList<Arrowseg>();
            StringTokenizer st = new StringTokenizer(source2.substring("JOIN"
                    .length() + 1), " ");
            source = new ArrayList<Arrowseg>();
            while (st.hasMoreTokens()) {
                source.add(getSegment(Integer.parseInt(st.nextToken())));
            }
        } else
            return null;
        return source;
    }

    private void createSegment(Arrowseg seg, MovingArea area) {
        SectorRefactor sr = area.getRefactor();
        Sector s = plugin.createSector();
        Function f = getFunction();
        s.setFunction(f);

        PaintSector ps = new PaintSector();
        ps.setSector(s);
        ps.setMovingArea(area);
        sr.addSector(ps);
        seg.sector = ps;

        ArrayList<FloatPoint> list = new ArrayList<FloatPoint>();
        StringTokenizer st = new StringTokenizer(seg.path, "() ");
        while (st.hasMoreElements())
            list.add(toPoint(st.nextToken()));

        seg.pointsList = list;

        String text = seg.getText();
        if ((text != null) && (text.length() > 0)) {
            s.setStream(getStream(text), ReplaceStreamType.CHILDREN);
            if (seg.coordinates != null) {
                MovingLabel label = new MovingLabel(area);
                StringTokenizer st2 = new StringTokenizer(seg.coordinates, "()");
                FloatPoint point = toPoint(st2.nextToken());
                label.getBounds().setX(point.getX());
                label.getBounds().setY(point.getY() - 10);
                label.setText(text);
                ps.setText(label);
                if (seg.squiggleCoordinates != null) {
                    ps.setShowTilda(true);
                }
            }
            if (seg.label.startsWith("{")) {
                StringTokenizer st2 = new StringTokenizer(seg.label, " ");
                st2.nextElement();
                st2.nextElement();
                try {
                    int font = Integer.parseInt(st2.nextToken());
                    if (font < uniqueFonts.size())
                        ps.setFont(uniqueFonts.get(font));
                } catch (Exception e) {
                }

                int color = Integer.parseInt(st2.nextToken());
                if (color < COLORS.length)
                    ps.setColor(COLORS[color]);
            }
        }
    }

    private Arrowseg getSegment(int index) {
        for (Arrowseg seg : segments)
            if (seg.index == index)
                return seg;
        return null;
    }

    private void addBox() {
        Function function = getFunction(box.reference);
        int i = box.name.indexOf('}');
        box.function = function;
        function.setName(box.name.substring(i + 1));
        StringTokenizer st = new StringTokenizer(box.name.substring(1, i), " ");
        if (st.hasMoreTokens())
            st.nextElement();
        if (st.hasMoreTokens()) {
            try {
                int font = Integer.parseInt(st.nextToken());
                function.setFont(uniqueFonts.get(font));
            } catch (Exception e) {
            }
        }
        if (st.hasMoreTokens())
            st.nextElement();

        Color bColor = null;
        Color fColor = null;

        if (st.hasMoreTokens()) {
            int tmp = Integer.parseInt(st.nextToken());
            if (tmp < COLORS.length) {
                fColor = COLORS[tmp];
            }
            tmp = Integer.parseInt(st.nextToken());
            if (tmp < COLORS.length) {
                bColor = COLORS[tmp];
            }
        }

        if (bColor != null)
            function.setBackground(bColor);
        if (fColor != null)
            function.setForeground(fColor);
        StringTokenizer tokenizer = new StringTokenizer(box.coordinates, " ()");
        FloatPoint p1 = toPoint(tokenizer.nextToken());
        FloatPoint p2 = toPoint(tokenizer.nextToken());
        FRectangle rectangle = new FRectangle(p1.getX(), p2.getY(), p2.getX()
                - p1.getX(), p1.getY() - p2.getY());
        function.setBounds(rectangle);
        Status status = new Status(Status.WORKING, "");

        function.setStatus(status);
    }

    private Function getFunction(String name) {
        Function f = null;
        if (name != null)
            f = hash.get(name);
        if (f == null) {
            Function function = getFunction();
            f = (Function) plugin.createRow(function, true);
            if (name != null)
                hash.put(name, f);
        }
        return f;
    }

    private Function getFunction() {
        return functions.get(functions.size() - 1);
    }

    private void loadFonts(String line) {
        StringTokenizer st = new StringTokenizer(line, "\n\r");
        while (st.hasMoreElements()) {
            String token = st.nextToken();
            if (token.trim().equals("{LWI"))
                break;
        }
        while (st.hasMoreElements()) {
            String token = st.nextToken().trim();
            if (starts(F, token)) {
                int size = 10;
                StringTokenizer st2 = new StringTokenizer(token, " ");
                if (st.hasMoreElements())
                    st2.nextToken();
                if (st2.hasMoreElements())
                    st2.nextToken();
                if (st2.hasMoreElements()) {
                    String sSize = st2.nextToken();
                    if (sSize.startsWith("-")) {
                        try {
                            size = Integer.parseInt(sSize.substring(1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (st.hasMoreElements())
                    st.nextElement();
                if (st.hasMoreElements())
                    token = st.nextToken();
                String name = valueOf(token);
                addFont(new Font(name, 0, size));
            }
        }
    }

    private boolean isEnd() {
        return cursor >= length;
    }

    private char read() throws IOException {
        try {
            return buff[cursor];
        } finally {
            cursor++;
            if (isEnd()) {
                length = reader.read(buff);
                cursor = 0;
            }
        }
    }

    private String readLine() throws IOException {
        if (isEnd())
            return null;
        StringBuffer sb = new StringBuffer();

        boolean inP = false;

        while (!isEnd()) {
            char c = read();
            if (inP) {
                if ((c == '\r') || (c == '\n'))
                    continue;
                if (c == '\\') {
                    c = read();
                } else if (c == '\'')
                    inP = false;
            } else {
                if (c == ';') {
                    c = read();
                    if ((c == '\n') || (c == '\r') || (c == ' ')) {
                        break;
                    } else
                        sb.append(';');
                } else if (c == '\'')
                    inP = true;
            }
            if (((c == '\r') || (c == '\n'))
                    && (sb.toString().trim().startsWith(LABEL)))
                continue;
            sb.append(c);
        }

        if (Metadata.DEBUG)
            System.out.println(sb);

        return removeCRs(sb.toString().trim());
    }

    private String removeCRs(String s) {
        StringBuffer sb = new StringBuffer();
        boolean slash = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c == '<') && (!slash)) {
                if (i + 4 <= s.length()) {
                    String sub = s.substring(i, i + 4);
                    if (sub.equals("<CR>")) {
                        sb.append(' ');
                        i += 3;
                        continue;
                    }
                }
            }
            if (c == '\\') {
                slash = true;
            } else {
                slash = false;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private boolean starts(String key, String value) {
        return value.toUpperCase().startsWith(key);
    }

    private String valueOf(String line) {
        StringBuffer res = new StringBuffer();
        boolean inP = false;
        boolean next = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (next) {
                res.append(c);
                next = false;
            } else if (inP) {
                if (c == '\\') {
                    next = true;
                } else if (c == '\'')
                    break;
                else
                    res.append(c);
            } else if (c == '\'') {
                inP = true;
            }
        }
        return res.toString();
    }

    private String removeNs(String source) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if ((c != '\n') && (c != '\r'))
                sb.append(c);
        }
        return sb.toString();
    }

    private FloatPoint toPoint(String coordinates) {
        coordinates = removeNs(coordinates);
        String left;
        String right;
        String split = ",";
        if (coordinates.indexOf(';') >= 0) {
            split = ";";
            coordinates = coordinates.replace(',', '.');
        }
        String[] strings = coordinates.split(split);
        left = strings[0];
        right = strings[1];
        return new FloatPoint((Double.parseDouble(left) - X_ADD) * WIDTH,
                (Double.parseDouble(right) - Y_ADD) * HEIGHT);
    }

    private Box getBox(int index) {
        for (Box box : boxes)
            if (box.index == index)
                return box;
        return null;
    }

    private Hashtable<String, Crosspoint> getConnections(Function function) {
        Hashtable<String, Crosspoint> hash = connections.get(function);
        if (hash == null) {
            hash = new Hashtable<String, Crosspoint>();
            connections.put(function, hash);
        }
        return hash;
    }

    private Stream getStream(String name) {
        Stream stream = streams.get(name);
        if (stream == null) {
            stream = (Stream) plugin.createRow(plugin.getBaseStream(), true);
            stream.setName(name);
            streams.put(name, stream);
        }
        return stream;
    }
}
