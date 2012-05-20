package org.chinux.pdc.http.impl;

import org.chinux.pdc.http.api.HTTPResponse;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPResponseImpl implements HTTPResponse {
	private HTTPBaseResponseReader bodyReader;
	private HTTPResponseHeader header;

	public HTTPResponseImpl(final HTTPResponseHeader header,
			final HTTPBaseResponseReader bodyReader) {
		this.header = header;
		this.bodyReader = bodyReader;
	}

	@Override
	public HTTPBaseResponseReader getBodyReader() {
		return this.bodyReader;
	}

	@Override
	public HTTPResponseHeader getHeaders() {
		return this.header;
	}
}
