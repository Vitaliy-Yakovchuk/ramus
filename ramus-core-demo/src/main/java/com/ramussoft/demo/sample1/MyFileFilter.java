package com.ramussoft.demo.sample1;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class MyFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isFile()) {
            return f.getName().toLowerCase().endsWith(".app");
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "My application file type (*.app)";
    }

}
