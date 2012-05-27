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
import org.chinux.pdc.http.api.HTTPRequest;
import org.chinux.pdc.http.api.HTTPRequestHeader;
import org.chinux.pdc.http.impl.HTTPBaseRequestReader;
import org.chinux.pdc.http.impl.HTTPRequestHeaderImpl;
import org.chinux.pdc.http.impl.HTTPRequestImpl;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;

public class HTTPRequestEventHandler {

	private static Charset isoCharset = Charset.forName("ISO-8859-1");
	private static Logger logger = Logger
			.getLogger(HTTPResponseEventHandler.class);
	private static Pattern headerCutPattern = Pattern.compile("(\\r\\n\\r\\n)",
			Pattern.MULTILINE);
	private ByteArrayOutputStream answer;

	private boolean isReadingRequestHeaders(final SocketChannel socketChannel) {
		if ((!this.readingServerSockets.containsKey(socketChannel) && !this.readingDataSockets
				.containsKey(socketChannel))) {
			this.readingServerSockets.put(socketChannel, new StringBuilder());
			return true;
		}

		return this.readingServerSockets.containsKey(socketChannel);
	}

	private ByteBuffer rawData;

	private boolean isReadingRequestData(final SocketChannel socketChannel) {
		return this.readingDataSockets.containsKey(socketChannel)
				&& this.rawData != null;
	}

	private Map<SocketChannel, StringBuilder> readingServerSockets = new HashMap<SocketChannel, StringBuilder>();

	private Map<SocketChannel, HTTPEvent> readingDataSockets = new HashMap<SocketChannel, HTTPEvent>();

	public HTTPRequestEventHandler(final ByteArrayOutputStream answerStream) {
		this.answer = answerStream;
	}

	private InetAddress getEventAddress(final SocketChannel clientChannel,
			final HTTPEvent httpEvent) {
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

	private HTTPEvent readRequestData(final SocketChannel clientChannel) {
		HTTPEvent httpEvent;
		this.logger.debug("Reading data from clientChannel: " + clientChannel);
		final HTTPEvent event = this.readingDataSockets.get(clientChannel);
		final HTTPRequest request = event.getRequest();

		final ByteBuffer data = request.getBodyReader().processData(
				this.rawData);

		event.setCanSend(data != null);

		try {
			if (event.canSend()) {
				this.answer.write(data.array());
			}
		} catch (final Exception e1) {
			e1.printStackTrace();
		}

		httpEvent = event;

		event.setCanClose(true);
		if (request.getBodyReader().isFinished()) {
			this.readingDataSockets.remove(clientChannel);
			this.readingServerSockets.remove(clientChannel);
		}
		return httpEvent;
	}

	/**
	 * Reads the current data and tries to build a new HTTPEvent after parsing a
	 * Request Header
	 * 
	 * @param clientChannel
	 * @param eventOwner
	 * @return
	 * @throws IOException
	 */
	private HTTPEvent readEventRequestHeader(final SocketChannel clientChannel,
			HTTPEvent eventOwner) throws IOException {
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

			this.answer.write(isoCharset.encode(
					CharBuffer.wrap(header.toString())).array());

			this.logger.debug(header.toString());

			final HTTPEvent event = new HTTPEvent(new HTTPRequestImpl(header,
					new HTTPBaseRequestReader(header)), clientChannel);

			this.readingDataSockets.put(clientChannel, event);

			eventOwner = event;

			eventOwner.setCanSend(true);

			if (headerAndBody.length > 1) {
				this.rawData = ByteBuffer.wrap(isoCharset.encode(
						CharBuffer.wrap(headerAndBody[1])).array());
			} else {
				this.rawData = ByteBuffer.allocate(0);
			}
		}
		return eventOwner;
	}

	public HTTPEvent handle(final ServerDataEvent serverEvent)
			throws IOException {
		HTTPEvent httpEvent = null;

		this.answer.reset();

		final SocketChannel clientChannel = serverEvent.getChannel();

		this.rawData = ByteBuffer.wrap(serverEvent.getData().array().clone());

		// If we are already building the httpRequest... we build it
		if (this.isReadingRequestHeaders(clientChannel)) {
			httpEvent = this.readEventRequestHeader(clientChannel, httpEvent);
		}

		// TODO: if httpEvent != null we must filter it and send the given error
		// if we find any.

		// We process all the reading data
		if (this.isReadingRequestData(clientChannel)) {
			httpEvent = this.readRequestData(clientChannel);
		}

		if (httpEvent != null) {
			httpEvent.setAddress(this.getEventAddress(clientChannel, httpEvent));
		}

		return httpEvent;
	}
}
