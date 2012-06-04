package org.chinux.pdc.workers.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ErrorDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.server.Configuration;
import org.chinux.pdc.server.ConfigurationProvider;
import org.chinux.pdc.server.LoginService;
import org.chinux.pdc.server.LoginService.Code;
import org.chinux.pdc.server.User;

public class ConfigurationWorker extends LogueableWorker {

	private Pattern ipPattern = Pattern
			.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/(0|8|16|24|32)");

	public ConfigurationWorker(final String propertiespath) {
		super(propertiespath);
	}

	@Override
	public DataEvent DoWork(final DataEvent dataEvent) {
		final ServerDataEvent event;
		/* if there is an error.. */
		if (dataEvent instanceof ErrorDataEvent) {
			this.resetWorkerState();
			return dataEvent;
		}
		/* obtains the command to process */
		final String command = this.obtainCommand(dataEvent);

		final InetAddress addr = ((ServerDataEvent) dataEvent).getChannel()
				.socket().getInetAddress();
		final int port = ((ServerDataEvent) dataEvent).getChannel().socket()
				.getPort();
		User currUser = new User(port, addr);
		if (this.users.contains(currUser)) {
			for (final User u : this.users) {
				if (u.equals(currUser)) {
					currUser = u;
				}
			}
		}
		/* initial salutation */
		if (!currUser.isGreeted()) {
			currUser.setGreeted(true);
			this.users.add(currUser);
			return this.helo(dataEvent, command, currUser);
		}
		/* if the user is not logged , it should be */
		if (!currUser.isLogged()) {
			this.loginservice = LoginService.getInstance(this.propertiespath);
			final Code code = this.loginservice.login(dataEvent, command,
					currUser);
			currUser.setLogged(this.loginservice.isLogged(code));
			return this.loginservice.createResponseEvent(code, dataEvent);
		}
		/* changes the proxy configuration */
		event = this.processConfiguration(dataEvent, command);
		return event;
	}

	private ServerDataEvent processConfiguration(final DataEvent dataEvent,
			final String command) {
		ServerDataEvent event;
		byte[] resp;
		final Configuration configuration = ConfigurationProvider
				.getConfiguration();

		resp = this.processCommand(command, configuration);

		event = new ServerDataEvent(((ServerDataEvent) dataEvent).getChannel(),
				ByteBuffer.wrap(resp), dataEvent.getReceiver());
		event.setCanClose(false);
		event.setCanSend(true);
		return event;
	}

	@Override
	void resetWorkerState() {
		this.loginservice = null;
		LoginService.resetInstance();
		this.quit();
	}

	private byte[] processCommand(final String command,
			final Configuration configuration) {
		byte[] resp;
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
		return resp;
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
		boolean chainProxy = configuration.isChainProxy();
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
		} else if (property.equals("chainProxy")) {
			if (this.data.split(" ").length >= 3) {
				chainProxy = Boolean.valueOf(this.data.split(" ")[2]);
				resp = ("ChainProxy set to " + this.data.split(" ")[2] + "\n")
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
				rotateImages, chainProxy));
		return resp;
	}

}