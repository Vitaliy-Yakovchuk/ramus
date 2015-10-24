package com.ramussoft;

//import java.util.Vector;

public class RamusLocalStartup {

    /**
     * @param args
     */
    public static void main(String[] args) {
        /*if (Startup.portable) {
            if (args.length > 1) {
				Vector v = new Vector();

				v.add("com.ramussoft.local.Main");

				for (int i = 0; i < args.length; i++) {
					if (args[i].equals("-launcher")) {
						i++;
						if (i >= args.length)
							break;
					}
					if (args[i].equals("-exitdata")) {
						i += 2;
						if (i >= args.length) {
							args = new String[] {};
							break;
						}
						for (int j = i; j < args.length; j++) {
							if (args[j].equals("-vm"))
								break;
							v.add(args[j]);
						}
						break;
					}
				}
				args = new String[v.size()];
				for (int i = 0; i < args.length; i++)
					args[i] = v.get(i).toString();

				Startup.main(args);
			}
		} else {*/
        String[] args2 = new String[args.length + 1];
        args2[0] = "com.ramussoft.local.Main";
        for (int i = 0; i < args.length; i++)
            args2[i + 1] = args[i];
        Startup.main(args2);
        //}
    }

}
