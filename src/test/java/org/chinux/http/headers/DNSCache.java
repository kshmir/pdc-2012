package org.chinux.http.headers;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DNSCache {
	public static void main(final String[] args) throws Exception {

		InetAddress.getByName("stackoverflow.com");
		InetAddress.getByName("www.google.com");
		InetAddress.getByName("www.yahoo.com");
		InetAddress.getByName("www.example.com");
		try {
			InetAddress.getByName("nowhere.example.com");
		} catch (final UnknownHostException e) {

		}

		final String addressCache = "addressCache";
		System.out.println(addressCache);
		printDNSCache(addressCache);
		final String negativeCache = "negativeCache";
		System.out.println(negativeCache);
		printDNSCache(negativeCache);
	}

	private static void printDNSCache(final String cacheName) throws Exception {
		final Class<InetAddress> klass = InetAddress.class;
		final Field acf = klass.getDeclaredField(cacheName);
		acf.setAccessible(true);
		final Object addressCache = acf.get(null);
		final Class cacheKlass = addressCache.getClass();
		final Field cf = cacheKlass.getDeclaredField("cache");
		cf.setAccessible(true);
		final Map<String, Object> cache = (Map<String, Object>) cf
				.get(addressCache);
		for (final Map.Entry<String, Object> hi : cache.entrySet()) {
			final Object cacheEntry = hi.getValue();
			final Class cacheEntryKlass = cacheEntry.getClass();
			final Field expf = cacheEntryKlass.getDeclaredField("expiration");
			expf.setAccessible(true);
			final long expires = (Long) expf.get(cacheEntry);

			final Field af = cacheEntryKlass.getDeclaredField("address");
			af.setAccessible(true);
			final InetAddress[] addresses = (InetAddress[]) af.get(cacheEntry);
			final List<String> ads = new ArrayList<String>(addresses.length);
			for (final InetAddress address : addresses) {
				ads.add(address.getHostAddress());
			}

			System.out.println(hi.getKey() + " " + new Date(expires) + " "
					+ ads);
		}
	}
}
