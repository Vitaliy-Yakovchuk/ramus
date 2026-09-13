package com.ramussoft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ShortcutsPatch {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {

		try {
			new ShortcutsPatch().start(args);
		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	private void start(String[] args) throws IOException {
		run(args);
	}

	private void run(String[] args) throws IOException {
		String ramusmimeType = loadFile("/com/ramussoft/x-ramus-extension-rsf.xml");
		String home = System.getProperty("user.home");
		File dest = new File(home + "/.local/share/icons/");
		dest.mkdirs();

		File icon = new File(dest, "x-ramus-extension-rsf.png");

		FileOutputStream fos = new FileOutputStream(icon);

		InputStream is = getClass().getResourceAsStream(
				"/com/ramussoft/icon.png");

		byte[] bs = new byte[is.available()];

		is.read(bs);

		fos.write(bs);

		is.close();
		fos.close();

		dest = new File(home + "/.local/share/mime/packages");
		dest.mkdirs();
		File cp = new File(dest, "x-ramus-extension-rsf.xml");
		fos = new FileOutputStream(cp);
		fos.write(ramusmimeType.getBytes());
		fos.close();

		dest = new File(home + "/.local/share/applications");

		for (File file : dest.listFiles()) {
			if (file.getName().startsWith("Ramus-")) {
				patchLink(file, args[0]);
			}
		}

		List<String> list = new ArrayList<String>();
		list.add("update-mime-database");
		list.add(home + "/.local/share/mime");

		ProcessBuilder pb = new ProcessBuilder(list);
		pb.start();

		list = new ArrayList<String>();
		list.add("xdg-icon-resource");
		list.add("install");
		list.add("--context");
		list.add("mimetypes");
		list.add("--size");
		list.add("48");
		list.add(icon.getAbsolutePath());
		list.add("x-ramus-extension-rsf");
		pb = new ProcessBuilder(list);
		pb.start();

		list = new ArrayList<String>();
		list.add("update-desktop-database");
		list.add(home + "/.local/share/applications");
		pb = new ProcessBuilder(list);
		pb.start();
	}

	private void patchLink(File file, String installPath) throws IOException {
		String string = MessageFormat.format(loadFile("/com/ramussoft/Ramus"),
				installPath);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(string.getBytes());
		fos.close();
	}

	private String loadFile(String name) throws IOException {
		InputStream is = getClass().getResourceAsStream(name);
		byte[] bs = new byte[is.available()];
		is.read(bs);
		return new String(bs);
	}

}
