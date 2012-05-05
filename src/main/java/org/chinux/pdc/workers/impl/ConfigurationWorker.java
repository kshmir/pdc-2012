package org.chinux.pdc.workers.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.server.Configuration;
import org.chinux.pdc.server.ConfigurationProvider;
import org.chinux.pdc.workers.api.Worker;

public class ConfigurationWorker implements Worker<DataEvent> {

	private String data;
	private final Pattern configPattern = Pattern
			.compile("([\\w-]+):([^\\ ^\n^\r]+)");
	private final Map<String, String> values = new HashMap<String, String>();

	@Override
	public DataEvent DoWork(final DataEvent dataEvent) {
		this.data = new String(dataEvent.getData());
		final Configuration configuration = ConfigurationProvider
				.getConfiguration();

		final Matcher match = this.configPattern.matcher(this.data);
		while (match.find()) {
			this.values.put(match.group(1), match.group(2));
		}

		final boolean blockAll = this.values.containsKey("blockAll") ? Boolean
				.valueOf(this.values.get("blockAll")) : configuration
				.isBlockAll();

		final List<String> blockedIPs = this.values.containsKey("blockedIPs") ? new ArrayList<String>()
				: configuration.getBlockedIPs();
		if (this.values.containsKey("blockedIPs")) {
			for (final String str : this.values.get("blockedIPs").split(",")) {
				blockedIPs.add(str);
			}
		}

		final List<String> blockedURLs = this.values.containsKey("blockedURLs") ? new ArrayList<String>()
				: configuration.getBlockedURLs();
		if (this.values.containsKey("blockedURLs")) {
			for (final String str : this.values.get("blockedURLs").split(",")) {
				blockedURLs.add(str);
			}
		}

		final List<String> blockedMediaTypes = this.values
				.containsKey("blockedMediaTypes") ? new ArrayList<String>()
				: configuration.getBlockedMediaTypes();
		if (this.values.containsKey("blockedMediaTypes")) {
			for (final String str : this.values.get("blockedMediaTypes").split(
					",")) {
				blockedMediaTypes.add(str);
			}
		}

		final int maxResSize = this.values.containsKey("maxResSize") ? Integer
				.valueOf(this.values.get("maxResSize")) : configuration
				.getMaxResSize();
		final boolean l33t = this.values.containsKey("l33t") ? Boolean
				.valueOf(this.values.get("l33t")) : configuration.isL33t();
		final boolean rotateImages = this.values.containsKey("rotateImages") ? Boolean
				.valueOf(this.values.get("rotateImages")) : configuration
				.isRotateImages();

		ConfigurationProvider.setConfiguration(new Configuration(blockAll,
				blockedIPs, blockedURLs, blockedMediaTypes, maxResSize, l33t,
				rotateImages));

		final ServerDataEvent event = new ServerDataEvent(
				((ServerDataEvent) dataEvent).getChannel(),
				"Configuration changed\n".getBytes(), dataEvent.getReceiver());
		event.setCanClose(true);
		event.setCanSend(true);
		return event;
	}
}