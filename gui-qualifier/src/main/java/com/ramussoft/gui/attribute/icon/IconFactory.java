package com.ramussoft.gui.attribute.icon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.simple.IconPersistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.prefrence.Options;

public class IconFactory {

    private static File iconsDirectory;

    static {
        String path = Options.getPreferencesPath() + "icons";
        iconsDirectory = new File(path);
        iconsDirectory.mkdirs();
        try {
            File file = new File(iconsDirectory, "default.zip");
            if (!file.exists())
                copy(new IconFactory().getClass().getResourceAsStream(
                        "/com/ramussoft/gui/icons.zip"), new FileOutputStream(
                        file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getIconsDirectory() {
        return iconsDirectory;
    }

    public static void clearQualifierIconsBuffer(Engine engine) {
        engine.setPluginProperty("Core", "LeafIcons", null);
        engine.setPluginProperty("Core", "OpenIcons", null);
        engine.setPluginProperty("Core", "ClosedIcons", null);
    }

    public static void setOpenIcon(Engine engine, IconPersistent p,
                                   Qualifier qualifier) {
        synchronized (engine) {
            Hashtable<Long, ImageIcon> icons = getOpenIcons(engine);
            Attribute attribute = StandardAttributesPlugin
                    .getOpenIconsAttribute(engine);
            updateIcons(engine, p, qualifier, icons, attribute);
        }
    }

    private static void updateIcons(Engine engine, IconPersistent p,
                                    Qualifier qualifier, Hashtable<Long, ImageIcon> icons,
                                    Attribute attribute) {
        if (p == null)
            p = new IconPersistent();
        if (p.getIcon() == null)
            icons.remove(qualifier.getId());
        else
            icons.put(qualifier.getId(), new ImageIcon(p.getIcon()));
        long index = getIconIndex(engine, p);
        Element element = engine.getElement(index);

        Qualifier q = StandardAttributesPlugin.getIconsQualifier(engine);
        List<Attribute> attrs = new ArrayList<Attribute>(1);
        attrs.add(attribute);
        Hashtable<Element, Object[]> res = engine.getElements(q, attrs);
        Enumeration<Element> keys = res.keys();

        long id = qualifier.getId();

        while (keys.hasMoreElements()) {
            Element e = keys.nextElement();
            String c = (String) res.get(e)[0];
            if (c == null) {
                c = "";
            }
            String c1 = removeId(c, id);
            if (!c1.equals(c)) {
                engine.setAttribute(e, attribute, c1);
            }
        }

        if (element != null) {
            String c = (String) engine.getAttribute(element, attribute);

            if (p.getIcon() != null) {
                c = addId(c, qualifier.getId());
                engine.setAttribute(element, attribute, c);
            }
        }
    }

    public static void setLeafIcon(Engine engine, IconPersistent p,
                                   Qualifier qualifier) {
        Hashtable<Long, ImageIcon> icons = getLeafIcons(engine);
        updateIcons(engine, p, qualifier, icons, StandardAttributesPlugin
                .getLeafIconsAttribute(engine));
    }

    public static void setClosedIcon(Engine engine, IconPersistent p,
                                     Qualifier qualifier) {
        Hashtable<Long, ImageIcon> icons = getClosedIcons(engine);
        updateIcons(engine, p, qualifier, icons, StandardAttributesPlugin
                .getClosedIconsAttribute(engine));
    }

    public static void setIcon(Engine engine, IconPersistent p, Row row) {
        synchronized (engine) {
            row.getHierarchicalPersistent().setIconId(getIconIndex(engine, p));
            row.setAttribute(row.getRowSet().getHAttribute(), row
                    .getHierarchicalPersistent());
        }
    }

    public static Hashtable<Long, ImageIcon> getLeafIcons(Engine engine) {
        synchronized (engine) {
            return getDefaultIcons(engine, "LeafIcons",
                    StandardAttributesPlugin.getLeafIconsAttribute(engine));
        }
    }

    public static Hashtable<Long, ImageIcon> getClosedIcons(Engine engine) {
        synchronized (engine) {
            return getDefaultIcons(engine, "ClosedIcons",
                    StandardAttributesPlugin.getClosedIconsAttribute(engine));
        }
    }

    public static Hashtable<Long, ImageIcon> getOpenIcons(Engine engine) {
        synchronized (engine) {
            return getDefaultIcons(engine, "OpenIcons",
                    StandardAttributesPlugin.getOpenIconsAttribute(engine));
        }
    }

    @SuppressWarnings("unchecked")
    private static Hashtable<Long, ImageIcon> getDefaultIcons(Engine engine,
                                                              String key, Attribute attribute) {
        Hashtable<Long, ImageIcon> result = (Hashtable<Long, ImageIcon>) engine
                .getPluginProperty("Core", key);
        if (result == null) {
            result = new Hashtable<Long, ImageIcon>();

            Qualifier qualifier = StandardAttributesPlugin
                    .getIconsQualifier(engine);
            List<Attribute> attributes = new ArrayList<Attribute>(2);
            Attribute iconsAttribute = StandardAttributesPlugin
                    .getIconsAttribute(engine);
            attributes.add(iconsAttribute);
            attributes.add(attribute);
            Hashtable<Element, Object[]> res = engine.getElements(qualifier,
                    attributes);

            Enumeration<Element> keys = res.keys();
            while (keys.hasMoreElements()) {
                Element element = keys.nextElement();
                Object[] objects = res.get(element);
                if ((objects[0] != null)
                        && (((IconPersistent) objects[0]).getIcon() != null)
                        && (objects[1] != null)) {
                    StringTokenizer st = new StringTokenizer(
                            (String) objects[1]);
                    ImageIcon icon = new ImageIcon(
                            ((IconPersistent) objects[0]).getIcon());
                    while (st.hasMoreElements()) {
                        result.put(Long.parseLong(st.nextToken()), icon);
                    }
                }
            }

            engine.setPluginProperty("Core", key, result);
        }
        return result;
    }

    private static String removeId(String c, long id) {
        if (c == null)
            c = "";
        StringTokenizer st = new StringTokenizer(c);
        StringBuffer res = new StringBuffer();
        String sId = Long.toString(id);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (!sId.equals(token)) {
                res.append(token);
                res.append(' ');
            }
        }
        return res.toString();
    }

    private static String addId(String c, long id) {
        if (c == null)
            c = "";
        StringTokenizer st = new StringTokenizer(c);
        StringBuffer res = new StringBuffer();
        String sId = Long.toString(id);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (sId.equals(token)) {
                return c;
            }
            res.append(token);
            res.append(' ');
        }
        res.append(Long.toString(id));
        res.append(' ');
        return res.toString();
    }

    private static long getIconIndex(Engine engine, IconPersistent p) {
        if ((p == null) || (p.getIcon() == null))
            return -1l;
        Qualifier qualifier = StandardAttributesPlugin
                .getIconsQualifier(engine);
        List<Attribute> attributes = new ArrayList<Attribute>(1);
        Attribute iconsAttribute = StandardAttributesPlugin
                .getIconsAttribute(engine);
        attributes.add(iconsAttribute);
        Hashtable<Element, Object[]> res = engine.getElements(qualifier,
                attributes);
        Enumeration<Element> e = res.keys();
        long index = -1l;
        while (e.hasMoreElements()) {
            Element element = e.nextElement();
            IconPersistent ip = (IconPersistent) res.get(element)[0];
            if (ip.getName().equals(p.getName())) {
                index = element.getId();
                break;
            }
        }
        if (index < 0l) {
            Element element = engine.createElement(qualifier.getId());
            engine.setAttribute(element, iconsAttribute, p);
            index = element.getId();
            getIcons(engine).put(index, new ImageIcon(p.getIcon()));
        }
        return index;
    }

    private static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] buff = new byte[1024 * 128];
        int r;
        while ((r = in.read(buff)) > 0) {
            out.write(buff, 0, r);
        }
        out.close();
        in.close();
    }

    public static void clearIconsBuffer(Engine engine) {
        synchronized (engine) {
            engine.setPluginProperty("Core", "Icons buffer", null);
        }
    }

    @SuppressWarnings("unchecked")
    public static Hashtable<Long, ImageIcon> getIcons(Engine engine) {
        synchronized (engine) {
            Hashtable<Long, ImageIcon> res = (Hashtable<Long, ImageIcon>) engine
                    .getPluginProperty("Core", "Icons buffer");
            if (res == null) {
                res = new Hashtable<Long, ImageIcon>();
                engine.setPluginProperty("Core", "Icons buffer", res);
                Qualifier qualifier = StandardAttributesPlugin
                        .getIconsQualifier(engine);
                Attribute attribute = StandardAttributesPlugin
                        .getIconsAttribute(engine);
                List<Attribute> list = new ArrayList<Attribute>(1);
                list.add(attribute);
                Hashtable<Element, Object[]> hash = engine.getElements(
                        qualifier, list);
                Enumeration<Element> keys = hash.keys();
                while (keys.hasMoreElements()) {
                    Element element = keys.nextElement();
                    res.put(element.getId(), new ImageIcon(
                            ((IconPersistent) hash.get(element)[0]).getIcon()));
                }
            }
            return res;
        }
    }
}
