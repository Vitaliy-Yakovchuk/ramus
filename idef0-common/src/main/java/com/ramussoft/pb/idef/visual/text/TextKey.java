package com.ramussoft.pb.idef.visual.text;

import java.awt.Font;

public class TextKey {

    float width;

    String text;

    Font font;

    final boolean cached;

    public TextKey(boolean cached) {
        this.cached = cached;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + text.hashCode();
        result = prime * result + Float.floatToIntBits(width);
        result = prime * result + font.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TextKey other = (TextKey) obj;
        if (!text.equals(other.text))
            return false;
        if (!font.equals(other.font))
            return false;
        if (Float.floatToIntBits(width) != Float.floatToIntBits(other.width))
            return false;
        return true;
    }

}
