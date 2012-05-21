package org.chinux.pdc.http.impl;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;
import org.chinux.pdc.http.util.ChunkedInputStream;

public class HTTPChunkedResponseReader implements HTTPReader {

	private HTTPResponseHeader responseHeader;
	private boolean isFinished = false;

	private StringBuilder builder = new StringBuilder();
	private ChunkedInputStream inputStream = new ChunkedInputStream();
	private Logger log = Logger.getLogger(this.getClass());

	public HTTPChunkedResponseReader(final HTTPResponseHeader responseHeader) {
		this.responseHeader = responseHeader;
	}

	@Override
	public byte[] processData(final byte[] data) {
		this.inputStream.write(data);

		byte[] answer;

		while ((answer = this.inputStream.read()) != null) {
			if (answer.length == 0) {
				return null;
			}
			this.builder.append(new String(answer));
		}

		this.isFinished = true;
		return this.builder.toString().getBytes();
	}

	@Override
	public boolean isFinished() {
		return this.isFinished;
	}

}
