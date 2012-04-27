package org.chinux.pdc.http.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chinux.pdc.http.api.HTTPRequest;

public class HTTPRequestImpl implements HTTPRequest {

	private static Pattern headPattern = Pattern
			.compile("([\\w-/]+) (.+) ([\\w-/\\.]+)");

	private static Pattern headerPattern = Pattern.compile("([\\w-]+): (.+)");

	private static Pattern parametersPattern = Pattern.compile("([\\w|=]+)");

	private String method;
	private Map<String, String> headers;
	private Map<String, String> parameters;
	private String URI;

	public HTTPRequestImpl(final String request) {
		this.headers = new HashMap<String, String>();
		this.parameters = new HashMap<String, String>();
		final String firstLine = request.substring(0, request.indexOf('\n'));

		Matcher match = headPattern.matcher(firstLine);
		if (match.find()) {
			this.method = match.group(1);
			this.URI = match.group(2);
		}

		match = headerPattern.matcher(request);
		while (match.find()) {
			this.headers.put(match.group(1).toLowerCase(), match.group(2));
		}

		if (this.method.equals("GET")) {
			if (!this.URI.contains("?")) {
				return;
			}

			match = parametersPattern.matcher(this.URI.substring(this.URI
					.indexOf('?') + 1));
			String[] values = null;
			while (match.find()) {
				values = match.group(1).split("=");
				this.parameters.put(values[0], values[1]);
			}
		}

		if (this.method.equals("POST")) {
			match = parametersPattern.matcher(request.substring(request
					.indexOf("\n\n") + 2));

			String[] values = null;
			while (match.find()) {
				values = match.group(1).split("=");
				this.parameters.put(values[0], values[1]);
			}
		}
	}

	@Override
	public String getHeader(final String name) {
		return this.headers.containsKey(name.toLowerCase()) ? this.headers
				.get(name.toLowerCase()) : null;
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
	public String getParameter(final String name) {
		return this.parameters.containsKey(name) ? this.parameters.get(name)
				: null;
	}
}
