package org.chinux.pdc;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import org.chinux.pdc.events.NIODataEvent;
import org.chinux.pdc.handlers.ServerHandler;
import org.chinux.pdc.handlers.TCPHandler;
import org.chinux.pdc.workers.Worker;

// TODO: Check all the TODO's
public class NIOServer {

	private InetAddress host;
	private int port;
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private ByteBuffer readBuffer;

	TCPHandler handler;

	private Worker<NIODataEvent> worker;

	public NIOServer(final int destPort, final ServerSelectorFactory selfactory)
			throws IOException {

		this.host = InetAddress.getByName("localhost");
		this.port = destPort;
		this.selector = selfactory.getSelector(this.host, this.port,
				this.serverChannel);
		this.readBuffer = ByteBuffer.allocate(1024);
		this.handler = new ServerHandler(this.selector, 8080, destPort);

	}

	public void setWorker(final Worker<NIODataEvent> worker) {
		this.worker = worker;
	}

	// TODO: Make this use the interface
	public void run() {

		while (true) {
			try {

				// Process any pending changes
				this.handler.handlePendingChanges();

				// Wait for an event one of the registered channels
				this.selector.select();

				// Iterate over the set of keys for which events are available
				final Iterator<SelectionKey> selectedKeys = this.selector
						.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					final SelectionKey key = selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isAcceptable()) {
						this.handler.handleAccept(key);
					} else if (key.isReadable()) {
						this.handler.handleRead(key);
					} else if (key.isWritable()) {
						this.handler.handleWrite(key);
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

}
