package org.chinux.pdc;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import org.chinux.pdc.handlers.TCPHandler;

public class NIOAsyncClient implements Runnable {

	private Selector selector;
	private TCPHandler handler;
	private int connectionPort;

	public NIOAsyncClient(final int connectionPort) throws IOException {
		this.selector = this.initSelector();
		this.connectionPort = connectionPort;
	}

	private void setHandler(final TCPHandler handler) {
		this.handler = handler;
		this.handler.setConnectionPort(this.connectionPort);
	}

	private Selector initSelector() throws IOException {
		return SelectorProvider.provider().openSelector();
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
