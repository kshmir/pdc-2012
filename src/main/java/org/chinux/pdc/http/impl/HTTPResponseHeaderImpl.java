package org.chinux.pdc.http.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPResponseHeaderImpl implements HTTPResponseHeader {

	private String response;

	private static Pattern headPattern = Pattern
			.compile("([\\w-/\\w]+) ([\\w-/]+) ([\\w-/]+)");

	private static Pattern headerPattern = Pattern.compile("([\\w-]+): (.+)");

	private int statusCode;
	private Map<String, String> headers;

	public HTTPResponseHeaderImpl(final String response) {
		this.response = response;
		this.headers = new HashMap<String, String>();
		final String firstLine = response.split("\n")[0];

		Matcher match = headPattern.matcher(firstLine);
		if (match.find()) {
			this.statusCode = Integer.valueOf(match.group(2));
		}

		headerPattern = Pattern.compile("([\\w-]+): (.+)");
		match = headerPattern.matcher(response);
		while (match.find()) {
			this.headers.put(match.group(1).toLowerCase(), match.group(2));
		}
	}

	@Override
	public void addHeader(final String name, final String value) {
		this.headers.put(name, value);
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
		return this.getResponse() + "\n\n";
	}
}
