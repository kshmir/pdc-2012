package org.chinux;

import java.nio.channels.SocketChannel;

import junit.framework.Assert;

import org.chinux.pdc.nio.events.api.DataReceiver;
import org.chinux.pdc.nio.events.impl.NIODataEvent;
import org.chinux.pdc.workers.EchoWorker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EchoWorkerTest {

	@Mock
	private SocketChannel socket;

	@Mock
	private DataReceiver<NIODataEvent> receiver;

	@Test
	public void doWorkTest() {
		final EchoWorker worker = new EchoWorker();

		final byte[] data = "data".getBytes();

		final NIODataEvent event = new NIODataEvent(socket, data, receiver);
		final NIODataEvent echoEvent = worker.DoWork(event);

		Assert.assertEquals(new String(event.data), new String(echoEvent.data));
		Assert.assertEquals(true, echoEvent.canSend());
	}
}
