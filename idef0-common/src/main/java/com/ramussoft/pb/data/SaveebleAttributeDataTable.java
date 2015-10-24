package com.ramussoft.pb.data;

import java.io.IOException;
import java.io.OutputStream;

public interface SaveebleAttributeDataTable {

    void saveToStream(OutputStream os) throws IOException;

}
