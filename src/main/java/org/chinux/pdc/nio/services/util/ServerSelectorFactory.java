package org.chinux.pdc.nio.services.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;

public class ServerSelectorFactory {

	public ServerSelectorFactory() {

	}

	public Selector getSelector(final InetAddress host, final int port,
			final ServerSocketChannel serverChannel) throws IOException {
		return this.initSelector(host, port, serverChannel);
	}

	private Selector initSelector(final InetAddress host, final int port,
			ServerSocketChannel serverChannel) throws IOException {
		// Create a new selector
		final Selector socketSelector = SelectorProvider.provider()
				.openSelector();

		// Create a new non-blocking server socket channel
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		final InetSocketAddress isa = new InetSocketAddress(host, port);
		serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}
}
