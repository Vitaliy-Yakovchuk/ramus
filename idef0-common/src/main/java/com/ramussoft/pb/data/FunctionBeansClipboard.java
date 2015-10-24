package com.ramussoft.pb.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.dmaster.UserTemplate;
import com.ramussoft.pb.idef.elements.SectorRefactor;

public class FunctionBeansClipboard {

    private static byte[] data;

    private FunctionBeansClipboard() {
    }

    public static void setCopy(Function function, DataPlugin dataPlugin,
                               SectorRefactor refactor, List<Function> functions) {
        UserTemplate template = new UserTemplate(function, dataPlugin, "",
                refactor, functions);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            template.saveToStream(stream);
            data = stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static UserTemplate getTemplate() {
        if (data == null)
            return null;

        UserTemplate template;
        try {
            template = new UserTemplate(new ByteArrayInputStream(data));
            return template;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
