package org.chinux.pdc.http.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPBaseResponseReader implements HTTPReader {

	private HTTPResponseHeader responseheader;
	private boolean finished;
	private HTTPImageResponseReader imagereader;
	private HTTPChunkedResponseReader chunkReader;

	public HTTPBaseResponseReader(final HTTPResponseHeader responseheader) {
		this.responseheader = responseheader;
		this.finished = false;
		this.imagereader = null;
	}

	@Override
	public byte[] processData(final byte[] data) {
		final String contenttype = this.responseheader
				.getHeader("Content-Type");
		// final boolean contentChunked = this.responseheader
		// .getHeader("Transfer-Encoding") != null
		// && this.responseheader.getHeader("Transfer-Encoding").equals(
		// "chunked");

		final Pattern pat = Pattern.compile("image/(.*)");
		final Matcher match = pat.matcher(contenttype);
		// if (contentChunked) {
		// final byte[] answer = this.getChunkedReader().processData(data);
		//
		// if (this.getChunkedReader().isFinished()) {
		// this.finished = true;
		// }
		//
		// return answer;
		// }
		if (match.find()) {
			final byte[] aux = this.getImageReader().processData(data);
			if (this.getImageReader().isFinished()) {
				this.finished = true;
			}
			return aux;
		} else {
			this.finished = true;
			return data;
		}

	}

	private HTTPChunkedResponseReader getChunkedReader() {
		if (this.chunkReader == null) {
			this.chunkReader = new HTTPChunkedResponseReader(
					this.responseheader);
		}
		return this.chunkReader;
	}

	private HTTPImageResponseReader getImageReader() {
		if (this.imagereader == null) {
			this.imagereader = new HTTPImageResponseReader(this.responseheader);
		}

		return this.imagereader;
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
