package org.chinux.pdc.server;

public class MonitorObject {

	private int fromClientsTransferedBytes;
	private int fromServersTransferedBytes;
	private int openedConnectionsQuant;
	private int totalblocksQuant;
	private int imageFlipsQuant;
	private int text2L33tQuant;

	public MonitorObject() {
		this.fromClientsTransferedBytes = 0;
		this.fromServersTransferedBytes = 0;
		this.openedConnectionsQuant = 0;
		this.totalblocksQuant = 0;
	}

	public void increaseText2L33tQuant() {
		this.text2L33tQuant += 1;
	}

	public void increaseImageFlipsQuant() {
		this.imageFlipsQuant += 1;
	}

	public int getTotalblocksQuant() {
		return this.totalblocksQuant;
	}

	public void setTotalblocksQuant(final int totalblocksQuant) {
		this.totalblocksQuant = totalblocksQuant;
	}

	public int getImageFlipsQuant() {
		return this.imageFlipsQuant;
	}

	public void setImageFlipsQuant(final int imageFlipsQuant) {
		this.imageFlipsQuant = imageFlipsQuant;
	}

	public int getText2L33tQuant() {
		return this.text2L33tQuant;
	}

	public void setText2L33tQuant(final int text2l33tQuant) {
		this.text2L33tQuant = text2l33tQuant;
	}

	public int getTransformationsQuant() {
		return this.imageFlipsQuant + this.text2L33tQuant;
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
