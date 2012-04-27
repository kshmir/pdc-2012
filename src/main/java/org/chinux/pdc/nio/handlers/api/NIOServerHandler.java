package org.chinux.pdc.nio.handlers.api;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface NIOServerHandler extends NIOHandler {
	public void handleAccept(SelectionKey key) throws IOException;
}
