package org.chinux.pdc.workers.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Since DNS cache can vary heavily, and it's hard to configure, it simplier to
 * cache it
 */
public class IPAddressResolver {

	private static IPAddressResolver instance;

	private IPAddressResolver(final int seconds) {
		final Timer t = new Timer();

		t.scheduleAtFixedRate(new TimerTask() {
			private Set<Object> toRemove = new HashSet<Object>();

			@Override
			public synchronized void run() {
				for (final String channel : IPAddressResolver.this.addressesDate
						.keySet()) {
					if (seconds * 1000 < System.currentTimeMillis()
							- IPAddressResolver.this.addressesDate.get(channel)) {

						this.toRemove.add(channel);
					}
				}

				for (final Object c : this.toRemove) {
					IPAddressResolver.this.addresses.remove(c);
					IPAddressResolver.this.addressesDate.remove(c);
				}

				this.toRemove.clear();
			}
		}, 0, 1000 * 5);
	}

	public static synchronized IPAddressResolver getInstance() {
		if (instance == null) {
			instance = new IPAddressResolver(5);
		}
		return instance;
	}

	private Map<String, InetAddress> addresses = new HashMap<String, InetAddress>();
	private Map<String, Long> addressesDate = new HashMap<String, Long>();

	public synchronized InetAddress getAddressForHost(final String host)
			throws UnknownHostException {
		if (this.addresses.containsKey(host)) {
			return this.addresses.get(host);
		}
		final InetAddress fromResolve = InetAddress.getByName(host);
		this.addressesDate.put(host, System.currentTimeMillis());
		this.addresses.put(host, fromResolve);
		return fromResolve;
	}
}
