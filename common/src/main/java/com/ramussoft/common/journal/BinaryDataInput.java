package com.ramussoft.common.journal;

import java.io.DataInput;
import java.io.IOException;

public interface BinaryDataInput extends DataInput {

    String read254String() throws IOException;

    String readString() throws IOException;

    String readSwimedString() throws IOException;

    int read() throws IOException;

    byte[] readBinary() throws IOException;

}
