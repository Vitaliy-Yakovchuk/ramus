package com.ramussoft.pb.data;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.StringTokenizer;

public class KeyVerificator {
    private static byte[] encKey = convert("444 48 -126 1 -72 48 -126 1 44 6 7 42 -122 "
            + "72 -50 56 4 1 48 -126 1 31 2 "
            + "-127 -127 0 -3 127 83 -127 29 117 18 41 82 -33 74 -100 46 -20 -28 -25 "
            + "-10 17 -73 82 60 -17 68 0 -61 30 63 -128 -74 81 38 105 69 93 64 34 81 -5 "
            + "89 61 -115 88 -6 -65 -59 -11 -70 48 -10 -53 -101 85 108 -41 -127 59 -128 "
            + "29 52 111 -14 102 96 -73 107 -103 80 -91 -92 -97 -97 -24 4 123 16 34 -62 "
            + "79 -69 -87 -41 -2 -73 -58 27 -8 59 87 -25 -58 -88 -90 21 15 4 -5 -125 -10 "
            + "-45 -59 30 -61 2 53 84 19 90 22 -111 50 -10 117 -13 -82 43 97 -41 42 -17 "
            + "-14 34 3 25 -99 -47 72 1 -57 2 21 0 -105 96 80 -113 21 35 11 -52 -78 -110 "
            + "-71 -126 -94 -21 -124 11 -16 88 28 -11 2 -127 -127 0 -9 -31 -96 -123 -42 "
            + "-101 61 -34 -53 -68 -85 92 54 -72 87 -71 121 -108 -81 -69 -6 58 -22 -126 "
            + "-7 87 76 11 61 7 -126 103 81 89 87 -114 -70 -44 89 79 -26 113 7 16 -127 "
            + "-128 -76 73 22 113 35 -24 76 40 22 19 -73 -49 9 50 -116 -56 -90 -31 60 "
            + "22 122 -117 84 124 -115 40 -32 -93 -82 30 43 -77 -90 117 -111 110 -93 "
            + "127 11 -6 33 53 98 -15 -5 98 122 1 36 59 -52 -92 -15 -66 -88 81 -112 "
            + "-119 -88 -125 -33 -31 90 -27 -97 6 -110 -117 102 94 -128 123 85 37 100 "
            + "1 76 59 -2 -49 73 42 3 -127 -123 0 2 -127 -127 0 -74 111 86 -38 -23 35 "
            + "34 -93 -3 -101 70 40 70 -84 42 0 -12 -41 -125 -35 13 52 41 114 -75 -91 "
            + "89 78 55 -121 -94 -13 5 -64 -24 6 -45 23 21 100 98 -99 -7 -100 -120 21 "
            + "-13 63 -19 105 104 78 -127 126 24 -115 -122 35 -49 -86 97 83 125 -24 "
            + "-54 -10 101 21 -31 -94 -91 -38 114 -85 -2 -122 39 7 -21 125 110 108 83 "
            + "-49 -80 2 33 -8 -51 14 -23 -4 5 54 86 -60 15 -42 122 -56 -50 48 -101 "
            + "-19 -67 -35 66 -24 -125 -23 -32 -74 19 47 72 -45 101 -1 125 -80 51 "
            + "-59 -79 98 16 -56 -84 73");

    private static byte[] convert(final String s) {
        final StringTokenizer st = new StringTokenizer(s, " ");
        final int len = Integer.parseInt(st.nextToken());
        final byte[] res = new byte[len];
        int i = 0;
        while (st.hasMoreTokens()) {
            res[i] = Byte.parseByte(st.nextToken());
            i++;
        }
        return res;
    }

    private final byte[] data;

    private final byte[] sign;

    public KeyVerificator(final byte[] data, final byte[] sign) {
        this.data = data;
        this.sign = sign;
    }

    public boolean verify() throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeySpecException,
            InvalidKeyException, SignatureException {
        final X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
        final KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
        final PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
        final byte[] sigToVerify = sign;
        final Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
        sig.initVerify(pubKey);
        sig.update(data, 0, data.length);
        final boolean verifies = sig.verify(sigToVerify);
        return verifies;
    }
}
