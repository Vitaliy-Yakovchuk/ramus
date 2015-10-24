package com.ramussoft.eval;

import java.util.Date;

public class StandardFunctions {

    public static EObject NUMBER(EObject[] params) {
        if (params.length == 1)
            return new EObject(params[0].doubleValue());
        if (params.length == 2) {
            if (!params[1].isString())
                throw new RuntimeException(
                        "Second param must be string for function NUMBER.");
            String d = params[1].stringValue();
            if (d.length() != 1)
                throw new RuntimeException(
                        "Second param must has wrong format \"" + d + "\"");
            StringBuffer sb = new StringBuffer();
            for (char c : params[0].stringValue().toCharArray())
                if (Character.isDigit(c))
                    sb.append(c);
                else if (c == d.charAt(0))
                    sb.append('.');
            return new EObject(Double.parseDouble(sb.toString()));
        }
        throw new RuntimeException("Unknown function for " + params.length
                + " param count for function NUMBER.");
    }

    public static EObject STRING(EObject[] params) {
        if (params.length == 1)
            return new EObject(params[0].stringValue());
        if (params.length == 2)
            return new EObject(Double.toString(params[0].doubleValue())
                    .replace(".", params[1].stringValue()));
        throw new RuntimeException("Unknown function for " + params.length
                + " param count for function STRING.");
    }

    public static EObject N(EObject[] params) {
        return NUMBER(params);
    }

    public static EObject S(EObject[] params) {
        return STRING(params);
    }

    public static EObject LENGTH(EObject[] params) {
        if (params.length == 1) {
            return new EObject(params[0].stringValue().length());
        }
        throw new RuntimeException("Unknown function for " + params.length
                + " param count for function LENGTH.");
    }

    public static EObject SUBSTRING(EObject[] params) {
        if (params.length == 3) {
            return new EObject(params[0].stringValue().substring(
                    params[1].intValue(), params[2].intValue()));
        }
        throw new RuntimeException("Unknown function for " + params.length
                + " param count for function SUBSTRING.");
    }

    public static EObject MAX(EObject[] params) {
        double res = 0;
        if (params.length > 0) {
            res = params[0].doubleValue();
            for (int i = 1; i < params.length; i++) {
                double p = params[i].doubleValue();
                if (p > res)
                    res = p;
            }
        }
        return new EObject(res);
    }

    public static EObject NOW(EObject[] params) {
        return new EObject(new Date());
    }

    public static EObject MIN(EObject[] params) {
        double res = 0;
        if (params.length > 0) {
            res = params[0].doubleValue();
            for (int i = 1; i < params.length; i++) {
                double p = params[i].doubleValue();
                if (p < res)
                    res = p;
            }
        }
        return new EObject(res);
    }
}
