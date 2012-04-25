package org.chinux.pdc.nio.handlers.api;

import java.nio.channels.SelectionKey;

public interface NIOClientHandler extends NIOHandler {
	public void setConnectionPort(final int port);

	public void handleConnection(SelectionKey key);
}
