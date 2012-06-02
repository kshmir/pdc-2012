package org.chinux.pdc.workers.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Since DNS cache can vary heavily, and it's hard to configure, it simplier to
 * cache it
 */
public class IPAddressResolver {

	private Map<String, InetAddress> addresses = new HashMap<String, InetAddress>();

	public InetAddress getAddressForHost(final String host)
			throws UnknownHostException {
		if (this.addresses.containsKey(host)) {
			return this.addresses.get(host);
		}
		final InetAddress fromResolve = InetAddress.getByName(host);
		this.addresses.put(host, fromResolve);
		return fromResolve;
	}
}
