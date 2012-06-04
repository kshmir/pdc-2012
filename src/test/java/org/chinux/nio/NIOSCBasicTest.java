package org.chinux.nio;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.chinux.pdc.nio.dispatchers.ASyncEventDispatcher;
import org.chinux.pdc.nio.dispatchers.EventDispatcher;
import org.chinux.pdc.nio.dispatchers.SyncEventDispatcher;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.handlers.impl.ClientHandler;
import org.chinux.pdc.nio.handlers.impl.ServerHandler;
import org.chinux.pdc.nio.receivers.api.ClientDataReceiver;
import org.chinux.pdc.nio.receivers.impl.ASyncClientDataReceiver;
import org.chinux.pdc.nio.services.NIOClient;
import org.chinux.pdc.nio.services.NIOServer;
import org.chinux.pdc.workers.api.Worker;
import org.chinux.pdc.workers.impl.EchoWorker;
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

	@Test
	public void test() throws IOException, InterruptedException {

		// Scheduler to prepare 3 threads, worker, client and server
		final ScheduledExecutorService service = Executors
				.newScheduledThreadPool(3);

		// Echo Worker for the server
		final EchoWorker worker = new EchoWorker();

		final ASyncEventDispatcher<DataEvent> serverDispatcher = new ASyncEventDispatcher<DataEvent>(
				worker);

		final EventDispatcher<DataEvent> clientDispatcher = new SyncEventDispatcher<DataEvent>(
				new Worker<DataEvent>() {
					@Override
					public DataEvent DoWork(final DataEvent dataEvent) {
						NIOSCBasicTest.this.receivedString = Charset
								.forName("ISO-8859-1")
								.decode(dataEvent.getData()).toString().trim();
						return dataEvent;
					}
				});

		final ClientDataReceiver clientReceiver = new ASyncClientDataReceiver();

		// Server logic
		final ServerHandler serverHandler = new ServerHandler();

		serverHandler.setEventDispatcher(serverDispatcher);

		// Client logic
		final ClientHandler clientHandler = new ClientHandler(clientReceiver);

		clientHandler.setEventDispatcher(clientDispatcher);

		final NIOClient client = new NIOClient(9090);
		client.setHandler(clientHandler);

		final NIOServer server = new NIOServer(9090);

		server.setHandler(serverHandler);

		service.execute(serverDispatcher);
		service.execute(client);
		service.execute(server);

		service.awaitTermination(1, TimeUnit.MILLISECONDS);

		final DataEvent event = new ClientDataEvent(
				ByteBuffer.wrap(this.toSendString.getBytes()),
				InetAddress.getLocalHost(), clientHandler);

		clientReceiver.receiveEvent(event);

		// 100ms should be enough to receive the data
		service.awaitTermination(100, TimeUnit.MILLISECONDS);

		service.shutdownNow();

		// We got the string, yay!
		Assert.assertEquals(this.toSendString, this.receivedString);
	}
}
