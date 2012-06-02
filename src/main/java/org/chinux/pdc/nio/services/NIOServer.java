package org.chinux.pdc.nio.services;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.chinux.pdc.nio.handlers.api.NIOServerHandler;

public class NIOServer implements Runnable {

	private InetAddress host;
	private int port;
	private Selector selector;
	private Logger log = Logger.getLogger(this.getClass());

	private NIOServerHandler handler;

	public NIOServer(final int destPort) throws IOException {
		this.host = InetAddress.getByName("0.0.0.0");
		this.port = destPort;
		this.selector = this.initSelector(this.host, this.port);
	}

	private Selector initSelector(final InetAddress host, final int port)
			throws IOException {
		// Create a new selector
		final Selector socketSelector = SelectorProvider.provider()
				.openSelector();

		// Create a new non-blocking server socket channel
		final ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		final InetSocketAddress isa = new InetSocketAddress(host, port);
		serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}

	public void setHandler(final NIOServerHandler handler) {
		this.handler = handler;
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
					if (key.isAcceptable()) {
						this.log.debug("Accepting new key");
						this.handler.handleAccept(key);
					} else if (key.isReadable()) {
						this.log.debug("Reading from key");
						this.handler.handleRead(key);
					} else if (key.isWritable()) {
						this.log.debug("Writing to key");
						this.handler.handleWrite(key);
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
}
