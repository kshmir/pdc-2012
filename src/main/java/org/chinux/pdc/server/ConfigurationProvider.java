package org.chinux.pdc.server;

public class ConfigurationProvider {

	private static Configuration configuration = new Configuration();

	public static Configuration getConfiguration() {
		return configuration;
	}

	public static void setConfiguration(final Configuration newConfiguration) {
		configuration = newConfiguration;
	}
}
