package org.chinux.pdc.http.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.chinux.pdc.http.api.HTTPRequestHeader;

public class HTTPRequestHeaderImpl implements HTTPRequestHeader {

	private static Pattern headPattern = Pattern
			.compile("([\\w-/]+) (.+) ([\\w-/\\.]+)");

	private static Pattern headerPattern = Pattern.compile("([\\w-]+): (.+)");

	private static Pattern parametersPattern = Pattern.compile("([\\w|=]+)");

	private String method;
	private Map<String, String> headers;
	private Map<String, String> parameters;
	private String URI;
	private String request;
	private String version;

	public HTTPRequestHeaderImpl(final String request) {
		this.request = request;
		this.headers = new HashMap<String, String>();
		this.parameters = new HashMap<String, String>();
		final String firstLine = request.split("\n")[0];

		Matcher match = headPattern.matcher(firstLine);
		if (match.find()) {
			this.method = match.group(1);
			this.URI = match.group(2);
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

			match = parametersPattern.matcher(this.URI.substring(this.URI
					.indexOf('?') + 1));
			String[] values = null;
			while (match.find()) {
				values = match.group(1).split("=");
				this.parameters.put(values[0], (values.length > 1) ? values[1]
						: "");
			}
		}

	}

	@Override
	public void addHeader(final String name, final String value) {
		this.headers.put(name.toLowerCase(), value);
	}

	@Override
	public String getHeader(final String name) {
		return this.headers.containsKey(name.toLowerCase()) ? this.headers
				.get(name.toLowerCase()) : null;
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
	public String getParameter(final String name) {
		return this.parameters.containsKey(name) ? this.parameters.get(name)
				: null;
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