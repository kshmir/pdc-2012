package org.chinux;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.chinux.pdc.nio.events.impl.NIODataEvent;
import org.chinux.pdc.nio.handlers.impl.AsyncClientHandler;
import org.chinux.pdc.nio.handlers.impl.ServerHandler;
import org.chinux.pdc.nio.handlers.util.SocketChannelFactoryImpl;
import org.chinux.pdc.nio.services.NIOClient;
import org.chinux.pdc.nio.services.NIOServer;
import org.chinux.pdc.nio.services.util.ClientSelectorFactoryImpl;
import org.chinux.pdc.nio.services.util.ServerSelectorFactoryImpl;
import org.chinux.pdc.workers.EchoWorker;
import org.chinux.pdc.workers.Worker;
import org.junit.Test;

public class NIOServerAndClientTest {

	String receivedString;

	@Test
	public void test() throws IOException, InterruptedException {

		final ScheduledExecutorService service = Executors
				.newScheduledThreadPool(3);

		service.awaitTermination(100, TimeUnit.MILLISECONDS);

		final EchoWorker worker = new EchoWorker();

		final AsyncClientHandler clientHandler = new AsyncClientHandler(
				new Worker<NIODataEvent>() {
					@Override
					public void processData(final Object event) {
						final NIODataEvent nioEvent = (NIODataEvent) event;

						receivedString = new String(nioEvent.getData()).trim();
					}

					@Override
					public NIODataEvent DoWork(final NIODataEvent dataEvent) {
						return dataEvent;
					}
				}, new SocketChannelFactoryImpl());

		@SuppressWarnings("unchecked")
		final ServerHandler serverHandler = new ServerHandler((Worker) worker);

		final NIOClient client = new NIOClient(9090,
				new ClientSelectorFactoryImpl());
		client.setHandler(clientHandler);

		final NIOServer server = new NIOServer(9090,
				new ServerSelectorFactoryImpl());

		server.setHandler(serverHandler);

		service.execute(worker);
		service.execute(client);
		service.execute(server);

		service.awaitTermination(100, TimeUnit.MILLISECONDS);

		// Wait for initial locks

		final NIODataEvent event = new NIODataEvent(null, "HOLA".getBytes(),
				clientHandler);
		event.inetAddress = InetAddress.getLocalHost();

		clientHandler.receiveEvent(event);

		service.awaitTermination(100, TimeUnit.MILLISECONDS);

		service.shutdownNow();

		Assert.assertEquals("HOLA", receivedString);
	}
}
