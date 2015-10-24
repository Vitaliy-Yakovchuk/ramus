package print.to.pdf;

import java.awt.Font;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.FontMapper;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.PrintPlugin;
import com.ramussoft.gui.common.print.RamusPrintable;
import com.ramussoft.pb.print.IDEF0Printable;

public class Plugin implements PrintPlugin {

    @Override
    public Action getPrintAction(GUIFramework framework,
                                 RamusPrintable printable) {
        return new PrintToPDFAction(framework, printable);
    }

    private class PrintToPDFAction extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = 1988072066292669411L;
        private GUIFramework framework;
        private RamusPrintable printable;

        public PrintToPDFAction(GUIFramework framework, RamusPrintable printable) {
            super(GlobalResourcesManager.getString("Action.Print"));
            this.framework = framework;
            this.printable = printable;
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/print/to/pdf/icon.jpg")));
            putValue(ACTION_COMMAND_KEY, "Action.Print");
            putValue(SHORT_DESCRIPTION,
                    GlobalResourcesManager.getString("Action.Print") + " (PDF)");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            File file = framework.showSaveDialog("PRINT_PDF", ".pdf");
            if (file != null)
                try {
                    printToPDF(file);
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(null,
                            e1.getLocalizedMessage());
                }
        }

        private void printToPDF(File file) throws DocumentException,
                PrinterException, IOException {
            // DefaultFontMapper mapper = new DefaultFontMapper();

            Document document = new Document();

            FileOutputStream outputStream = new FileOutputStream(file);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            // TODO Реалізувати вибір формату PDF
            document.setPageSize(PageSize.A3.rotate());

            document.open();

            FontFactory.registerDirectories();

            FontMapper mapper = new DefaultFontMapper() {
                @Override
                public BaseFont awtToPdf(Font font) {
                    try {
                        BaseFontParameters p = getBaseFontParameters(font
                                .getFontName());
                        if (p != null)
                            return BaseFont.createFont(p.fontName, p.encoding,
                                    p.embedded, p.cached, p.ttfAfm, p.pfb);
                        String fontKey = null;
                        String logicalName = font.getName();

                        if (logicalName.equalsIgnoreCase("DialogInput")
                                || logicalName.equalsIgnoreCase("Monospaced")
                                || logicalName.equalsIgnoreCase("Courier")) {

                            if (font.isItalic()) {
                                if (font.isBold()) {
                                    fontKey = BaseFont.COURIER_BOLDOBLIQUE;

                                } else {
                                    fontKey = BaseFont.COURIER_OBLIQUE;
                                }

                            } else {
                                if (font.isBold()) {
                                    fontKey = BaseFont.COURIER_BOLD;

                                } else {
                                    fontKey = BaseFont.COURIER;
                                }
                            }

                        } else if (logicalName.equalsIgnoreCase("Serif")
                                || logicalName.equalsIgnoreCase("TimesRoman")) {

                            if (font.isItalic()) {
                                if (font.isBold()) {
                                    fontKey = BaseFont.TIMES_BOLDITALIC;

                                } else {
                                    fontKey = BaseFont.TIMES_ITALIC;
                                }

                            } else {
                                if (font.isBold()) {
                                    fontKey = BaseFont.TIMES_BOLD;

                                } else {
                                    fontKey = BaseFont.TIMES_ROMAN;
                                }
                            }

                        } else { // default, this catches Dialog and SansSerif

                            if (font.isItalic()) {
                                if (font.isBold()) {
                                    fontKey = BaseFont.HELVETICA_BOLDOBLIQUE;

                                } else {
                                    fontKey = BaseFont.HELVETICA_OBLIQUE;
                                }

                            } else {
                                if (font.isBold()) {
                                    fontKey = BaseFont.HELVETICA_BOLD;
                                } else {
                                    fontKey = BaseFont.HELVETICA;
                                }
                            }
                        }

                        if (fontKey.equals(BaseFont.HELVETICA)) {
                            return BaseFont.createFont(
                                    "/print/to/pdf/AGHelvetica.TTF",
                                    BaseFont.IDENTITY_H, true);
                        }

                        return FontFactory.getFont(fontKey, "cp1251", true)
                                .getCalculatedBaseFont(true);
                    } catch (Exception e) {
                        throw new ExceptionConverter(e);
                    }
                }

            };

            PdfContentByte cb = writer.getDirectContent();
            int pageCount = printable.getPageCount();
            if (printable instanceof IDEF0Printable)
                ((IDEF0Printable) printable).setNativeTextPaint(true);
            for (int i = 0; i < pageCount; i++) {
                PageFormat format = printable.getPageFormat(
                        printable.getPageFormat(), i);
                Graphics2D g2d = new PdfGraphics2D(cb,
                        (float) format.getWidth(), (float) format.getHeight(),
                        mapper);

                printable.print(g2d, i);

                g2d.dispose();
                if (i + 1 < pageCount)
                    document.newPage();
            }
            if (printable instanceof IDEF0Printable)
                ((IDEF0Printable) printable).setNativeTextPaint(false);
            document.close();
            outputStream.close();
        }

        ;
    }

    ;

}
