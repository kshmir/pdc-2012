package org.chinux;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.chinux.pdc.nio.events.impl.NIOClientDataEvent;
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

/**
 * Tests a basic connection between a client and server
 * 
 * @author cris
 */
public class NIOSCBasicTest {

	// String to be sent to the server and to be received back by the echo
	// worker.
	private String toSendString = "HOLA";

	// String to be received from the server
	private String receivedString;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void test() throws IOException, InterruptedException {

		// Scheduler to prepare 3 threads, worker, client and server
		final ScheduledExecutorService service = Executors
				.newScheduledThreadPool(3);

		// Echo Worker for the server
		final EchoWorker worker = new EchoWorker();

		// Server logic
		final ServerHandler serverHandler = new ServerHandler((Worker) worker);

		// Client logic
		final AsyncClientHandler clientHandler = new AsyncClientHandler(
				new Worker<NIODataEvent>() {
					@Override
					public void processData(final NIODataEvent event) {
						receivedString = new String(event.getData()).trim();
					}

					@Override
					public NIODataEvent DoWork(final NIODataEvent dataEvent) {
						return dataEvent;
					}
				}, new SocketChannelFactoryImpl());

		final NIOClient client = new NIOClient(9090,
				new ClientSelectorFactoryImpl());
		client.setHandler(clientHandler);

		final NIOServer server = new NIOServer(9090,
				new ServerSelectorFactoryImpl());

		server.setHandler(serverHandler);

		service.execute(worker);
		service.execute(client);
		service.execute(server);

		service.awaitTermination(1, TimeUnit.MILLISECONDS);

		final NIODataEvent event = new NIOClientDataEvent(
				toSendString.getBytes(), InetAddress.getLocalHost(),
				clientHandler);

		clientHandler.receiveEvent(event);

		// 1ms should be enough to receive the data
		service.awaitTermination(10, TimeUnit.MILLISECONDS);

		service.shutdownNow();

		// We got the string, yay!
		Assert.assertEquals(toSendString, receivedString);
	}
}
