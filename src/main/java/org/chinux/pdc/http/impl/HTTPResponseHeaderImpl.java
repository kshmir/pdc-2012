package org.chinux.pdc.http.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPResponseHeaderImpl implements HTTPResponseHeader {

	private String response;

	private static Pattern headPattern = Pattern
			.compile("HTTP/([0-9].[0-9]+) ([\\w-/]+) ([\\w-/]+)");

	private static Pattern headerPattern = Pattern.compile("([\\w-]+): (.+)");

	private int statusCode;
	private Map<String, String> headers;
	private String headerLine;
	private String httpVersion;

	public HTTPResponseHeaderImpl(final String response) {
		this.response = response;
		this.headers = new HashMap<String, String>();
		final String firstLine = this.headerLine = response.split("\r\n")[0];

		Matcher match = headPattern.matcher(firstLine);
		if (match.find()) {
			this.statusCode = Integer.valueOf(match.group(2));
			this.httpVersion = match.group(1);
		}

		headerPattern = Pattern.compile("([\\w-]+): (.+)");
		match = headerPattern.matcher(response);
		while (match.find()) {
			this.headers.put(match.group(1).toLowerCase(), match.group(2));
		}
	}

	@Override
	public String getHTTPVersion() {
		return this.httpVersion;
	}

	@Override
	public void addHeader(final String name, final String value) {
		this.headers.put(name.toLowerCase(), value);
	}

	@Override
	public void removeHeader(final String name) {
		this.headers.remove(name.toLowerCase());
	}

	@Override
	public boolean containsHeader(final String name) {
		return this.headers.containsKey(name);
	}

	@Override
	public int returnStatusCode() {
		return this.statusCode;
	}

	@Override
	public String getHeader(final String name) {
		return this.headers.containsKey(name.toLowerCase()) ? this.headers
				.get(name.toLowerCase()) : null;
	}

	@Override
	public String getResponse() {
		return this.response;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();

		builder.append(this.headerLine).append("\r\n");

		for (final String string : this.headers.keySet()) {
			builder.append(StringUtils.capitalize(string)).append(": ")
					.append(this.headers.get(string)).append("\r\n");
		}

		return builder.toString() + "\r\n";
	}
}
