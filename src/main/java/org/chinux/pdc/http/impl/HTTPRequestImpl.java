package org.chinux.pdc.http.impl;

import org.chinux.pdc.http.api.HTTPRequest;
import org.chinux.pdc.http.api.HTTPRequestHeader;

public class HTTPRequestImpl implements HTTPRequest {
	private HTTPBaseRequestReader bodyReader;
	private HTTPRequestHeader header;

	public HTTPRequestImpl(final HTTPRequestHeader header,
			final HTTPBaseRequestReader bodyReader) {
		this.header = header;
		this.bodyReader = bodyReader;
	}

	@Override
	public HTTPRequestHeader getHeader() {
		return this.header;
	}

	@Override
	public HTTPBaseRequestReader getBodyReader() {
		return this.bodyReader;
	}
}
