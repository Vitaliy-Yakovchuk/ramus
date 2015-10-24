package com.ramussoft.database;

import java.text.Collator;

public class StringCollator {

    private static Collator collator = Collator.getInstance();

    public static int compare(String o1, String o2) {
        if (o1 == null) {
            if (o2 == null)
                return 0;
            return -1;
        }
        if (o2 == null)
            return 1;

        if ((o1.length() > 0) && (o2.length() > 0)
                && (Character.isDigit(o1.charAt(0)))
                && (Character.isDigit(o2.charAt(0)))) {
            StringBuffer b1 = new StringBuffer();
            b1.append(o1.charAt(0));
            StringBuffer b2 = new StringBuffer();
            b2.append(o2.charAt(0));
            for (int i = 1; i < o1.length(); i++)
                if (Character.isDigit(o1.charAt(i)))
                    b1.append(o1.charAt(i));
                else
                    break;
            for (int i = 1; i < o2.length(); i++)
                if (Character.isDigit(o2.charAt(i)))
                    b2.append(o2.charAt(i));
                else
                    break;
            int n1 = Integer.parseInt(b1.toString());
            int n2 = Integer.parseInt(b2.toString());

            if (n1 < n2)
                return -1;
            if (n1 > n2)
                return 1;
        }

        return collator.compare(o1, o2);
    }

}
