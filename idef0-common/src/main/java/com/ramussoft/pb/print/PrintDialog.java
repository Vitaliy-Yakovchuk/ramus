package com.ramussoft.pb.print;

import java.awt.Dimension;
import java.awt.print.PrinterJob;

import javax.swing.JFrame;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.print.RamusPrintable;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.frames.BaseDialog;
import com.ramussoft.pb.idef.frames.IDEF0ChackedPanel;

public class PrintDialog extends BaseDialog {

    protected final IDEF0ChackedPanel panel;

    private IDEF0Printable printable; // @jve:decl-index=0:

    private final GUIFramework framework;

    public void showModal(final RamusPrintable printable) {
        this.printable = (IDEF0Printable) printable;
        setTitle(printable.getJobName());
        setVisible(true);
        Options.saveOptions("printDialog", this);
    }

    /**
     * This method initializes
     */
    public PrintDialog(DataPlugin dataPlugin, GUIFramework framework) {
        super(framework.getMainFrame());
        this.framework = framework;
        this.panel = new IDEF0ChackedPanel();
        panel.setFunctionParents(dataPlugin);
        initialize();
        setLocationRelativeTo(null);
    }

    public PrintDialog(final JFrame frame, DataPlugin dataPlugin,
                       GUIFramework framework) {
        super(frame);
        this.framework = framework;
        this.panel = new IDEF0ChackedPanel();
        panel.setFunctionParents(dataPlugin);
        initialize();
        setLocationRelativeTo(frame);
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        setMinimumSize(new Dimension(491, 320));
        this.setSize(new Dimension(491, 320));

        setMainPane(panel);
        ResourceLoader.setJComponentsText(this);
        Options.loadOptions("printDialog", this);
    }

    @Override
    protected void onOk() {
        super.onOk();
        final PrinterJob job = framework.getPrinterJob("idef0");
        try {
            printable.setPrintFunctions(panel.getSelectedFunctions());
            job.setPrintable(printable.createPrintable(), printable
                    .getPageFormat());
            job.setJobName(printable.getJobName());
            if (job.printDialog()) {

                job.print();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
} // @jve:decl-index=0:visual-constraint="10,10"
