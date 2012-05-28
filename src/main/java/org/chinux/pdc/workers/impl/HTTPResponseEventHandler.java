package org.chinux.pdc.workers.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPResponse;
import org.chinux.pdc.http.api.HTTPResponseHeader;
import org.chinux.pdc.http.impl.HTTPBaseReader;
import org.chinux.pdc.http.impl.HTTPResponseHeaderImpl;
import org.chinux.pdc.http.impl.HTTPResponseImpl;
import org.chinux.pdc.http.impl.readers.HTTPChunkedResponseReader;
import org.chinux.pdc.http.impl.readers.HTTPContentLengthReader;
import org.chinux.pdc.http.impl.readers.HTTPImageResponseReader;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;

public class HTTPResponseEventHandler {

	private static Charset isoCharset = Charset.forName("ISO-8859-1");
	private static Logger logger = Logger
			.getLogger(HTTPResponseEventHandler.class);

	private static Pattern headerCutPattern = Pattern.compile("(\\r\\n\\r\\n)",
			Pattern.MULTILINE);

	private HTTPProxyEvent event;

	public HTTPResponseEventHandler(final HTTPProxyEvent event) {
		this.event = event;
	}

	public void handle(final ByteArrayOutputStream stream,
			final ClientDataEvent clientEvent) {

		stream.reset();

		ByteBuffer rawData = ByteBuffer.wrap(clientEvent.getData().array()
				.clone());

		if (this.noHeaderYetParsed()) {
			final StringBuilder pendingHeader = this.event.getBuilder();

			final String rawString = isoCharset.decode(rawData).toString();

			pendingHeader.append(rawString);

			// If we match with the end of a header...
			if (this.matchesHeader(pendingHeader)) {
				// The rawdata is used for the data of the response, since both
				// can fit in the same space.
				rawData = this.buildEventResponse(stream, pendingHeader);
			} else {
				// TODO: Ver una forma de handlear responses inválidos
				// this.event.builder = new StringBuilder();
			}
		}

		if (this.canProcessData(rawData)) {
			this.processData(stream, rawData);
		}
	}

	private boolean matchesHeader(final StringBuilder pendingHeader) {
		return headerCutPattern.matcher(pendingHeader.toString()).find();
	}

	private boolean noHeaderYetParsed() {
		// No response is handled if there is no response built yet for this
		// event
		return this.event.getResponse() == null;
	}

	private boolean canProcessData(final ByteBuffer rawData) {
		// Data can be processed then there is rawdata after a response header
		// built
		return this.event.getResponse() != null && rawData != null;
	}

	private void processData(final ByteArrayOutputStream stream,
			final ByteBuffer rawData) {
		final HTTPResponse response = this.event.getResponse();

		final ByteBuffer data = response.getBodyReader().processData(rawData);

		this.event.setCanSend(data != null);

		if (this.event.canSend()) {
			try {
				stream.write(data.array());
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		if (response.getBodyReader().isFinished()) {
			this.event.setCanClose(true);
		}
	}

	private ByteBuffer buildEventResponse(final ByteArrayOutputStream stream,
			final StringBuilder pendingHeader) {
		ByteBuffer rawData;
		final String[] headerAndBody = pendingHeader.toString().split(
				"\\r\\n\\r\\n", 2);
		final String headerString = headerAndBody[0];

		final HTTPResponseHeader header = new HTTPResponseHeaderImpl(
				headerString);

		this.event.setCanSend(true);

		final HTTPResponse response = new HTTPResponseImpl(header,
				new HTTPBaseReader(header));

		logger.debug(header.toString());

		this.applyReadersToResponse(response);

		try {
			stream.write(isoCharset.encode(CharBuffer.wrap(header.toString()))
					.array());
		} catch (final IOException e) {
			e.printStackTrace();
		}

		this.event.setResponse(response);

		if (headerAndBody.length > 1) {
			rawData = ByteBuffer.wrap(isoCharset.encode(headerAndBody[1])
					.array().clone());
		} else {
			rawData = ByteBuffer.allocate(0);
		}
		return rawData;
	}

	// Loads the readers to the HTTPResponse based on the event we have
	private void applyReadersToResponse(final HTTPResponse response) {
		// Si tiene content-length usamos el content-length reader
		if (this.hasContentLength(response) || this.mustDecodeChunked(response)) {
			response.getBodyReader().addResponseReader(
					new HTTPContentLengthReader(response.getHeaders()), 100);
		} else if (!this.hasContentLength(response)
				&& !this.mustDecodeChunked(response)) {
			// CRLF ended
		}

		if (this.hasImageMIME(response)) {
			response.getBodyReader().addResponseReader(
					new HTTPImageResponseReader(response.getHeaders()), 50);
		}

		if (this.mustDecodeChunked(response)) {
			response.getBodyReader().addResponseReader(
					new HTTPChunkedResponseReader(response.getHeaders()), 0);
		}
	}

	private boolean hasContentLength(final HTTPResponse response) {
		return response.getHeaders().getHeader("content-length") != null;
	}

	private boolean mustDecodeChunked(final HTTPResponse response) {
		return this.hasEncodingChunked(response) && this.hasImageMIME(response);
	}

	private boolean hasImageMIME(final HTTPResponse response) {
		final String contenttype = response.getHeaders().getHeader(
				"Content-Type");
		if (contenttype == null) {
			return false;
		}

		return contenttype.startsWith("image/");
	}

	private boolean hasEncodingChunked(final HTTPResponse response) {
		return response.getHeaders().getHeader("transfer-encoding") != null
				&& response.getHeaders().getHeader("transfer-encoding")
						.equals("chunked");
	}
}
