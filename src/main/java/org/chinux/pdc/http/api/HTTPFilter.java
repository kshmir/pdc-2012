package org.chinux.pdc.http.api;

import java.nio.ByteBuffer;

import org.chinux.pdc.workers.impl.HTTPProxyEvent;

/**
 * Interface for filtering different httpevents
 */
public interface HTTPFilter {

	/**
	 * Checks the validity of the http event given
	 */
	public boolean isValid(HTTPProxyEvent event);

	/**
	 * @param event
	 *            Event to get response from
	 * @return A response with the valid status code of accoding to the request
	 */
	public ByteBuffer getErrorResponse(HTTPProxyEvent event);
}
