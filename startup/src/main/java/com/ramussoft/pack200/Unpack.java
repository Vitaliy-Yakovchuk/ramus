package com.ramussoft.pack200;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;

public class Unpack {

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println("command unput-directory output-directory");
            System.exit(1);
        }

        File inputDirectory = new File(args[0]);

        File outputDirectory = new File(args[1]);

        outputDirectory.mkdirs();

        Pack200.Unpacker unpacker = Pack200.newUnpacker();

        unpack(inputDirectory, outputDirectory, unpacker);

    }

    private static void unpack(File inputDirectory, File outputDirectory,
                               Pack200.Unpacker unpacker) throws FileNotFoundException, IOException {
        File[] files = inputDirectory.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isFile()) {
                if (file.getName().endsWith(".pack")) {

                    String fn = file.getName().substring(0,
                            file.getName().length() - 5)
                            + ".jar";

                    String fileName = outputDirectory.getAbsolutePath()
                            + File.separator + fn;

                    System.out
                            .println(file.getAbsolutePath() + "->" + fileName);
                    FileOutputStream fostream = new FileOutputStream(fileName);
                    JarOutputStream jostream = new JarOutputStream(fostream);
                    unpacker.unpack(file, jostream);
                    jostream.close();
                } else {
                    FileInputStream fis = new FileInputStream(file);
                    FileOutputStream fos = new FileOutputStream(new File(
                            outputDirectory, file.getName()));
                    byte[] buff = new byte[1024 * 64];
                    int r;
                    while ((r = fis.read(buff)) > 0) {
                        fos.write(buff, 0, r);
                    }
                    fis.close();
                    fos.close();
                }
            } else if (file.isDirectory()) {
                File outputDirectory2 = new File(outputDirectory, file
                        .getName());
                outputDirectory2.mkdir();
                unpack(file, outputDirectory2, unpacker);
            }
        }
    }

}
