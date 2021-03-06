package org.chinux.pdc.nio.events.api;

import java.nio.ByteBuffer;

import org.chinux.pdc.nio.receivers.api.DataReceiver;

/**
 * Represents any kind of processed data ready to be sent to a receiver
 * 
 * @author cris
 * 
 */
public abstract class DataEvent implements Cloneable {

	@Override
	public String toString() {
		return "DataEvent [receiver=" + this.receiver + ", canSend="
				+ this.canSend + ", canClose=" + this.canClose + "]";
	}

	private DataReceiver<DataEvent> receiver;
	private ByteBuffer data;

	private boolean canSend;
	private boolean canClose;

	public DataEvent(final ByteBuffer data,
			final DataReceiver<DataEvent> receiver) {
		this(data, receiver, false, false);
	}

	public DataEvent(final ByteBuffer data,
			final DataReceiver<DataEvent> receiver, final boolean canSend,
			final boolean canClose) {
		this.receiver = receiver;
		this.data = data;
		this.canSend = canSend;
		this.canClose = canClose;
	}

	public void setCanClose(final boolean closeable) {
		this.canClose = closeable;
	}

	public void setCanSend(final boolean sendable) {
		this.canSend = sendable;
	}

	public ByteBuffer getData() {
		return this.data;
	}

	public void setData(final ByteBuffer newData) {
		this.data = newData;
	}

	public boolean canSend() {
		return this.canSend;
	}

	public boolean canClose() {
		return this.canClose;
	}

	public DataReceiver<DataEvent> getReceiver() {
		return this.receiver;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (final CloneNotSupportedException e) {
			return null;
		}
	}
}