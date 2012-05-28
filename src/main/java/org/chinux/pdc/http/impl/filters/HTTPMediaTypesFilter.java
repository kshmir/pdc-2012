package org.chinux.pdc.http.impl.filters;

import java.nio.ByteBuffer;
import java.util.List;

import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPMediaTypesFilter implements HTTPFilter {

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		final String reqTypes = event.getResponse().getHeaders()
				.getHeader("Content-Type");
		final String[] reqTypesList = reqTypes.split(";");
		final List<String> bloquedMediaTypes = event.getEventConfiguration()
				.getBlockedMediaTypes();
		for (final String str : reqTypesList) {
			if (bloquedMediaTypes.contains(str)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ByteBuffer getErrorResponse(final HTTPProxyEvent event) {
		return ByteBuffer.wrap("The requested media type has been blocked.\n"
				.getBytes());
	}

}
