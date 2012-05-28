package org.chinux.pdc.workers.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.chinux.pdc.FilterException;
import org.chinux.pdc.http.api.HTTPRequest;
import org.chinux.pdc.http.api.HTTPRequestHeader;
import org.chinux.pdc.http.impl.HTTPBaseFilter;
import org.chinux.pdc.http.impl.HTTPBaseReader;
import org.chinux.pdc.http.impl.HTTPRequestHeaderImpl;
import org.chinux.pdc.http.impl.HTTPRequestImpl;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;

public class HTTPRequestEventHandler {

	private static Charset isoCharset = Charset.forName("ISO-8859-1");
	private static Logger logger = Logger
			.getLogger(HTTPResponseEventHandler.class);
	private static Pattern headerCutPattern = Pattern.compile("(\\r\\n\\r\\n)",
			Pattern.MULTILINE);

	private ByteArrayOutputStream outputBuffer;
	private ByteBuffer rawData;

	private Map<SocketChannel, StringBuilder> readingServerSockets = new HashMap<SocketChannel, StringBuilder>();

	private Map<SocketChannel, HTTPProxyEvent> readingDataSockets = new HashMap<SocketChannel, HTTPProxyEvent>();

	public HTTPProxyEvent handle(final ServerDataEvent serverEvent)
			throws IOException {
		HTTPProxyEvent httpEvent = null;

		this.outputBuffer.reset();

		final SocketChannel clientChannel = serverEvent.getChannel();

		this.rawData = ByteBuffer.wrap(serverEvent.getData().array().clone());

		// If we are already building the httpRequest... we build it
		if (this.isReadingRequestHeaders(clientChannel)) {
			httpEvent = this.readEventRequestHeader(clientChannel, httpEvent);
		}

		// We process all the reading data
		if (this.isReadingRequestData(clientChannel)) {
			httpEvent = this.readRequestData(clientChannel);
		}

		if (httpEvent != null) {
			httpEvent
					.setAddress(this.getEventAddress(clientChannel, httpEvent));
		}

		return httpEvent;
	}

	private boolean isReadingRequestHeaders(final SocketChannel socketChannel) {
		if ((!this.readingServerSockets.containsKey(socketChannel) && !this.readingDataSockets
				.containsKey(socketChannel))) {
			this.readingServerSockets.put(socketChannel, new StringBuilder());
			return true;
		}

		return this.readingServerSockets.containsKey(socketChannel);
	}

	private boolean isReadingRequestData(final SocketChannel socketChannel) {
		return this.readingDataSockets.containsKey(socketChannel)
				&& this.rawData != null;
	}

	public HTTPRequestEventHandler(final ByteArrayOutputStream answerStream) {
		this.outputBuffer = answerStream;
	}

	private InetAddress getEventAddress(final SocketChannel clientChannel,
			final HTTPProxyEvent httpEvent) {
		InetAddress address = null;
		try {
			if (httpEvent.getRequest().getHeaders().getHeader("Host") == null) {
				throw new Exception();
			}
			address = InetAddress.getByName(httpEvent.getRequest().getHeaders()
					.getHeader("Host"));
		} catch (final Exception e) {
			address = clientChannel.socket().getInetAddress();
		}
		return address;
	}

	private HTTPProxyEvent readRequestData(final SocketChannel clientChannel) {
		HTTPProxyEvent httpEvent;
		this.logger.debug("Reading data from clientChannel: " + clientChannel);
		final HTTPProxyEvent event = this.readingDataSockets.get(clientChannel);
		final HTTPRequest request = event.getRequest();

		final ByteBuffer data = request.getBodyReader().processData(
				this.rawData);

		event.setCanSend(data != null);

		try {
			if (event.canSend()) {
				this.outputBuffer.write(data.array());
			}
		} catch (final Exception e1) {
			e1.printStackTrace();
		}

		httpEvent = event;

		if (request.getBodyReader().isFinished()) {
			this.readingDataSockets.remove(clientChannel);
			this.readingServerSockets.remove(clientChannel);
			event.setCanClose(true);
		}
		return httpEvent;
	}

	/**
	 * Reads the current data and tries to build a new HTTPEvent after parsing a
	 * Request Header
	 */
	private HTTPProxyEvent readEventRequestHeader(
			final SocketChannel clientChannel, HTTPProxyEvent proxyEvent)
			throws IOException {
		final StringBuilder pendingHeader = this.readingServerSockets
				.get(clientChannel);

		final String rawString = isoCharset.decode(this.rawData).toString();

		pendingHeader.append(rawString);

		if (headerCutPattern.matcher(pendingHeader.toString()).find()) {

			final String[] headerAndBody = pendingHeader.toString().split(
					"\\r\\n\\r\\n", 2);

			final String headerString = headerAndBody[0];
			final HTTPRequestHeader header = new HTTPRequestHeaderImpl(
					headerString);

			logger.debug(header.toString());

			final HTTPProxyEvent event = new HTTPProxyEvent(
					new HTTPRequestImpl(header, new HTTPBaseReader(header)),
					clientChannel);

			if (!HTTPBaseFilter.getBaseFilter().isValid(event)) {
				try {
					throw new FilterException(HTTPBaseFilter.getBaseFilter()
							.getErrorResponse(event).toString());
				} catch (final FilterException e) {
					e.printStackTrace();
				}
			} else {
				this.outputBuffer.write(isoCharset.encode(
						CharBuffer.wrap(header.toString())).array());
			}

			this.readingDataSockets.put(clientChannel, event);

			proxyEvent = event;

			proxyEvent.setCanSend(true);

			if (headerAndBody.length > 1) {
				this.rawData = ByteBuffer.wrap(isoCharset.encode(
						headerAndBody[1]).array());
			} else {
				this.rawData = ByteBuffer.allocate(0);
			}
		}
		return proxyEvent;
	}

	private void applyReadersToRequest(final HTTPProxyEvent event) {
		final HTTPRequest request = event.getRequest();
	}

}
