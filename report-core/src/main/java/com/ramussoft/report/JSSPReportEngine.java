package com.ramussoft.report;

import java.io.BufferedReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Metadata;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Out;

public class JSSPReportEngine extends ReportEngine {

    protected static final String SCRIPT_WORKED_TOO_LONG = "Script worked too long, and was interrupted!!! (виконання сценарію тривало більше 50-ти секунд, в результаті чого виконання було перервано)";

    protected ScriptEngineManager manager = new ScriptEngineManager();

    protected ReportQueryImpl reportQuery;

    public JSSPReportEngine(Engine engine, ReportQueryImpl reportQuery) {
        super(engine);
        this.reportQuery = reportQuery;
    }

    protected class ExceptionHolder {
        ScriptException scriptException;
        RuntimeException exception;
        boolean finished = false;
    }

    ;

    @SuppressWarnings("deprecation")
    public void execute(String scriptPath, OutputStream stream,
                        Map<String, Object> parameters) throws IOException {
        byte[] bytes = engine.getStream(scriptPath);
        if (bytes == null)
            bytes = new byte[]{};
        String script = new String(bytes, "UTF-8");
        JSSPToJsConverter converter = new JSSPToJsConverter(script);
        final ScriptEngine engine = manager.getEngineByName("JavaScript");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Out out = createOut(outputStream);
        try {
            SimpleOut simpleOut = new SimpleOut(out);
            engine.put("doc", simpleOut);
            engine.put("document", simpleOut);
            engine.put("out", simpleOut);
            Query query = (Query) parameters.get("query");
            engine.put("data", new Data(this.engine, query, reportQuery));
            for (Entry<String, Object> entry : parameters.entrySet())
                engine.put(entry.getKey(), entry.getValue());
            final String convert = converter.convert();
            final Object w = new Object();
            final ExceptionHolder exceptionHolder = new ExceptionHolder();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        engine.eval(convert);
                    } catch (ScriptException e) {
                        exceptionHolder.scriptException = e;
                    } catch (RuntimeException e) {
                        exceptionHolder.exception = e;
                    }
                    synchronized (w) {
                        exceptionHolder.finished = true;
                        w.notify();
                    }
                }
            };
            thread.start();
            try {
                synchronized (w) {
                    if (Metadata.CORPORATE) {
                        if (!exceptionHolder.finished)
                            w.wait(300000);
                    } else {
                        if (!exceptionHolder.finished)
                            w.wait(50000);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!exceptionHolder.finished) {
                thread.stop();
                throw new ScriptException(SCRIPT_WORKED_TOO_LONG);
            }
            if (exceptionHolder.scriptException != null) {
                /*
                 * if(exceptionHolder.scriptException.getCause() instanceof
				 * Exception)
				 * ((Exception)exceptionHolder.scriptException.getCause
				 * ()).printStackTrace();
				 */
                throw exceptionHolder.scriptException;
            }
            if (exceptionHolder.exception != null)
                throw exceptionHolder.exception;
            out.flush();
            out.realWriteWithHTMLUpdate();
            stream.write(outputStream.toByteArray());
        } catch (ScriptException e) {
            e.printStackTrace();
            out = new Out(stream);
            String message = e.getLocalizedMessage();
            String moz = "sun.org.mozilla.javascript.internal.EcmaError:";
            if (message.startsWith(moz))
                message = message.substring(moz.length() + 1);

            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(message);
            int minus = 5;
            int plus = 5;
            if (matcher.find()) {
                String group = matcher.group();
                while (matcher.find())
                    group = matcher.group();

                int number = Integer.parseInt(group);
                int from = 0;
                if (number > minus)
                    from = number - minus;

                BufferedReader br = new BufferedReader(new StringReader(script));
                int i = 0;
                while (i < from) {
                    try {
                        br.readLine();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    i++;
                }

                out.println("<html>");
                out.println("<body>");
                out.println(message);

                out.println("<br><br><table width=\"100%\">");
                i = from;
                while (true) {
                    String line = null;
                    line = br.readLine();
                    if (line == null)
                        break;
                    if (i - plus >= number)
                        break;
                    out.println("<tr>");
                    out.println("<td width=\"1%\"><font color=green>" + (i + 1)
                            + "</font></td>");
                    String string = line.replace("<", "&lt;").replace(">",
                            "&gt;");
                    if (i + 1 == number)
                        string = "<font color=\"#FF0000\">" + string
                                + "</font>";
                    out.println("<td width=\"99%\"><pre>" + string
                            + "</pre></td>");
                    out.println("</tr>");
                    i++;
                }
                out.println("</table>");
                out.println("</body>");
                out.println("</html>");
            } else {
                out.println("<html>");
                out.println("<body>");
                out.println("<pre>");
                if (e.getLocalizedMessage().equals(SCRIPT_WORKED_TOO_LONG))
                    out.print(e.getLocalizedMessage());
                else
                    e.printStackTrace(out);
                out.println("</pre>");
                out.println("</body>");
                out.println("</html>");
            }
            out.flush();
            out.realWrite();
        }
    }

    protected Out createOut(ByteArrayOutputStream outputStream)
            throws UnsupportedEncodingException {
        return new Out(outputStream);
    }

    public class SimpleOut {
        private Out out;

        public SimpleOut(Out out) {
            this.out = out;
        }

        public Out getOut() {
            return out;
        }

        public void print(Object object) {
            out.print(object);
        }

        public void println(Object object) {
            out.println(object);
        }

        public void write(Object object) {
            out.print(object);
        }
    }
}
