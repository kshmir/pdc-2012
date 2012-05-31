package org.chinux.pdc.nio.receivers.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.receivers.api.ClientDataReceiver;
import org.chinux.pdc.nio.services.util.ChangeRequest;

public class ASyncClientDataReceiver extends ClientDataReceiver implements
		ConnectionCloseHandler {

	private TimeoutablePool pool = new TimeoutablePool(30);

	@Override
	public void receiveEvent(final DataEvent dataEvent) {

		this.log.debug("Receiving data event " + dataEvent);

		if (!(dataEvent instanceof ClientDataEvent)) {
			throw new RuntimeException("Must receive a NIOClientDataEvent!");
		}

		final ClientDataEvent event = (ClientDataEvent) dataEvent;

		final InetAddress host = event.getAddress();

		final InetSocketAddress socketHost = new InetSocketAddress(host,
				this.connectionPort);

		SocketChannel socketChannel;
		synchronized (this.clientIPMap) {
			socketChannel = this.clientIPMap.get(event.getAttachment());

			if (socketChannel == null) {

//				socketChannel = this.pool.getObject(socketHost);

				if (socketChannel == null) {

					try {
						this.log.info("New socket spawned for " + socketHost);
						socketChannel = SocketChannel.open();
						socketChannel.configureBlocking(false);
						socketChannel.connect(socketHost);

					} catch (final IOException e) {
						e.printStackTrace();
					}
				}

				this.clientIPMap.put(event.getAttachment(), socketChannel);

				// Queue a channel registration since the caller is not the
				// selecting thread. As part of the registration we'll register
				// an interest in connection events. These are raised when a
				// channel
				// is ready to complete connection establishment.
				synchronized (this.changeRequests) {
					if (!socketChannel.isConnected()) {
						this.log.info("registering for CONNECT");
						this.changeRequests
								.add(new ChangeRequest(socketChannel,
										ChangeRequest.REGISTER,
										SelectionKey.OP_CONNECT, event
												.getAttachment()));

					} else {

						if (socketChannel.keyFor(this.selector) == null) {
							this.log.info("registering for WRITE");
							this.changeRequests.add(new ChangeRequest(
									socketChannel, ChangeRequest.REGISTER,
									SelectionKey.OP_WRITE, event
											.getAttachment()));
						} else {
							this.log.info("Changing ops to WRITE");
							this.changeRequests.add(new ChangeRequest(
									socketChannel, ChangeRequest.CHANGEOPS,
									SelectionKey.OP_WRITE, event
											.getAttachment()));
						}
					}
				}
			}
		}

		synchronized (this.pendingData) {
			ArrayList<ByteBuffer> queue = this.pendingData.get(event
					.getAttachment());
			if (queue == null) {
				queue = new ArrayList<ByteBuffer>();
				this.pendingData.put(event.getAttachment(), queue);
			}
			queue.add(event.getData());
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		this.selector.wakeup();
	}

	@Override
	public void closeConnection(final DataEvent dataEvent) {

		if (dataEvent instanceof ClientDataEvent) {
			this.handleConnectionClose(this.clientIPMap
					.get(((ClientDataEvent) dataEvent).getAttachment()));
			// final ClientDataEvent clientEvent = (ClientDataEvent) dataEvent;
			// this.changeRequests.add(new ChangeRequest(this.clientIPMap
			// .get(((ClientDataEvent) dataEvent).getAttachment()),
			// ChangeRequest.CLOSE, 0, clientEvent.getAttachment()));
		}
	}

	@Override
	public boolean handlePendingChanges() throws ClosedChannelException {

		synchronized (this.changeRequests) {
			if (!this.changeRequests.isEmpty()) {
				this.log.debug("Handling pending changes...");
				final ChangeRequest change = this.changeRequests.remove(0);

				SelectionKey key;

				if (change != null) {
					switch (change.type) {
					case ChangeRequest.CLOSE:
						if (change.socket.isConnected()
								&& this.pendingData.get(change.attachment) != null
								&& this.pendingData.get(change.attachment)
										.size() > 0) {
							this.changeRequests.add(change);
							return false;
						}

						if (change.socket.isConnected()) {
							this.doClose(change.socket);
						}

						break;
					case ChangeRequest.CHANGEOPS:
						key = change.socket.keyFor(this.selector);
						if (key != null && key.isValid()) {
							key.interestOps(change.ops);
							key.attach(change.attachment);
						} else {
							if (change.socket.isConnected()) {
								change.socket.register(this.selector,
										change.ops, change.attachment);
							} else {
								throw new RuntimeException(
										"I expected this socket to be connected, we must reconnect :(");
							}
						}
						break;
					case ChangeRequest.REGISTER:
						change.socket.register(this.selector, change.ops,
								change.attachment);
						break;
					}
				}
			} else {
				return false;
			}
		}
		return this.changeRequests.size() > 0;
	}

	private void doClose(final SocketChannel socket) {

		this.pool.saveObject(new InetSocketAddress(socket.socket()
				.getInetAddress(), socket.socket().getPort()), socket);
	}

	@Override
	public void handleConnectionClose(final SocketChannel socket) {
		this.doClose(socket);
	}

}
