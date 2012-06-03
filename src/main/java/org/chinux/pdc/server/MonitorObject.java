package org.chinux.pdc.server;

public class MonitorObject {

	private int fromClientsTransferedBytes;
	private int fromServersTransferedBytes;
	private int openedConnectionsQuant;
	private int totalblocksQuant;
	private int imageFlipsQuant;
	private int text2L33tQuant;
	private int allAccessBlocksQuant;
	private int ipBlocksQuant;
	private int contentTypeBlocksQuant;
	private int tooBigResourceBlocksQuant;
	private int urlBlocksQuant;
	private int clientConnectionsQuant;
	private int originServerConnectionsQuant;
	private int connectionsQuant;

	public MonitorObject() {
	}

	public void setConnectionsQuant(final int connectionsQuant) {
		this.connectionsQuant = connectionsQuant;
	}

	public int getConnectionsQuant() {
		return this.connectionsQuant;
	}

	public int getClientConnectionsQuant() {
		return this.clientConnectionsQuant;
	}

	public void setClientConnectionsQuant(final int clientConnectionsQuant) {
		this.clientConnectionsQuant = clientConnectionsQuant;
	}

	public int getOriginServerConnectionsQuant() {
		return this.originServerConnectionsQuant;
	}

	public void setOriginServerConnectionsQuant(
			final int originServerConnectionsQuant) {
		this.originServerConnectionsQuant = originServerConnectionsQuant;
	}

	public void increaseUrlBlocksQuant() {
		this.urlBlocksQuant += 1;
	}

	public int getUrlBlocksQuant() {
		return this.urlBlocksQuant;
	}

	public void setUrlBlocksQuant(final int urlBlocksQuant) {
		this.urlBlocksQuant = urlBlocksQuant;
	}

	public void increaseAllAccessBlocksQuant() {
		this.allAccessBlocksQuant += 1;
	}

	public void increaseIpBlocksQuant() {
		this.ipBlocksQuant += 1;
	}

	public void increaseContentTypeBlocksQuant() {
		this.contentTypeBlocksQuant += 1;
	}

	public void increaseTooBigResourceBlocksQuant() {
		this.tooBigResourceBlocksQuant += 1;
	}

	public int getTotalBlocksQuant() {
		return this.allAccessBlocksQuant + this.ipBlocksQuant
				+ this.contentTypeBlocksQuant + this.tooBigResourceBlocksQuant;
	}

	public int getOpenedConnectionsQuant() {
		return this.openedConnectionsQuant;
	}

	public void setOpenedConnectionsQuant(final int openedConnectionsQuant) {
		this.openedConnectionsQuant = openedConnectionsQuant;
	}

	public int getAllAccessBlocksQuant() {
		return this.allAccessBlocksQuant;
	}

	public void setAllAccessBlocksQuant(final int allAccessBlocksQuant) {
		this.allAccessBlocksQuant = allAccessBlocksQuant;
	}

	public int getIpBlocksQuant() {
		return this.ipBlocksQuant;
	}

	public void setIpBlocksQuant(final int ipBlocksQuant) {
		this.ipBlocksQuant = ipBlocksQuant;
	}

	public int getContentTypeBlocksQuant() {
		return this.contentTypeBlocksQuant;
	}

	public void setContentTypeBlocksQuant(final int contentTypeBlocksQuant) {
		this.contentTypeBlocksQuant = contentTypeBlocksQuant;
	}

	public int getTooBigResourceBlocksQuant() {
		return this.tooBigResourceBlocksQuant;
	}

	public void setTooBigResourceBlocksQuant(final int tooBigResourceBlocksQuant) {
		this.tooBigResourceBlocksQuant = tooBigResourceBlocksQuant;
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
