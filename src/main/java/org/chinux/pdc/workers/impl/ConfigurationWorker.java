package org.chinux.pdc.workers.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.server.Configuration;
import org.chinux.pdc.server.ConfigurationProvider;
import org.chinux.pdc.workers.api.Worker;

public class ConfigurationWorker implements Worker<DataEvent> {

	private String data;
	private boolean logged = false;
	private boolean helo = false;
	private String username = null;
	private Map<String, String> users;
	private Pattern ipPattern = Pattern
			.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

	@Override
	public DataEvent DoWork(final DataEvent dataEvent) {
		this.data = new String(dataEvent.getData()).split("\r")[0];
		this.users = new HashMap<String, String>();
		final String command = this.data.split(" ")[0];
		ServerDataEvent event;
		byte[] resp = null;

		if (!this.helo) {
			return this.helo(dataEvent, command);
		}

		if (!this.logged) {
			final Properties props = new Properties();
			try {
				final FileInputStream fis = new FileInputStream(
						"src/main/resources/users.properties");
				props.load(fis);
				fis.close();

			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			final String users[] = props.getProperty("user").split(",");
			final String passwords[] = props.getProperty("password").split(",");
			for (int i = 0; i < users.length && i < passwords.length; i++) {
				this.users.put(users[i], passwords[i]);
			}
			return this.login(dataEvent, command);
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
		} else if (command.equals("LOGOUT")) {
			this.quit();
			resp = "250 Logout OK\n".getBytes();
		} else {
			resp = "Invalid Command\n".getBytes();
		}

		event = new ServerDataEvent(((ServerDataEvent) dataEvent).getChannel(),
				resp, dataEvent.getReceiver());
		event.setCanClose(true);
		event.setCanSend(true);
		return event;
	}

	private void quit() {
		final Configuration configuration = ConfigurationProvider
				.getConfiguration();
		final Properties prop = new Properties();
		String aux = "";
		prop.put("blockAll", configuration.isBlockAll() ? "true" : "false");
		for (final String str : configuration.getBlockedIPs()) {
			aux += str + ",";
		}
		prop.put("blockedIPs", aux);
		aux = "";
		for (final String str : configuration.getBlockedURLs()) {
			aux += str + ",";
		}
		prop.put("blockedURLs", aux);
		aux = "";
		for (final String str : configuration.getBlockedMediaTypes()) {
			aux += str + ",";
		}
		prop.put("blockedMediaTypes", aux);
		prop.put("maxResSize",
				new Integer(configuration.getMaxResSize()).toString());
		prop.put("l33t", configuration.isL33t() ? "true" : "false");
		prop.put("rotateImages", configuration.isRotateImages() ? "true"
				: "false");
		try {
			final FileOutputStream fos = new FileOutputStream(
					"src/main/resources/configuration.properties");
			prop.store(fos, "pdc2012");
			fos.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		this.username = null;
		this.helo = false;
		this.logged = false;
	}

	private DataEvent helo(final DataEvent dataEvent, final String command) {
		ServerDataEvent event;
		byte[] resp;
		if (command.compareTo("HELO") != 0) {
			resp = "".getBytes();
		} else {
			this.helo = true;
			resp = "250 Hello user, I am glad to meet you\nEnter user name: "
					.getBytes();
		}
		event = new ServerDataEvent(((ServerDataEvent) dataEvent).getChannel(),
				resp, dataEvent.getReceiver());
		event.setCanClose(true);
		event.setCanSend(true);
		return event;
	}

	private DataEvent login(final DataEvent dataEvent, final String command) {
		ServerDataEvent event;
		byte[] resp;
		if (this.username != null) {
			if (!this.users.containsKey(this.username)
					|| command.compareTo(this.users.get(this.username)) != 0) {
				resp = "Login Incorrect\n".getBytes();
				this.username = null;
			} else {
				this.logged = true;
				resp = "250 Login OK\n".getBytes();
			}
		} else {
			this.username = command;
			resp = "Enter password: ".getBytes();
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
			String resp = "";
			for (final String str : configuration.getBlockedIPs()) {
				resp += str + " ";
			}
			return (resp + "\n").getBytes();
		} else if (property.equals("blockedURLs")) {
			String resp = "";
			for (final String str : configuration.getBlockedURLs()) {
				resp += str + " ";
			}
			return (resp + "\n").getBytes();
		} else if (property.equals("blockedMediaTypes")) {
			String resp = "";
			for (final String str : configuration.getBlockedMediaTypes()) {
				resp += str + " ";
			}
			return (resp + "\n").getBytes();
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
			resp = ("BlockedMediaTypes set to " + blockedMediaTypes + "\n")
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