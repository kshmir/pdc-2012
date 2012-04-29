package org.chinux.pdc.http.impl;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponse;

public class HTTPImageResponseReader implements HTTPReader {

	private HTTPResponse responseheader;
	private boolean finished;
	private Integer currlenght;

	public HTTPImageResponseReader(final HTTPResponse responseheader) {
		this.responseheader = responseheader;
		this.finished = false;
		this.currlenght = 0;
	}

	@Override
	public byte[] processData(final byte[] data) {
		final Integer contentlenght = Integer.valueOf(this.responseheader
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
