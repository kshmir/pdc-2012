package org.chinux.pdc.nio.handlers.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Map;

import org.chinux.pdc.nio.dispatchers.EventDispatcher;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.handlers.api.NIOClientHandler;
import org.chinux.pdc.nio.receivers.api.ClientDataReceiver;
import org.chinux.pdc.nio.receivers.impl.ASyncClientDataReceiver;

public class ClientHandler implements NIOClientHandler {

	private ByteBuffer readBuffer;
	private EventDispatcher<DataEvent> dispatcher;
	private ClientDataReceiver receiver;
	private Map<Object, ArrayList<ByteBuffer>> pendingData;

	public ClientHandler(final EventDispatcher<DataEvent> dispatcher) {
		this(dispatcher, null);
	}

	public ClientHandler(final EventDispatcher<DataEvent> dispatcher,
			ClientDataReceiver receiver) {
		if (receiver == null) {
			receiver = new ASyncClientDataReceiver();
		}
		this.dispatcher = dispatcher;
		this.receiver = receiver;
		readBuffer = ByteBuffer.allocate(1024);
		pendingData = receiver.getPendingData();
	}

	@Override
	public void setSelector(final Selector selector) {
		receiver.setSelector(selector);
	}

	@Override
	public void setConnectionPort(final int externalPort) {
		receiver.setConnectionPort(externalPort);
	}

	@Override
	public void handleRead(final SelectionKey key) throws IOException {
		final SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		readBuffer.clear();

		final ByteBuffer readBuffer = ByteBuffer.allocate(1024);

		// Attempt to read off the channel
		int numRead;
		try {
			numRead = socketChannel.read(readBuffer);
		} catch (final IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			return;
		}
		// Hand the data off to our worker thread
		final byte[] data = (numRead > 0) ? readBuffer.array() : new byte[] {};

		dispatcher.processData(new ClientDataEvent(data, key.attachment()));
	}

	@Override
	public void handleWrite(final SelectionKey key) throws IOException {
		final SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (pendingData) {
			final ArrayList<ByteBuffer> queue = pendingData.get(key
					.attachment());

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				final ByteBuffer buf = queue.get(0);
				socketChannel.write(buf);
				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	@Override
	public void handleConnection(final SelectionKey key) {

		final SocketChannel socketChannel = (SocketChannel) key.channel();

		try {
			socketChannel.finishConnect();
		} catch (final IOException e) {
			e.printStackTrace(); // TODO: Handle this
		}

		// Register an interest in writing on this channel
		key.interestOps(SelectionKey.OP_WRITE);
	}

	@Override
	public void handlePendingChanges() throws ClosedChannelException {
		receiver.handlePendingChanges();
	}

}
