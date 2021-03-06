package org.chinux.pdc.http.api;

import org.chinux.pdc.http.impl.HTTPBaseReader;

public interface HTTPResponse {
	/**
	 * @return HttpResponseHeader of the http resonse
	 */
	public HTTPResponseHeader getHeaders();

	/**
	 * @return HttpReader of the http response
	 */
	public HTTPBaseReader getBodyReader();
}
