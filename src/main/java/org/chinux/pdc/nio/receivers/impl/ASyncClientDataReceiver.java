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

public class ASyncClientDataReceiver extends ClientDataReceiver {

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
			socketChannel = this.clientIPMap.get(event.getOwner());

			if (socketChannel == null) {
				try {
					socketChannel = SocketChannel.open();
					socketChannel.configureBlocking(false);
					socketChannel.connect(socketHost);
				} catch (final IOException e) {
					e.printStackTrace();
				}

				this.clientIPMap.put(event.getOwner(), socketChannel);

				// Queue a channel registration since the caller is not the
				// selecting thread. As part of the registration we'll register
				// an interest in connection events. These are raised when a
				// channel
				// is ready to complete connection establishment.
				synchronized (this.changeRequests) {
					this.changeRequests.add(new ChangeRequest(socketChannel,
							ChangeRequest.REGISTER, SelectionKey.OP_CONNECT,
							event.getOwner()));
				}
			}
		}

		synchronized (this.pendingData) {
			ArrayList<ByteBuffer> queue = this.pendingData
					.get(event.getOwner());
			if (queue == null) {
				queue = new ArrayList<ByteBuffer>();
				this.pendingData.put(event.getOwner(), queue);
			}
			queue.add(event.getData());
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		this.selector.wakeup();
	}

	@Override
	public void closeConnection(final DataEvent dataEvent) {
		// TODO Make this
	}

	@Override
	public void handlePendingChanges() throws ClosedChannelException {

		synchronized (this.changeRequests) {
			if (!this.changeRequests.isEmpty()) {
				this.log.debug("Handling pending changes...");
				final ChangeRequest change = this.changeRequests.remove(0);

				SelectionKey key;
				switch (change.type) {
				case ChangeRequest.CHANGEOPS:
					key = change.socket.keyFor(this.selector);
					key.interestOps(change.ops);
					break;
				case ChangeRequest.REGISTER:
					key = change.socket.register(this.selector, change.ops);
					key.attach(change.attachment);
					break;
				}
			}
		}
	}
}
