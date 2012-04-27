package org.chinux.pdc;

public interface HTTPResponse {

	public void addHeader(String name, String value);

	public boolean containsHeader(String name);

	public int returnStatusCode();

	public String getHeader(String name);

}
