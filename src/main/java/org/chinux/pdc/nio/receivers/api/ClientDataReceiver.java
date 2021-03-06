package org.chinux.pdc.nio.receivers.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.chinux.pdc.nio.dispatchers.EventDispatcher;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.services.util.ChangeRequest;

public abstract class ClientDataReceiver implements DataReceiver<DataEvent> {

	protected Selector selector;
	protected int connectionPort;

	protected List<ChangeRequest> changeRequests = new ArrayList<ChangeRequest>();

	protected Map<Object, ArrayList<ByteBuffer>> pendingData = new HashMap<Object, ArrayList<ByteBuffer>>();
	protected Map<Object, SocketChannel> attachmentSocketMap = new HashMap<Object, SocketChannel>();
	protected Map<Object, InetSocketAddress> attachmentIPMap = new HashMap<Object, InetSocketAddress>();

	protected EventDispatcher<DataEvent> dispatcher;

	protected Logger log = Logger.getLogger(this.getClass());

	public Map<Object, ArrayList<ByteBuffer>> getPendingData() {
		return this.pendingData;
	}

	public void setDispatcher(final EventDispatcher<DataEvent> dispatcher) {
		this.dispatcher = dispatcher;
	}

	public abstract boolean handlePendingChanges()
			throws ClosedChannelException;

	public void setSelector(final Selector selector) {
		this.selector = selector;
	}

	public void setConnectionPort(final int connectionPort) {
		this.connectionPort = connectionPort;
	}
}
