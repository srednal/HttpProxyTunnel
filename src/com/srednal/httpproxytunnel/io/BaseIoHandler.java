package com.srednal.httpproxytunnel.io;

import com.srednal.httpproxytunnel.config.TunnelConfig;
import org.apache.mina.common.*;
import org.apache.mina.util.SessionLog;

public abstract class BaseIoHandler extends IoHandlerAdapter
{
    protected final TunnelConfig tunnelConfig;

    public BaseIoHandler(TunnelConfig tunnelConfig)
    {
        this.tunnelConfig = tunnelConfig;
    }

    public void enableTunneling(IoSession s1, IoSession s2)
    {
        s1.setAttachment(s2);
        s2.setAttachment(s1);
        s1.setTrafficMask(TrafficMask.ALL);
        s2.setTrafficMask(TrafficMask.ALL);
        SessionLog.debug(s1, "Traffic tunneling enabled.");
    }

    @Override
	public final void sessionClosed(IoSession session) throws Exception
    {
        IoSession otherSession = (IoSession) session.getAttachment();
        if( otherSession != null && ! otherSession.isClosing() )
        {
            otherSession.close();
        }
    }

    @Override
	public void messageReceived(IoSession session, Object message)
    {
        ByteBuffer rb = (ByteBuffer )message;
        ByteBuffer wb = ByteBuffer.allocate(rb.remaining());
        rb.mark();
        wb.put(rb);
        rb.reset();
        wb.flip();
        ((IoSession)session.getAttachment()).write(wb);
    }
}