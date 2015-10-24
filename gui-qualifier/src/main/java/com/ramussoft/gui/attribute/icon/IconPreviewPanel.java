package com.ramussoft.gui.attribute.icon;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ramussoft.core.attribute.simple.IconPersistent;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

public class IconPreviewPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -3578026656714657412L;

    private static ArrayList<IconPersistent> data = null;

    private ArrayList<IconPersistent> lData = null;

    private static ImageIcon[] images = null;

    private ImageIcon[] lImages = null;

    private JList list;

    private IconPersistent res;

    private File dir;

    private final AbstractListModel dataModel = new AbstractListModel() {

        /**
         *
         */
        private static final long serialVersionUID = -385384784930955128L;

        @Override
        public Object getElementAt(int index) {
            return index;
        }

        @Override
        public int getSize() {
            return lData.size();
        }

    };

    private class IconRenderer extends DefaultListCellRenderer {

        /**
         *
         */
        private static final long serialVersionUID = -3001570896044310933L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int aIndex, boolean isSelected, boolean cellHasFocus) {
            int index = ((Integer) value).intValue();
            Component res = super.getListCellRendererComponent(list, lData.get(
                    index).getName(), aIndex, isSelected, cellHasFocus);
            if (index > 0) {
                if (lImages[index] == null) {
                    lImages[index] = new ImageIcon(lData.get(index).getIcon());
                }
                setIcon(lImages[index]);
            } else
                setIcon(null);
            return res;
        }

    }

    ;

    public IconPreviewPanel(File dir) {
        super(new BorderLayout());
        this.dir = dir;
        try {
            loadImages();
        } catch (IOException e) {
            e.printStackTrace();
        }
        init();
    }

    private void init() {
        JScrollPane pane = new JScrollPane();
        list = new JList(dataModel);
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F5) {
                    try {
                        data = null;
                        loadImages();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, e1
                                .getLocalizedMessage());
                    }
                    list.setModel(dataModel);
                    list.updateUI();
                }
            }
        });
        list.setCellRenderer(new IconRenderer());
        pane.setViewportView(list);
        for (int i = 0; i < data.size(); i++) {
        }
        this.add(pane, BorderLayout.CENTER);
    }

    private void loadImages() throws IOException {
        if (data == null) {
            data = new ArrayList<IconPersistent>();
            for (File file : dir.listFiles())
                if ((file.isFile())
                        && (file.getName().toLowerCase().endsWith(".zip"))) {
                    loadZip(new ZipFile(file));
                }

            Collections.sort(data);

            IconPersistent e = new IconPersistent();
            data.add(0, e);
            e.setName(GlobalResourcesManager.getString("EmptyIcon"));

            images = new ImageIcon[data.size()];
        }
        lData = data;
        lImages = images;
    }

    private void loadZip(ZipFile file) throws IOException {

        Enumeration<? extends ZipEntry> e = file.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = e.nextElement();
            if ((!entry.isDirectory())
                    && (isImage(entry.getName().toLowerCase()))) {
                InputStream is = file.getInputStream(entry);
                byte[] bs = new byte[is.available()];
                is.read(bs);
                IconPersistent persistent = new IconPersistent();
                persistent.setIcon(bs);
                persistent.setName(entry.getName());
                data.add(persistent);
                is.close();
            }
        }

        file.close();
    }

    private boolean isImage(String string) {
        return string.endsWith(".png") || (string.endsWith(".gif"));
    }

    public IconPersistent select(JFrame frame) {
        BaseDialog dialog = new BaseDialog(frame, true) {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            protected void onOk() {
                super.onOk();
                res = getSelectedValue();
            }
        };
        dialog.setTitle(GlobalResourcesManager
                .getString("IconPreviewDialog.Title"));
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setMainPane(this);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        Options.loadOptions(dialog);
        dialog.setVisible(true);
        Options.saveOptions(dialog);
        return res;
    }

    public IconPersistent getSelectedValue() {
        Integer value = (Integer) list.getSelectedValue();
        if (value == null)
            return null;
        else
            return lData.get(value);
    }

}
