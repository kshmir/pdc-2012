package org.chinux.pdc.http.api;

import java.nio.charset.Charset;

public interface HTTPResponseHeader extends HTTPMessageHeader {

	public Charset getCharset();

	public String getHTTPVersion();

	public void addHeader(String name, String value);

	public boolean containsHeader(String name);

	public int returnStatusCode();

	public String getHeader(String name);

	public String getResponse();

	public void removeHeader(String name);

}
