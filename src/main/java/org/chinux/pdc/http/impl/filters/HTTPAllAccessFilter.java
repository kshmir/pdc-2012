package org.chinux.pdc.http.impl.filters;

import java.nio.ByteBuffer;

import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.http.util.ErrorPageProvider;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPAllAccessFilter implements HTTPFilter {

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		return !event.getEventConfiguration().isBlockAll();
	}

	@Override
	public ByteBuffer getErrorResponse(final HTTPProxyEvent event) {
		return ByteBuffer.wrap(ErrorPageProvider.get403());
	}
}
