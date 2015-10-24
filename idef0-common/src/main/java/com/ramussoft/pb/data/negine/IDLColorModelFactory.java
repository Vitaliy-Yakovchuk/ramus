package com.ramussoft.pb.data.negine;

import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

public class IDLColorModelFactory {

    public static IndexColorModel createColorModel() {
        // Create a 6x6x6 color cube
        int[] cmap = new int[256];
        int i = 0;
        for (int r = 0; r < 256; r += 51) {
            for (int g = 0; g < 256; g += 51) {
                for (int b = 0; b < 256; b += 51) {
                    cmap[i++] = (r << 16) | (g << 8) | b;
                }
            }
        }
        // And populate the rest of the cmap with gray values
        int grayIncr = 256 / (256 - i);

        // The gray ramp will be between 18 and 252
        int gray = grayIncr * 3;
        for (; i < 256; i++) {
            cmap[i] = (gray << 16) | (gray << 8) | gray;
            gray += grayIncr;
        }

        return new IndexColorModel(8, 256, cmap, 0, false, -1,
                DataBuffer.TYPE_BYTE);
    }

}
