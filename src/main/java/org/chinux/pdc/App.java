package org.chinux.pdc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

	private NIOServer server;
	private Worker<DataEvent> worker;

	public App(final NIOServer server, final Worker<DataEvent> worker) {
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
