package com.srednal.httpproxytunnel.log;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.support.BaseIoService;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.util.SessionLog;

/**
 * Log messages seen on the socket - debugging mainly
 */
public class DebugLogFilter extends LoggingFilter
{
	private static final String NAME = DebugLogFilter.class.getSimpleName();

	public static void init(BaseIoService acceptor)
	{
		acceptor.getFilterChain().addFirst(NAME, new DebugLogFilter());
	}

    @Override
	public void messageReceived(NextFilter nextFilter, IoSession session, Object message)
    {
        if(message != null && SessionLog.isDebugEnabled(session))
		{
			SessionLog.debug(session, "RECEIVED: " + message);
		}
        nextFilter.messageReceived(session, message);
    }

    @Override
	public void messageSent(NextFilter nextFilter, IoSession session, Object message)
    {
        if(SessionLog.isDebugEnabled(session))
		{
			SessionLog.debug(session, "SENT: " + message);
		}
        nextFilter.messageSent(session, message);
    }

    @Override
	public void filterWrite(NextFilter nextFilter, IoSession session, org.apache.mina.common.IoFilter.WriteRequest writeRequest)
    {
        if(SessionLog.isDebugEnabled(session))
		{
			SessionLog.debug(session, "WRITE: " + writeRequest);
		}
        nextFilter.filterWrite(session, writeRequest);
    }
}
