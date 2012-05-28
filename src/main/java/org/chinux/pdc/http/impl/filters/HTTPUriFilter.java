package org.chinux.pdc.http.impl.filters;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPUriFilter implements HTTPFilter {

	private List<String> uris = new ArrayList<String>();

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		this.uris = event.getEventConfiguration().getBlockedURLs();
		System.out.println(event.getRequest().getHeaders().getHeader("host"));
		return !this.uris.contains(event.getRequest().getHeaders()
				.getHeader("host"));
	}

	@Override
	public ByteBuffer getErrorResponse(final HTTPProxyEvent event) {
		return ByteBuffer.wrap("Invalid URL\n".getBytes());
	}

}
