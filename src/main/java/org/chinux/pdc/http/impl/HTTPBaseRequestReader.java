package org.chinux.pdc.http.impl;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPRequestHeader;

public class HTTPBaseRequestReader implements HTTPReader {

	private HTTPRequestHeader requestheader;
	private boolean finished;
	private HTTPPostRequestReader postereader;
	private Logger log = Logger.getLogger(this.getClass());

	public HTTPBaseRequestReader(final HTTPRequestHeader requestheader) {
		this.requestheader = requestheader;
		this.finished = false;
		this.postereader = null;
	}

	@Override
	public ByteBuffer processData(final ByteBuffer data) {
		return data;
		// final String method = this.requestheader.getMethod();
		//
		// if (method.equals("HEAD") || method.equals("GET")) {
		// this.finished = true;
		// return data;
		// } else if (method.equals("POST")) {
		// this.getPostReader();
		// final byte[] aux = this.postereader.processData(data);
		// if (this.postereader.isFinished()) {
		// this.finished = true;
		// }
		// return aux;
		// } else {
		// this.log.error("Method unsupported: " + method);
		// throw new RuntimeException();
		// }
	}

	private void getPostReader() {
		if (this.postereader == null) {
			this.postereader = new HTTPPostRequestReader(this.requestheader);
		}
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
