package org.chinux.pdc.http.api;

public interface HTTPResponseHeader {

	public void addHeader(String name, String value);

	public boolean containsHeader(String name);

	public int returnStatusCode();

	public String getHeader(String name);

	public String getResponse();

	public void removeHeader(String name);

}
