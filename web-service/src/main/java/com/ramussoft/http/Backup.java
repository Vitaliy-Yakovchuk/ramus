package com.ramussoft.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ramussoft.server.BaseExporter;

public class Backup extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -5919718663021964989L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        accept(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        accept(req, resp);
    }

    private void accept(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Calendar c = Calendar.getInstance();
        String tmp = System.getProperty("java.io.tmpdir");
        if (!tmp.endsWith(File.separator))
            tmp += File.separator;
        String fineName = "backup-" + c.get(Calendar.YEAR) + "-"
                + (c.get(Calendar.MONTH) + 1) + "-"
                + c.get(Calendar.DAY_OF_MONTH) + ".rsf";
        tmp += fineName;
        boolean exit = false;
        try {
            BaseExporter.main(new String[]{tmp});
        } catch (IOException e) {
            e.printStackTrace(resp.getWriter());
            exit = true;
        }
        if (exit)
            return;
        try {
            resp.setContentType("application/rsf ");
            resp.setHeader("Content-Disposition", "attachment; filename="
                    + fineName);
            OutputStream stream = resp.getOutputStream();
            byte[] bs = new byte[1024 * 64];
            int r;
            FileInputStream fis = new FileInputStream(tmp);
            while ((r = fis.read(bs)) > 0) {
                stream.write(bs, 0, r);
            }
            fis.close();
        } catch (IOException e) {
            throw e;
        } finally {
            if (!new File(tmp).delete()) {
                new File(tmp).deleteOnExit();
            }
        }
    }

}
