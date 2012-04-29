package org.chinux.pdc.http.impl;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPBaseResponseReader implements HTTPReader {

	private HTTPResponseHeader responseheader;
	private boolean finished;
	private HTTPPostResponseReader postereader;
	private HTTPGetResponseReader getreader;

	public HTTPBaseResponseReader(final HTTPResponseHeader responseheader) {
		this.responseheader = responseheader;
		this.finished = false;
		this.postereader = null;
	}

	@Override
	public byte[] processData(final byte[] data) {
		final String method = this.responseheader.getHeader("Method");

		if (method.equals("HEAD")) {
			return this.responseheader.getResponse().getBytes();
		} else if (method.equals("GET")) {
			this.getreader = this.getGetReader();
			final byte[] aux = this.getreader.processData(data);
			if (this.getreader.isFinished()) {
				this.finished = true;
			}
			return aux;
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

	private HTTPPostResponseReader getPostReader() {
		if (this.postereader == null) {
			this.postereader = new HTTPPostResponseReader(this.responseheader);
		}
		return this.postereader;
	}

	private HTTPGetResponseReader getGetReader() {
		if (this.getreader == null) {
			this.getreader = new HTTPGetResponseReader(this.responseheader);
		}
		return this.getreader;
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
