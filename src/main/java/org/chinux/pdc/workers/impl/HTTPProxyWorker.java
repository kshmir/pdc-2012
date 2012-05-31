package org.chinux.pdc.workers.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.events.impl.ErrorDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.nio.receivers.api.DataReceiver;

public class HTTPProxyWorker extends HTTPBaseProxyWorker {

	private static Charset isoCharset = Charset.forName("ISO-8859-1");
	private Logger logger = Logger.getLogger(this.getClass());
	private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

	private Map<SocketChannel, ByteBuffer> lastBufferForSocket = new HashMap<SocketChannel, ByteBuffer>();

	private Set<HTTPProxyEvent> receivedRequests = new HashSet<HTTPProxyEvent>();

	private DataReceiver<DataEvent> clientDataReceiver = null;
	private DataReceiver<DataEvent> serverDataReceiver = null;

	private HTTPRequestEventHandler requestEventHandler;

	public void setClientDataReceiver(
			final DataReceiver<DataEvent> clientDataReceiver) {
		this.clientDataReceiver = clientDataReceiver;
	}

	public void setServerDataReceiver(
			final DataReceiver<DataEvent> serverDataReceiver) {
		this.serverDataReceiver = serverDataReceiver;
	}

	private boolean clientDisconnectedForEvent(final HTTPProxyEvent event) {
		return !event.getSocketChannel().isConnected();
	}

	private boolean isEndOfRequest(final ClientDataEvent clientEvent) {
		final HTTPProxyEvent event = (HTTPProxyEvent) clientEvent
				.getAttachment();

		if (event.getResponse() != null) {
			boolean connectionClose = true;
			if (event.getResponse().getHeaders().getHTTPVersion() != null
					&& event.getResponse().getHeaders().getHTTPVersion()
							.equals("1.0")) {
				if (event.getResponse().getHeaders().getHeader("connection") != null) {
					connectionClose = event.getResponse().getHeaders()
							.getHeader("connection").equals("close");
				}
				return (event.canClose() && !connectionClose)
						|| clientEvent.canClose();
			} else {
				return event.canClose();
			}

		} else {
			return false;
		}
	}

	private boolean isEndOfConnection(final ClientDataEvent clientEvent) {
		final HTTPProxyEvent event = (HTTPProxyEvent) clientEvent
				.getAttachment();

		if (event.getResponse() != null) {
			boolean connectionClose = false;
			if (event.getResponse().getHeaders().getHTTPVersion() != null
					&& event.getResponse().getHeaders().getHTTPVersion()
							.equals("1.0")) {
				if (event.getResponse().getHeaders().getHeader("connection") != null) {
					connectionClose = event.getResponse().getHeaders()
							.getHeader("connection").equals("close");
				}
				return (connectionClose || clientEvent.canClose());
			} else {
				if (event.getResponse().getHeaders().getHeader("connection") != null) {
					connectionClose = event.getResponse().getHeaders()
							.getHeader("connection").equals("close");
				} else {
					return false;
				}
				return (connectionClose || clientEvent.canClose())
						&& event.canClose();
			}
		} else {
			return false;
		}
	}

	@Override
	protected DataEvent DoWork(final ClientDataEvent clientEvent)
			throws IOException {
		final HTTPProxyEvent event = (HTTPProxyEvent) clientEvent
				.getAttachment();

		DataEvent e = null;
		if (this.clientDisconnectedForEvent(event)) {
			e = new ClientDataEvent(null, this.clientDataReceiver, null, event,
					null);
			e.setCanClose(true);
			e.setCanSend(false);

		} else {

			final HTTPResponseEventHandler eventHandler = new HTTPResponseEventHandler(
					event);

			eventHandler.handle(this.outputBuffer, clientEvent);

			e = new ServerDataEvent(event.getSocketChannel(),
					ByteBuffer.wrap(this.outputBuffer.toByteArray().clone()),
					this.serverDataReceiver);

			// TODO: Aprolijar esto
			if (this.isEndOfRequest(clientEvent)) {
				this.logger
						.info("Reenviando RESPONSE: "
								+ event.getResponse().getHeaders()
										.returnStatusCode()
								+ " "
								+ event.getRequest().getHeaders()
										.getRequestURI());
				final ClientDataEvent tellToClose = new ClientDataEvent(null,
						this.clientDataReceiver, null, event, null);
				this.clientDataReceiver.closeConnection(tellToClose);
			}

			// TODO: Aprolijar esto
			if (this.isEndOfConnection(clientEvent)) {
				e.setCanClose(clientEvent.canClose());
			}
			e.setCanSend(event.canSend());

		}

		this.logger.debug(clientEvent.getData());

		return e;
	}

	@Override
	protected DataEvent DoWork(final ServerDataEvent serverEvent)
			throws IOException {

		final HTTPRequestEventHandler handler = this.getRequestEventHandler();

		if (this.lastBufferForSocket.containsKey(serverEvent.getChannel())) {
			final ByteArrayOutputStream concatenator = new ByteArrayOutputStream();
			concatenator.write(this.lastBufferForSocket.get(
					serverEvent.getChannel()).array());
			concatenator.write(serverEvent.getData().array());
			serverEvent.setData(ByteBuffer.wrap(concatenator.toByteArray()));
		}

		final HTTPProxyEvent httpEvent = handler.handle(serverEvent);

		final DataEvent e = new ClientDataEvent(
				ByteBuffer.wrap(this.outputBuffer.toByteArray()),
				this.clientDataReceiver,
				(httpEvent != null) ? httpEvent.getAddress() : null, httpEvent,
				(httpEvent != null) ? httpEvent.getSocketChannel() : null);

		// If the client doesn't send back this HTTPEvent, we must expire it
		// later on.
		if (!this.receivedRequests.contains(httpEvent)) {
			this.receivedRequests.add(httpEvent);
		}

		if (httpEvent != null) {
			if (httpEvent.canClose()) {
				if (httpEvent.getRequest() != null) {
					this.logger.info("Reenviando REQUEST: "
							+ httpEvent.getRequest().getHeaders().getMethod()
							+ " "
							+ httpEvent.getRequest().getHeaders()
									.getRequestURI());
				}
			}

			if (httpEvent.getParseOffsetData() != null) {
				this.lastBufferForSocket.put(serverEvent.getChannel(),
						httpEvent.getParseOffsetData());
			}

			e.setCanSend(httpEvent.canSend());

		} else {
			e.setCanClose(false);
			e.setCanSend(false);
		}

		this.logger.debug("Server answer event:" + e);

		return e;
	}

	private HTTPRequestEventHandler getRequestEventHandler() {
		if (this.requestEventHandler == null) {
			this.requestEventHandler = new HTTPRequestEventHandler(
					this.outputBuffer);
		}
		return this.requestEventHandler;
	}

	@Override
	protected DataEvent DoWork(final ErrorDataEvent errorEvent)
			throws IOException {

		ErrorDataEvent answerDataEvent = null;
		switch (errorEvent.getErrorType()) {
		case ErrorDataEvent.PROXY_CLIENT_DISCONNECT:
			// Nothing is done here yet.
			errorEvent.setCanClose(false);
			errorEvent.setCanSend(false);
			answerDataEvent = errorEvent;
			break;
		case ErrorDataEvent.REMOTE_CLIENT_DISCONNECT:

			final HTTPProxyEvent event = (HTTPProxyEvent) errorEvent.getOwner();
			answerDataEvent = new ErrorDataEvent(
					ErrorDataEvent.REMOTE_CLIENT_DISCONNECT,
					errorEvent.getAttachment(), event.getSocketChannel());
			// We must tell the server to close itself
			answerDataEvent.getReceivers().addReceiver(this.serverDataReceiver);
			// TODO: Return data before closing!!!
			answerDataEvent.setCanSend(false);
			answerDataEvent.setCanClose(false);
			break;
		}

		return answerDataEvent;
	}

}
