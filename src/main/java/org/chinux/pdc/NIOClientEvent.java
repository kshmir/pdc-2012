package main.java.org.chinux.pdc;

import java.nio.channels.SocketChannel;

public class NIOClientEvent extends NIODataEvent {

	public NIOClientEvent(final SocketChannel socket, final byte[] data) {
		super(socket, data);
	}

}
