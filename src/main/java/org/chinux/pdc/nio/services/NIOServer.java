package org.chinux.pdc.nio.services;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import org.chinux.pdc.nio.handlers.api.NIOServerHandler;
import org.chinux.pdc.nio.services.util.ServerSelectorFactory;

public class NIOServer implements Runnable {

	private InetAddress host;
	private int port;
	private ServerSocketChannel serverChannel;
	private Selector selector;

	private NIOServerHandler handler;

	public NIOServer(final int destPort, final ServerSelectorFactory selfactory)
			throws IOException {

		host = InetAddress.getLocalHost();
		port = destPort;
		selector = selfactory.getSelector(host, port, serverChannel);

	}

	public void setHandler(final NIOServerHandler handler) {
		this.handler = handler;
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
					if (key.isAcceptable()) {
						handler.handleAccept(key);
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
