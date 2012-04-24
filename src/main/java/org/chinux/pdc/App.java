package org.chinux.pdc;

import org.chinux.pdc.events.DataEvent;
import org.chinux.pdc.workers.WorkerAsync;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

	private NIOServer server;
	private WorkerAsync<DataEvent> worker;

	public App(final NIOServer server, final WorkerAsync<DataEvent> worker) {
		this.server = server;
		this.worker = worker;

		this.run();
	}

	public void run() {

		new Thread(this.worker).start();
		new Thread(this.server).start();
		// new Thread(client).run();

	}

	public static void main(final String[] args) {
		new ClassPathXmlApplicationContext("META-INF/beans.xml");
	}
}
