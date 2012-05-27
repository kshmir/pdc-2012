package org.chinux.pdc.http.impl;

import java.nio.ByteBuffer;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPRequestHeader;

public class HTTPPostRequestReader implements HTTPReader {

	private HTTPRequestHeader requestheader;
	private boolean finished;
	private Integer currlenght;

	public HTTPPostRequestReader(final HTTPRequestHeader requestheader) {
		this.requestheader = requestheader;
		this.finished = false;
		this.currlenght = 0;
	}

	@Override
	public ByteBuffer processData(final ByteBuffer data) {
		return data;
		// TODO: Fix this
		// final Integer contentlenght = Integer.valueOf(this.requestheader
		// .getHeader("Content-Length"));
		// this.currlenght += data.length;
		// if (this.currlenght >= contentlenght) {
		// this.finished = true;
		// }
		// return data;
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
