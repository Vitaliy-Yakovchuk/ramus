package com.ramussoft.storage;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingText;
import com.ramussoft.pb.print.PIDEF0painter;

public final class DiagramRenderer {

    private static final Dimension SIZE = new Dimension(1200, 900);

    private DiagramRenderer() {
    }

    public static Map<String, String> render(Engine engine, AccessRules rules)
            throws IOException {
        Map<String, String> result = new LinkedHashMap<String, String>();

        List<Qualifier> models = new ArrayList<Qualifier>(
                IDEF0Plugin.getBaseQualifiers(engine));
        Collections.sort(models, new java.util.Comparator<Qualifier>() {
            @Override
            public int compare(Qualifier a, Qualifier b) {
                return a.getName().compareTo(b.getName());
            }
        });

        for (Qualifier model : models) {
            DataPlugin plugin = NDataPluginFactory.getDataPlugin(model, engine,
                    rules);
            Function base = plugin.getBaseFunction();
            if (base == null)
                continue;
            prepare(base);
            renderRecursive(plugin, base, model.getName(), result);
        }
        return result;
    }

    static void prepare(Function base) {
        RsfFixture.freezeDates(base);
        useTestFont(base);
    }

    private static void useTestFont(Function function) {
        Font font = function.getFont();
        if (font != null)
            function.setFont(TestFonts.like(font));
        for (int i = 0; i < function.getChildCount(); i++)
            useTestFont((Function) function.getChildAt(i));
    }

    private static void useTestFont(MovingArea area) {
        SectorRefactor refactor = area.getRefactor();
        for (int i = 0; i < refactor.getSectorsCount(); i++) {
            PaintSector sector = refactor.getSector(i);
            sector.setFont(TestFonts.like(sector.getFont()));
            MovingText text = sector.getText();
            if (text != null)
                text.setFont(TestFonts.like(text.getFont()));
        }
        for (int i = 0; i < refactor.getTextCount(); i++) {
            MovingText text = refactor.getText(i);
            text.setFont(TestFonts.like(text.getFont()));
        }
    }

    private static void renderRecursive(DataPlugin plugin, Function function,
                                        String path, Map<String, String> out)
            throws IOException {
        out.put(path, fingerprint(plugin, function));
        for (int i = 0; i < function.getChildCount(); i++) {
            Function child = (Function) function.getChildAt(i);
            renderRecursive(plugin, child, path + "/" + child.getName(), out);
        }
    }

    private static String fingerprint(DataPlugin plugin, Function function)
            throws IOException {
        PIDEF0painter painter = new PIDEF0painter(function, SIZE, plugin);
        useTestFont(painter.getMovingArea());
        return signature(painter.createImage(
                TestFonts.of(Font.PLAIN, DEFAULT_FONT_SIZE)));
    }

    private static final int DEFAULT_FONT_SIZE = 12;

    static List<Font> paintedFonts(DataPlugin plugin, Function function) {
        List<Font> fonts = new ArrayList<Font>();
        fonts.add(TestFonts.of(Font.PLAIN, DEFAULT_FONT_SIZE));
        collectFonts(function, fonts);

        PIDEF0painter painter = new PIDEF0painter(function, SIZE, plugin);
        useTestFont(painter.getMovingArea());
        SectorRefactor refactor = painter.getMovingArea().getRefactor();
        for (int i = 0; i < refactor.getSectorsCount(); i++) {
            PaintSector sector = refactor.getSector(i);
            fonts.add(sector.getFont());
            if (sector.getText() != null)
                fonts.add(sector.getText().getFont());
        }
        for (int i = 0; i < refactor.getTextCount(); i++)
            fonts.add(refactor.getText(i).getFont());
        return fonts;
    }

    private static void collectFonts(Function function, List<Font> fonts) {
        if (function.getFont() != null)
            fonts.add(function.getFont());
        for (int i = 0; i < function.getChildCount(); i++)
            collectFonts((Function) function.getChildAt(i), fonts);
    }

    static final int SIGNATURE_SIDE = 48;

    private static String signature(BufferedImage image) {
        BufferedImage small = new BufferedImage(SIGNATURE_SIDE, SIGNATURE_SIDE,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = small.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, SIGNATURE_SIDE, SIGNATURE_SIDE, null);
        g.dispose();

        StringBuilder sb = new StringBuilder(SIGNATURE_SIDE * SIGNATURE_SIDE * 2);
        for (int y = 0; y < SIGNATURE_SIDE; y++)
            for (int x = 0; x < SIGNATURE_SIDE; x++) {
                int rgb = small.getRGB(x, y);
                int grey = (((rgb >> 16) & 0xff) * 299
                        + ((rgb >> 8) & 0xff) * 587 + (rgb & 0xff) * 114) / 1000;
                sb.append(String.format("%02x", Integer.valueOf(grey)));
            }
        return sb.toString();
    }

    public static double difference(String a, String b) {
        if (a.length() != b.length())
            return 255.0;
        long sum = 0;
        int count = a.length() / 2;
        for (int i = 0; i < count; i++) {
            int left = Integer.parseInt(a.substring(i * 2, i * 2 + 2), 16);
            int right = Integer.parseInt(b.substring(i * 2, i * 2 + 2), 16);
            sum += Math.abs(left - right);
        }
        return (double) sum / count;
    }
}
