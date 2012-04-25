package org.chinux;

import java.nio.channels.SocketChannel;

import junit.framework.Assert;

import org.chinux.pdc.nio.events.api.DataReceiver;
import org.chinux.pdc.nio.events.impl.NIODataEvent;
import org.chinux.pdc.workers.EchoWorker;
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
	public void doWorkTest() {
		final SocketChannel socket = this.context.mock(SocketChannel.class);

		final EchoWorker worker = new EchoWorker();

		final byte[] data = "data".getBytes();

		@SuppressWarnings("unchecked")
		final NIODataEvent event = new NIODataEvent(socket, data,
				this.context.mock(DataReceiver.class));

		final NIODataEvent echoEvent = worker.DoWork(event);

		Assert.assertEquals(new String(event.data), new String(echoEvent.data));
		Assert.assertEquals(true, echoEvent.canSend());
	}
}
