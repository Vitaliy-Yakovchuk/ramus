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
import java.io.OutputStream;

import com.dsoft.pb.idef.elements.Status;
import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.dsoft.utils.DataLoader.MemoryData;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.visual.ArrowedStroke;
import com.ramussoft.pb.idef.visual.WayStroke;

/**
 * @author ZDD
 */
public class DataSaver {

    public static void saveBoolean(final OutputStream stream, final boolean i)
            throws IOException {
        stream.write(i ? 1 : 0);
    }

    public static void saveByte(final OutputStream stream, final int i)
            throws IOException {
        stream.write(i);
    }

    public static void saveInteger(final OutputStream stream, final int i)
            throws IOException {
        stream.write(i & 255);
        stream.write(i >> 8 & 255);
        stream.write(i >> 16 & 255);
        stream.write(i >> 24 & 255);
    }

    public static void saveString(final OutputStream stream, final String data)
            throws IOException {
        if (data == null)
            saveInteger(stream, -1);
        else {
            final byte bytes[] = data.getBytes("UTF8");
            saveInteger(stream, bytes.length);
            stream.write(bytes);
        }
    }

    public static void saveFont(final OutputStream stream, final Font data,
                                final MemoryData memoryData) throws IOException {
        if (data == null) {
            saveBoolean(stream, true);
            return;
        } else
            saveBoolean(stream, false);
        final int index = memoryData.fonts.indexOf(data);
        if (index < 0) {
            saveBoolean(stream, true);
            saveString(stream, data.getName());
            saveInteger(stream, data.getSize());
            saveInteger(stream, data.getStyle());
            memoryData.fonts.add(data);
        } else {
            saveBoolean(stream, false);
            saveInteger(stream, index);
        }
    }

    public static void saveDouble(final OutputStream stream, final double i)
            throws IOException {
        final long l = Double.doubleToLongBits(i);
        stream.write((int) (l & 255));
        stream.write((int) (l >> 8 & 255));
        stream.write((int) (l >> 16 & 255));
        stream.write((int) (l >> 24 & 255));
        stream.write((int) (l >> 32 & 255));
        stream.write((int) (l >> 40 & 255));
        stream.write((int) (l >> 48 & 255));
        stream.write((int) (l >> 56 & 255));
    }

    public static void saveColor(final OutputStream stream, final Color data,
                                 final MemoryData memoryData) throws IOException {
        final int index = memoryData.colors.indexOf(data);
        if (index < 0) {
            saveBoolean(stream, true);
            saveInteger(stream, data.getRed());
            saveInteger(stream, data.getGreen());
            saveInteger(stream, data.getBlue());
            memoryData.colors.add(data);
        } else {
            saveBoolean(stream, false);
            saveInteger(stream, index);
        }
    }

    public static void saveRectangle(final OutputStream stream,
                                     final Rectangle data) throws IOException {
        saveDouble(stream, data.getX());
        saveDouble(stream, data.getY());
        saveDouble(stream, data.getWidth());
        saveDouble(stream, data.getHeight());
    }

    public static void saveFRectangle(final OutputStream stream,
                                      final FRectangle data) throws IOException {
        saveDouble(stream, data.getX());
        saveDouble(stream, data.getY());
        saveDouble(stream, data.getWidth());
        saveDouble(stream, data.getHeight());
    }

    public static void saveFloatPoint(final OutputStream stream,
                                      final FloatPoint point) throws IOException {
        if (point == null)
            saveBoolean(stream, true);
        else {
            saveBoolean(stream, false);
            saveDouble(stream, point.getX());
            saveDouble(stream, point.getY());
        }
    }

    public static void saveStatus(final OutputStream stream, final Status status)
            throws IOException {
        stream.write(status.getType());
        if (status.getType() == Status.OTHER)
            saveString(stream, status.getAtherName());
    }

    public static void saveStroke(final OutputStream stream,
                                  final Stroke stroke, final MemoryData memoryData)
            throws IOException {
        if (stroke instanceof BasicStroke) {
            BasicStroke basickStroke = (BasicStroke) stroke;
            saveBoolean(stream, true);

            saveDouble(stream, basickStroke.getLineWidth());
            saveInteger(stream, basickStroke.getEndCap());
            saveInteger(stream, basickStroke.getLineJoin());
            saveDouble(stream, basickStroke.getDashPhase());
            saveDouble(stream, basickStroke.getMiterLimit());
            final float ar[] = basickStroke.getDashArray();
            if (ar == null)
                saveInteger(stream, -1);
            else {
                saveInteger(stream, ar.length);
                for (final float element : ar)
                    saveDouble(stream, element);
            }

            memoryData.stroukes.add(stroke);
        } else if (stroke instanceof WayStroke) {
            saveBoolean(stream, false);
            saveInteger(stream, -10 - ((WayStroke) stroke).getType());
        } else if (stroke instanceof ArrowedStroke) {
            saveBoolean(stream, false);
            saveInteger(stream, -20 - ((ArrowedStroke) stroke).getType());
        }
    }

    public static void saveOrdinate(final OutputStream stream,
                                    final Ordinate ordinate, final MemoryData memoryData)
            throws IOException {
        final int index = memoryData.ordinates.indexOf(ordinate);
        if (index < 0) {
            saveBoolean(stream, true);
            Ordinate.saveToStream(stream, ordinate);
            memoryData.ordinates.add(ordinate);
        } else {
            saveBoolean(stream, false);
            saveInteger(stream, index);
        }
    }

    public static void saveLong(final OutputStream stream, final long l)
            throws IOException {
        stream.write((int) (l & 255));
        stream.write((int) (l >> 8 & 255));
        stream.write((int) (l >> 16 & 255));
        stream.write((int) (l >> 24 & 255));
        stream.write((int) (l >> 32 & 255));
        stream.write((int) (l >> 40 & 255));
        stream.write((int) (l >> 48 & 255));
        stream.write((int) (l >> 56 & 255));
    }

    public static void saveBytes(final OutputStream stream, final byte[] i)
            throws IOException {
        saveInteger(stream, i.length);
        stream.write(i);
    }
}
