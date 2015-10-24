/*
 * Created on 3/9/2005
 */
package com.dsoft.utils;

/**
 * @author ZDD
 */
public class MyTokanizer {
    private final String text;

    private int start;

    private final int end;

    private final String simbols;

    private final boolean doee;

    private final String spaces;

    public MyTokanizer(final String text, final int start, final int length, final String simbols,
                       final String spaces, final boolean doee) {
        this.text = text;
        this.start = start;
        end = length;
        this.simbols = simbols;
        this.doee = doee;
        this.spaces = spaces;
    }

    public MyTokanizer(final String text, final int start, final int length) {
        this.text = text;
        this.start = start;
        end = length;
        simbols = "()[],=";
        spaces = "\n\r \t";
        doee = true;
    }

    private boolean isR(final char c) {
        final int len = simbols.length();
        for (int i = 0; i < len; i++)
            if (simbols.charAt(i) == c)
                return true;
        return false;

		/*
         * return (c=='(')||(c==')')||(c=='[')|| (c==']')||(c==',')||(c=='=');
		 */
    }

    private boolean isC(final char c) {
        final int len = spaces.length();
        for (int i = 0; i < len; i++)
            if (spaces.charAt(i) == c)
                return true;
        return false;

    }

    public String nextElement() {
        if (start >= end)
            return null;
        char c;
        String next = "";
        while (start < end) {
            c = text.charAt(start);
            if (c == '\\') {
                start++;
                c = text.charAt(start);
            }
            if (!isC(c)) {
                if (doee) {
                    if (c == '\"') {
                        start++;
                        while (true) {
                            if (start >= text.length())
                                return next;
                            c = text.charAt(start);
                            if (c == '\\') {
                                start++;
                                c = text.charAt(start);
                                next += c;
                            } else {
                                if (c == '\"')
                                    break;
                                else
                                    next += c;
                            }
                            start++;
                        }
                        start++;
                        break;
                    }
                }
                if (next.length() > 0) {
                    if (isR(c))
                        break;
                }
                next += c;
                if (isR(c)) {
                    start++;
                    break;
                }
            } else {
                start++;
                break;
            }
            start++;
        }
        while (start < end && isC(text.charAt(start)))
            start++;
        return next;
    }

    public boolean hasMore() {
        return start < end;
    }

    /**
     * @return Returns the start.
     */
    public int getStart() {
        return start;
    }

    /**
     * @param start The start to set.
     */
    public void setStart(final int start) {
        this.start = start;
    }

    public static void main(final String[] args) {
        final String s = "\"Прибуток всіх видів\"/\"Сумарні активи\"";
        final MyTokanizer mt = new MyTokanizer(s, 0, s.length(), "*/+-^", " \n", true);
        while (mt.hasMore()) {
            System.out.println(mt.nextElement());
        }
    }
}
