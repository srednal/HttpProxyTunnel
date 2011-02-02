package com.srednal.httpproxytunnel.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Initialize and hold configuration for httpproxytunnel.
 */
public class Configuration
{
	/** configuration file for the tunnels and proxies */
	public static final File CONFIG_FILE = new File(System.getProperty("user.home"), ".httpproxytunnel.properties");

	/** base logger config file - will either grab from ~/.httpproxytunnel-log.properties or
	 * as a classpath resource at /httpproxytunnel-log.properties
	 */
	private static final String LOG_CONFIG_NAME = "httpproxytunnel-log.properties";

	/** prefered log config file location in HOME */
	private static final File LOG_CONFIG_FILE = new File(System.getProperty("user.home"), "." + LOG_CONFIG_NAME );

	/** singleton instance */
	private static final Configuration INSTANCE = new Configuration();

	/** configuration for the proxy */
	private ProxyConfig proxyConfig;

	/** The tunnels, keyed by name */
	private final Map<String, TunnelConfig> tunnels = new HashMap<String, TunnelConfig>();

	/** get singleton configuration instance */
	public static Configuration getInstance()
	{
		return INSTANCE;
	}

	/** Get the proxy configuration */
	public ProxyConfig getProxyConfig()
	{
		return proxyConfig;
	}

	/** Get the tunnel configurations */
	public Collection<TunnelConfig> getTunnelConfigs()
	{
		return tunnels.values();
	}

	///////////////////////////////////////////////////////////////////////////

	/** instantiate and initialize */
	private Configuration()
	{
		load();
	}

	/**
	 * Load and initialize the configuration:
	 *  Logger, Property file, proxyConfig, tunnelConfigs, MBeans
	 */
	private void load()
	{
		initLog();

		Properties properties = new Properties();
		FileInputStream propStream = null;
		try
		{
			// Load the properties
			propStream = new FileInputStream(CONFIG_FILE);
			properties.load(propStream);
		}
		catch (IOException e)
		{
			Logger.getLogger(getClass().getName()).log(Level.SEVERE,
					"Error reading config file "+CONFIG_FILE+", unable to proceed.", e);
			// no properties file is a fatal error
			throw new Error("Error reading httpproxytunnel configuration file " + CONFIG_FILE, e);
		}
		finally
		{
			if (propStream != null) {
				try {
					propStream.close();
				} catch (IOException ioexception) { }
			}
		}

		initProxy(properties);
		initTunnels(properties);
		initMBeans();
	}

	/** Initialize the logger.
	 * Note that we are initializing java.util.logging here, and using that to log in this class.
	 * The rest of the logging happens from MINA thru slf4j, which is using j.u.l because we include
	 * the slf5j-jdk14 impl jar with the runtime.
     */
	private void initLog()
	{
		String configSource = null;
		InputStream stream = null;
		try
		{
			// try reading the ~/.httpproxytunnel-log.properties file
			if (LOG_CONFIG_FILE.canRead())
			{
				configSource = LOG_CONFIG_FILE.getAbsolutePath();
				stream = ((InputStream) (new FileInputStream(LOG_CONFIG_FILE)));
			}
			else
			{
				// no? then grab the default config from the jar
				URL configUrl = ((Object) this).getClass().getResource( "/" + LOG_CONFIG_NAME);
				if (configUrl != null)
				{
					configSource = configUrl.toString();
					stream = configUrl.openStream();
				}
			}
			if (stream != null)
			{
				// Initializing
				LogManager.getLogManager().readConfiguration(stream);
				Logger.getLogger(getClass().getName()).log(Level.INFO, "Logging initialized from " + configSource);
			}
		}
		catch (IOException e)
		{
			// as logger is not inited, this will hopefully log _somewhere_
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error reading log configuration", e);
		}
		finally
		{
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException ioexception) { }
			}
		}
	}

	/** Initialize the proxyConfig from the properties */
	private void initProxy(Properties properties)
	{
		String host = properties.getProperty("proxyHost");
		int port = Integer.valueOf(properties.getProperty("proxyPort")).intValue();

		// default for enabled and dynamic is true - gotta say "false" to turn them off
		// anything else and they stay set
		boolean enabled = ! "false".equals(properties.getProperty("proxyEnabled"));
		boolean dynamic = ! "false".equals(properties.getProperty("proxyDynamic"));

		proxyConfig = new ProxyConfig(host, port);
		proxyConfig.setEnabled(enabled);
		proxyConfig.setDynamic(dynamic);
	}

	/** Initialize the tunnelConfigs from the properties */
	private void initTunnels(Properties properties)
	{
		tunnels.clear();
		// first, we need a property to tell us which tunnels to config.
		// That would be "tunnels", containing a comma-delimited list of tunnel names
		for (String tunnelName : properties.getProperty("tunnels").split("\\s*,\\s*"))
		{
			// find properties for this tunnelName

			// listen port and bind address
			int listenPort = Integer.valueOf( properties.getProperty( tunnelName + ".listenPort" ) );
			String bindAddress = properties.getProperty( tunnelName + ".bindAddress");

			// target host and port
			String targetHost = properties.getProperty( tunnelName + ".targetHost" );
			int targetPort = Integer.valueOf( properties.getProperty( tunnelName + ".targetPort" ) );

			// connect timeout
			String connectTimeout = properties.getProperty( tunnelName + ".connectTimeout" );
			if (connectTimeout == null)
			{
				// global value
				connectTimeout = properties.getProperty("connectTimeout");
			}

			// create the tunnel config
			TunnelConfig tunnel = new TunnelConfig(tunnelName, listenPort, bindAddress);
			tunnel.setTargetAddress(targetHost, targetPort);

			// if connectTimeout not set, leave the default alone
			if (connectTimeout != null)
			{
				tunnel.setConnectTimeout(Integer.valueOf(connectTimeout));
			}

			tunnels.put(tunnelName, tunnel);
		}
	}

	/** Initialize the MBeans.
	 *  You can connect to the mbean server using (for example) jconsole, and can
	 *  query the configuration, and change a few things live.
	 *
	 *  There's one MBean for the proxyConfig:  com.srednal.httpproxytunnel.config:type=Proxy
	 *  and one MBean for each tunnelConfig: com.srednal.httpproxytunnel.config:type=Tunnel,name=<tunnelName>
	 */
	private void initMBeans()
	{
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		try
		{
			String domain = getClass().getPackage().getName();
			mbeanServer.registerMBean(proxyConfig, new ObjectName( domain + ":type=Proxy" ) );

			for (TunnelConfig tc : tunnels.values() )
			{
				mbeanServer.registerMBean(tc, new ObjectName(domain + ":type=Tunnel,name=" + tc.getName()));
			}
		}
		catch (JMException e)
		{
			Logger.getLogger( getClass().getName()).log(Level.SEVERE, "Error initializing MBeans", e);
		}
	}
}
