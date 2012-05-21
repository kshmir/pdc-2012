package org.chinux.pdc.http.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPBaseResponseReader implements HTTPReader {

	private HTTPResponseHeader responseheader;
	private boolean finished;
	private HTTPImageResponseReader imagereader;
	private HTTPChunkedResponseReader chunkReader;
	private boolean chunkedResponse;
	private boolean bufferData;
	private Logger log = Logger.getLogger(this.getClass());

	public HTTPBaseResponseReader(final HTTPResponseHeader responseheader,
			final boolean chunkedResponse) {
		this.responseheader = responseheader;
		this.finished = false;
		this.imagereader = null;
		this.chunkedResponse = chunkedResponse;
		this.bufferData = this.chunkedResponse;
	}

	@Override
	public byte[] processData(byte[] data) {

		final String contenttype = this.responseheader
				.getHeader("Content-Type");

		if (contenttype == null) {
			return data;
		}

		// Acumulate all the request chunked and transform it to a normal one
		// if and only if we have text/plain or image/* as a request type
		if (this.chunkedResponse) {
			final byte[] chunkedData = this.getChunkedReader()
					.processData(data);

			if (chunkedData == null) {
				this.log.debug("Buffering chunk...");
				return null;
			} else {
				this.log.debug("Sending all chunks!");
				data = chunkedData;
			}
		}

		final Pattern pat = Pattern.compile("image/(.*)");
		final Matcher match = pat.matcher(contenttype);

		if (match.find()) {
			final byte[] aux = this.getImageReader().processData(data);
			if (this.getImageReader().isFinished()) {
				this.finished = true;
			}
			return aux;
		} else {
			this.finished = true;
			return this.buildAnswer(data);
		}
	}

	private byte[] buildAnswer(final byte[] data) {
		if (this.bufferData) {
			this.log.debug("Modifying content-length");
			this.responseheader.addHeader("content-length",
					String.valueOf(data.length));
			final StringBuilder builder = new StringBuilder();
			builder.append(this.responseheader.toString());
			builder.append(new String(data));
			return builder.toString().getBytes();
		} else {
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
