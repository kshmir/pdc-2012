package org.chinux.pdc.http.impl.filters;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.http.util.ErrorPageProvider;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;
import org.chinux.pdc.workers.impl.HTTPResponseEventHandler;

public class HTTPUriFilter implements HTTPFilter {

	static Logger log = Logger.getLogger(HTTPResponseEventHandler.class);

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		final List<String> uris = event.getEventConfiguration()
				.getBlockedURLs();
		final String req = event.getRequest().getHeaders().getHeader("Host");
		for (String uri : uris) {
			Pattern pattern;
			uri += ")";
			uri = "(" + uri;
			try {
				pattern = Pattern.compile(uri);
			} catch (final PatternSyntaxException e) {
				log.error("Invalid regular expresion");
				return true;
			}
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
