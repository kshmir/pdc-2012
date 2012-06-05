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
	private Object attachment; // The attachment on a clientDataEvent can be a
								// HTTPProxyEvent
	private Object owner; // The owner of a clientDataEvent can be a
							// SocketChannel

	private Integer port = null; // For proxy chaining

	public ClientDataEvent(final ByteBuffer data, final Object attachment,
			final Object owner) {
		this(data, null, null, attachment, owner);
	}

	public ClientDataEvent(final ByteBuffer data, final Object attachment) {
		this(data, null, null, attachment, null);
	}

	public ClientDataEvent(final ByteBuffer data, final InetAddress address,
			final Object attachment) {
		this(data, null, address, attachment, null);
	}

	public ClientDataEvent(final ByteBuffer data,
			final DataReceiver<DataEvent> receiver, final InetAddress address,
			final Object attachment, final Object owner) {
		super(data, receiver);
		this.address = address;
		this.attachment = attachment;
		this.owner = owner;
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
	 * Represents the attachment of the event, which could be any instance of an
	 * object, all consequent dataEvents must be of the same owner
	 * 
	 * @return
	 */
	public Object getAttachment() {
		return this.attachment;
	}

	@Override
	public String toString() {
		return "ClientDataEvent [address=" + this.address + ", attachment="
				+ this.attachment + ", owner()=" + this.getOwner() + "]";
	}

	public Object getOwner() {
		return this.owner;
	}

	public void setOwner(final Object owner) {
		this.owner = owner;
	}

	public Integer getPort() {
		return this.port;
	}

	public void setPort(final Integer port) {
		this.port = port;
	}

}
