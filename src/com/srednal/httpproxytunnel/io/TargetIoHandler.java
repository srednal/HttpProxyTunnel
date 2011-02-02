package com.srednal.httpproxytunnel.io;

import com.srednal.httpproxytunnel.config.TunnelConfig;
import java.io.IOException;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TrafficMask;
import org.apache.mina.filter.LoggingFilter;

public class TargetIoHandler extends BaseIoHandler
{
	protected final IoSession clientSession;

	public TargetIoHandler(TunnelConfig tunnelConfig, IoSession clientSession)
	{
		super(tunnelConfig);
		this.clientSession = clientSession;
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception
	{
		session.setTrafficMask(TrafficMask.NONE);
		session.setAttribute(LoggingFilter.PREFIX, clientSession.getAttribute(LoggingFilter.PREFIX));
	}

	@Override
	public void sessionOpened(IoSession session) throws IOException
	{
		enableTunneling(session, clientSession);
	}
}
