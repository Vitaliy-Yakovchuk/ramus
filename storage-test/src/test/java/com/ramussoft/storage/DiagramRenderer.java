package com.ramussoft.storage;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
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
import com.ramussoft.pb.print.PIDEF0painter;

/**
 * Малює діаграми моделі в PNG без екрана.
 * <p>
 * Потрібен, щоб зміни в коді відображення (винесення блоба, розділення
 * семантики й розкладки) можна було перевіряти автоматично: якщо картинка
 * змінилась — тест це побачить, а не людина під час клікання.
 */
public final class DiagramRenderer {

    /**
     * Розмір навмисно фіксований: масштаб рахується від нього, і будь-яка
     * залежність від розміру вікна зробила б знімки невідтворюваними.
     */
    private static final Dimension SIZE = new Dimension(1200, 900);

    private DiagramRenderer() {
    }

    /**
     * @return «шлях діаграми → відбиток зображення», відсортовано за шляхом
     */
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
            renderRecursive(plugin, base, model.getName(), result);
        }
        return result;
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

    /**
     * Стиснутий відбиток зображення: сірий {@value #SIGNATURE_SIDE}×{@value
     * #SIGNATURE_SIDE}.
     * <p>
     * Точний хеш PNG для еталона не годиться: відмальовування має ділянки, що
     * залежать від порядку обходу множин, і одна діаграма зі зразків між
     * запусками JVM дає різні байти при однаковій картинці. Стиснутий відбиток
     * такі дрібниці поглинає, але зсув блоку чи зникнення стрілки — ні.
     */
    private static String fingerprint(DataPlugin plugin, Function function)
            throws IOException {
        PIDEF0painter painter = new PIDEF0painter(function, SIZE, plugin);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        painter.writeToStream(out, PIDEF0painter.PNG_FORMAT);
        BufferedImage image = ImageIO.read(
                new java.io.ByteArrayInputStream(out.toByteArray()));
        if (image == null)
            throw new IOException("Не вдалося прочитати відмальоване зображення");
        return signature(image);
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

    /**
     * Середня різниця яскравості між двома відбитками, 0..255.
     */
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
