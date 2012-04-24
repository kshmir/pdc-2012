package org.chinux.pdc.nio.handlers.api;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.api.DataReceiver;

public interface NIOHandler extends DataReceiver<DataEvent> {

	public void setSelector(final Selector selector);

	public void setConnectionPort(final int port);

	public void handleAccept(SelectionKey key) throws IOException;

	public void handleRead(SelectionKey key) throws IOException;

	public void handleWrite(SelectionKey key) throws IOException;

	public void handlePendingChanges();
}
