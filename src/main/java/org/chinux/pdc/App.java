package org.chinux.pdc;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.services.NIOServer;
import org.chinux.pdc.workers.ASyncWorker;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

	private NIOServer server;
	private ASyncWorker<DataEvent> worker;

	public App(final NIOServer server, final ASyncWorker<DataEvent> worker) {
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
