package com.srednal.httpproxytunnel;

import org.apache.mina.common.IoHandler;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.slf4j.LoggerFactory;

import com.srednal.httpproxytunnel.config.Configuration;
import com.srednal.httpproxytunnel.config.TunnelConfig;
import com.srednal.httpproxytunnel.io.ClientIoHandler;
import com.srednal.httpproxytunnel.log.DebugLogFilter;

public class Main
{
	/**
	 * Launch the httproxytunnel daemon.
	 *
	 * Looks for configuration in ~/.httpproxytunnel.properties
	 * and log configuration from ~/.httpproxytunnel-log.properties
	 *
	 * @param args - ignored
	 */
	public static void main(String args[]) throws Exception
	{
		// Listener for connections
		SocketAcceptor acceptor = new SocketAcceptor();
		acceptor.getDefaultConfig().setReuseAddress(true);

		DebugLogFilter.init(acceptor);

		// set up each tunnel
		for (TunnelConfig tunnelConfig : Configuration.getInstance().getTunnelConfigs())
		{
			LoggerFactory.getLogger(Main.class).info(
					"[" + tunnelConfig.getName() + "] Listening on "
							+ tunnelConfig.getListenAddress() + " for "
							+ tunnelConfig.getTargetAddress());

			// handler for a connection from the client bound to the listenAddress
			IoHandler handler = new ClientIoHandler(tunnelConfig);
			acceptor.bind(tunnelConfig.getListenAddress(), handler);
		}
	}
}
