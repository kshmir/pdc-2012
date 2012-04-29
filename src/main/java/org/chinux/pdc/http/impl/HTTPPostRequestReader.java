package org.chinux.pdc.http.impl;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPRequest;

public class HTTPPostRequestReader implements HTTPReader {

	private HTTPRequest requestheader;
	private boolean finished;

	public HTTPPostRequestReader(final HTTPRequest requestheader) {
		this.requestheader = requestheader;
		this.finished = false;
	}

	@Override
	public byte[] processData(final byte[] data) {
		final Integer contentlenght = Integer.valueOf(this.requestheader
				.getHeader("Content-Length"));
		final int quant = 0;
		return data;
	}

	@Override
	public boolean isFinished() {
		return false;
	}

}
