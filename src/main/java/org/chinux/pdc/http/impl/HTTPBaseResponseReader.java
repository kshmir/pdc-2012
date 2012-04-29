package org.chinux.pdc.http.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPBaseResponseReader implements HTTPReader {

	private HTTPResponseHeader responseheader;
	private boolean finished;
	private HTTPImageResponseReader imagereader;

	public HTTPBaseResponseReader(final HTTPResponseHeader responseheader) {
		this.responseheader = responseheader;
		this.finished = false;
		this.imagereader = null;
	}

	@Override
	public byte[] processData(final byte[] data) {
		final String contenttype = this.responseheader
				.getHeader("Content-Type");

		final Pattern pat = Pattern.compile("image/(.*)");
		final Matcher match = pat.matcher(contenttype);
		if (match.find()) {
			this.getImageReader();
			final byte[] aux = this.imagereader.processData(data);
			if (this.imagereader.isFinished()) {
				this.finished = true;
			}
			return aux;
		} else {
			this.finished = true;
			return data;
		}
	}

	private void getImageReader() {
		if (this.imagereader == null) {
			this.imagereader = new HTTPImageResponseReader(this.responseheader);
		}
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
