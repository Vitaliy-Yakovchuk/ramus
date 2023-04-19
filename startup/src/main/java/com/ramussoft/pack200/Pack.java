package com.ramussoft.pack200;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.JarFile;

public class Pack {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("command unput-directory output-directory");
            System.exit(1);
        }

        Pack200.Packer packer = Pack200.newPacker();

        File inputDirectory = new File(args[0]);

        File outputDirectory = new File(args[1]);

        pack(packer, inputDirectory, outputDirectory);
    }

    private static void pack(Pack200.Packer packer, File inputDirectory,
                             File outputDirectory) throws IOException, FileNotFoundException {
        File[] files = inputDirectory.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isFile()) {
                if (file.getName().endsWith(".jar")) {

                    String fn = file.getName().substring(0,
                            file.getName().length() - 4)
                            + ".pack";

                    JarFile input = new JarFile(file);
                    String fileName = outputDirectory.getAbsolutePath()
                            + File.separator + fn;
                    OutputStream stream = new FileOutputStream(fileName);

                    System.out
                            .println(file.getAbsolutePath() + "->" + fileName);
                    packer.pack(input, stream);
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
                pack(packer, file, outputDirectory2);
            }
        }
    }
}
