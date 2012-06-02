package org.chinux.pdc.http.api;

public interface HTTPRequestHeader extends HTTPMessageHeader {

	/**
	 * Returns all the values of the specified request header as a String
	 */
	public String getHeader(String name);

	/**
	 * Returns the name of the HTTP method with which this request was made, for
	 * example, GET, POST, or PUT.
	 */
	public String getMethod();

	/**
	 * Returns the part of this request's URL from the protocol name up to the
	 * query string in the first line of the HTTP request.
	 */
	public String getRequestURI();

	public String getHTTPVersion();

	public void removeHeader(String name);

	public void addHeader(String name, String value);

}
