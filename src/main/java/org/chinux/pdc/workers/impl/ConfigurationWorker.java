package org.chinux.pdc.workers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.server.Configuration;
import org.chinux.pdc.server.ConfigurationProvider;
import org.chinux.pdc.workers.api.Worker;

public class ConfigurationWorker implements Worker<DataEvent> {

	private String data;
	private String password = "pdc2012";
	private boolean logged = false;
	private Pattern ipPattern = Pattern
			.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

	@Override
	public DataEvent DoWork(final DataEvent dataEvent) {
		this.data = new String(dataEvent.getData()).split("\r")[0];
		final String command = this.data.split(" ")[0];
		ServerDataEvent event;
		byte[] resp = null;

		if (!this.logged) {
			return this.login(dataEvent);
		}

		final Configuration configuration = ConfigurationProvider
				.getConfiguration();

		if (command.equals("GET")) {
			if (this.data.split(" ").length <= 1) {
				resp = "Invalid Parameter\n".getBytes();
			} else {
				resp = this.get(this.data.split(" ")[1], configuration);
			}
		} else if (command.equals("GETALL")) {
			resp = configuration.toString().getBytes();
		} else if (command.equals("SET")) {
			if (this.data.split(" ").length <= 1) {
				resp = "Invalid Parameter\n".getBytes();
			} else {
				resp = this.set(this.data.split(" ")[1], configuration);
			}
		} else {
			resp = "Invalid Command\n".getBytes();
		}

		event = new ServerDataEvent(((ServerDataEvent) dataEvent).getChannel(),
				resp, dataEvent.getReceiver());
		event.setCanClose(true);
		event.setCanSend(true);
		return event;
	}

	private DataEvent login(final DataEvent dataEvent) {
		ServerDataEvent event;
		byte[] resp;
		if (this.data.compareTo(this.password) != 0) {
			resp = "Login Incorrect\n".getBytes();
		} else {
			this.logged = true;
			resp = "Login OK\n".getBytes();
		}
		event = new ServerDataEvent(((ServerDataEvent) dataEvent).getChannel(),
				resp, dataEvent.getReceiver());
		event.setCanClose(true);
		event.setCanSend(true);
		return event;
	}

	private byte[] get(final String property, final Configuration configuration) {
		if (property.equals("blockAll")) {
			return ((new Boolean(configuration.isBlockAll())).toString() + "\n")
					.getBytes();
		} else if (property.equals("l33t")) {
			return ((new Boolean(configuration.isL33t())).toString() + "\n")
					.getBytes();
		} else if (property.equals("rotateImages")) {
			return ((new Boolean(configuration.isRotateImages())).toString() + "\n")
					.getBytes();
		} else if (property.equals("maxResSize")) {
			return ((new Integer(configuration.getMaxResSize())).toString() + "\n")
					.getBytes();
		} else if (property.equals("blockedIPs")) {
			byte[] resp = null;
			for (final String str : configuration.getBlockedIPs()) {
				resp = (resp.toString() + str + " ").getBytes();
			}
			return (resp.toString() + "\n").getBytes();
		} else if (property.equals("blockedURLs")) {
			byte[] resp = null;
			for (final String str : configuration.getBlockedURLs()) {
				resp = (resp.toString() + str + " ").getBytes();
			}
			return (resp.toString() + "\n").getBytes();
		} else if (property.equals("blockedMediaTypes")) {
			byte[] resp = null;
			for (final String str : configuration.getBlockedMediaTypes()) {
				resp = (resp.toString() + str + " ").getBytes();
			}
			return (resp.toString() + "\n").getBytes();
		} else {
			return "Invalid Configuration Parameter\n".getBytes();
		}
	}

	private byte[] set(final String property, final Configuration configuration) {
		boolean blockAll = configuration.isBlockAll();
		boolean l33t = configuration.isL33t();
		boolean rotateImages = configuration.isRotateImages();
		int maxResSize = configuration.getMaxResSize();
		List<String> blockedIPs = configuration.getBlockedIPs();
		List<String> blockedURLs = configuration.getBlockedURLs();
		List<String> blockedMediaTypes = configuration.getBlockedMediaTypes();
		byte[] resp = null;
		resp = "Configuration changed\n".getBytes();

		if (property.equals("blockAll")) {
			if (this.data.split(" ").length >= 3) {
				blockAll = Boolean.valueOf(this.data.split(" ")[2]);
				resp = ("BlockAll set to " + this.data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "Invalid Parameter\n".getBytes();
			}
		} else if (property.equals("l33t")) {
			if (this.data.split(" ").length >= 3) {
				l33t = Boolean.valueOf(this.data.split(" ")[2]);
				resp = ("l33t set to " + this.data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "Invalid Parameter\n".getBytes();
			}
		} else if (property.equals("rotateImages")) {
			if (this.data.split(" ").length >= 3) {
				rotateImages = Boolean.valueOf(this.data.split(" ")[2]);
				resp = ("RotateImages set to " + this.data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "Invalid Parameter\n".getBytes();
			}
		} else if (property.equals("maxResSize")) {
			if (this.data.split(" ").length >= 3) {
				maxResSize = Integer.valueOf(this.data.split(" ")[2]);
				resp = ("MaxResSize set to " + this.data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "Invalid Parameter\n".getBytes();
			}
		} else if (property.equals("blockedIPs")) {
			blockedIPs = new ArrayList<String>();
			if (this.data.split(" ").length >= 3) {
				for (final String str : this.data.split(" ")[2].split(",")) {
					final Matcher match = this.ipPattern.matcher(str);
					if (match.find()) {
						blockedIPs.add(str);
					} else {
						resp = "Invalid Parameter\n".getBytes();
						break;
					}
				}
			}
			resp = ("BlockedIPs set to " + blockedIPs + "\n").getBytes();
		} else if (property.equals("blockedURLs")) {
			blockedURLs = new ArrayList<String>();
			if (this.data.split(" ").length >= 3) {
				for (final String str : this.data.split(" ")[2].split(",")) {
					blockedURLs.add(str);
				}
			}
			resp = ("BlockedURLs set to " + blockedURLs + "\n").getBytes();
		} else if (property.equals("blockedMediaTypes")) {
			blockedMediaTypes = new ArrayList<String>();
			if (this.data.split(" ").length >= 3) {
				for (final String str : this.data.split(" ")[2].split(",")) {
					blockedMediaTypes.add(str);
				}
			}
			resp = ("BlockedMediaTypes set to " + blockedURLs + "\n")
					.getBytes();
		} else {
			return "Invalid Configuration Parameter\n".getBytes();
		}
		ConfigurationProvider.setConfiguration(new Configuration(blockAll,
				blockedIPs, blockedURLs, blockedMediaTypes, maxResSize, l33t,
				rotateImages));
		return resp;
	}
}