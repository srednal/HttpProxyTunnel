package com.srednal.httpproxytunnel.config;

import java.net.InetSocketAddress;

/**
 * Configuration of the HTTP Proxy which (may) sit between
 * us and the target.
 */
public class ProxyConfig implements ProxyMBean
{
	/**
	 * A dynamic proxy configuration can switch between DIRECT and PROXY modes.
	 * If a connection fails in one mode, the other is tried.
	 * A successful retry will stick the new mode for future tunnel connections.
	 * This allows the tunnel to dynamically switch between environments where
	 * a proxy is or is not present (i.e. work and home).
	 */
	private boolean dynamic = true;

	/** is the proxy enabled?  false means DIRECT */
	private boolean enabled = true;

	/** address of the proxy */
	private final InetSocketAddress address;

	/**
	 * @param host - proxy host
	 * @param port - proxy port
	 */
	public ProxyConfig(String host, int port)
	{
		address = new InetSocketAddress(host, port);
	}

	/**
	 * @return the proxy address
	 */
	@Override
	public InetSocketAddress getAddress()
	{
		return address;
	}

	/**
	 * @return true if the proxy is enabled (PROXY), false if DIRECT
	 */
	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * @param enabled the enabled (PROXY or DIRECT) mode of the proxy
	 */
	@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * A dynamic proxy configuration can switch between DIRECT and PROXY modes.
	 * If a connection fails in one mode, the other is tried.
	 * A successful retry will stick the new mode for future tunnel connections.
	 * This allows the tunnel to dynamically switch between environments where
	 * a proxy is or is not present (i.e. work and home).
	 *
	 * @return true if the proxy is dynamic
	 */
	@Override
	public boolean isDynamic()
	{
		return dynamic;
	}

	/**
	 * @param dynamic set the dynamic mode of the proxy
	 */
	@Override
	public void setDynamic(boolean dynamic)
	{
		this.dynamic = dynamic;
	}
}
