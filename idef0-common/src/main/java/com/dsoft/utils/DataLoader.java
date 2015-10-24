/*
 * Created on 18/7/2005
 */
package com.dsoft.utils;

import java.awt.BasicStroke;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Vector;

import com.dsoft.pb.idef.elements.Status;
import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.ramussoft.idef0.attribute.SectorPointPersistent;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.visual.ArrowedStroke;
import com.ramussoft.pb.idef.visual.WayStroke;

/**
 * @author ZDD
 */
public class DataLoader {

    public static class MemoryData {

        public Vector fonts = new Vector();

        public Vector colors = new Vector();

        public Vector stroukes = new Vector();

        public Vector ordinates = new Vector();

        public Vector crosspoints = new Vector();

        private HashMap<Long, Ordinate> ordinatesHash = new HashMap<Long, Ordinate>();

        public MemoryData() {
            super();
        }

        public Ordinate getXOrdinate(SectorPointPersistent persistent) {
            Ordinate xOrdinate = ordinatesHash.get(persistent.getXOrdinateId());
            if (xOrdinate == null) {
                xOrdinate = new Ordinate(Ordinate.TYPE_X);
                xOrdinate.setPosition(persistent.getXPosition());
                xOrdinate.setOrdinateId(persistent.getXOrdinateId());
                ordinatesHash.put(persistent.getXOrdinateId(), xOrdinate);
            }
            return xOrdinate;
        }

        public Ordinate getYOrdinate(SectorPointPersistent persistent) {
            Ordinate yOrdinate = ordinatesHash.get(persistent.getYOrdinateId());
            if (yOrdinate == null) {
                yOrdinate = new Ordinate(Ordinate.TYPE_Y);
                yOrdinate.setPosition(persistent.getYPosition());
                yOrdinate.setOrdinateId(persistent.getYOrdinateId());
                ordinatesHash.put(persistent.getYOrdinateId(), yOrdinate);
            }
            return yOrdinate;
        }
    }

    public static boolean readBoolean(final InputStream stream)
            throws IOException {
        final int i = stream.read();
        if (i == 0)
            return false;
        else
            return true;
    }

    public static int readByte(final InputStream stream) throws IOException {
        return stream.read();
    }

    public static int readInteger(final InputStream stream) throws IOException {
        int res = 0;
        res |= stream.read();
        res |= stream.read() << 8;
        res |= stream.read() << 16;
        res |= stream.read() << 24;
        return res;
    }

    public static String readString(final InputStream stream)
            throws IOException {
        final int len = readInteger(stream);
        if (len == -1)
            return null;
        final byte[] data = new byte[len];
        stream.read(data);
        final String res = new String(data, "UTF8");
        return res;
    }

    public static Font readFont(final InputStream stream, final MemoryData data)
            throws IOException {
        if (readBoolean(stream))
            return null;
        if (readBoolean(stream)) {
            final String name = readString(stream);
            final int size = readInteger(stream);
            final int style = readInteger(stream);
            final Font r = new Font(name, style, size);
            data.fonts.add(r);
            return r;
        }
        return (Font) data.fonts.get(readInteger(stream));
    }

    public static double readDouble(final InputStream stream)
            throws IOException {
        long res = stream.read();
        res |= (long) stream.read() << 8;
        res |= (long) stream.read() << 16;
        res |= (long) stream.read() << 24;
        res |= (long) stream.read() << 32;
        res |= (long) stream.read() << 40;
        res |= (long) stream.read() << 48;
        res |= (long) stream.read() << 56;
        return Double.longBitsToDouble(res);
    }

    public static Color readColor(final InputStream stream,
                                  final MemoryData data) throws IOException {
        if (readBoolean(stream)) {
            final int r = readInteger(stream);
            final int g = readInteger(stream);
            final int b = readInteger(stream);
            final Color c = new Color(r, g, b);
            data.colors.add(c);
            return c;
        }
        return (Color) data.colors.get(readInteger(stream));
    }

    public static Rectangle readRectangle(final InputStream stream)
            throws IOException {
        final double x = readDouble(stream);
        final double y = readDouble(stream);
        final double w = readDouble(stream);
        final double h = readDouble(stream);
        final Rectangle rec = new Rectangle();
        rec.setFrame(x, y, w, h);
        return rec;
    }

    /**
     * @param stream
     * @return
     * @throws IOException
     */
    public static FRectangle readFRectangle(final InputStream stream)
            throws IOException {
        final double x = readDouble(stream);
        final double y = readDouble(stream);
        final double w = readDouble(stream);
        final double h = readDouble(stream);
        return new FRectangle(x, y, w, h);
    }

    public static FloatPoint readFloatPoint(final InputStream stream)
            throws IOException {
        final boolean isNull = readBoolean(stream);
        if (isNull)
            return null;
        final double x = readDouble(stream);
        final double y = readDouble(stream);
        return new FloatPoint(x, y);
    }

    public static Status readStatus(final InputStream stream)
            throws IOException {
        final Status res = new Status();
        res.setType(stream.read());
        if (res.getType() == Status.OTHER)
            res.setOtherName(readString(stream));
        return res;
    }

    public static Stroke readStroke(final InputStream stream,
                                    final MemoryData data) throws IOException {
        if (readBoolean(stream)) {
            final float lineWidth = (float) readDouble(stream);
            final int ec = readInteger(stream);
            final int lj = readInteger(stream);
            final float dp = (float) readDouble(stream);
            final float ml = (float) readDouble(stream);
            final int len = readInteger(stream);
            float ar[] = null;
            if (len > 0) {
                ar = new float[len];
                for (int i = 0; i < ar.length; i++)
                    ar[i] = (float) readDouble(stream);
            }
            final BasicStroke bs = new BasicStroke(lineWidth, ec, lj, ml, ar,
                    dp);
            data.stroukes.add(bs);
            return bs;
        } else {
            int type = readInteger(stream);
            if (type == -10) {
                WayStroke wayStroke = new WayStroke(-1);
                wayStroke.setType(0);
                return wayStroke;
            }
            if (type == -11) {
                WayStroke wayStroke = new WayStroke(-1);
                wayStroke.setType(1);
                return wayStroke;
            }
            if (type == -12) {
                WayStroke wayStroke = new WayStroke(-1);
                wayStroke.setType(2);
                return wayStroke;
            }
            if (type == -20) {
                ArrowedStroke arrowedStroke = new ArrowedStroke(-1, -1);
                arrowedStroke.setType(0);
                return arrowedStroke;
            }
            if (type == -21) {
                ArrowedStroke arrowedStroke = new ArrowedStroke(-1, -1);
                arrowedStroke.setType(1);
                return arrowedStroke;
            }
            if (type == -22) {
                ArrowedStroke arrowedStroke = new ArrowedStroke(-1, -1);
                arrowedStroke.setType(2);
                return arrowedStroke;
            }
            if (data.stroukes.size() <= type)
                return new BasicStroke();
            return (BasicStroke) data.stroukes.get(type);
        }
    }

    public static Ordinate readOrdinate(final InputStream stream,
                                        final MemoryData data) throws IOException {
        if (readBoolean(stream)) {
            final Ordinate o = Ordinate.loadFromStream(stream);
            data.ordinates.add(o);
            return o;
        } else {
            return (Ordinate) data.ordinates.get(readInteger(stream));
        }
    }

    public static long readLong(final InputStream stream) throws IOException {
        long res = 0;
        res |= stream.read();
        res |= stream.read() << 8;
        res |= stream.read() << 16;
        res |= stream.read() << 24;
        res |= (long) stream.read() << 32;
        res |= (long) stream.read() << 40;
        res |= (long) stream.read() << 48;
        res |= (long) stream.read() << 56;
        return res;
    }

    public static byte[] readBytes(final InputStream stream) throws IOException {
        final byte[] res = new byte[readInteger(stream)];
        stream.read(res);
        return res;
    }
}
