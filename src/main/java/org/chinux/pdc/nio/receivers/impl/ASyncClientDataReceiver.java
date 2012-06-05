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
import org.chinux.pdc.nio.events.impl.ErrorDataEvent;
import org.chinux.pdc.nio.receivers.api.ClientDataReceiver;
import org.chinux.pdc.nio.services.util.ChangeRequest;
import org.chinux.pdc.server.MonitorObject;
import org.chinux.pdc.server.api.Monitoreable;

public class ASyncClientDataReceiver extends ClientDataReceiver implements
		ConnectionCloseHandler, Monitoreable {

	private MonitorObject monitorObject;

	private TimeoutableSocketPool pool = new TimeoutableSocketPool(30);

	@Override
	public synchronized void receiveEvent(final DataEvent dataEvent) {

		this.log.debug("Receiving data event " + dataEvent);

		if (dataEvent instanceof ErrorDataEvent) {
			final ErrorDataEvent event = (ErrorDataEvent) dataEvent;
			synchronized (this.changeRequests) {
				this.changeRequests.add(new ChangeRequest((SocketChannel) event
						.getAttachment(), ChangeRequest.MAKE_NEW,
						SelectionKey.OP_WRITE, event.getOwner()));
			}
			return;
		}

		if (!(dataEvent instanceof ClientDataEvent)) {
			throw new RuntimeException("Must receive a NIOClientDataEvent!");
		}

		final ClientDataEvent event = (ClientDataEvent) dataEvent;

		final InetAddress host = event.getAddress();

		final InetSocketAddress socketHost = new InetSocketAddress(host,
				(event.getPort() != null) ? event.getPort()
						: this.connectionPort);

		synchronized (this.attachmentIPMap) {
			synchronized (this.changeRequests) {
				synchronized (this.pendingData) {

					final InetSocketAddress oldHost = this.attachmentIPMap
							.get(event.getAttachment());

					if (oldHost != null && !oldHost.getAddress().equals(host)) {
						this.attachmentSocketMap.remove(event.getAttachment());
					}
					SocketChannel socketChannel;

					boolean isNew = false;

					socketChannel = this.attachmentSocketMap.get(event
							.getAttachment());

					if (socketChannel == null) {

						socketChannel = this.pool.getObject(socketHost);

						if (socketChannel == null) {
							socketChannel = this.makeSocketChannel(socketHost);

							isNew = true;
						}

						this.attachmentIPMap.put(event.getAttachment(),
								socketHost);
						this.attachmentSocketMap.put(event.getAttachment(),
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
						if (!isNew) {
							this.makeSocketChannelFromOld(socketChannel,
									event.getAttachment());
						} else {
							this.changeRequests.add(new ChangeRequest(
									socketChannel, ChangeRequest.REGISTER,
									SelectionKey.OP_CONNECT, event
											.getAttachment()));
						}

					} else {
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
			System.out.println(socketHost.getPort());
			System.out.println(socketHost.getHostName());
			e.printStackTrace();
			socketChannel = null; // TODO: Que onda?
		}
		return socketChannel;
	}

	@Override
	public synchronized void closeConnection(final DataEvent dataEvent) {

		if (dataEvent instanceof ClientDataEvent) {
			final ClientDataEvent clientEvent = (ClientDataEvent) dataEvent;
			this.changeRequests.add(new ChangeRequest(this.attachmentSocketMap
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

				if (change.socket == null) {
					this.pendingData.remove(change.attachment);
					this.attachmentSocketMap.remove(change.attachment);
					this.attachmentIPMap.remove(change.attachment);
				}

				if (change != null && change.socket != null) {
					switch (change.type) {
					case ChangeRequest.MAKE_NEW:
						this.makeSocketChannelFromOld(change.socket,
								change.attachment);
					case ChangeRequest.CLOSE:
						if (change.socket.isConnected()
								&& this.pendingData.get(change.attachment) != null
								&& this.pendingData.get(change.attachment)
										.size() > 0) {
							this.changeRequests.add(change);
							return false;
						}

						this.pendingData.remove(change.attachment);
						this.attachmentSocketMap.remove(change.attachment);
						this.attachmentIPMap.remove(change.attachment);

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
								this.makeSocketChannelFromOld(change.socket,
										change.attachment);
								return true;
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
				.makeSocketChannel(this.attachmentIPMap.get(attachment));

		this.attachmentSocketMap.remove(oldSocket);
		this.attachmentSocketMap.put(attachment, newSocket);
		this.changeRequests.add(new ChangeRequest(newSocket,
				ChangeRequest.REGISTER, SelectionKey.OP_CONNECT, attachment));

		try {
			oldSocket.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void doClose(final SocketChannel socket) {

		this.pool.saveObject(new InetSocketAddress(socket.socket()
				.getInetAddress(), socket.socket().getPort()), socket);
	}

	@Override
	public void handleConnectionClose(final SocketChannel socket) {
		this.doClose(socket);
	}

	public MonitorObject getMonitorObject() {
		return this.monitorObject;
	}

	public void setMonitorObject(final MonitorObject monitorObject) {
		this.monitorObject = monitorObject;
	}

	@Override
	public void updateMonitorObject() {
		synchronized (this) {
			this.monitorObject.setTimedConnections(this.pool.getElementsSize());
		}
	}

}
