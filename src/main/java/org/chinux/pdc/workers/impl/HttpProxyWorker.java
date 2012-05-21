package org.chinux.pdc.workers.impl;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPRequest;
import org.chinux.pdc.http.api.HTTPRequestHeader;
import org.chinux.pdc.http.api.HTTPResponse;
import org.chinux.pdc.http.api.HTTPResponseHeader;
import org.chinux.pdc.http.impl.HTTPBaseRequestReader;
import org.chinux.pdc.http.impl.HTTPBaseResponseReader;
import org.chinux.pdc.http.impl.HTTPRequestHeaderImpl;
import org.chinux.pdc.http.impl.HTTPRequestImpl;
import org.chinux.pdc.http.impl.HTTPResponseHeaderImpl;
import org.chinux.pdc.http.impl.HTTPResponseImpl;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.nio.receivers.api.DataReceiver;

public class HttpProxyWorker extends HttpBaseProxyWorker {

	private Logger logger = Logger.getLogger(this.getClass());

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
		public StringBuilder builder = new StringBuilder();

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
			throws UnsupportedEncodingException {
		final HTTPEvent event = (HTTPEvent) clientEvent.getOwner();

		this.logger.debug("Got clientEvent:" + clientEvent.toString());

		// final DataEvent e = new ServerDataEvent(event.client,
		// clientEvent.getData(), this.serverDataReceiver);
		//
		// if (clientEvent.canClose()) {
		//
		// e.setCanClose(true);
		// }
		// e.setCanSend(true);
		// return e;

		final StringBuilder answer = new StringBuilder();

		System.out.println(clientEvent.getData().length);
		byte[] rawData = clientEvent.getData();

		boolean canSend = false;
		boolean canClose = false;

		if (event.response == null) {
			final StringBuilder pendingHeader = event.builder;

			final String rawString = new String(rawData);

			pendingHeader.append(rawString);

			if (this.headerCutPattern.matcher(pendingHeader.toString()).find()) {

				final String[] headerAndBody = pendingHeader.toString().split(
						"\\r\\n\\r\\n", 2);
				final String headerString = headerAndBody[0];
				final HTTPResponseHeader header = new HTTPResponseHeaderImpl(
						headerString);

				// TODO: Call header filter!
				final String contenttype = header.getHeader("Content-Type");

				final boolean mustParseChunked = header
						.getHeader("Transfer-Encoding") != null
						&& header.getHeader("Transfer-Encoding").equals(
								"chunked")
						&& (contenttype != null && (contenttype
								.startsWith("image/") || contenttype
								.equals("text/plain")));

				if (mustParseChunked) {
					header.removeHeader("transfer-encoding");
				}
				canSend = true;

				final HTTPResponse response = new HTTPResponseImpl(header,
						new HTTPBaseResponseReader(header, mustParseChunked));

				if (!mustParseChunked) {
					answer.append(header.toString());
				}

				event.response = response;

				if (headerAndBody.length > 1) {
					rawData = headerAndBody[1].getBytes();
				} else {
					rawData = new byte[] {};
				}
			} else {
				this.logger.debug("Header not done");
			}
		}

		if (event.response != null && rawData != null) {
			this.logger.debug("Reading data");
			this.logger.debug(new String(rawData));
			final HTTPResponse response = event.response;

			final byte[] data = response.getBodyReader().processData(rawData);

			canSend = data != null;

			if (canSend) {
				answer.append(new String(data));
			}

			if (response.getBodyReader().isFinished()) {
				canClose = true;
			}
		}

		if (!Arrays.equals(clientEvent.getData(),
				answer.toString().getBytes("US-ASCII"))) {
			this.logger.debug("Different output!");
		} else {
			this.logger.debug("Same output!");
		}

		final DataEvent e = new ServerDataEvent(event.client, answer.toString()
				.getBytes(), this.serverDataReceiver);

		e.setCanClose(canClose);
		e.setCanSend(canSend);

		this.logger.debug(new String(e.getData()));

		return e;
	}

	private Pattern headerCutPattern = Pattern.compile("(\\r\\n\\r\\n)",
			Pattern.MULTILINE);

	private boolean isReadingServerHeaders(final SocketChannel socketChannel) {

		if ((!this.readingServerSockets.containsKey(socketChannel) && !this.readingDataSockets
				.containsKey(socketChannel))) {

			this.readingServerSockets.put(socketChannel, new StringBuilder());

			return true;
		}

		return this.readingServerSockets.containsKey(socketChannel);
	}

	private boolean isReadingData(final SocketChannel socketChannel) {
		return this.readingDataSockets.containsKey(socketChannel);
	}

	@Override
	protected DataEvent DoWork(final ServerDataEvent serverEvent) {

		final SocketChannel clientChannel = serverEvent.getChannel();

		final StringBuilder answer = new StringBuilder();

		byte[] rawData = serverEvent.getData();

		boolean canClose = false;
		boolean canSend = false;

		HTTPEvent eventOwner = null;

		this.logger.debug("Got serverEvent:" + serverEvent.toString());

		// If we are already building the httpRequest... we build it
		if (this.isReadingServerHeaders(clientChannel)) {

			this.logger.debug("Reading headers for clientChannel: "
					+ clientChannel);
			final StringBuilder pendingHeader = this.readingServerSockets
					.get(clientChannel);

			final String rawString = new String(rawData);

			pendingHeader.append(rawString);

			if (this.headerCutPattern.matcher(pendingHeader.toString()).find()) {

				final String[] headerAndBody = pendingHeader.toString().split(
						"\\r\\n\\r\\n", 2);

				final String headerString = headerAndBody[0];
				final HTTPRequestHeader header = new HTTPRequestHeaderImpl(
						headerString);

				// TODO: Call header filter!

				canSend = true;

				answer.append(header.toString());

				this.logger.debug(header.toString());

				final HTTPEvent event = new HTTPEvent(new HTTPRequestImpl(
						header, new HTTPBaseRequestReader(header)),
						clientChannel);

				this.readingDataSockets.put(clientChannel, event);

				eventOwner = event;

				if (headerAndBody.length > 1) {
					rawData = headerAndBody[1].getBytes();
				} else {
					rawData = new byte[] {};
				}
			}
		}

		// We process all the reading data
		if (this.isReadingData(clientChannel) && rawData != null) {
			this.logger.debug("Reading data from clientChannel: "
					+ clientChannel);
			final HTTPEvent event = this.readingDataSockets.get(clientChannel);
			final HTTPRequest request = event.request;

			final byte[] data = request.getBodyReader().processData(rawData);

			canSend = data != null;

			if (canSend) {
				answer.append(new String(data));
			}

			eventOwner = event;

			if (request.getBodyReader().isFinished()) {
				this.logger.debug("Client channel IS finished!");
				canClose = true;
				this.readingDataSockets.remove(clientChannel);
				this.readingServerSockets.remove(clientChannel);
			} else {
				this.logger.debug("Client channel is not finished!");
			}
		}

		InetAddress address = null;
		try {
			if (eventOwner.request.getHeaders().getHeader("Host") == null) {
				throw new Exception();
			}

			address = InetAddress.getByName(eventOwner.request.getHeaders()
					.getHeader("Host"));
		} catch (final Exception e) {
			address = clientChannel.socket().getInetAddress();
		}

		// If the client doesn't send back this HTTPEvent, we must expire it
		// later on.
		if (!this.receivedRequests.contains(eventOwner)) {
			this.receivedRequests.add(eventOwner);
		}

		final DataEvent e = new ClientDataEvent(answer.toString().getBytes(),
				this.clientDataReceiver, address, eventOwner);

		e.setCanClose(canClose);
		e.setCanSend(canSend);

		this.logger.debug("Server answer event:" + e);
		this.logger.debug(new String(e.getData()));

		return e;
	}
}
