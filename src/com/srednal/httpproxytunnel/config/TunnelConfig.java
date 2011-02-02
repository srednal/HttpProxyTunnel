package com.srednal.httpproxytunnel.config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.slf4j.LoggerFactory;

/**
 * Represents the configuration of a tunnel.
 * A tunnel has a name, a pair of addresses (listen and target)
 * and a connection timeout.
 */
public class TunnelConfig implements TunnelMBean
{

	/** name of this tunnel */
	private final String name;

	/** address to listen on - immutable */
	private final InetSocketAddress listenAddress;

	/** target to connect to */
	private InetSocketAddress targetAddress;

	/** connection timeout, in seconds */
	private int connectTimeoutSec = 2;

	/**
	 * Create a config.
	 * @param name - name of this tunnel
	 * @param listenPort - port to listen on
	 * @param bindAddress - bind address to listen to (null for loopback)
	 */
	public TunnelConfig(String name, int listenPort, String bindAddress)
	{
		InetAddress bindInetAddr = null;
		try
		{
			bindInetAddr = InetAddress.getByName(bindAddress);
		}
		catch (UnknownHostException e)
		{
			LoggerFactory.getLogger(getClass()).error(
				"[" + name + "] Unknown host for bindAddress " + bindAddress, e);
		}
		this.name = name;
		this.listenAddress = new InetSocketAddress(bindInetAddr, listenPort);
	}

	@Override
	public String getName()
	{
		return name;
	}

	public InetSocketAddress getListenAddress()
	{
		return listenAddress;
	}

	public InetSocketAddress getTargetAddress()
	{
		return targetAddress;
	}

	public void setTargetAddress(InetSocketAddress targetAddress)
	{
		this.targetAddress = targetAddress;
	}

	public void setTargetAddress(String targetHost, int targetPort)
	{
		setTargetAddress(new InetSocketAddress(targetHost, targetPort));
	}

	@Override
	public int getConnectTimeout()
	{
		return connectTimeoutSec;
	}

	@Override
	public void setConnectTimeout(int connectTimeout)
	{
		this.connectTimeoutSec = connectTimeout;
	}

	@Override
	public String getBindAddress()
	{
		return listenAddress.getHostName();
	}

	@Override
	public int getListenPort()
	{
		return listenAddress.getPort();
	}
}
