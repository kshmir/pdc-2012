package org.chinux.pdc.nio.receivers.impl;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class TimeoutablePool {

	private Timer timeoutTimer = new Timer();

	private Map<Object, LinkedList<SocketChannel>> elements = new HashMap<Object, LinkedList<SocketChannel>>();
	private Map<SocketChannel, Long> timeAdded = new HashMap<SocketChannel, Long>();

	private int seconds;

	private static Logger log = Logger.getLogger(TimeoutablePool.class);

	public TimeoutablePool(final int seconds) {
		this.seconds = seconds;
		final TimerTask task = new TimerTask() {

			private Set<Object> toRemove = new HashSet<Object>();

			@Override
			public synchronized void run() {
				for (final SocketChannel channel : TimeoutablePool.this.timeAdded
						.keySet()) {
					if (TimeoutablePool.this.seconds * 1000 < System
							.currentTimeMillis()
							- TimeoutablePool.this.timeAdded.get(channel)) {
						try {
							channel.close();
							this.toRemove.add(channel);
						} catch (final IOException e) {
							e.printStackTrace();
						}
					}
				}

				log.info("Idle sockets: "
						+ TimeoutablePool.this.timeAdded.size());

				for (final Object c : this.toRemove) {
					TimeoutablePool.this.elements.remove(c);
					TimeoutablePool.this.timeAdded.remove(c);
				}
			}
		};

		this.timeoutTimer.scheduleAtFixedRate(task, 0, 1000);
	}

	public synchronized void saveObject(final Object key,
			final SocketChannel channel) {
		if (!channel.isConnected()) {
			return;
		}

		if (!this.elements.containsKey(key)) {
			this.elements.put(key, new LinkedList<SocketChannel>());

		}
		this.timeAdded.put(channel, System.currentTimeMillis());
		this.elements.get(key).add(channel);
		log.info("Saving socketChannel");
	}

	public synchronized SocketChannel getObject(final Object key) {
		if (this.elements.containsKey(key) && this.elements.get(key).size() > 0) {
			final SocketChannel answer = this.elements.get(key).removeFirst();
			this.timeAdded.remove(answer);
			if (answer.isConnected()) {
				log.info("!!!!!!!!!! Reusing socketChannel " + answer);
				return answer;
			}
		}
		return null;
	}
}
