package com.ramussoft.pb.idef.visual;

import java.awt.Color;
import java.awt.Font;

import com.dsoft.pb.types.FRectangle;

/**
 * Зміна властивостей і для безпосередньо носія інформації
 *
 * @author vitaliy.yakovchuk
 */
public interface VisualPanel {

    Color getBackgroundA();

    Color getForegroundA();

    Font getFontA();

    FRectangle getBoundsA();

    void setBackgroundA(Color background);

    void setForegroundA(Color foreground);

    void setFontA(Font font);

    void setBoundsA(FRectangle bounds);
}
