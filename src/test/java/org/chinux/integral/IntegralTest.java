package org.chinux.integral;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.chinux.pdc.nio.dispatchers.ASyncEventDispatcher;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.handlers.impl.ServerHandler;
import org.chinux.pdc.nio.receivers.api.ClientDataReceiver;
import org.chinux.pdc.nio.receivers.impl.ASyncClientDataReceiver;
import org.chinux.pdc.workers.impl.EchoWorker;

/**
 * Tests all the complete life cicle of a request
 * 
 * 1 -instantiates external fake server 2- instantiates proxy 3- makes a fixed
 * request to fake server via proxy
 * 
 * @author joseignaciosg
 */
public class IntegralTest {

	private String toSendString = "HOLA";

	private String receivedString;

	// @Test
	public void test() throws IOException, InterruptedException {

		// Scheduler to prepare 4 Threads proxy (worker, client, server), and
		// fake external server
		final ScheduledExecutorService service = Executors
				.newScheduledThreadPool(4);

		// Worker for the fake external server

		// Echo Worker for the server
		final EchoWorker worker = new EchoWorker();

		final ASyncEventDispatcher<DataEvent> serverDispatcher = new ASyncEventDispatcher<DataEvent>(
				worker);
		//
		// final EventDispatcher<DataEvent> clientDispatcher = new
		// SyncEventDispatcher<DataEvent>(
		// new Worker<DataEvent>() {
		// @Override
		// public DataEvent DoWork(final DataEvent dataEvent) {
		// IntegralTest.this.receivedString = new String(
		// dataEvent.getData()).trim();
		// return dataEvent;
		// }
		// });

		final ClientDataReceiver clientReceiver = new ASyncClientDataReceiver();

		// Server logic
		final ServerHandler serverHandler = new ServerHandler();

		serverHandler.setEventDispatcher(serverDispatcher);

		// Client logic
		// final ClientHandler clientHandler = new
		// ClientHandler(clientDispatcher,
		// clientReceiver);
		//
		// final NIOClient client = new NIOClient(9090);
		// client.setHandler(clientHandler);
		//
		// final NIOServer server = new NIOServer(9090);

		// server.setHandler(serverHandler);
		//
		// service.execute(serverDispatcher);
		// service.execute(client);
		// service.execute(server);
		//
		// service.awaitTermination(1, TimeUnit.MILLISECONDS);
		//
		// final DataEvent event = new ClientDataEvent(
		// this.toSendString.getBytes(), InetAddress.getLocalHost(),
		// clientHandler);
		//
		// clientReceiver.receiveEvent(event);

		// 1ms should be enough to receive the data
		service.awaitTermination(30, TimeUnit.MILLISECONDS);

		service.shutdownNow();

		// We got the string, yay!
		Assert.assertEquals(this.toSendString, this.receivedString);
	}

}
