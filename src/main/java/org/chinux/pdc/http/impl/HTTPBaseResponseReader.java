package org.chinux.pdc.http.impl;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.workers.impl.HttpProxyWorker.HTTPEvent;

public class HTTPBaseResponseReader implements HTTPReader {

	private boolean finished;
	private Logger log = Logger.getLogger(this.getClass());
	private HTTPEvent event;

	public HTTPBaseResponseReader(final HTTPEvent event) {
		this.event = event;
		this.finished = false;
	}

	@Override
	public ByteBuffer processData(final ByteBuffer data) {
		this.finished = true;
		return data;
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
