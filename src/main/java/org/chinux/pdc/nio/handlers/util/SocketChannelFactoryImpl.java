package org.chinux.pdc.nio.handlers.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class SocketChannelFactoryImpl implements SocketChannelFactory {

	public SocketChannelFactoryImpl() {

	}

	@Override
	public SocketChannel getSocketChannel(final InetSocketAddress address)
			throws IOException {
		final SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.connect(address);

		return socketChannel;
	}
}
