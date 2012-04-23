package org.chinux;

import java.nio.channels.SocketChannel;

import junit.framework.Assert;

import org.chinux.pdc.DataReceiver;
import org.chinux.pdc.EchoWorker;
import org.chinux.pdc.NIODataEvent;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

public class EchoWorkerTest {
	Mockery context = new Mockery() {
		{
			this.setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	@Test
	@SuppressWarnings("unchecked")
	public void doWorkTest() {
		final DataReceiver<NIODataEvent> receiver = this.context
				.mock(DataReceiver.class);

		final SocketChannel socket = this.context.mock(SocketChannel.class);

		final EchoWorker worker = new EchoWorker(receiver);

		final byte[] data = "data".getBytes();

		final NIODataEvent event = new NIODataEvent(socket, data);

		final NIODataEvent echoEvent = worker.DoWork(event);

		Assert.assertEquals(new String(event.data), new String(echoEvent.data));
		Assert.assertEquals(true, echoEvent.canSend());
	}
}
