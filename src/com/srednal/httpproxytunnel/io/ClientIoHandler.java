package com.srednal.httpproxytunnel.io;

import java.io.IOException;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoFutureListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TrafficMask;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.util.SessionLog;

import com.srednal.httpproxytunnel.config.Configuration;
import com.srednal.httpproxytunnel.config.ProxyConfig;
import com.srednal.httpproxytunnel.config.TunnelConfig;
import com.srednal.httpproxytunnel.log.DebugLogFilter;

public final class ClientIoHandler extends BaseIoHandler
{
    private static int sessionId = 0;
    final ProxyConfig proxyConfig = Configuration.getInstance().getProxyConfig();

    public ClientIoHandler(TunnelConfig tunnelConfig)
    {
        super(tunnelConfig);
    }

    private static synchronized int nextSessionId()
    {
        sessionId = (sessionId + 1) % 100;
        return sessionId;
    }

    @Override
	public void sessionCreated(IoSession session)
    {
        session.setTrafficMask(TrafficMask.NONE);
        String logPrefix = "[" + tunnelConfig.getName() + "(" + nextSessionId() + ")] ";
        session.setAttribute(LoggingFilter.PREFIX, logPrefix);
    }

    @Override
	public void sessionOpened(IoSession clientSession) throws IOException
    {
        boolean usingProxy = proxyConfig.isEnabled();
        connect(clientSession, usingProxy, ((IoFutureListener) (new DynamicProxyRetryListener(clientSession, usingProxy))));
    }

    protected void connect(IoSession clientSession, boolean usingProxy, IoFutureListener listener)
    {
        SocketConnector connector = new SocketConnector();
        connector.getDefaultConfig().setConnectTimeout(tunnelConfig.getConnectTimeout());
        DebugLogFilter.init(connector);
        ConnectFuture cf;
        if(usingProxy)
        {
            SessionLog.debug(clientSession, "Attempting connect via PROXY");
            cf = connector.connect(proxyConfig.getAddress(), new HttpProxyIoHandler(tunnelConfig, clientSession));
        }
        else
        {
            SessionLog.debug(clientSession, "Attempting connect via DIRECT");
            cf = connector.connect(tunnelConfig.getTargetAddress(), new TargetIoHandler(tunnelConfig, clientSession));
        }
        cf.addListener(listener);
    }
}
