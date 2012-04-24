package org.chinux.pdc.nio.services;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import org.chinux.pdc.nio.handlers.api.NIOHandler;
import org.chinux.pdc.nio.services.util.ServerSelectorFactory;

// TODO: Check all the TODO's
public class NIOServer implements Runnable {

	private InetAddress host;
	private int port;
	private ServerSocketChannel serverChannel;
	private Selector selector;

	private NIOHandler handler;

	public NIOServer(final int destPort, final ServerSelectorFactory selfactory)
			throws IOException {

		this.host = InetAddress.getByName("localhost");
		this.port = destPort;
		this.selector = selfactory.getSelector(this.host, this.port,
				this.serverChannel);

	}

	public void setHandler(final NIOHandler handler) {
		this.handler = handler;
		this.handler.setConnectionPort(this.port);
		this.handler.setSelector(this.selector);
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
