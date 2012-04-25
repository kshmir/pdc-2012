package org.chinux.pdc.nio.handlers.api;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public interface NIOHandler {

	public void setSelector(final Selector selector);

	public void handleRead(SelectionKey key) throws IOException;

	public void handleWrite(SelectionKey key) throws IOException;

	public void handlePendingChanges();
}
