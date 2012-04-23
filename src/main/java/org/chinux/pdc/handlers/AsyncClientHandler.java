package org.chinux.pdc.handlers;

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.chinux.pdc.ChangeRequest;
import org.chinux.pdc.NIODataEvent;
import org.chinux.pdc.NIOServerEvent;
import org.chinux.pdc.Worker;

public class AsyncClientHandler implements TCPHandler {

	private InetAddress host;
	private ByteBuffer readBuffer;
	private Selector selector;
	private int externalPort;
	private int port;
	private Worker worker;

	private Map<SocketChannel, ArrayList<ByteBuffer>> pendingData = new HashMap<SocketChannel, ArrayList<ByteBuffer>>();
	private List<ChangeRequest> changeRequests = new LinkedList<ChangeRequest>();

	public AsyncClientHandler(final Selector selector, final int externalPort,
			final int port) {
		this.selector = selector;
		this.externalPort = externalPort;
		this.port = port;
		this.readBuffer = ByteBuffer.allocate(1024);
	}

	void setWorker(Worker worker){
		this.worker = worker;
	}
	
	@Override
	public void handleAccept(final SelectionKey key) throws IOException {
		System.out.println("not used");
	}

	@Override
	public void handleRead(final SelectionKey key) throws IOException {
		final SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		this.readBuffer.clear();

		// No se porqué falla con esto
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

		this.worker.processData(new NIOServerEvent(socketChannel, data));
	}

	@Override
	public void handleWrite(final SelectionKey key) throws IOException {
		final SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (this.pendingData) {
			final ArrayList<ByteBuffer> queue = this.pendingData
					.get(socketChannel);

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
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	@Override
	public void sendAnswer(final NIODataEvent event) {
		// get the ip from socket1
		this.host = event.socket.socket().getInetAddress();
		// create the new socket for communication with the external server
		SocketChannel socket2 = null;
		try {
			socket2 = SocketChannel.open();
			socket2.configureBlocking(false);
			socket2.connect(new InetSocketAddress(this.host, this.externalPort));
		} catch (final IOException e) {
			e.printStackTrace();
		}

		// final SocketChannel socket1 = event.socket;
		final byte[] data = event.data;

		synchronized (this.changeRequests) {
			// Indicate we want the interest ops set changed
			this.changeRequests.add(new ChangeRequest(socket2,
					ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			// And queue the data we want written
			synchronized (this.pendingData) {
				ArrayList<ByteBuffer> queue = this.pendingData.get(socket2);
				if (queue == null) {
					queue = new ArrayList<ByteBuffer>();
					this.pendingData.put(socket2, queue);
				}
				queue.add(ByteBuffer.wrap(data));
			}
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		this.selector.wakeup();
	}

	@Override
	public void closeConnection(final NIODataEvent event) {
		synchronized (this.changeRequests) {
			// Indicate we want the interest ops set changed
			this.changeRequests.add(new ChangeRequest(event.socket,
					ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
		}
	}

	@Override
	public void handlePendingChanges() {
		synchronized (this.changeRequests) {
			final Iterator<ChangeRequest> changes = this.changeRequests
					.iterator();
			while (changes.hasNext()) {
				final ChangeRequest change = changes.next();
				SelectionKey key;
				switch (change.type) {
				case ChangeRequest.CHANGEOPS:
					key = change.socket.keyFor(this.selector);
					key.interestOps(change.ops);
					break;
				case ChangeRequest.REGISTER:
					try {
						change.socket.register(this.selector, change.ops);
					} catch (final ClosedChannelException e) {
						e.printStackTrace();
					}
					break;
				}
			}
			this.changeRequests.clear();
		}
	}

	@Override
	public void finishConnection(final SelectionKey key) throws IOException {
		final SocketChannel socketChannel = (SocketChannel) key.channel();

		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			socketChannel.finishConnect();
		} catch (final IOException e) {
			// Cancel the channel's registration with our selector
			System.out.println(e);
			key.cancel();
			return;
		}

		// Register an interest in writing on this channel
		key.interestOps(SelectionKey.OP_WRITE);
	}

	@Override
	public SocketChannel initiateConnection() throws IOException {
		// Create a non-blocking socket channel
		final SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);

		// Kick off connection establishment
		socketChannel.connect(new InetSocketAddress(this.host, this.port));

		// Queue a channel registration since the caller is not the
		// selecting thread. As part of the registration we'll register
		// an interest in connection events. These are raised when a channel
		// is ready to complete connection establishment.
		synchronized (this.changeRequests) {
			this.changeRequests.add(new ChangeRequest(socketChannel,
					ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
		}

		return socketChannel;
	}

}
