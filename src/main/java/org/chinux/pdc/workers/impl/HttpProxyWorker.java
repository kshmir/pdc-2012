package org.chinux.pdc.workers.impl;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
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

	/**
	 * Represents a unique httprequest sent by a client. When receiving a new
	 * httpresponse object, we know which request is the owner by this object
	 * And we also know which is the client to answer to
	 * 
	 * @author cris
	 */
	public static class HTTPEvent {
		public HTTPRequest request;
		public SocketChannel client;

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

	private Map<SocketChannel, StringBuilder> readingHeaderSockets = new HashMap<SocketChannel, StringBuilder>();

	private Map<SocketChannel, HTTPEvent> readingDataSockets = new HashMap<SocketChannel, HTTPEvent>();

	private Map<HTTPEvent, HTTPResponse> receivedRequests = new HashMap<HTTPEvent, HTTPResponse>();

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
	protected DataEvent DoWork(final ClientDataEvent clientEvent) {
		return null;
	}

	private Pattern headerCutPattern = Pattern.compile("\\n\\n",
			Pattern.MULTILINE);

	private boolean isReadingHeaders(final SocketChannel socketChannel) {

		if ((!this.readingHeaderSockets.containsKey(socketChannel) && !this.readingDataSockets
				.containsKey(socketChannel))) {

			this.readingHeaderSockets.put(socketChannel, new StringBuilder());

			return true;
		}

		return this.readingHeaderSockets.containsKey(socketChannel);
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

		// If we are already building the httpRequest... we build it
		if (this.isReadingHeaders(clientChannel)) {
			final StringBuilder pendingHeader = this.readingHeaderSockets
					.get(clientChannel);

			pendingHeader.append(new String(rawData));

			if (this.headerCutPattern.matcher(pendingHeader.toString()).find()) {

				final String[] headerAndBody = pendingHeader.toString().split(
						"\\n\\n");
				final String headerString = headerAndBody[0];
				final HTTPRequestHeader header = new HTTPRequestHeaderImpl(
						headerString);

				// TODO: Call header filter!
				canSend = true;

				answer.append(header.toString());

				this.readingHeaderSockets.remove(clientChannel);

				final HTTPEvent event = new HTTPEvent(new HTTPRequestImpl(
						header, new HTTPBaseRequestReader(header)),
						clientChannel);

				this.readingDataSockets.put(clientChannel, event);

				eventOwner = event;

				if (headerAndBody.length > 1) {
					rawData = StringUtils.join(
							ArrayUtils.subarray(headerAndBody, 1,
									headerAndBody.length), "\n\n").getBytes();
				} else {
					rawData = new byte[] {};
				}
			}
		}

		// We process all the reading data
		if (this.isReadingData(clientChannel) && rawData != null) {
			final HTTPEvent event = this.readingDataSockets.get(clientChannel);
			final HTTPRequest request = event.request;

			final byte[] data = request.getBodyReader().processData(rawData);

			canSend = data != null;

			if (canSend) {
				answer.append(new String(data));
			}

			eventOwner = event;
			if (request.getBodyReader().isFinished()) {
				canClose = true;
				this.readingDataSockets.remove(clientChannel);
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

		final DataEvent e = new ClientDataEvent(answer.toString().getBytes(),
				this.clientDataReceiver, address, eventOwner);

		e.setCanClose(canClose);
		e.setCanSend(canSend);

		return e;
	}
}
