package org.chinux.pdc.nio.handlers.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chinux.pdc.nio.events.api.DataReceiver;
import org.chinux.pdc.nio.events.impl.NIODataEvent;
import org.chinux.pdc.nio.handlers.api.NIOClientHandler;
import org.chinux.pdc.nio.handlers.util.SocketChannelFactory;
import org.chinux.pdc.nio.services.util.ChangeRequest;
import org.chinux.pdc.workers.Worker;

public class AsyncClientHandler implements NIOClientHandler,
		DataReceiver<NIODataEvent> {

	private Selector selector;
	private int externalPort;

	private ByteBuffer readBuffer;
	private Worker<NIODataEvent> worker;

	private Map<Object, ArrayList<ByteBuffer>> pendingData = new HashMap<Object, ArrayList<ByteBuffer>>();
	private List<ChangeRequest> changeRequests = new ArrayList<ChangeRequest>();
	private Map<Object, SocketChannel> clientIPMap = new HashMap<Object, SocketChannel>();

	private SocketChannelFactory channelFactory;

	public AsyncClientHandler(final Worker<NIODataEvent> worker,
			final SocketChannelFactory channelFactory) {
		this.worker = worker;
		this.channelFactory = channelFactory;
		readBuffer = ByteBuffer.allocate(1024);
	}

	@Override
	public void setSelector(final Selector selector) {
		this.selector = selector;
	}

	@Override
	public void setConnectionPort(final int externalPort) {
		this.externalPort = externalPort;
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

		final NIODataEvent event = new NIODataEvent(socketChannel, data, this);
		event.owner = key.attachment();
		worker.processData(event);
		key.attach(null);
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
	public void receiveEvent(final NIODataEvent event) {
		final InetAddress host = event.inetAddress;

		final InetSocketAddress socketHost = new InetSocketAddress(host,
				externalPort);

		SocketChannel socketChannel;
		synchronized (clientIPMap) {
			socketChannel = clientIPMap.get(event.owner);

			if (socketChannel == null) {
				try {
					socketChannel = channelFactory.getSocketChannel(socketHost);
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				clientIPMap.put(event.owner, socketChannel);

				// Queue a channel registration since the caller is not the
				// selecting thread. As part of the registration we'll register
				// an interest in connection events. These are raised when a
				// channel
				// is ready to complete connection establishment.
				synchronized (changeRequests) {
					changeRequests.add(new ChangeRequest(socketChannel,
							ChangeRequest.REGISTER, SelectionKey.OP_CONNECT,
							event.owner));
				}
			}
		}

		final byte[] data = event.data;

		synchronized (pendingData) {
			ArrayList<ByteBuffer> queue = pendingData.get(event.owner);
			if (queue == null) {
				queue = new ArrayList<ByteBuffer>();
				pendingData.put(event.owner, queue);
			}
			queue.add(ByteBuffer.wrap(data));
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		selector.wakeup();
	}

	@Override
	public void closeConnection(final NIODataEvent dataEvent) {
		// TODO
	}

	@Override
	public void handlePendingChanges() {
		synchronized (changeRequests) {
			if (!changeRequests.isEmpty()) {
				final ChangeRequest change = changeRequests.remove(0);

				SelectionKey key;
				switch (change.type) {
				case ChangeRequest.CHANGEOPS:
					key = change.socket.keyFor(selector);
					key.interestOps(change.ops);
					break;
				case ChangeRequest.REGISTER:
					try {
						key = change.socket.register(selector, change.ops);
						key.attach(change.attachment);
					} catch (final ClosedChannelException e) {
						e.printStackTrace(); // TODO: Handle this
					}
					break;
				}
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
}
