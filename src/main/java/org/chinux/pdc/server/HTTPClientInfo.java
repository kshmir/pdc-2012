package org.chinux.pdc.server;

import java.net.InetAddress;

import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;

import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPClientInfo {

	private InetAddress address;
	private OperatingSystem operatingSystem;
	private Browser browser;

	public HTTPClientInfo(final HTTPProxyEvent event) {
		final String UA = event.getRequest().getHeaders()
				.getHeader("user-agent");

		final InetAddress address = event.getSocketChannel().socket()
				.getInetAddress();
		UserAgent agent = null;
		if (UA != null) {
			agent = UserAgent.parseUserAgentString(UA);
		} else {
			agent = new UserAgent(OperatingSystem.WINDOWS, Browser.CHROME);
		}

		this.address = address;
		this.operatingSystem = agent.getOperatingSystem().getGroup();
		this.browser = agent.getBrowser().getGroup();
	}

	public InetAddress getAddress() {
		return this.address;
	}

	public Browser getBrowser() {
		return this.browser;
	}

	public OperatingSystem getOperatingSystem() {
		return this.operatingSystem;
	}
}
