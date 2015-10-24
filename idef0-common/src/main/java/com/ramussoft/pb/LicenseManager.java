package com.ramussoft.pb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.UIManager;

public class LicenseManager {

    public static final String USER_PB_LICENSE_SIGN_DAT = "user.pb.license.sign.dat";

    public static final String USER_PB_LICENSE_DATA = "user.pb.license.data";

    public void loadLicenseKey(final String[] args) {
        File file = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-lkey")) {
                i++;
                if (i < args.length) {
                    file = new File(args[i]);
                }
                break;
            }
        }
        if (file != null) {
            try {
                loadLicense(file);
                return;
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        System.err.println("License key file not fount, use -lkey FILE_NAME");
        System.exit(0);
    }

    private void loadLicense(final File file) throws IOException {
        final ZipFile zf = new ZipFile(file);
        final ZipEntry ze = zf.getEntry("data.xml");
        int s = (int) ze.getSize();
        byte[] bs = new byte[s];
        InputStream is = zf.getInputStream(ze);
        is.read(bs);
        is.close();
        UIManager.put(USER_PB_LICENSE_DATA, bs);
        s = (int) ze.getSize();
        bs = new byte[s];
        is = zf.getInputStream(ze);
        is.read(bs);
        UIManager.put(USER_PB_LICENSE_SIGN_DAT, bs);
        is.close();
        zf.close();
    }

}
