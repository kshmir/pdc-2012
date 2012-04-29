package org.chinux.pdc.http.impl;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPRequest;

public class HTTPBaseRequestReader implements HTTPReader {

	private HTTPRequest requestheader;
	private boolean finished;
	private HTTPPostRequestReader postereader;

	private HTTPBaseRequestReader(final HTTPRequest requestheader) {
		this.requestheader = requestheader;
		this.finished = false;
		this.postereader = null;
	}

	@Override
	public byte[] processData(final byte[] data) {
		final String method = this.requestheader.getHeader("METHOD");

		if (method.equals("HEAD") || method.equals("GET")) {
			this.finished = true;
			return data;
		} else if (method.equals("POST")) {
			this.postereader = this.getPostReader();
			final byte[] aux = this.postereader.processData(data);
			if (this.postereader.isFinished()) {
				this.finished = true;
			}
			return aux;
		} else {
			throw new RuntimeException();
		}
	}

	private HTTPPostRequestReader getPostReader() {
		if (this.postereader == null) {
			this.postereader = new HTTPPostRequestReader(this.requestheader);
		}
		return this.postereader;
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
