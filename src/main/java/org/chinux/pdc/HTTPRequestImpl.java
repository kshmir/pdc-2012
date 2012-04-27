package org.chinux.pdc;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPRequestImpl implements HTTPRequest {

	private String method;
	private Map<String, String> headers;
	private Map<String, String> parameters;
	private String URI;

	public HTTPRequestImpl(final String request) {
		this.headers = new HashMap<String, String>();
		this.parameters = new HashMap<String, String>();
		final String firstLine = request.substring(0, request.indexOf('\n'));

		Pattern pattern = Pattern.compile("([\\w-/]+) (.+) ([\\w-/\\.]+)");
		Matcher match = pattern.matcher(firstLine);
		if (match.find()) {
			this.method = match.group(1);
			this.URI = match.group(2);
		}

		pattern = Pattern.compile("([\\w-]+): (.+)");
		match = pattern.matcher(request);
		while (match.find()) {
			this.headers.put(match.group(1).toLowerCase(), match.group(2));
		}

		if (this.method.equals("GET")) {
			if (!this.URI.contains("?")) {
				return;
			}

			pattern = Pattern.compile("([\\w|=]+)");
			match = pattern
					.matcher(this.URI.substring(this.URI.indexOf('?') + 1));
			pattern = Pattern.compile("(\\w+)=(\\w+)");
			String[] values = null;
			while (match.find()) {
				values = match.group(1).split("=");
				this.parameters.put(values[0], values[1]);
			}
		}

		if (this.method.equals("POST")) {
			pattern = Pattern.compile("([\\w|=]+)");
			match = pattern
					.matcher(request.substring(request.indexOf("\n\n") + 2));

			String[] values = null;
			while (match.find()) {
				values = match.group(1).split("=");
				this.parameters.put(values[0], values[1]);
			}
		}
	}

	public String getHeader(final String name) {
		return this.headers.containsKey(name.toLowerCase()) ? this.headers
				.get(name.toLowerCase()) : null;
	}

	public String getMethod() {
		return this.method;
	}

	public String getRequestURI() {
		return this.URI;
	}

	public String getParameter(final String name) {
		return this.parameters.containsKey(name) ? this.parameters.get(name)
				: null;
	}
}
