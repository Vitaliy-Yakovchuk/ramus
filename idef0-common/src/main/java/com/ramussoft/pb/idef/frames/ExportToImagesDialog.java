package com.ramussoft.pb.idef.frames;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.Metadata;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.BusyDialog;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.TextField;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.visual.MovingFunction;
import com.ramussoft.pb.print.PIDEF0painter;

public class ExportToImagesDialog extends BaseDialog {

    private static final String LAST_IMG_EXPORT_DIRECTORY = "LAST_IMG_EXPORT_DIRECTORY";

    private DataPlugin dataPlugin;

    private TextField directory = new TextField();

    private JComboBox imageSizeComboBox;

    private JComboBox imageTypeComboBox;

    private IDEF0ChackedPanel chackedPanel;

    public ExportToImagesDialog(JFrame frame, DataPlugin dataPlugin) {
        super(frame, true);
        this.directory.setText(Options.getString(LAST_IMG_EXPORT_DIRECTORY,
                new JFileChooser().getFileSystemView().getDefaultDirectory()
                        .getAbsolutePath()));
        this.dataPlugin = dataPlugin;
        setTitle(ResourceLoader.getString("ExportToImages"));
        setMainPane(createMainPane());
        pack();
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        Options.loadOptions(this);
    }

    private JComponent createMainPane() {
        JPanel panel = new JPanel(new BorderLayout());
        chackedPanel = new IDEF0ChackedPanel();
        chackedPanel.setFunctionParents(dataPlugin);
        panel.add(chackedPanel, BorderLayout.CENTER);
        panel.add(createBottomPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private Component createBottomPanel() {
        double[][] size = {
                {5, TableLayout.MINIMUM, 5, TableLayout.FILL, 5,
                        TableLayout.MINIMUM, 5},
                {5, TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                        TableLayout.MINIMUM, 5}};

        JPanel panel = new JPanel(new TableLayout(size));

        imageSizeComboBox = new JComboBox();
        imageSizeComboBox.addItem("799x530");
        imageSizeComboBox.addItem("904x601");
        imageSizeComboBox.addItem("1023x680");
        imageSizeComboBox.addItem("1151x765");
        imageSizeComboBox.addItem("1299x864");
        imageSizeComboBox.addItem("1600x1064");

        imageTypeComboBox = new JComboBox();
        imageTypeComboBox.addItem(".bmp");
        imageTypeComboBox.addItem(".png");
        imageTypeComboBox.addItem(".jpg");
        imageTypeComboBox.addItem(".svg");
        imageTypeComboBox.addItem(".emf");
        imageTypeComboBox.setSelectedIndex(1);

        panel.add(new JLabel(ResourceLoader.getString("ImageSize")), "1,1");
        panel.add(imageSizeComboBox, "3,1,5,1");

        panel.add(new JLabel(ResourceLoader.getString("ImageType")), "1,3");
        panel.add(imageTypeComboBox, "3,3,5,3");

        panel.add(new Label(ResourceLoader.getString("Folder")), "1, 5");
        panel.add(directory, "3, 5");
        panel.add(new JButton(new AbstractAction(GlobalResourcesManager
                .getString("Action.Browse")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(directory.getText()));
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int r = fileChooser.showOpenDialog(null);
                if (r == JFileChooser.APPROVE_OPTION)
                    directory.setText(fileChooser.getSelectedFile()
                            .getAbsolutePath());
            }
        }), "5, 5");

        return panel;
    }

    @Override
    protected void onOk() {
        final File dir = new File(directory.getText());
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(
                        ResourceLoader.getString("FileIsNotADirectory"),
                        directory.getText()));
            } else {
                for (File file : dir.listFiles()) {
                    if (file.isFile()) {
                        if (JOptionPane.showConfirmDialog(this, ResourceLoader
                                        .getString("DirectoryIsNotEmpty"), UIManager
                                        .getString("OptionPane.titleText"),
                                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                            return;
                        break;
                    }
                }
            }
        } else {
            if (!dir.mkdirs()) {
                JOptionPane.showMessageDialog(this, ResourceLoader
                        .getString("CanNotCreateADirectory"));
            }
        }

        final BusyDialog dialog = new BusyDialog(this, ResourceLoader.getString("ExportingBusy"));

        Thread export = new Thread("Export-to-images") {
            @Override
            public void run() {
                int i = 0;
                int[] js = chackedPanel.getSelected();
                for (Function f : chackedPanel.getSelectedFunctions()) {
                    try {
                        String prefix = Integer.toString(js[i] + 1);
                        while (prefix.length() < 2)
                            prefix = "0" + prefix;
                        exportToFile(dir, f, prefix + "_");
                        i++;
                    } catch (IOException e) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                dialog.setVisible(false);
                            }
                        });
                        JOptionPane.showMessageDialog(
                                ExportToImagesDialog.this, e
                                        .getLocalizedMessage());
                        if (Metadata.DEBUG)
                            e.printStackTrace();
                        return;
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        dialog.setVisible(false);
                    }
                });
                Options.setString(LAST_IMG_EXPORT_DIRECTORY, directory
                        .getText());
                ExportToImagesDialog.super.onOk();
            }
        };
        export.start();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (ExportToImagesDialog.this.isVisible())
                    dialog.setVisible(true);
            }
        });
    }

    protected void exportToFile(File dir, Function f, String prefix)
            throws FileNotFoundException, IOException {
        String size = null;
        switch (imageSizeComboBox.getSelectedIndex()) {
            case 0:
                size = "800x535";
                break;
            case 1:
                size = "905x700";
                break;
            case 2:
                size = "1024x768";
                break;
            case 3:
                size = "1152x864";
                break;
            case 4:
                size = "1300x1000";
                break;
            case 5:
                size = "1601x1200";
                break;

        }

        StringTokenizer st = new StringTokenizer(size, "x");

        int width = Integer.valueOf(st.nextToken());
        int height = Integer.valueOf(st.nextToken());

        PIDEF0painter painter = new PIDEF0painter(f, new Dimension(width,
                height), dataPlugin);
        File file = new File(dir, prefix + MovingFunction.getIDEF0Kod((com.ramussoft.database.common.Row) f)
                + imageTypeComboBox.getSelectedItem().toString());
        FileOutputStream stream = new FileOutputStream(file);
        painter.writeToStream(stream, imageTypeComboBox.getSelectedIndex());
        stream.close();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b)
            Options.saveOptions(this);
    }

}
