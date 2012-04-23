package org.chinux.pdc.events;

import java.nio.channels.SocketChannel;

public class NIOClientEvent extends NIODataEvent {

	public NIOClientEvent(final SocketChannel socket, final byte[] data) {
		super(socket, data);
	}

}
