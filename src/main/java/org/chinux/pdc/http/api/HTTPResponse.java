package org.chinux.pdc.http.api;

import org.chinux.pdc.http.impl.HTTPBaseResponseReader;

public interface HTTPResponse {
	/**
	 * @return HttpResponseHeader of the http resonse
	 */
	public HTTPResponseHeader getHeader();

	/**
	 * @return HttpReader of the http response
	 */
	public HTTPBaseResponseReader getBodyReader();

}
