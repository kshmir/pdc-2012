package org.chinux.pdc.server;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.chinux.pdc.nio.receivers.impl.ASyncClientDataReceiver;
import org.chinux.pdc.workers.impl.HTTPProxyWorker;

public class MonitorObject {

	private class MonitorDataPoller extends TimerTask {
		@Override
		public void run() {
			if (MonitorObject.this.clientDataReceiver == null) {
				return;
			}
			MonitorObject.this.clientDataReceiver.updateMonitorObject();
			MonitorObject.this.connectionsQuant = 0;
			for (final HTTPProxyWorker worker : MonitorObject.this.httpProxyWorkers) {
				worker.updateMonitorObject();
			}
		}
	}

	private ASyncClientDataReceiver clientDataReceiver;
	private Set<HTTPProxyWorker> httpProxyWorkers;
	private int fromClientsTransferedBytes;
	private int fromServersTransferedBytes;
	private int openedConnectionsQuant;
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
	private int timedConnections;

	Timer timer;

	public MonitorObject() {
		new Timer();
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(new MonitorDataPoller(), 0, 1000);
	}

	public void addFromClientsBytes(final int bytes) {
		this.fromClientsTransferedBytes += bytes;
	}

	public void addFromServersBytes(final int bytes) {
		this.fromServersTransferedBytes += bytes;
	}

	public int getAllAccessBlocksQuant() {
		return this.allAccessBlocksQuant;
	}

	public int getClientConnectionsQuant() {
		return this.clientConnectionsQuant;
	}

	public ASyncClientDataReceiver getClientDataReceiver() {
		return this.clientDataReceiver;
	}

	public int getConnectionsQuant() {
		return this.connectionsQuant;
	}

	public int getContentTypeBlocksQuant() {
		return this.contentTypeBlocksQuant;
	}

	public int getFromClientsTransferedBytes() {
		return this.fromClientsTransferedBytes;
	}

	public int getFromServersTransferedBytes() {
		return this.fromServersTransferedBytes;
	}

	public Set<HTTPProxyWorker> getHttpProxyWorker() {
		return this.httpProxyWorkers;
	}

	public int getImageFlipsQuant() {
		return this.imageFlipsQuant;
	}

	public int getIpBlocksQuant() {
		return this.ipBlocksQuant;
	}

	public int getOpenedConnectionsQuant() {
		return this.openedConnectionsQuant;
	}

	public int getOriginServerConnectionsQuant() {
		return this.originServerConnectionsQuant;
	}

	public int getText2L33tQuant() {
		return this.text2L33tQuant;
	}

	public int getTimedConnections() {
		return this.timedConnections;
	}

	public int getTooBigResourceBlocksQuant() {
		return this.tooBigResourceBlocksQuant;
	}

	public int getTotalBlocksQuant() {
		return this.allAccessBlocksQuant + this.ipBlocksQuant
				+ this.contentTypeBlocksQuant + this.tooBigResourceBlocksQuant
				+ this.urlBlocksQuant;
	}

	public int getTotalTransferedBytes() {
		return this.fromClientsTransferedBytes
				+ this.fromServersTransferedBytes;
	}

	public int getTransformationsQuant() {
		return this.imageFlipsQuant + this.text2L33tQuant;
	}

	public int getUrlBlocksQuant() {
		return this.urlBlocksQuant;
	}

	public void increaseAllAccessBlocksQuant() {
		this.allAccessBlocksQuant += 1;
	}

	public void increaseContentTypeBlocksQuant() {
		this.contentTypeBlocksQuant += 1;
	}

	public void increaseImageFlipsQuant() {
		this.imageFlipsQuant += 1;
	}

	public void increaseIpBlocksQuant() {
		this.ipBlocksQuant += 1;
	}

	public void increaseText2L33tQuant() {
		this.text2L33tQuant += 1;
	}

	public void increaseTooBigResourceBlocksQuant() {
		this.tooBigResourceBlocksQuant += 1;
	}

	public void increaseUrlBlocksQuant() {
		this.urlBlocksQuant += 1;
	}

	public void setAllAccessBlocksQuant(final int allAccessBlocksQuant) {
		this.allAccessBlocksQuant = allAccessBlocksQuant;
	}

	public void setClientConnectionsQuant(final int clientConnectionsQuant) {
		this.clientConnectionsQuant = clientConnectionsQuant;
	}

	public void setClientDataReceiver(
			final ASyncClientDataReceiver clientDataReceiver) {
		this.clientDataReceiver = clientDataReceiver;
	}

	public void setConnectionsQuant(final int connectionsQuant) {
		this.connectionsQuant += connectionsQuant;
	}

	public void setContentTypeBlocksQuant(final int contentTypeBlocksQuant) {
		this.contentTypeBlocksQuant = contentTypeBlocksQuant;
	}

	public void setFromClientsTransferedBytes(
			final int fromClientsTransferedBytes) {
		this.fromClientsTransferedBytes = fromClientsTransferedBytes;
	}

	public void setFromServersTransferedBytes(
			final int fromServersTransferedBytes) {
		this.fromServersTransferedBytes = fromServersTransferedBytes;
	}

	public void setHttpProxyWorkers(final Set<HTTPProxyWorker> httpProxyWorkers) {
		this.httpProxyWorkers = httpProxyWorkers;
	}

	public void setImageFlipsQuant(final int imageFlipsQuant) {
		this.imageFlipsQuant = imageFlipsQuant;
	}

	public void setIpBlocksQuant(final int ipBlocksQuant) {
		this.ipBlocksQuant = ipBlocksQuant;
	}

	public void setOpenedConnectionsQuant(final int openedConnectionsQuant) {
		this.openedConnectionsQuant = openedConnectionsQuant;
	}

	public void setOriginServerConnectionsQuant(
			final int originServerConnectionsQuant) {
		this.originServerConnectionsQuant = originServerConnectionsQuant;
	}

	public void setText2L33tQuant(final int text2l33tQuant) {
		this.text2L33tQuant = text2l33tQuant;
	}

	public void setTimedConnections(final int timedConnections) {
		this.timedConnections = timedConnections;
	}

	public void setTooBigResourceBlocksQuant(final int tooBigResourceBlocksQuant) {
		this.tooBigResourceBlocksQuant = tooBigResourceBlocksQuant;
	}

	public void setUrlBlocksQuant(final int urlBlocksQuant) {
		this.urlBlocksQuant = urlBlocksQuant;
	}
}
