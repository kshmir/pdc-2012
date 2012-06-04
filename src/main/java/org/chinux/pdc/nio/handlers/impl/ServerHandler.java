package org.chinux.pdc.nio.handlers.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.chinux.pdc.nio.dispatchers.EventDispatcher;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ErrorDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.nio.handlers.api.NIOServerHandler;
import org.chinux.pdc.nio.receivers.api.DataReceiver;
import org.chinux.pdc.nio.services.util.ChangeRequest;
import org.chinux.pdc.nio.util.NIOUtil;

public class ServerHandler implements NIOServerHandler, DataReceiver<DataEvent> {

	private ByteBuffer readBuffer;
	private Selector selector;

	private List<ChangeRequest> changeRequests = new LinkedList<ChangeRequest>();

	private Map<SocketChannel, ArrayList<ByteBuffer>> pendingData = new HashMap<SocketChannel, ArrayList<ByteBuffer>>();

	private EventDispatcher<DataEvent> dispatcher;

	public ServerHandler(final EventDispatcher<DataEvent> dispatcher) {
		this.dispatcher = dispatcher;
		this.readBuffer = ByteBuffer.allocate(1480);
	}

	@Override
	public void setSelector(final Selector selector) {
		this.selector = selector;
	}

	@Override
	public void receiveEvent(final DataEvent dataEvent) {

		if (!(dataEvent instanceof ServerDataEvent)) {
			throw new RuntimeException("Must receive a NIOServerDataEvent");
		}

		final ServerDataEvent event = (ServerDataEvent) dataEvent;

		final SocketChannel socket = event.getChannel();
		final ByteBuffer data = event.getData();

		synchronized (this.changeRequests) {
			// Indicate we want the interest ops set changed

			// And queue the data we want written
			synchronized (this.pendingData) {
				ArrayList<ByteBuffer> queue = this.pendingData.get(socket);
				if (queue == null) {
					queue = new ArrayList<ByteBuffer>();
					this.pendingData.put(socket, queue);
				}
				queue.add(data);
			}

			this.changeRequests.add(new ChangeRequest(socket,
					ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		this.selector.wakeup();
	}

	@Override
	public void closeConnection(final DataEvent dataEvent) {

		if (dataEvent instanceof ErrorDataEvent) {

			final ErrorDataEvent errorEvent = (ErrorDataEvent) dataEvent;
			synchronized (this.changeRequests) {
				this.changeRequests.add(new ChangeRequest(
						(SocketChannel) errorEvent.getOwner(),
						ChangeRequest.CLOSE, 0));
			}
			this.selector.wakeup();
			return;
		}

		if (!(dataEvent instanceof ServerDataEvent)) {
			throw new RuntimeException("Must receive a NIOServerDataEvent");
		}

		final ServerDataEvent event = (ServerDataEvent) dataEvent;

		synchronized (this.changeRequests) {
			// Indicate we want the interest ops set changed
			this.changeRequests.add(new ChangeRequest(event.getChannel(),
					ChangeRequest.CLOSE, 0, event.getChannel()));
		}
		this.selector.wakeup();
	}

	@Override
	public void handleAccept(final SelectionKey key) {
		// For an accept to be pending the channel must be a server socket
		// channel.
		final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
				.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = null;
		try {
			socketChannel = serverSocketChannel.accept();
			socketChannel.configureBlocking(false);

			// Register the new SocketChannel with our Selector, indicating
			// we'd like to be notified when there's data waiting to be read
			socketChannel.register(this.selector, SelectionKey.OP_READ);
		} catch (final IOException e) {
			if (socketChannel != null) {
				this.handleUnexpectedDisconnect(key);
			}
		}

	}

	@Override
	public void handleRead(final SelectionKey key) {

		final SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		this.readBuffer.clear();

		// No se porqué falla con esto
		final ByteBuffer readBuffer = ByteBuffer.allocate(1024);

		// Attempt to read off the channel
		int numRead = 0;
		try {
			numRead = socketChannel.read(readBuffer);
		} catch (final IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			try {
				socketChannel.close();
			} catch (final IOException e1) {
			}

			this.handleUnexpectedDisconnect(key);
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			this.handleUnexpectedDisconnect(key);
			key.cancel();
			// this.handleUnexpectedDisconnect(socketChannel);
		}
		// Hand the data off to our worker thread
		final ByteBuffer data = NIOUtil.readBuffer(readBuffer, numRead);

		this.dispatcher.processData(new ServerDataEvent(socketChannel, data,
				this));
	}

	@Override
	public void handleWrite(final SelectionKey key) {
		final SocketChannel socketChannel = (SocketChannel) key.channel();

		try {

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

					// if (queue == null) {
					// throw new RuntimeException("JO!");
					// }
					// We wrote away all data, so we're no longer interested
					// in writing on this socket. Switch back to waiting for
					// data.
					key.interestOps(SelectionKey.OP_READ);
				}
			}
		} catch (final IOException e) {
			this.handleUnexpectedDisconnect(key);
		}
	}

	@Override
	public boolean handlePendingChanges() {
		try {
			synchronized (this.changeRequests) {
				if (this.changeRequests.size() == 0) {
					return false;
				}
				final ChangeRequest change = this.changeRequests.remove(0);
				SelectionKey key;
				switch (change.type) {
				case ChangeRequest.CHANGEOPS:
					try {
						key = change.socket.keyFor(this.selector);

						if (key == null) {
							throw new ClientDisconnectException(change);
						} else {
							key.interestOps(change.ops);
						}

					} catch (final ClientDisconnectException e) {
						throw e;
					} catch (final CancelledKeyException e) {
						throw new ClientDisconnectException(change);
					} catch (final Exception e) {
						e.printStackTrace();
					}
					break;
				case ChangeRequest.CLOSE:
					if (change.socket.isOpen()
							&& this.pendingData.get(change.socket) != null
							&& this.pendingData.get(change.socket).size() > 0) {
						this.changeRequests.add(change);
						return true;
					}
					key = change.socket.keyFor(this.selector);
					try {
						change.socket.close();
					} catch (final IOException e) {
						e.printStackTrace();
					}
					if (key != null) {
						key.cancel();
					}
					break;
				}
			}
		} catch (final ClientDisconnectException e) {
			this.handleUnexpectedDisconnect(e.getChange());
		}
		return this.changeRequests.size() > 0;
	}

	private void handleUnexpectedDisconnect(final ChangeRequest change) {
		try {
			if (change.socket != null) {
				change.socket.close();
			}
		} catch (final Exception e) {

		}

		this.dispatcher.processData(new ErrorDataEvent(
				ErrorDataEvent.PROXY_CLIENT_DISCONNECT, change.socket, null));
	}

	@Override
	public void handleUnexpectedDisconnect(final SelectionKey key) {
		try {
			key.cancel();
			key.channel().close();
		} catch (final Exception e) {

		}

		this.dispatcher.processData(new ErrorDataEvent(
				ErrorDataEvent.PROXY_CLIENT_DISCONNECT, key.channel(), null));
	}
}
