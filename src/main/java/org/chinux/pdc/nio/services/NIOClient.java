package org.chinux.pdc.nio.services;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.chinux.pdc.nio.handlers.api.NIOClientHandler;

public class NIOClient implements Runnable {

	private Selector selector;
	private NIOClientHandler handler;
	private int connectionPort;
	private Logger log = Logger.getLogger(this.getClass());

	public NIOClient(final int connectionPort) throws IOException {
		this.selector = SelectorProvider.provider().openSelector();
		this.connectionPort = connectionPort;
	}

	public void setHandler(final NIOClientHandler handler) {
		this.handler = handler;
		this.handler.setConnectionPort(this.connectionPort);
		this.handler.setSelector(this.selector);
	}

	@Override
	public void run() {
		while (true) {
			try {
				// Process any pending changes
				final boolean hasChanges = this.handler.handlePendingChanges();

				// Wait for an event one of the registered channels
				if (!hasChanges) {
					this.selector.select();
				} else {
					// System.out.println("Selection non block client");
					this.selector.selectNow();
				}

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
					if (key.isConnectable()) {
						this.log.debug("Connecting with new key");
						this.handler.handleConnection(key);
					} else if (key.isReadable()) {
						this.log.debug("Reading key");
						this.handler.handleRead(key);
					} else if (key.isWritable()) {
						this.log.debug("Writing key");
						this.handler.handleWrite(key);
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
}
