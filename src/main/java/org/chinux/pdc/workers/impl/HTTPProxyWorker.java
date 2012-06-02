package org.chinux.pdc.workers.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.chinux.pdc.nio.dispatchers.ASyncEventDispatcher;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.events.impl.ErrorDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.nio.receivers.api.DataReceiver;

public class HTTPProxyWorker extends HTTPBaseProxyWorker {

	private Logger logger = Logger.getLogger(this.getClass());
	private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

	private Map<SocketChannel, Deque<HTTPProxyEvent>> eventsForChannel = new HashMap<SocketChannel, Deque<HTTPProxyEvent>>();

	private void addEventForChannel(final SocketChannel channel,
			final HTTPProxyEvent event) {
		if (this.eventsForChannel.get(channel) == null) {
			this.eventsForChannel
					.put(channel, new LinkedList<HTTPProxyEvent>());
		}

		for (final HTTPProxyEvent e : this.eventsForChannel.get(channel)) {
			if (event == e) {
				return;
			}
		}

		this.eventsForChannel.get(channel).addLast(event);
	}

	private HTTPProxyEvent pollEventForChannel(final SocketChannel channel) {
		return (this.eventsForChannel.get(channel) == null || this.eventsForChannel
				.get(channel).size() == 0) ? null : this.eventsForChannel.get(
				channel).getFirst();
	}

	private HTTPProxyEvent popEventForChannel(final SocketChannel channel) {
		return (this.eventsForChannel.get(channel) == null || this.eventsForChannel
				.get(channel).size() == 0) ? null : this.eventsForChannel.get(
				channel).removeFirst();
	}

	private Set<HTTPProxyEvent> receivedRequests = new HashSet<HTTPProxyEvent>();

	private DataReceiver<DataEvent> clientDataReceiver = null;
	private DataReceiver<DataEvent> serverDataReceiver = null;

	private HTTPRequestEventHandler requestEventHandler;
	private ASyncEventDispatcher<DataEvent> eventDispatcher;

	public void setEventDispatcher(
			final ASyncEventDispatcher<DataEvent> eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

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

	private boolean isEndOfRequest(final ClientDataEvent clientEvent,
			final HTTPProxyEvent event) {

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

	private boolean isEndOfConnection(final ClientDataEvent clientEvent,
			final HTTPProxyEvent event) {

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
				return (connectionClose) && event.canClose();
			}
		} else {
			return false;
		}
	}

	int first = 0;

	@Override
	protected DataEvent DoWork(final ClientDataEvent clientEvent)
			throws IOException {

		HTTPProxyEvent event = (HTTPProxyEvent) clientEvent.getAttachment();
		event = this.pollEventForChannel(event.getSocketChannel());
		if (event == null) {
			event = (HTTPProxyEvent) clientEvent.getAttachment();
		}

		DataEvent e = null;
		if (this.clientDisconnectedForEvent(event)) {
			e = new ClientDataEvent(null, this.clientDataReceiver, null, event,
					null);
			e.setCanClose(true);
			e.setCanSend(false);
			this.logger.debug("Closing sockets for local socket disconnect!");
		} else {

			final HTTPResponseEventHandler eventHandler = new HTTPResponseEventHandler(
					event);

			eventHandler.handle(this.outputBuffer, clientEvent);

			if (event.getParseClientOffsetData() != null
					&& event.getParseClientOffsetData().array().length > 0) {

				final ClientDataEvent nextEvent = new ClientDataEvent(
						event.getParseClientOffsetData(), event.next);

				if (event.next != null) {
					nextEvent.setCanClose(event.canClose());
					nextEvent.setCanSend(event.canSend());

					this.eventDispatcher.processDataUrgent(nextEvent);
				}

			}
			e = new ServerDataEvent(event.getSocketChannel(),
					ByteBuffer.wrap(this.outputBuffer.toByteArray().clone()),
					this.serverDataReceiver);

			// TODO: Aprolijar esto
			if (this.isEndOfConnection(clientEvent, event)) {

				this.logger.info("CERRANDO CON RESPONSE: "
						+ event.getSocketChannel().socket().getPort() + " "
						+ event.getResponse().getHeaders().returnStatusCode()
						+ " "
						+ event.getRequest().getHeaders().getHeader("host")
						+ event.getRequest().getHeaders().getRequestURI());
				e.setCanClose(true);

				final ClientDataEvent tellToClose = new ClientDataEvent(null,
						this.clientDataReceiver, null, event, null);
				this.clientDataReceiver.closeConnection(tellToClose);
			}
			e.setCanSend(event.canSend());

			// TODO: Aprolijar esto
			if (this.isEndOfRequest(clientEvent, event)) {
				this.logger.info("Reenviando RESPONSE: "
						+ event.getSocketChannel().socket().getPort() + " "
						+ event.getResponse().getHeaders().returnStatusCode()
						+ " "
						+ event.getRequest().getHeaders().getHeader("host")
						+ event.getRequest().getHeaders().getRequestURI());

				this.receivedRequests.remove(event);
				this.popEventForChannel(event.getSocketChannel());
			}

			this.logger.debug("Sending event to server: " + e);
		}

		if (this.eventsForChannel.get(event.getSocketChannel()) != null
				&& this.eventsForChannel.get(event.getSocketChannel()).size() == 0) {
			this.eventsForChannel.remove(event.getSocketChannel());
			this.receivedRequests.remove(event);
		}

		return e;
	}

	@Override
	protected DataEvent DoWork(final ServerDataEvent serverEvent)
			throws IOException {

		final HTTPRequestEventHandler handler = this.getRequestEventHandler();

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
							+ httpEvent.getSocketChannel().socket().getPort()
							+ " "
							+ httpEvent.getRequest().getHeaders().getMethod()
							+ " "
							+ httpEvent.getRequest().getHeaders()
									.getHeader("host")
							+ httpEvent.getRequest().getHeaders()
									.getRequestURI());

				}

				final HTTPProxyEvent ev = this.pollEventForChannel(httpEvent
						.getSocketChannel());

				if (ev != null) {
					ev.next = httpEvent;
				}

				if (httpEvent.getParseOffsetData() != null
						&& httpEvent.getParseOffsetData().array().length > 0) {

					final ServerDataEvent nextEvent = new ServerDataEvent(
							serverEvent.getChannel(), ByteBuffer.wrap(httpEvent
									.getParseOffsetData().array().clone()),
							this.serverDataReceiver);

					this.eventDispatcher.processDataUrgent(nextEvent);

				}
			}

			this.addEventForChannel(httpEvent.getSocketChannel(), httpEvent);

			e.setCanSend(httpEvent.canSend());
			this.logger.debug("Sending event to client:" + e);

		} else {
			e.setCanClose(false);
			e.setCanSend(false);

		}

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

		DataEvent answerDataEvent = null;
		switch (errorEvent.getErrorType()) {
		case ErrorDataEvent.PROXY_CLIENT_DISCONNECT:

			final ClientDataEvent e = new ClientDataEvent(null,
					this.clientDataReceiver, null, errorEvent.getAttachment(),
					null);

			e.setCanClose(true);
			e.setCanSend(false);
			answerDataEvent = e;

			break;
		case ErrorDataEvent.REMOTE_CLIENT_DISCONNECT:

			answerDataEvent = errorEvent;

			this.logger.debug("Handling error of remote client disconnect!");
			break;
		}

		return answerDataEvent;
	}

}
