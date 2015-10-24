package com.ramussoft.report.data;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class DataException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 5908437885552665630L;

    private String messageKey;

    private Object[] arguments;

    private Exception exception;

    private static ResourceBundle bundle = ResourceBundle
            .getBundle("com.ramussoft.report.reportgui");

    public DataException(String messageKey, Object... arguments) {
        this.messageKey = messageKey;
        this.arguments = arguments;
    }

    public DataException(String messageKey, String defaultMessage,
                         Object... arguments) {
        super(defaultMessage);
        this.messageKey = messageKey;
        this.arguments = arguments;
    }

    public DataException(Exception exception) {
        this.exception = exception;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public String getMessage(MessageFormatter formatter) {
        if (exception != null)
            return exception.getLocalizedMessage();
        return formatter.getString(messageKey, arguments);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        if (exception != null)
            exception.printStackTrace(s);
        else
            super.printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        if (exception != null)
            exception.printStackTrace(s);
        else
            super.printStackTrace(s);
    }

    @Override
    public String getMessage() {
        if (exception != null)
            return exception.getMessage();
        return getMessage(new MessageFormatter() {

            @Override
            public String getString(String key, Object[] arguments) {
                return MessageFormat.format(bundle.getString(key), arguments);
            }
        });
    }
}
