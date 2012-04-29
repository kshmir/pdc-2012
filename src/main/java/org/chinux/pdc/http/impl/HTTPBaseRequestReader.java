package org.chinux.pdc.http.impl;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPRequest;

public class HTTPBaseRequestReader implements HTTPReader {

	private HTTPRequest requestheader;
	private boolean finished;

	public HTTPBaseRequestReader(final HTTPRequest requestheader) {
		this.requestheader = requestheader;
		this.finished = false;
	}

	@Override
	public byte[] processData(final byte[] data) {
		final String method = this.requestheader.getHeader("METHOD");

		if (method.equals("HEAD") || method.equals("GET")) {
			this.finished = true;
			return data;
		} else if (method.equals("POST")) {
			final HTTPPostRequestReader postereader = new HTTPPostRequestReader(
					this.requestheader);
			return postereader.processData(data);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
