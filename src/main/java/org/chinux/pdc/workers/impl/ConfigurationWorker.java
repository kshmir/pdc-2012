package org.chinux.pdc.workers.impl;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.OperatingSystem;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ErrorDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.server.Configuration;
import org.chinux.pdc.server.ConfigurationProvider;
import org.chinux.pdc.server.LoginService;
import org.chinux.pdc.server.LoginService.Code;
import org.chinux.pdc.server.User;

public class ConfigurationWorker extends LogueableWorker {

	private static final int BROWSER = 0;

	private static final int OPERATING_SYSTEM = 1;

	private static final int IP_ADDRESS = 2;

	private static final int SUBNET = 3;

	private Pattern ipPattern = Pattern
			.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(/(8|16|24|32))?");

	private String[] subcommands = new String[] { "BROWSER", "OS", "IP",
			"SUBNET" };

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
		final ServerDataEvent event;
		byte[] resp;
		Configuration configuration = null;
		Object owner = null;

		try {

			String subCommand = command.split(" ")[0];

			int i = -1;
			for (int j = 0; j < this.subcommands.length; j++) {
				if (subCommand.equals(this.subcommands[j])) {
					i = j;
					break;
				}
			}

			if (command.split(" ").length < 2) {
				i = -1;
			} else {
				subCommand = command.split(" ")[1];
			}

			switch (i) {
			case BROWSER:
				try {
					final Browser b = Browser.valueOf(subCommand.toUpperCase())
							.getGroup();
					configuration = ConfigurationProvider
							.getConfigurationFromBrowser(b);
					owner = b;
				} catch (final Exception e) {
					throw new IllegalArgumentException();
				}
				break;
			case OPERATING_SYSTEM:
				try {
					final OperatingSystem os = OperatingSystem.valueOf(
							subCommand.toUpperCase()).getGroup();
					configuration = ConfigurationProvider
							.getConfigurationFromOperatingSystem(os);
					owner = os;
				} catch (final Exception e) {
					throw new IllegalArgumentException();
				}
				break;
			case IP_ADDRESS:
				try {
					final InetAddress address = InetAddress
							.getByName(subCommand);

					configuration = ConfigurationProvider
							.getConfigurationFromIP(address);
					owner = address;
				} catch (final Exception e) {
					throw new IllegalArgumentException();
				}
				break;
			case SUBNET:
				try {
					final SubnetInfo subnet = new SubnetUtils(subCommand)
							.getInfo();

					configuration = ConfigurationProvider
							.getConfigurationFromSubnet(subnet);

					owner = subnet;
				} catch (final Exception e) {
					throw new IllegalArgumentException();
				}
				break;
			default:
				configuration = ConfigurationProvider.getDefaultConfiguration();
				break;
			}

			if (configuration == null) {
				configuration = ConfigurationProvider.getDefaultConfiguration();
			}

			final String endCommand = (i != -1) ? command.split(" ", 3)[2]
					: command;

			resp = this.processCommand(endCommand, configuration, owner);
		} catch (final IllegalArgumentException e) {
			resp = "400; Invalid parameters\n".getBytes();
		}

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

	private byte[] processCommand(final String rawCommand,
			final Configuration configuration, final Object owner) {
		final String command = rawCommand.split(" ")[0];

		byte[] resp;
		if (command.equals("GET")) {
			if (rawCommand.split(" ").length <= 1) {
				resp = "401; Invalid Parameter\n".getBytes();
			} else {
				resp = this.get(rawCommand.split(" ")[1], configuration);
			}
		} else if (command.equals("GETALL")) {
			resp = configuration.toString().getBytes();
		} else if (command.equals("SET")) {
			if (rawCommand.split(" ").length <= 1) {
				resp = "401; Invalid Parameter\n".getBytes();
			} else {
				resp = this.set(rawCommand.split(" ")[1], configuration, owner,
						rawCommand);
			}
		} else if (command.equals("LOGOUT")) {
			this.quit();
			resp = "201; Logout OK\nEnter user name: ".getBytes();
		} else {
			resp = "400; Invalid Command\n".getBytes();
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
			return "402; Invalid Configuration Parameter\n".getBytes();
		}
	}

	private byte[] set(final String property,
			final Configuration configuration, final Object owner,
			final String data) {
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
			if (data.split(" ").length >= 3) {
				maxResEnabled = Boolean.valueOf(data.split(" ")[2]);
				resp = ("200; maxResEnabled set to " + data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "402; Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("chainproxyport")) {
			if (data.split(" ").length >= 3) {
				chainProxyPort = Integer.valueOf(data.split(" ")[2]);
				resp = ("200; chainProxyPort set to " + data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "402; Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("chainproxyhost")) {
			if (data.split(" ").length >= 3) {
				chainProxyHost = data.split(" ")[2];
				resp = ("200; chainProxyHost set to " + data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "402; Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("blockall")) {
			if (data.split(" ").length >= 3) {
				blockAll = Boolean.valueOf(data.split(" ")[2]);
				resp = ("200; BlockAll set to " + data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "402; Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("chainproxy")) {
			if (data.split(" ").length >= 3) {
				chainProxy = Boolean.valueOf(data.split(" ")[2]);
				resp = ("200; ChainProxy set to " + data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "402; Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("l33t")) {
			if (data.split(" ").length >= 3) {
				l33t = Boolean.valueOf(data.split(" ")[2]);
				resp = ("200; l33t set to " + data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "402; Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("rotateimages")) {
			if (data.split(" ").length >= 3) {
				rotateImages = Boolean.valueOf(data.split(" ")[2]);
				resp = ("200; RotateImages set to " + data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "402; Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("maxressize")) {
			if (data.split(" ").length >= 3) {
				maxResSize = Integer.valueOf(data.split(" ")[2]);
				resp = ("200; MaxResSize set to " + data.split(" ")[2] + "\n")
						.getBytes();
			} else {
				resp = "402; Invalid Parameter\n".getBytes();
			}
		} else if (property.toLowerCase().equals("blockedips")) {
			blockedIPs = new ArrayList<String>();
			if (data.split(" ").length >= 3) {
				blockedIPs.addAll(configuration.getBlockedIPs());
				for (final String str : data.split(" ")[2].split(",")) {
					final Matcher match = this.ipPattern.matcher(str);
					if (match.find()) {
						blockedIPs.add(str);
					} else {
						resp = "402; Invalid Parameter\n".getBytes();
						break;
					}
				}
			}
			resp = ("200; BlockedIPs set to " + blockedIPs + "\n").getBytes();
		} else if (property.toLowerCase().equals("blockedurls")) {
			blockedURLs = new ArrayList<String>();
			if (data.split(" ").length >= 3) {
				blockedURLs.addAll(configuration.getBlockedURLs());
				for (final String str : data.split(" ")[2].split(",")) {
					blockedURLs.add(str);
				}
			}
			resp = ("200; BlockedURLs set to " + blockedURLs + "\n").getBytes();
		} else if (property.toLowerCase().equals("blockedmediatypes")) {
			blockedMediaTypes = new ArrayList<String>();
			if (data.split(" ").length >= 3) {
				blockedMediaTypes.addAll(configuration.getBlockedMediaTypes());
				for (final String str : data.split(" ")[2].split(",")) {
					blockedMediaTypes.add(str);
				}
			}
			resp = ("200; BlockedMediaTypes set to " + blockedMediaTypes + "\n")
					.getBytes();
		} else {
			return "403; Invalid Configuration Parameter\n".getBytes();
		}
		ConfigurationProvider.setConfiguration(new Configuration(blockAll,
				blockedIPs, blockedURLs, blockedMediaTypes, maxResSize, l33t,
				rotateImages, chainProxy, maxResEnabled, chainProxyPort,
				chainProxyHost), owner);
		return resp;
	}

}