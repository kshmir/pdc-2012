package org.chinux.pdc.http.api;

import org.chinux.pdc.http.impl.HTTPBaseReader;

public interface HTTPRequest {
	/**
	 * @return Header object of the httprequest
	 */
	public HTTPRequestHeader getHeaders();

	/**
	 * @return HttpReader of the httprequest
	 */
	public HTTPBaseReader getBodyReader();
}
