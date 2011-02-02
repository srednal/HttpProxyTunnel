package com.srednal.httpproxytunnel.io;

import com.srednal.httpproxytunnel.config.TunnelConfig;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.mina.common.*;
import org.apache.mina.util.SessionLog;

public final class HttpProxyIoHandler extends TargetIoHandler
{
    private static final String CRLFCRLF = "\r\n\r\n";
    private static final Charset ASCII = Charset.forName("US-ASCII");

    private final StringBuilder headerBuffer = new StringBuilder(80);

    private boolean connected = false;

    public HttpProxyIoHandler(TunnelConfig tunnelConfig, IoSession clientSession)
    {
        super(tunnelConfig, clientSession);
    }

    @Override
	public void sessionOpened(IoSession session) throws IOException
    {
        session.setTrafficMask(TrafficMask.ALL);
        SessionLog.info(session, "Sending connect request to proxy.");
        InetSocketAddress targetAddress = tunnelConfig.getTargetAddress();
        String connect = "CONNECT " + targetAddress.getHostName() +  ":" + targetAddress.getPort() +" HTTP/1.0" + CRLFCRLF;
        ByteBuffer connectBuff = ByteBuffer.allocate(connect.length()).setAutoExpand(true);
        connectBuff.putString(connect, ASCII.newEncoder());
        connectBuff.flip();
        session.write(connectBuff);
    }

    @Override
	public void messageReceived(IoSession session, Object message)
    {
        if(!connected)
        {
            SessionLog.debug(session, "Reading proxy reply...");
            ByteBuffer buffer = (ByteBuffer) message;
            byte b[] = new byte[1];
            while( buffer.hasRemaining() && !headerBuffer.toString().endsWith(CRLFCRLF) )
            {
            	buffer.get(b);
            	headerBuffer.append(new String(b, ASCII));
            }

            String headers = headerBuffer.toString();
            if(headers.endsWith(CRLFCRLF))
            {
                if(headers.startsWith("HTTP/1.0 200 ") || headers.startsWith("HTTP/1.1 200 "))
                {
                    SessionLog.info(session, "Proxy connected successfully");
                    SessionLog.debug(session, "PROXY REPLY: " + headers);
                    SessionLog.debug(session, "Buffer remainder: " + buffer.remaining());
                    connected = true;
                    enableTunneling(session, clientSession);
                } else
                {
                    SessionLog.error(session, "Bad status reply from proxy: " + headers);
                    session.close();
                }
            } else
            {
                SessionLog.debug(session, "Partial reply: " + headers);
            }
        }
        if(connected)
        {
            SessionLog.debug(session, "forwarding messageReceived to base handler");
            super.messageReceived(session, message);
        }
    }
}
