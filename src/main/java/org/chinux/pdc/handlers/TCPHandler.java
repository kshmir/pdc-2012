package org.chinux.pdc.handlers;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.chinux.pdc.events.NIODataEvent;

public interface TCPHandler extends DataReceiver<NIODataEvent> {

	void handleAccept(SelectionKey key) throws IOException;

	void handleRead(SelectionKey key) throws IOException;

	void handleWrite(SelectionKey key) throws IOException;

	void handlePendingChanges();

	void finishConnection(final SelectionKey key) throws IOException;

	SocketChannel initiateConnection() throws IOException;

}
