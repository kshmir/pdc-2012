package org.chinux.pdc.server;

public class MonitorObject {

	private int fromClientsTransferedBytes;
	private int fromServersTransferedBytes;

	public MonitorObject() {
	}

	public int getTotalTransferedBytes() {
		return this.fromClientsTransferedBytes
				+ this.fromServersTransferedBytes;
	}

	public int getFromClientsTransferedBytes() {
		return this.fromClientsTransferedBytes;
	}

	public void setFromClientsTransferedBytes(
			final int fromClientsTransferedBytes) {
		this.fromClientsTransferedBytes = fromClientsTransferedBytes;
	}

	public int getFromServersTransferedBytes() {
		return this.fromServersTransferedBytes;
	}

	public void setFromServersTransferedBytes(
			final int fromServersTransferedBytes) {
		this.fromServersTransferedBytes = fromServersTransferedBytes;
	}

	public void addFromClientsBytes(final int bytes) {
		this.fromClientsTransferedBytes += bytes;
	}

	public void addFromServersBytes(final int bytes) {
		this.fromServersTransferedBytes += bytes;
	}

}
