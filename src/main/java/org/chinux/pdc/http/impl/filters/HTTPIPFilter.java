package org.chinux.pdc.http.impl.filters;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.http.util.ErrorPageProvider;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPIPFilter implements HTTPFilter {

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		final List<String> ips = event.getEventConfiguration().getBlockedIPs();
		String dest = null;
		try {
			dest = InetAddress.getByName(
					event.getRequest().getHeaders().getHeader("host"))
					.toString();
		} catch (final UnknownHostException e) {
			e.printStackTrace();
		}
		dest = dest.split("/")[1];
		if (dest != null) {
			return !this.matches(ips, dest);
		}
		return true;
	}

	@Override
	public ByteBuffer getErrorResponse(final HTTPProxyEvent event) {
		return ByteBuffer.wrap(ErrorPageProvider.get403());
	}

	private boolean matches(final List<String> list, final String ip) {
		for (final String str : list) {
			if (str.split("/").length != 2) {
				return false;
			}
			final Integer mask = Integer.valueOf(str.split("/")[1]);
			final String[] req = ip.split("\\.");
			final String[] value = str.split("/")[0].split("\\.");
			switch (mask) {
			case 8:
				return req[0].compareTo(value[0]) == 0;
			case 16:
				return req[0].compareTo(value[0]) == 0
						&& req[1].compareTo(value[1]) == 0;
			case 24:
				return req[0].compareTo(value[0]) == 0
						&& req[1].compareTo(value[1]) == 0
						&& req[2].compareTo(value[2]) == 0;
			case 32:
				return ip.compareTo(str) == 0;
			default:
				return false;
			}

		}
		return false;
	}
}
