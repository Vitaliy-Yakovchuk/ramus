package com.ramussoft.localefix;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.ParseException;
import java.util.Locale;

import com.ramussoft.eval.Eval;

public class DecimalFormatWithFix extends DecimalFormat {

    /**
     *
     */
    private static final long serialVersionUID = -6391324064534808756L;

    public DecimalFormatWithFix() {
        super();
        if (Locale.getDefault().getLanguage().equals("uk")) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setCurrencySymbol("грн.");
            symbols.setGroupingSeparator(' ');
            symbols.setDecimalSeparator(',');
            setDecimalFormatSymbols(symbols);
        }
        if (Locale.getDefault().getLanguage().equals("ru")) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            symbols.setDecimalSeparator(',');
            setDecimalFormatSymbols(symbols);
        }
    }

    @Override
    public StringBuffer format(double number, StringBuffer resultA,
                               FieldPosition fieldPosition) {
        if (number <= 0d) {
            return super.format(number, resultA, fieldPosition);
        }
        String res = new BigDecimal(number).toPlainString();
        DecimalFormatSymbols symbols = getDecimalFormatSymbols();
        String s = getNegativePrefix();
        String p = getPositivePrefix();
        char g = symbols.getGroupingSeparator();
        char d = symbols.getDecimalSeparator();
        StringBuffer result = new StringBuffer();
        if (number < 0d)
            result.append(s);
        else
            result.append(p);
        if (res.startsWith("-"))
            res = res.substring(1);
        int index = res.indexOf('.');
        if (index < 0)
            return super.format(number, result, fieldPosition);
        boolean r = false;
        int count = 0;
        int added = 0;
        for (int i = 0; i < res.length(); i++) {
            if (index > i) {
                if ((index - i) % 3 == 0 && i > 0)
                    result.append(g);
            } else {
                added++;
                if (r)
                    count++;
                if (count >= 3) {
                    int j = i;
                    if (j < res.length()) {
                        char c = res.charAt(j);
                        if (c >= '5' && c <= '9') {
                            StringBuffer sb = new StringBuffer();
                            boolean check = true;
                            for (int k = result.length() - 1; k >= 0; k--) {
                                char n = result.charAt(k);
                                if (check) {
                                    if (n >= '0' && n <= '8') {
                                        n++;
                                        check = false;
                                    } else if (n == '9') {
                                        n = '0';
                                    }
                                }
                                sb.append(n);
                            }
                            result = sb.reverse();
                        }
                    }
                    break;
                }
            }
            char c = res.charAt(i);
            if (c == '.')
                result.append(d);
            else {
                result.append(c);
                if (c != '0')
                    r = true;
            }
        }
        if (added > 3) {
            for (int i = result.length() - 1; i >= 0; i--) {
                char c = result.charAt(i);
                if (c == g)
                    break;
                if ((c == '0' || c == d) && (result.length() > 1))
                    result.deleteCharAt(i);
                else
                    break;
            }
            if (result.toString().equals("-0"))
                return super.format(number, resultA, fieldPosition);
            return result;
        }
        return super.format(number, resultA, fieldPosition);
    }

    @Override
    public Number parse(String source) throws ParseException {
        String s = source;
        if (source.startsWith("=")) {
            s = s.substring(1);
        }

        Eval eval = new Eval(s);
        return eval.calculate().numberValue();
    }

    public Number parseNative(String value) throws ParseException {
        return super.parse(value);
    }

}
