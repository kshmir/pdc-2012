package org.chinux.pdc.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConfigurationProvider {

	private static Configuration configuration = null;

	public static synchronized Configuration getConfiguration() {
		if (configuration == null) {
			try {
				final FileInputStream fis = new FileInputStream(
						"src/main/resources/configuration.properties");
				final Properties prop = new Properties();
				try {
					prop.load(fis);
					fis.close();
					final List<String> blockedIPs = new ArrayList<String>();
					for (final String str : prop.getProperty("blockedIPs")
							.split(",")) {
						blockedIPs.add(str);
					}
					final List<String> blockedURLs = new ArrayList<String>();
					for (final String str : prop.getProperty("blockedURLs")
							.split(",")) {
						blockedURLs.add(str);
					}
					final List<String> blockedMediaTypes = new ArrayList<String>();
					for (final String str : prop.getProperty(
							"blockedMediaTypes").split(",")) {
						blockedMediaTypes.add(str);
					}
					configuration = new Configuration(new Boolean(
							prop.getProperty("blockAll")), blockedIPs,
							blockedURLs, blockedMediaTypes, new Integer(
									prop.getProperty("maxResSize")),
							new Boolean(prop.getProperty("l33t")), new Boolean(
									prop.getProperty("rotateImages")));
				} catch (final IOException e) {
					System.err
							.println("Configuration file error. Default configuration loaded");
					configuration = new Configuration();
				}
			} catch (final FileNotFoundException e) {
				System.err
						.println("Configuration file not found. Default configuration loaded");
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
