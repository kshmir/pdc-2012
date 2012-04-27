package org.chinux.pdc;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPResponseImpl implements HTTPResponse {

	private int statusCode;
	private Map<String, String> headers;

	public HTTPResponseImpl(final String response) {
		this.headers = new HashMap<String, String>();
		final String firstLine = response.substring(0, response.indexOf('\n'));

		Pattern pattern = Pattern
				.compile("([\\w-/\\w]+) ([\\w-/]+) ([\\w-/]+)");
		Matcher match = pattern.matcher(firstLine);
		if (match.find()) {
			this.statusCode = Integer.valueOf(match.group(2));
		}

		pattern = Pattern.compile("([\\w-]+): (.+)");
		match = pattern.matcher(response);
		while (match.find()) {
			this.headers.put(match.group(1).toLowerCase(), match.group(2));
		}
	}

	public void addHeader(final String name, final String value) {
		this.headers.put(name, value);
	}

	public boolean containsHeader(final String name) {
		return this.headers.containsKey(name);
	}

	public int returnStatusCode() {
		return this.statusCode;
	}

	public String getHeader(final String name) {
		return this.headers.containsKey(name.toLowerCase()) ? this.headers
				.get(name.toLowerCase()) : null;
	}

}
