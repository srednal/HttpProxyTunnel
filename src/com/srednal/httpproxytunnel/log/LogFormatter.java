package com.srednal.httpproxytunnel.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogFormatter extends SimpleFormatter
{
	private static final String NEW_LINE = System.getProperty("line.separator");

	@Override
	public String format(LogRecord record)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format("{0,date} {0,time} {1}: {2}",
            new Date(record.getMillis()), record.getLevel().getLocalizedName(), formatMessage(record) ));
        sb.append(NEW_LINE);
        if(record.getThrown() != null)
		{
			try
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            }
            catch(Exception exception) { }
		}
        return sb.toString();
    }
}
