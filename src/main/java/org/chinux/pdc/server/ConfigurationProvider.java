package org.chinux.pdc.server;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.OperatingSystem;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

public class ConfigurationProvider {

	private static Map<OperatingSystem, Configuration> osConfig = new HashMap<OperatingSystem, Configuration>();
	private static Map<Browser, Configuration> browserConfig = new HashMap<Browser, Configuration>();
	private static Map<SubnetInfo, Configuration> subNetConfigs = new HashMap<SubnetInfo, Configuration>();
	private static Map<String, Configuration> ipConfigs = new HashMap<String, Configuration>();

	private static Configuration configuration = null;

	public static synchronized Configuration getDefaultConfiguration() {
		if (configuration == null) {

			final Properties prop = new Properties();
			try {
				final InputStream is = ConfigurationProvider.class
						.getResourceAsStream("/configuration.properties");
				prop.load(is);
				is.close();
				final List<String> blockedIPs = new ArrayList<String>();
				for (final String str : prop.getProperty("blockedIPs").split(
						",")) {
					blockedIPs.add(str);
				}
				final List<String> blockedURLs = new ArrayList<String>();
				for (final String str : prop.getProperty("blockedURLs").split(
						",")) {
					blockedURLs.add(str);
				}
				final List<String> blockedMediaTypes = new ArrayList<String>();
				for (final String str : prop.getProperty("blockedMediaTypes")
						.split(",")) {
					blockedMediaTypes.add(str);
				}
				configuration = new Configuration(new Boolean(
						prop.getProperty("blockAll")), blockedIPs, blockedURLs,
						blockedMediaTypes, new Integer(
								prop.getProperty("maxResSize")), new Boolean(
								prop.getProperty("l33t")), new Boolean(
								prop.getProperty("rotateImages")), new Boolean(
								prop.getProperty("chainProxy")), new Boolean(
								prop.getProperty("maxResEnabled")),
						Integer.valueOf(prop.getProperty("chainProxyPort")),
						prop.getProperty("chainProxyHost"));
			} catch (final Exception e) {
				System.err
						.println("Configuration file error. Default configuration loaded");
				configuration = new Configuration();
			}

		}
		return configuration;
	}

	public static synchronized void setConfiguration(
			final Configuration newConfiguration, final Object owner) {
		if (owner == null) {
			configuration = newConfiguration;
		}

		if (owner instanceof Browser) {
			browserConfig.put((Browser) owner, newConfiguration);
		}
		if (owner instanceof OperatingSystem) {
			osConfig.put((OperatingSystem) owner, newConfiguration);
		}
		if (owner instanceof InetAddress) {
			ipConfigs.put(((InetAddress) owner).getHostAddress(),
					newConfiguration);
		}
		if (owner instanceof SubnetInfo) {
			subNetConfigs.put((SubnetInfo) owner, newConfiguration);
		}

	}

	public static synchronized Configuration getFromInetSubmask(
			final String submask) {
		try {
			return subNetConfigs.get(new SubnetUtils(submask).getInfo());
		} catch (final Exception e) {
			return null;
		}
	}

	public static synchronized Configuration getConfigurationFromIP(
			final InetAddress address) {
		return ipConfigs.get(address.getHostAddress());
	}

	public static synchronized Configuration getConfigurationFromBrowser(
			final Browser browser) {
		return browserConfig.get(browser);
	}

	public static synchronized Configuration getConfigurationFromOperatingSystem(
			final OperatingSystem os) {
		return osConfig.get(os);
	}

	public static synchronized Configuration getConfigurationFromSubnet(
			final SubnetInfo subnet) {
		return subNetConfigs.get(subnet);
	}

	private static synchronized Configuration fromInetSubmask(
			final InetAddress address) {
		for (final SubnetInfo subnet : subNetConfigs.keySet()) {
			if (subnet.isInRange(address.getHostAddress())) {
				return subNetConfigs.get(subnet);
			}
		}
		return null;
	}

	public static synchronized Configuration getConfiguration(
			final HTTPClientInfo httpClientInfo) {

		Configuration config = null;

		if ((config = getConfigurationFromIP(httpClientInfo.getAddress())) != null) {
			return config;
		}

		if ((config = fromInetSubmask(httpClientInfo.getAddress())) != null) {
			return config;
		}

		if ((config = getConfigurationFromBrowser(httpClientInfo.getBrowser())) != null) {
			return config;
		}

		if ((config = getConfigurationFromOperatingSystem(httpClientInfo
				.getOperatingSystem())) != null) {
			return config;
		}

		return getDefaultConfiguration();
	}
}
