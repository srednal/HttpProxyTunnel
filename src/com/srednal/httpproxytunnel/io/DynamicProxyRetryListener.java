package com.srednal.httpproxytunnel.io;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoFuture;
import org.apache.mina.common.IoFutureListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.util.SessionLog;

import com.srednal.httpproxytunnel.config.ProxyConfig;

class DynamicProxyRetryListener implements IoFutureListener
{
    private final IoSession clientSession;
    private final ClientIoHandler clientHandler;
    private final ProxyConfig proxyConfig;
    private boolean usingProxy;
    private boolean isRetry;

    DynamicProxyRetryListener(IoSession clientSession, boolean usingProxy)
    {
    	this.isRetry = false;
        this.clientSession = clientSession;
        this.clientHandler = (ClientIoHandler)clientSession.getHandler();
        this.proxyConfig = clientHandler.proxyConfig;
        this.usingProxy = usingProxy;
    }

    @Override
    public void operationComplete(IoFuture f)
    {
        ConnectFuture future = (ConnectFuture) f;
        try
        {
            IoSession targetSession = future.getSession();
            if(isRetry)
            {
                proxyConfig.setEnabled(usingProxy);
                SessionLog.info(targetSession, "Proxy mode switched to " + usingProxyName() + " for future connections.");
            }
        }
        catch(RuntimeIOException e)
        {
            SessionLog.debug(clientSession, "Target connection exception: ", e);
            if(!isRetry && proxyConfig.isDynamic())
            {
                isRetry = true;
                usingProxy = !usingProxy;
                SessionLog.info(clientSession, "Connect failed as " + usingProxyName(!usingProxy) + ", trying " + usingProxyName());
                clientHandler.connect(clientSession, usingProxy, this);
            }
            else
            {
                SessionLog.error(clientSession, "Target conection failed " + (isRetry ? "after retry" : "" ) +", giving up.");
                clientSession.close();
            }
        }
    }

    private String usingProxyName()
    {
        return usingProxyName(usingProxy);
    }

    private String usingProxyName(boolean proxy)
    {
        return proxy ? "PROXY" : "DIRECT";
    }
}
