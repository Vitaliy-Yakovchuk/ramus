package com.ramussoft.common.journal;

import java.io.DataOutput;
import java.io.IOException;

public interface BinaryDataOutput extends DataOutput {

    void write254String(String string) throws IOException;

    void writeString(String string) throws IOException;

    void writeSwimedString(String string) throws IOException;

    void writeBinary(byte[] object) throws IOException;

}
