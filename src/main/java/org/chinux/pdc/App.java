package org.chinux.pdc;

import java.io.IOException;
import java.net.InetAddress;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.NIODataEvent;
import org.chinux.pdc.nio.handlers.impl.AsyncClientHandler;
import org.chinux.pdc.nio.handlers.util.SocketChannelFactoryImpl;
import org.chinux.pdc.nio.services.NIOClient;
import org.chinux.pdc.nio.services.NIOServer;
import org.chinux.pdc.nio.services.util.ClientSelectorFactoryImpl;
import org.chinux.pdc.workers.ASyncWorker;
import org.chinux.pdc.workers.Worker;
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

		// new Thread(this.worker).start();
		// new Thread(this.server).start();
		// new Thread(client).run();

		try {
			final AsyncClientHandler handler = new AsyncClientHandler(
					new Worker<NIODataEvent>() {
						@Override
						public void processData(final Object obj) {
							final NIODataEvent event = (NIODataEvent) obj;
							System.out.println(new String(event.data));
						}

						@Override
						public NIODataEvent DoWork(final NIODataEvent dataEvent) {
							return null;
						}
					}, new SocketChannelFactoryImpl());

			final NIOClient client = new NIOClient(80,
					new ClientSelectorFactoryImpl());
			client.setHandler(handler);

			new Thread(client).start();

			Thread.sleep(1000);

			final NIODataEvent event = new NIODataEvent(null,
					"GET / HTTP/1.1\n\n".getBytes(), handler);

			event.inetAddress = InetAddress.getByName("www.google.com");
			event.owner = "HOLA";

			handler.receiveEvent(event);

		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) {
		new ClassPathXmlApplicationContext("META-INF/beans.xml");
	}
}
