package org.chinux.pdc.http.impl.filters;

import java.nio.ByteBuffer;

import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.http.util.ErrorPageProvider;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPSizeFilter implements HTTPFilter {

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		final int maxSize = event.getEventConfiguration().getMaxResSize();
		if (event.getResponse().getHeaders().getHeader("Content-Length") == null) {
			return true;
		}
		final int reqSize = Integer.valueOf(event.getResponse().getHeaders()
				.getHeader("Content-Length"));
		return reqSize < maxSize;
	}

	@Override
	public ByteBuffer getErrorResponse(final HTTPProxyEvent event) {
		return ByteBuffer.wrap(ErrorPageProvider.get403());
	}

}
