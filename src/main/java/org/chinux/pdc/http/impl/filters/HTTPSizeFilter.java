package org.chinux.pdc.http.impl.filters;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.http.impl.HTTPBaseFilter;
import org.chinux.pdc.http.util.ErrorPageProvider;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPSizeFilter implements HTTPFilter {

	private static Logger log = Logger.getLogger(HTTPBaseFilter.class);

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		if (event.getResponse() == null) {
			return true;
		}
		final int maxSize = event.getEventConfiguration().getMaxResSize();
		if (event.getResponse().getHeaders().getHeader("Content-Length") == null) {
			return true;
		}
		final int reqSize = Integer.valueOf(event.getResponse().getHeaders()
				.getHeader("Content-Length"));
		if (reqSize > maxSize) {
			log.info("Request size too big. Permission denied");
			return false;
		}
		return true;
	}

	@Override
	public ByteBuffer getErrorResponse(final HTTPProxyEvent event) {
		return ByteBuffer.wrap(ErrorPageProvider.get403());
	}

}
