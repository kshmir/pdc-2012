package org.chinux.pdc.server;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConfigurationProvider {

	private static Configuration configuration = null;

	public static synchronized Configuration getConfiguration() {
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
			final Configuration newConfiguration) {
		configuration = newConfiguration;
	}
}
