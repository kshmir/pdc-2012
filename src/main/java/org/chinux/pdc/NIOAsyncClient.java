package org.chinux.pdc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NIOAsyncClient implements DataForwarder<NIODataEvent>,
		DataReceiver<NIODataEvent> {

	private InetAddress host;
	private int port;
	private int externalPort;
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private ByteBuffer readBuffer;

	// A list of ChangeRequest instances
	private List<ChangeRequest> changeRequests = new LinkedList<ChangeRequest>();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map<SocketChannel, ArrayList<ByteBuffer>> pendingData = new HashMap<SocketChannel, ArrayList<ByteBuffer>>();
	private Worker<NIODataEvent> worker;

	public NIOAsyncClient(final int destPort) throws IOException {
		this.host = InetAddress.getByName("localhost");
		this.port = destPort;
		this.externalPort = 8080;
		this.selector = this.initSelector();
		this.readBuffer = ByteBuffer.allocate(1024);
	}

	public void setWorker(final Worker<NIODataEvent> worker) {
		this.worker = worker;
	}

	// TODO: See if we can extract this.
	private Selector initSelector() throws IOException {
		// Create a new selector
		final Selector socketSelector = SelectorProvider.provider()
				.openSelector();

		// Create a new non-blocking server socket channel
		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		final InetSocketAddress isa = new InetSocketAddress(this.host,
				this.port);
		this.serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in
		// accepting new connections
		this.serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}

	// TODO: Extract this into an interface
	private void accept(final SelectionKey key) throws IOException {
		// For an accept to be pending the channel must be a server socket
		// channel.
		final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
				.channel();

		// Accept the connection and make it non-blocking
		final SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating
		// we'd like to be notified when there's data waiting to be read
		socketChannel.register(this.selector, SelectionKey.OP_READ);
	}

	// TODO: Make this use the interface
	public void run() {

		// TODO: Make this better, can we?
		new Thread(this.worker).start();

		while (true) {
			try {

				// Process any pending changes
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
						case ChangeRequest.CLOSE:
							key = change.socket.keyFor(this.selector);
							change.socket.close();
							key.cancel();
							break;
						case ChangeRequest.REGISTER:
							change.socket.register(this.selector, change.ops);
							break;
						}
					}
					this.changeRequests.clear();
				}

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
						this.accept(key);
					} else if (key.isReadable()) {
						this.read(key);
					} else if (key.isWritable()) {
						this.write(key);
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	// TODO: Extract this into an interface
	private void write(final SelectionKey key) throws IOException {
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

	// TODO: Extract this into an interface
	private void read(final SelectionKey key) throws IOException {
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

		this.worker.processData(new NIODataEvent(socketChannel, data));
	}

	public static void main(final String[] args) throws UnknownHostException,
			IOException {
		int inPort;

		if (args.length == 1) {
			inPort = Integer.valueOf(args[0]);
		} else {
			inPort = 8080;
		}

		final NIOServer server = new NIOServer(inPort);

		final Worker<NIODataEvent> worker = new EchoWorker(server);

		server.setWorker(worker);

		server.run();
	}

	// TODO: Make this in a subclass
	@Override
	public void sendAnswer(final NIODataEvent event) {
		final SocketChannel socket = event.socket;
		final byte[] data = event.data;

		synchronized (this.changeRequests) {
			// Indicate we want the interest ops set changed
			this.changeRequests.add(new ChangeRequest(socket,
					ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			// And queue the data we want written
			synchronized (this.pendingData) {
				ArrayList<ByteBuffer> queue = this.pendingData.get(socket);
				if (queue == null) {
					queue = new ArrayList<ByteBuffer>();
					this.pendingData.put(socket, queue);
				}
				queue.add(ByteBuffer.wrap(data));
			}
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		this.selector.wakeup();
	}

	@Override
	public void sendForward(final NIODataEvent event) {
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

	// TODO: Make this in a subclass
	@Override
	public void closeConnection(final NIODataEvent event) {
		synchronized (this.changeRequests) {
			// Indicate we want the interest ops set changed
			this.changeRequests.add(new ChangeRequest(event.socket,
					ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
		}
	}

	private SocketChannel initiateConnection() throws IOException {
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

	private void finishConnection(final SelectionKey key) throws IOException {
		final SocketChannel socketChannel = (SocketChannel) key.channel();

		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			socketChannel.finishConnection();
		} catch (final IOException e) {
			// Cancel the channel's registration with our selector
			key.cancel();
			return;
		}

		// Register an interest in writing on this channel
		key.interestOps(SelectionKey.OP_WRITE);
	}
}
