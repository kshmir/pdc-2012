package org.chinux.pdc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import org.chinux.pdc.handlers.TCPHandler;

// TODO: Check all the TODO's
public class NIOServer implements Runnable {

	private InetAddress host;
	private int port;
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private ByteBuffer readBuffer;

	private TCPHandler handler;

	public NIOServer(final int destPort) throws IOException {
		this.host = InetAddress.getByName("localhost");
		this.port = destPort;
		this.selector = this.initSelector();
		this.readBuffer = ByteBuffer.allocate(1024);

	}

	public void setHandler(final TCPHandler handler) {
		this.handler = handler;
		this.handler.setConnectionPort(this.port);
		this.handler.setSelector(this.selector);
	}

	// TODO: See if we can extract this.
	private Selector initSelector() throws IOException {
		// Create a new selector
		final Selector socketSelector = SelectorProvider.provider()
				.openSelector();

		// Create a new non-blocking server socket channel
		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		final InetSocketAddress isa = new InetSocketAddress(this.host,
				this.port);
		this.serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in
		// accepting new connections
		this.serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}

	// TODO: Make this use the interface
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
