package com.ramussoft.pb.frames.components;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.zip.ZipInputStream;

public class SerialCheker {

    /**
     * Method will close is anyway.
     */

    private boolean check(InputStream is, String serial) {
        try {
            serial = serial.toUpperCase();
            InputStreamReader reader = new InputStreamReader(is, "iso-8859-1");
            BufferedReader br = new BufferedReader(reader);
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");
            byte[] sha1hash = new byte[40];
            md.update(serial.getBytes("iso-8859-1"), 0, serial.length());
            sha1hash = md.digest();
            String hex = convertToHex(sha1hash);

            boolean res = false;

            String line;
            while ((line = br.readLine()) != null) {
                if (hex.equals(line))
                    res = true;
            }

            br.close();
            return res;
        } catch (Exception e) {
        }
        return false;
    }

    private String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public boolean check(String serial) {
        if (serial == null)
            return false;
        try {
            ZipInputStream zis = new ZipInputStream(getClass()
                    .getResourceAsStream("/com/ramussoft/gui/logo.png"));
            zis.getNextEntry();
            return check(zis, serial);
        } catch (Exception e) {
        }
        return false;
    }

}
