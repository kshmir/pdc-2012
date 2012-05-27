package org.chinux.pdc.http.impl;

import org.chinux.pdc.http.api.HTTPRequest;
import org.chinux.pdc.http.api.HTTPRequestHeader;

public class HTTPRequestImpl implements HTTPRequest {
	private HTTPBaseReader bodyReader;
	private HTTPRequestHeader header;

	public HTTPRequestImpl(final HTTPRequestHeader header,
			final HTTPBaseReader bodyReader) {
		this.header = header;
		this.bodyReader = bodyReader;
	}

	@Override
	public HTTPRequestHeader getHeaders() {
		return this.header;
	}

	@Override
	public HTTPBaseReader getBodyReader() {
		return this.bodyReader;
	}

}
