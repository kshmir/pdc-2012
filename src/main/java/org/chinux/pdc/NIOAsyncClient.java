package org.chinux.pdc;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import org.chinux.pdc.events.NIODataEvent;
import org.chinux.pdc.handlers.AsyncClientHandler;
import org.chinux.pdc.handlers.TCPHandler;
import org.chinux.pdc.workers.Worker;

public class NIOAsyncClient implements Runnable {

	private Selector selector;
	TCPHandler handler;

	private Worker<NIODataEvent> worker;

	public static void main(final String[] args) throws UnknownHostException,
			IOException {
		try {
			final NIOAsyncClient client = new NIOAsyncClient(80,
					new ClientSelectorFactory());
			final Thread t = new Thread(client);
			t.setDaemon(true);
			t.start();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public NIOAsyncClient(final int destPort,
			final ClientSelectorFactory selfactory) throws IOException {
		this.selector = selfactory.getSelector();
		this.handler = new AsyncClientHandler(this.selector, 8080, destPort);
	}

	public void setWorker(final Worker<NIODataEvent> worker) {
		this.worker = worker;
	}

	@Override
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
						this.handler.finishConnection(key);
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
