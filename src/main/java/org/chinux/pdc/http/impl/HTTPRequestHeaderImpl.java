package org.chinux.pdc.http.impl;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.chinux.pdc.FilterException;
import org.chinux.pdc.http.api.HTTPRequestHeader;
import org.chinux.pdc.http.util.ErrorPageProvider;

public class HTTPRequestHeaderImpl implements HTTPRequestHeader {

	private static Pattern headPattern = Pattern
			.compile("([\\w-/]+) (.+) ([\\w-/\\.]+)");

	private static Pattern headerPattern = Pattern.compile("([\\w-]+): (.+)");

	private String method;
	private Map<String, String> headers;
	private String URI;
	private String version;

	public HTTPRequestHeaderImpl(final String request) throws FilterException {
		this.headers = new HashMap<String, String>();

		final String firstLine = request.split("\n")[0];

		if (firstLine.startsWith("CONNECT") || firstLine.startsWith("TRACE")
				|| firstLine.startsWith("OPTIONS")
				|| firstLine.startsWith("PUT")
				|| firstLine.startsWith("DELETE")) {
			throw new FilterException(ByteBuffer.wrap(ErrorPageProvider
					.get501()));
		}

		Matcher match = headPattern.matcher(firstLine);
		if (match.find()) {
			this.method = match.group(1);
			this.URI = match.group(2);
			if (this.URI.startsWith("http")) {
				try {
					String params = "";
					if (this.URI.indexOf("?") != -1) {
						params = this.URI.substring(this.URI.indexOf("?"));
					}
					this.URI = new java.net.URI(this.URI).getPath() + params;
				} catch (final URISyntaxException e) {

				}
			}

			this.version = match.group(3);
		}

		match = headerPattern.matcher(request);
		while (match.find()) {
			this.headers.put(match.group(1).toLowerCase(), match.group(2));
		}

		if (this.method.equals("GET")) {
			if (!this.URI.contains("?")) {
				return;
			}
		}

	}

	@Override
	public void addHeader(final String name, final String value) {
		this.headers.put(name.toLowerCase(), value);
	}

	@Override
	public String getHeader(final String name) {
		return this.headers.containsKey(name.toLowerCase()) ? this.headers.get(
				name.toLowerCase()).trim() : null;
	}

	@Override
	public void removeHeader(final String name) {
		this.headers.remove(name.toLowerCase());
	}

	@Override
	public String getMethod() {
		return this.method;
	}

	@Override
	public String getRequestURI() {
		return this.URI;
	}

	@Override
	public String getHTTPVersion() {
		return this.version.split("/")[1];
	}

	@Override
	public String toString() {

		final StringBuilder builder = new StringBuilder();

		builder.append(this.method).append(" ");
		builder.append(this.URI).append(" ");
		builder.append(this.version).append("\r\n");

		for (final String string : this.headers.keySet()) {
			builder.append(StringUtils.capitalize(string)).append(": ")
					.append(this.headers.get(string)).append("\r\n");
		}

		return builder.toString() + "\r\n";
	}
}
