package org.chinux.pdc.workers.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.nio.receivers.api.DataReceiver;

public class HTTPProxyWorker extends HTTPBaseProxyWorker {

	private static Charset isoCharset = Charset.forName("ISO-8859-1");
	private Logger logger = Logger.getLogger(this.getClass());
	private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

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

	@Override
	protected DataEvent DoWork(final ClientDataEvent clientEvent)
			throws IOException {
		final HTTPProxyEvent event = (HTTPProxyEvent) clientEvent.getOwner();

		final HTTPResponseEventHandler eventHandler = new HTTPResponseEventHandler(
				event);

		eventHandler.handle(this.outputBuffer, clientEvent);

		final DataEvent e = new ServerDataEvent(event.getSocketChannel(),
				ByteBuffer.wrap(this.outputBuffer.toByteArray().clone()),
				this.serverDataReceiver);

		e.setCanClose(event.canClose());
		e.setCanSend(event.canSend());

		this.logger.debug(clientEvent.getData());

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
				(httpEvent != null) ? httpEvent.getAddress() : null, httpEvent);

		// If the client doesn't send back this HTTPEvent, we must expire it
		// later on.
		if (!this.receivedRequests.contains(httpEvent)) {
			this.receivedRequests.add(httpEvent);
		}

		if (httpEvent != null) {
			e.setCanClose(httpEvent.canClose());
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

}
