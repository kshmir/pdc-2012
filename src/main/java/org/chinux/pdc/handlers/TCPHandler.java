package org.chinux.pdc.handlers;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import org.chinux.pdc.events.DataEvent;

public interface TCPHandler extends DataReceiver<DataEvent> {

	public void setSelector(final Selector selector);

	public void setConnectionPort(final int port);

	public void handleAccept(SelectionKey key) throws IOException;

	public void handleRead(SelectionKey key) throws IOException;

	public void handleWrite(SelectionKey key) throws IOException;

	public void handlePendingChanges();
}
