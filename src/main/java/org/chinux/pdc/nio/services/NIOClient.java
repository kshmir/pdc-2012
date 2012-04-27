package org.chinux.pdc.nio.services;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import org.chinux.pdc.nio.handlers.api.NIOClientHandler;

public class NIOClient implements Runnable {

	private Selector selector;
	private NIOClientHandler handler;
	private int connectionPort;

	public NIOClient(final int connectionPort) throws IOException {
		selector = SelectorProvider.provider().openSelector();
		this.connectionPort = connectionPort;
	}

	public void setHandler(final NIOClientHandler handler) {
		this.handler = handler;
		this.handler.setConnectionPort(connectionPort);
		this.handler.setSelector(selector);
	}

	@Override
	public void run() {

		while (true) {
			try {
				// Process any pending changes
				handler.handlePendingChanges();
				// Wait for an event one of the registered channels
				selector.select();

				// Iterate over the set of keys for which events are available
				final Iterator<SelectionKey> selectedKeys = selector
						.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					final SelectionKey key = selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}
					// Check what event is available and deal with it
					if (key.isConnectable()) {
						handler.handleConnection(key);
					} else if (key.isReadable()) {
						handler.handleRead(key);
					} else if (key.isWritable()) {
						handler.handleWrite(key);
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
}
