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

		if (!(dataEvent instanceof ClientDataEvent)) {
			throw new RuntimeException("Must receive a NIOClientDataEvent!");
		}

		final ClientDataEvent event = (ClientDataEvent) dataEvent;

		final InetAddress host = event.getAddress();

		final InetSocketAddress socketHost = new InetSocketAddress(host,
				connectionPort);

		SocketChannel socketChannel;
		synchronized (clientIPMap) {
			socketChannel = clientIPMap.get(event.getOwner());

			if (socketChannel == null) {
				try {
					socketChannel = SocketChannel.open();
					socketChannel.configureBlocking(false);
					socketChannel.connect(socketHost);
				} catch (final IOException e) {
					e.printStackTrace();
				}

				clientIPMap.put(event.getOwner(), socketChannel);

				// Queue a channel registration since the caller is not the
				// selecting thread. As part of the registration we'll register
				// an interest in connection events. These are raised when a
				// channel
				// is ready to complete connection establishment.
				synchronized (changeRequests) {
					changeRequests.add(new ChangeRequest(socketChannel,
							ChangeRequest.REGISTER, SelectionKey.OP_CONNECT,
							event.getOwner()));
				}

			}
		}

		synchronized (pendingData) {
			ArrayList<ByteBuffer> queue = pendingData.get(event.getOwner());
			if (queue == null) {
				queue = new ArrayList<ByteBuffer>();
				pendingData.put(event.getOwner(), queue);
			}
			queue.add(ByteBuffer.wrap(event.getData()));
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		selector.wakeup();
	}

	@Override
	public void closeConnection(final DataEvent dataEvent) {
		// TODO Make this
	}

	@Override
	public void handlePendingChanges() throws ClosedChannelException {
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
					key = change.socket.register(selector, change.ops);
					key.attach(change.attachment);
					break;
				}
			}
		}
	}
}
