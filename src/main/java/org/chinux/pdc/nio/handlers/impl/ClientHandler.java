package org.chinux.pdc.nio.handlers.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.chinux.pdc.nio.dispatchers.EventDispatcher;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.handlers.api.NIOClientHandler;
import org.chinux.pdc.nio.receivers.api.ClientDataReceiver;
import org.chinux.pdc.nio.receivers.impl.ASyncClientDataReceiver;
import org.chinux.pdc.nio.util.NIOUtil;

public class ClientHandler implements NIOClientHandler {

	private static Logger log = Logger.getLogger(ClientHandler.class);
	private ByteBuffer readBuffer;
	private EventDispatcher<DataEvent> dispatcher;
	private ClientDataReceiver receiver;
	private Map<Object, ArrayList<ByteBuffer>> pendingData;

	public ClientHandler(final EventDispatcher<DataEvent> dispatcher) {
		this(dispatcher, new ASyncClientDataReceiver());
	}

	public ClientHandler(final EventDispatcher<DataEvent> dispatcher,
			final ClientDataReceiver receiver) {

		this.setReceiver(receiver);
		this.dispatcher = dispatcher;
		this.readBuffer = ByteBuffer.allocate(1024);
		this.pendingData = this.receiver.getPendingData();
	}

	public void setReceiver(final ClientDataReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void setSelector(final Selector selector) {
		this.receiver.setSelector(selector);
	}

	@Override
	public void setConnectionPort(final int externalPort) {
		this.receiver.setConnectionPort(externalPort);
	}

	@Override
	public void handleRead(final SelectionKey key) throws IOException {
		final SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		this.readBuffer.clear();

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
			this.handleUnexpectedDisconnect(key);
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.

			// TODO: Analizar qué se hace en este caso.
			key.channel().close();
			key.cancel();
			// this.handleDisconnect(socketChannel);
		}

		// Hand the data off to our worker thread
		final ByteBuffer data = NIOUtil.readBuffer(readBuffer, numRead);

		final ClientDataEvent event = new ClientDataEvent(data,
				key.attachment());

		if (numRead == -1) {
			event.setCanClose(true);
		}
		this.dispatcher.processData(event);
	}

	@Override
	public void handleWrite(final SelectionKey key) throws IOException {
		final SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (this.pendingData) {
			final ArrayList<ByteBuffer> queue = this.pendingData.get(key
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
			// Register an interest in writing on this channel
			key.interestOps(SelectionKey.OP_WRITE);
		} catch (final IOException e) {
			this.handleUnexpectedDisconnect(key);
		}
	}

	@Override
	public void handlePendingChanges() throws ClosedChannelException {
		this.receiver.handlePendingChanges();
	}

	@Override
	public void handleUnexpectedDisconnect(final SelectionKey key) {
		// Clear buffers and stuff
		// this.dispatcher.processData(new ErrorDataEvent(
		// ErrorDataEvent.REMOTE_CLIENT_DISCONNECT, key.channel(), key
		// .attachment()));
	}

}
