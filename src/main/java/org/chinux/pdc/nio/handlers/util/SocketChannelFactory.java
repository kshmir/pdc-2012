package org.chinux.pdc.nio.handlers.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public interface SocketChannelFactory {

	public SocketChannel getSocketChannel(final InetSocketAddress address)
			throws IOException;

}