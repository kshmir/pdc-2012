package org.chinux.pdc.http.impl.filters;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.http.util.ErrorPageProvider;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPUriFilter implements HTTPFilter {

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		if (event.getRequest() == null) {
			return true;
		}
		final List<String> uris = event.getEventConfiguration()
				.getBlockedURLs();
		final String req = event.getRequest().getHeaders().getHeader("host");
		for (final String uri : uris) {
			final Pattern pattern = Pattern.compile(uri);
			final Matcher match = pattern.matcher(req);
			if (match.find()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ByteBuffer getErrorResponse(final HTTPProxyEvent event) {
		return ByteBuffer.wrap(ErrorPageProvider.get403());
	}

}
