package com.srednal.httpproxytunnel.config;

import java.net.InetSocketAddress;

/** MBean interface that gets bound to ProxyConfig */
public interface ProxyMBean
{
	InetSocketAddress getAddress();
    boolean isEnabled();
    void setEnabled(boolean flag);
    boolean isDynamic();
    void setDynamic(boolean flag);
}
