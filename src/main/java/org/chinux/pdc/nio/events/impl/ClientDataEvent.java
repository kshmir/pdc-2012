package org.chinux.pdc.nio.events.impl;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.receivers.api.DataReceiver;

/**
 * Represents a client data event
 * 
 * @author cris
 */
public class ClientDataEvent extends DataEvent {

	private InetAddress address;
	private Object owner; // The owner on a clientDataEvent can be a
							// HTTPProxyEvent
	private Object key; // The key of a clientDataEvent can be a SocketChannel

	public ClientDataEvent(final ByteBuffer data, final Object owner,
			final Object key) {
		this(data, null, null, owner, key);
	}

	public ClientDataEvent(final ByteBuffer data, final Object owner) {
		this(data, null, null, owner, null);
	}

	public ClientDataEvent(final ByteBuffer data, final InetAddress address,
			final Object owner) {
		this(data, null, address, owner, null);
	}

	public ClientDataEvent(final ByteBuffer data,
			final DataReceiver<DataEvent> receiver, final InetAddress address,
			final Object owner, final Object key) {
		super(data, receiver);
		this.address = address;
		this.owner = owner;
		this.key = key;
	}

	/**
	 * Represents the address the dataEvent connects to
	 * 
	 * @return
	 */
	public InetAddress getAddress() {
		return this.address;
	}

	/**
	 * Represents the owner of the event, which could be any instance of an
	 * object, all consequent dataEvents must be of the same key
	 * 
	 * @return
	 */
	public Object getOwner() {
		return this.owner;
	}

	@Override
	public String toString() {
		return "ClientDataEvent [address=" + this.address + ", owner="
				+ this.owner + ", toString()=" + super.toString() + "]";
	}

}
