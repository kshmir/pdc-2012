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
	public synchronized void receiveEvent(final DataEvent dataEvent) {

		this.log.debug("Receiving data event " + dataEvent);

		if (!(dataEvent instanceof ClientDataEvent)) {
			throw new RuntimeException("Must receive a NIOClientDataEvent!");
		}

		final ClientDataEvent event = (ClientDataEvent) dataEvent;

		final InetAddress host = event.getAddress();

		final InetSocketAddress socketHost = new InetSocketAddress(host,
				this.connectionPort);

		synchronized (this.attachmentIPMap) {
			synchronized (this.changeRequests) {
				synchronized (this.pendingData) {

					final InetAddress oldHost = this.attachmentIPMap.get(event
							.getAttachment());

					if (oldHost != null && !oldHost.equals(host)) {
						this.clientIPMap.remove(event.getAttachment());
					}
					SocketChannel socketChannel;

					final boolean isNotNew = false;

					// this.so

					socketChannel = this.clientIPMap.get(event.getAttachment());

					if (socketChannel == null) {

						socketChannel = this.pool.getObject(socketHost);

						if (socketChannel == null) {
							socketChannel = this.makeSocketChannel(socketHost);
						}

						this.attachmentIPMap.put(event.getAttachment(), host);
						this.clientIPMap.put(event.getAttachment(),
								socketChannel);
					}

					// Queue a channel registration since the caller is not
					// the
					// selecting thread. As part of the registration we'll
					// register
					// an interest in connection events. These are raised
					// when a
					// channel
					// is ready to complete connection establishment.

					if (!socketChannel.isConnected()
							&& this.pendingData.get(event.getAttachment()) == null) {
						this.makeSocketChannelFromOld(socketChannel,
								event.getAttachment());

					} else {
						System.out.println("Changing ops to write!");
						this.changeRequests.add(new ChangeRequest(
								socketChannel, ChangeRequest.CHANGEOPS,
								SelectionKey.OP_WRITE, event.getAttachment()));
					}

				}

				ArrayList<ByteBuffer> queue = this.pendingData.get(event
						.getAttachment());
				if (queue == null) {
					queue = new ArrayList<ByteBuffer>();
					this.pendingData.put(event.getAttachment(), queue);
				}

				queue.add(event.getData());

			}
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		this.selector.wakeup();
	}

	private SocketChannel makeSocketChannel(final InetSocketAddress socketHost) {
		SocketChannel socketChannel = null;
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.connect(socketHost);

		} catch (final IOException e) {
			e.printStackTrace();
			socketChannel = null; // TODO: Que onda?
		}
		return socketChannel;
	}

	@Override
	public synchronized void closeConnection(final DataEvent dataEvent) {

		if (dataEvent instanceof ClientDataEvent) {
			// this.handleConnectionClose(this.clientIPMap
			// .get(((ClientDataEvent) dataEvent).getAttachment()));
			final ClientDataEvent clientEvent = (ClientDataEvent) dataEvent;
			this.changeRequests.add(new ChangeRequest(this.clientIPMap
					.get(((ClientDataEvent) dataEvent).getAttachment()),
					ChangeRequest.CLOSE, 0, clientEvent.getAttachment()));
		}
	}

	@Override
	public synchronized boolean handlePendingChanges()
			throws ClosedChannelException {

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
							System.out.println("Attaching!");
							key.interestOps(change.ops);
							key.attach(change.attachment);
						} else {
							if (change.socket.isConnected()) {
								System.out.println("Registering back!");
								change.socket.register(this.selector,
										change.ops, change.attachment);
							} else {
								System.out.println("Using a new socket!");
								this.makeSocketChannelFromOld(change.socket,
										change.attachment);
								return true;
								// throw new RuntimeException(
								// "I expected this socket to be connected, we must reconnect :(");
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

	private void makeSocketChannelFromOld(final SocketChannel oldSocket,
			final Object attachment) {
		final SocketChannel newSocket = this
				.makeSocketChannel(new InetSocketAddress(this.attachmentIPMap
						.get(attachment), this.connectionPort));

		this.clientIPMap.remove(oldSocket);
		this.clientIPMap.put(attachment, newSocket);
		this.changeRequests.add(new ChangeRequest(newSocket,
				ChangeRequest.REGISTER, SelectionKey.OP_CONNECT, attachment));
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
