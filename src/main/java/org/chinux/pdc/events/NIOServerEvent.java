package org.chinux.pdc.events;

import java.nio.channels.SocketChannel;

public class NIOServerEvent extends NIODataEvent {

	public NIOServerEvent(final SocketChannel socket, final byte[] data) {
		super(socket, data);
	}

}
