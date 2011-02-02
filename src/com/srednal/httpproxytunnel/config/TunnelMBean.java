package com.srednal.httpproxytunnel.config;

/** MBean interface that gets bound to TunnelConfig */
public interface TunnelMBean
{
    public abstract String getName();
    public abstract int getListenPort();
    public abstract String getBindAddress();
    public abstract int getConnectTimeout();
    public abstract void setConnectTimeout(int i);
}
