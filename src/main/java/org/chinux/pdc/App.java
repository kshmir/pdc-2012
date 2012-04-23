package org.chinux.pdc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

	public void run(final NIOServer server, final Runnable client,
			final Worker<DataEvent> worker) {

		new Thread(server).run();
		new Thread(client).run();
		new Thread(worker).run();
	}

	public static void main(final String[] args) {
		new ClassPathXmlApplicationContext("META-INF/beans.xml");
	}
}
