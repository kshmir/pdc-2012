package org.chinux.pdc.workers.impl;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
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
			.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/(8|16|24|32)");

	public ConfigurationWorker() {
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
			this.loginservice = LoginService.getInstance();
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
			resp = "Logout OK\nEnter user name: ".getBytes();
		} else {
			resp = "Invalid Command\n".getBytes();
		}
		return resp;
	}

	private void quit() {
	}

	private byte[] get(final String property, final Configuration configuration) {
		if (property.toLowerCase().equals("blockall")) {
			return ((new Boolean(configuration.isBlockAll())).toString() + "\n")
					.getBytes();
		} else if (property.toLowerCase().equals("l33t")) {
			return ((new Boolean(configuration.isL33t())).toString() + "\n")
					.getBytes();
		} else if (property.toLowerCase().equals("rotateimages")) {
			return ((new Boolean(configuration.isRotateImages())).toString() + "\n")
					.getBytes();
		} else if (property.toLowerCase().equals("maxressize")) {
			return ((new Integer(configuration.getMaxResSize())).toString() + "\n")
					.getBytes();
		} else if (property.toLowerCase().equals("blockedips")) {
			String resp = "";
			for (final String str : configuration.getBlockedIPs()) {
				resp += str + " ";
			}
			return (resp + "\n").getBytes();
		} else if (property.toLowerCase().equals("blockedurls")) {
			String resp = "";
			for (final String str : configuration.getBlockedURLs()) {
				resp += str + " ";
			}
			return (resp + "\n").getBytes();
		} else if (property.toLowerCase().equals("blockedmediatypes")) {
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
		boolean maxResEnabled = configuration.isMaxResEnabled();
		Integer chainProxyPort = configuration.getChainProxyPort();
		String chainProxyHost = configuration.getChainProxyHost();
		int maxResSize = configuration.getMaxResSize();
		List<String> blockedIPs = configuration.getBlockedIPs();
		List<String> blockedURLs = configuration.getBlockedURLs();
		List<String> blockedMediaTypes = configuration.getBlockedMediaTypes();
		byte[] resp = null;
		resp = "Configuration changed\n".getBytes();

		if (property.toLowerCase().equals("maxresenabled")) {
			if (this.data.split(" ").length >= 3) {
				maxResEnabled = Boolean.valueOf(this.data.split(" ")[2]);
				resp = ("maxResEnabled set to " + this.data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("chainproxyport")) {
			if (this.data.split(" ").length >= 3) {
				chainProxyPort = Integer.valueOf(this.data.split(" ")[2]);
				resp = ("chainProxyPort set to " + this.data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("chainproxyhost")) {
			if (this.data.split(" ").length >= 3) {
				chainProxyHost = this.data.split(" ")[2];
				resp = ("chainProxyHost set to " + this.data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("blockall")) {
			if (this.data.split(" ").length >= 3) {
				blockAll = Boolean.valueOf(this.data.split(" ")[2]);
				resp = ("BlockAll set to " + this.data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("chainproxy")) {
			if (this.data.split(" ").length >= 3) {
				chainProxy = Boolean.valueOf(this.data.split(" ")[2]);
				resp = ("ChainProxy set to " + this.data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("l33t")) {
			if (this.data.split(" ").length >= 3) {
				l33t = Boolean.valueOf(this.data.split(" ")[2]);
				resp = ("l33t set to " + this.data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("rotateimages")) {
			if (this.data.split(" ").length >= 3) {
				rotateImages = Boolean.valueOf(this.data.split(" ")[2]);
				resp = ("RotateImages set to " + this.data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("maxressize")) {
			if (this.data.split(" ").length >= 3) {
				maxResSize = Integer.valueOf(this.data.split(" ")[2]);
				resp = ("MaxResSize set to " + this.data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("blockedips")) {
			blockedIPs = new ArrayList<String>();
			if (this.data.split(" ").length >= 3) {
				blockedIPs.addAll(configuration.getBlockedIPs());
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
		} else if (property.toLowerCase().equals("blockedurls")) {
			blockedURLs = new ArrayList<String>();
			if (this.data.split(" ").length >= 3) {
				blockedURLs.addAll(configuration.getBlockedURLs());
				for (final String str : this.data.split(" ")[2].split(",")) {
					blockedURLs.add(str);
				}
			}
			resp = ("BlockedURLs set to " + blockedURLs + "\n").getBytes();
		} else if (property.toLowerCase().equals("blockedmediatypes")) {
			blockedMediaTypes = new ArrayList<String>();
			if (this.data.split(" ").length >= 3) {
				blockedMediaTypes.addAll(configuration.getBlockedMediaTypes());
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
				rotateImages, chainProxy, maxResEnabled, chainProxyPort,
				chainProxyHost));
		return resp;
	}

}