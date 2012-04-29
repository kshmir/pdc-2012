package org.chinux.pdc.http.impl;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponse;

public class HTTPBaseResponseReader implements HTTPReader {

	private HTTPResponse responseheader;

	public HTTPBaseResponseReader(final HTTPResponse responseheader) {
		this.responseheader = responseheader;
	}

	@Override
	public byte[] processData(final byte[] data) {
		final String method = this.responseheader.getHeader("METHOD");

		if (method.equals("HEAD")) {
			/*
			 * TODO if the method is head the data must be returned
			 */
		} else if (method.equals("GET")) {
			return data;
		} else if (method.equals("POST")) {
			return data;
		} else {
			throw new RuntimeException();
		}
		return null;

	}

	@Override
	public boolean isFinished() {
		return false;
	}

}
