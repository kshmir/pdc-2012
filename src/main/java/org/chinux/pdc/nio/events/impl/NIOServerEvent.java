package org.chinux.pdc.nio.events.impl;

import java.nio.channels.SocketChannel;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.api.DataReceiver;

public class NIOServerEvent extends NIODataEvent {

	public NIOServerEvent(final SocketChannel socket, final byte[] data,
			final DataReceiver<DataEvent> receiver) {
		super(socket, data, receiver);
	}

}
