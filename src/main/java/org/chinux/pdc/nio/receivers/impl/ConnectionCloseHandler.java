package org.chinux.pdc.nio.receivers.impl;

import java.nio.channels.SocketChannel;

public interface ConnectionCloseHandler {
	public void handleConnectionClose(SocketChannel socket);
}
