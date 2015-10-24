package com.ramussoft.pb.print.xmlpars;

import java.io.OutputStream;

public interface HTMLPage {

    public static final int COLONTITUL_BOTTOM = 0;

    public static final int COLONTITUL_TOP = 1;

    OutputStream getBodyStream();

    void endHtml();

    OutputStream getColontitulStream(int colontitulType);
}
