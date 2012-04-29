package org.chinux.pdc.http.impl;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPRequest;

public class HTTPPostRequestReader implements HTTPReader {

	private HTTPRequest requestheader;
	private boolean finished;
	private Integer currlenght;

	public HTTPPostRequestReader(final HTTPRequest requestheader) {
		this.requestheader = requestheader;
		this.finished = false;
		this.currlenght = 0;
	}

	@Override
	public byte[] processData(final byte[] data) {
		final Integer contentlenght = Integer.valueOf(this.requestheader
				.getHeader("Content-Length"));
		this.currlenght += data.length;
		if (this.currlenght >= contentlenght) {
			this.finished = true;
		}
		return data;
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
