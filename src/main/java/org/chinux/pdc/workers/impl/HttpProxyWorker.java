package org.chinux.pdc.workers.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPRequest;
import org.chinux.pdc.http.api.HTTPRequestHeader;
import org.chinux.pdc.http.api.HTTPResponse;
import org.chinux.pdc.http.impl.HTTPBaseRequestReader;
import org.chinux.pdc.http.impl.HTTPRequestHeaderImpl;
import org.chinux.pdc.http.impl.HTTPRequestImpl;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.nio.receivers.api.DataReceiver;

public class HttpProxyWorker extends HttpBaseProxyWorker {

	private static Charset isoCharset = Charset.forName("ISO-8859-1");
	private Logger logger = Logger.getLogger(this.getClass());
	private final ByteArrayOutputStream answer = new ByteArrayOutputStream();
	private ByteBuffer rawData;
	public static Pattern headerCutPattern = Pattern.compile("(\\r\\n\\r\\n)",
			Pattern.MULTILINE);

	/**
	 * Represents a unique httprequest sent by a client. When receiving a new
	 * httpresponse object, we know which request is the owner by this object
	 * And we also know which is the client to answer to
	 * 
	 * @author cris
	 */
	public static class HTTPEvent {
		public HTTPRequest request;
		public HTTPResponse response;
		public SocketChannel client;
		public Charset dataCharset = isoCharset; // Por ahora re va
		public StringBuilder builder = new StringBuilder();
		public boolean canSend;
		public boolean canClose;

		public HTTPEvent(final HTTPRequest request, final SocketChannel channel) {
			this.request = request;
			this.client = channel;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((this.client == null) ? 0 : this.client.hashCode());
			result = prime * result
					+ ((this.request == null) ? 0 : this.request.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			final HTTPEvent other = (HTTPEvent) obj;
			if (this.client == null) {
				if (other.client != null) {
					return false;
				}
			} else if (!this.client.equals(other.client)) {
				return false;
			}
			if (this.request == null) {
				if (other.request != null) {
					return false;
				}
			} else if (!this.request.equals(other.request)) {
				return false;
			}
			return true;
		}
	}

	private Map<SocketChannel, StringBuilder> readingServerSockets = new HashMap<SocketChannel, StringBuilder>();

	private Map<SocketChannel, HTTPEvent> readingDataSockets = new HashMap<SocketChannel, HTTPEvent>();

	private Set<HTTPEvent> receivedRequests = new HashSet<HTTPEvent>();

	private DataReceiver<DataEvent> clientDataReceiver = null;

	private DataReceiver<DataEvent> serverDataReceiver = null;

	public void setClientDataReceiver(
			final DataReceiver<DataEvent> clientDataReceiver) {
		this.clientDataReceiver = clientDataReceiver;
	}

	public void setServerDataReceiver(
			final DataReceiver<DataEvent> serverDataReceiver) {
		this.serverDataReceiver = serverDataReceiver;
	}

	@Override
	protected DataEvent DoWork(final ClientDataEvent clientEvent)
			throws IOException {
		final HTTPEvent event = (HTTPEvent) clientEvent.getOwner();

		final HTTPResponseEventHandler eventHandler = new HTTPResponseEventHandler(
				event);

		eventHandler.handle(this.answer, clientEvent);

		final DataEvent e = new ServerDataEvent(event.client,
				ByteBuffer.wrap(this.answer.toByteArray().clone()),
				this.serverDataReceiver);

		e.setCanClose(event.canClose);
		e.setCanSend(event.canSend);

		this.logger.debug(clientEvent.getData());

		return e;
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
		return this.readingDataSockets.containsKey(socketChannel);
	}

	@Override
	protected DataEvent DoWork(final ServerDataEvent serverEvent)
			throws IOException {

		final SocketChannel clientChannel = serverEvent.getChannel();

		this.answer.reset();

		HTTPEvent httpEvent = null;

		this.rawData = ByteBuffer.wrap(serverEvent.getData().array().clone());

		this.logger.debug("Got serverEvent:" + serverEvent.toString());

		// If we are already building the httpRequest... we build it
		if (this.isReadingRequestHeaders(clientChannel)) {
			httpEvent = this.readEventRequestHeader(clientChannel, httpEvent);
		}

		// We process all the reading data
		if (this.isReadingRequestData(clientChannel) && this.rawData != null) {
			httpEvent = this.readRequestData(clientChannel);
		}

		final InetAddress address = this.getEventAddress(clientChannel,
				httpEvent);

		// If the client doesn't send back this HTTPEvent, we must expire it
		// later on.
		if (!this.receivedRequests.contains(httpEvent)) {
			this.receivedRequests.add(httpEvent);
		}

		final DataEvent e = new ClientDataEvent(ByteBuffer.wrap(this.answer
				.toByteArray()), this.clientDataReceiver, address, httpEvent);

		if (httpEvent != null) {
			e.setCanClose(httpEvent.canClose);
			e.setCanSend(httpEvent.canSend);
		} else {
			e.setCanClose(false);
			e.setCanSend(false);
		}

		this.logger.debug("Server answer event:" + e);

		return e;
	}

	private InetAddress getEventAddress(final SocketChannel clientChannel,
			final HTTPEvent httpEvent) {
		InetAddress address = null;
		try {
			if (httpEvent.request.getHeaders().getHeader("Host") == null) {
				throw new Exception();
			}
			address = InetAddress.getByName(httpEvent.request.getHeaders()
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
		final HTTPRequest request = event.request;

		final ByteBuffer data = request.getBodyReader().processData(
				this.rawData);

		event.canSend = data != null;

		try {
			if (event.canSend) {
				this.answer.write(data.array());
			}
		} catch (final Exception e1) {
			e1.printStackTrace();
		}

		httpEvent = event;

		event.canClose = true;
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

			// TODO: Call header filter!

			this.answer.write(isoCharset.encode(
					CharBuffer.wrap(header.toString())).array());

			this.logger.debug(header.toString());

			final HTTPEvent event = new HTTPEvent(new HTTPRequestImpl(header,
					new HTTPBaseRequestReader(header)), clientChannel);

			this.readingDataSockets.put(clientChannel, event);

			eventOwner = event;

			eventOwner.canSend = true;

			if (headerAndBody.length > 1) {
				this.rawData = ByteBuffer.wrap(isoCharset.encode(
						CharBuffer.wrap(headerAndBody[1])).array());
			} else {
				this.rawData = ByteBuffer.allocate(0);
			}
		}
		return eventOwner;
	}
}
