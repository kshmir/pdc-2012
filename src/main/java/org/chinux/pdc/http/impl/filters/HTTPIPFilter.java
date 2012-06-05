package org.chinux.pdc.http.impl.filters;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.http.impl.HTTPBaseFilter;
import org.chinux.pdc.http.util.ErrorPageProvider;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPIPFilter implements HTTPFilter {

	private static Logger log = Logger.getLogger(HTTPBaseFilter.class);

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		InetAddress toAddress = null;
		try {
			toAddress = InetAddress.getByName(event.getRequest().getHeaders()
					.getHeader("host"));
		} catch (final Exception e1) {
			return true;
		}
		final List<String> ips = event.getEventConfiguration().getBlockedIPs();
		for (final String ip : ips) {
			if (ip.contains("/")) {
				try {
					if (new SubnetUtils(ip).getInfo().isInRange(
							toAddress.getHostAddress())) {
						return false;
					}
				} catch (final Exception e) {

				}
			} else {
				// TODO: DNS checker
				InetAddress ofIp;
				try {
					ofIp = InetAddress.getByName(ip);
					if (ofIp.equals(toAddress)) {
						return false;
					}
				} catch (final UnknownHostException e) {
				}

			}
		}
		return true;
	}

	@Override
	public ByteBuffer getErrorResponse(final HTTPProxyEvent event) {
		return ByteBuffer.wrap(ErrorPageProvider.get403());
	}
}
