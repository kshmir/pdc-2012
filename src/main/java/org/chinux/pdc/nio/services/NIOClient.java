package org.chinux.pdc.nio.services;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import org.chinux.pdc.nio.handlers.api.NIOHandler;
import org.chinux.pdc.nio.services.util.ClientSelectorFactory;

public class NIOClient implements Runnable {

	private Selector selector;
	private NIOHandler handler;
	private int connectionPort;

	public NIOClient(final int connectionPort,
			final ClientSelectorFactory selfactory) throws IOException {
		this.selector = selfactory.getSelector();
		this.connectionPort = connectionPort;
	}

	public void setHandler(final NIOHandler handler) {
		this.handler = handler;
		this.handler.setConnectionPort(this.connectionPort);
	}

	@Override
	public void run() {

		while (true) {
			try {
				// Process any pending changes
				this.handler.handlePendingChanges();
				// Wait for an event one of the registered channels
				this.selector.select();

				// Iterate over the set of keys for which events are available
				final Iterator<SelectionKey> selectedKeys = this.selector
						.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					final SelectionKey key = selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}
					// Check what event is available and deal with it
					if (key.isAcceptable()) {
						this.handler.handleAccept(key);
					} else if (key.isReadable()) {
						this.handler.handleRead(key);
					} else if (key.isWritable()) {
						this.handler.handleWrite(key);
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

}
