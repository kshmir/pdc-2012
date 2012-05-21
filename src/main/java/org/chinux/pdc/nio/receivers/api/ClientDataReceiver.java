package org.chinux.pdc.nio.receivers.api;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.services.util.ChangeRequest;

public abstract class ClientDataReceiver implements DataReceiver<DataEvent> {

	protected Selector selector;
	protected int connectionPort;

	protected Map<Object, ArrayList<ByteBuffer>> pendingData = new HashMap<Object, ArrayList<ByteBuffer>>();
	protected List<ChangeRequest> changeRequests = new ArrayList<ChangeRequest>();
	protected Map<Object, SocketChannel> clientIPMap = new HashMap<Object, SocketChannel>();

	protected Logger log = Logger.getLogger(this.getClass());

	public Map<Object, ArrayList<ByteBuffer>> getPendingData() {
		return this.pendingData;
	}

	public abstract void handlePendingChanges() throws ClosedChannelException;

	public void setSelector(final Selector selector) {
		this.selector = selector;
	}

	public void setConnectionPort(final int connectionPort) {
		this.connectionPort = connectionPort;
	}
}
