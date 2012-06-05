package org.chinux.pdc.nio.dispatchers;

import java.nio.channels.SocketChannel;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.events.impl.ErrorDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.nio.receivers.api.DataReceiver;
import org.chinux.pdc.server.MonitorObject;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;
import org.chinux.pdc.workers.impl.HTTPProxyWorker;

/**
 * Basically this class binds a socket channel session to a single
 * EventDispatcher no matter what it does. It spawns multiple threads and sends
 * them the data to process. It acts as a load balancer via a round robin
 * algorithm.
 * 
 * @author cris
 * 
 * @param <T>
 *            Any compatible DataEvent: currently ErrorDataEvent,
 *            ClientDataEvent and ServerDataEvent
 */
public class MultiThreadedEventDispatcher<T extends DataEvent> implements
		UrgentEventDispatcher<T> {

	private Timer timeoutTimer = new Timer();

	private Map<SocketChannel, UrgentEventDispatcher<T>> dispatcherRoutes = new HashMap<SocketChannel, UrgentEventDispatcher<T>>();

	private Deque<UrgentEventDispatcher<T>> dispatchers = new LinkedList<UrgentEventDispatcher<T>>();

	public MultiThreadedEventDispatcher(
			final DataReceiver<DataEvent> serverReceiver,
			final DataReceiver<DataEvent> clientReceiver,
			final MonitorObject object) {
		// If no parameter is given, we use 2 threads per core.
		this(Runtime.getRuntime().availableProcessors() * 2, serverReceiver,
				clientReceiver, object);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MultiThreadedEventDispatcher(final int dispatcherCount,
			final DataReceiver<DataEvent> serverReceiver,
			final DataReceiver<DataEvent> clientReceiver,
			final MonitorObject object) {
		final Set<HTTPProxyWorker> workers = new HashSet<HTTPProxyWorker>();
		for (int i = 0; i < dispatcherCount; i++) {
			final HTTPProxyWorker worker = new HTTPProxyWorker();
			worker.setMonitorObject(object);
			worker.setEventDispatcher((UrgentEventDispatcher) this);
			worker.setClientDataReceiver(clientReceiver);
			worker.setServerDataReceiver(serverReceiver);
			final ASyncEventDispatcher dispatcher = new ASyncEventDispatcher(
					worker);

			workers.add(worker);
			this.dispatchers.addFirst(dispatcher);
			final Thread t = new Thread(dispatcher);
			t.start();
		}

		object.setHttpProxyWorkers(workers);

		final TimerTask task = new TimerTask() {

			private Set<Object> toRemove = new HashSet<Object>();

			@Override
			public synchronized void run() {
				synchronized (MultiThreadedEventDispatcher.this.dispatcherRoutes) {
					for (final Object object : MultiThreadedEventDispatcher.this.dispatcherRoutes
							.keySet()) {
						if (object instanceof SocketChannel) {
							final SocketChannel socket = (SocketChannel) object;
							if (!socket.isConnected()) {
								this.toRemove.add(object);
							}
						} else if (object instanceof HTTPProxyEvent) {
							final HTTPProxyEvent event = (HTTPProxyEvent) object;
							if (!event.getSocketChannel().isConnected()) {
								this.toRemove.add(object);
							}
						} else {
							throw new RuntimeException(
									"Something unexpected happened! invalid object in dispatcherRoutes!");
						}
					}

					for (final Object c : this.toRemove) {
						MultiThreadedEventDispatcher.this.dispatcherRoutes
								.remove(c);
					}
					this.toRemove.clear();
				}
			}
		};

		this.timeoutTimer.scheduleAtFixedRate(task, 0, 5000);
	}

	private synchronized UrgentEventDispatcher<T> getNextEventDispatcher() {
		synchronized (this.dispatcherRoutes) {
			final UrgentEventDispatcher<T> next = this.dispatchers
					.removeFirst();

			this.dispatchers.addLast(next);

			return next;
		}
	}

	private synchronized UrgentEventDispatcher<T> dispatcherForKey(
			final SocketChannel key) {
		synchronized (this.dispatcherRoutes) {
			if (!this.dispatcherRoutes.containsKey(key)) {
				this.dispatcherRoutes.put(key, this.getNextEventDispatcher());
			}

			return this.dispatcherRoutes.get(key);
		}
	}

	private UrgentEventDispatcher<T> solveEventDispatcher(
			final ServerDataEvent event) {
		final SocketChannel key = event.getChannel();

		return this.dispatcherForKey(key);
	}

	private UrgentEventDispatcher<T> solveEventDispatcher(
			final ClientDataEvent event) {
		// Attachment is HTTPProxyEvent which has same equals/hashcode as
		// socketChannel
		final SocketChannel key = ((HTTPProxyEvent) event.getAttachment())
				.getSocketChannel();

		return this.dispatcherForKey(key);
	}

	private UrgentEventDispatcher<T> solveEventDispatcher(
			final ErrorDataEvent event) {
		SocketChannel key = null;

		if (event.getErrorType() == ErrorDataEvent.REMOTE_CLIENT_DISCONNECT) {
			// HTTPProxyEvent owner of the error
			key = ((HTTPProxyEvent) event.getOwner()).getSocketChannel();
		} else {
			// SocketChannel owner of the error
			key = (SocketChannel) event.getAttachment();
		}

		return this.dispatcherForKey(key);
	}

	public UrgentEventDispatcher<T> solveEventDispatcher(final T event) {
		if (event instanceof ServerDataEvent) {
			return this.solveEventDispatcher((ServerDataEvent) event);
		}
		if (event instanceof ClientDataEvent) {
			return this.solveEventDispatcher((ClientDataEvent) event);
		}
		if (event instanceof ErrorDataEvent) {
			return this.solveEventDispatcher((ErrorDataEvent) event);
		}
		throw new RuntimeException("Invalid Data Event for this dispatcher!");
	}

	@Override
	public void processData(final T event) {
		final UrgentEventDispatcher<T> dispatcherForEvent = this
				.solveEventDispatcher(event);
		dispatcherForEvent.processData(event);
	}

	@Override
	public void processDataUrgent(final T event) {
		final UrgentEventDispatcher<T> dispatcherForEvent = this
				.solveEventDispatcher(event);
		dispatcherForEvent.processDataUrgent(event);
	}

}
