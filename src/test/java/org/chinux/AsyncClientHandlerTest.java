package org.chinux;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.chinux.pdc.nio.events.impl.NIODataEvent;
import org.chinux.pdc.nio.handlers.impl.AsyncClientHandler;
import org.chinux.pdc.nio.handlers.util.SocketChannelFactory;
import org.chinux.pdc.workers.Worker;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.action.VoidAction;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class AsyncClientHandlerTest {

	Mockery context = new JUnit4Mockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	@Mock
	Worker<NIODataEvent> mockWorker;

	@Mock
	SocketChannelFactory mockSocketFactory;

	@Mock
	SocketChannel mockSocketChannel;

	@Mock
	SelectionKey registerSelectionKey;

	@Mock
	SelectionKey connectSelectionKey;

	@Mock
	SelectionKey writeSelectionKey;

	@Mock
	SelectionKey readSelectionKey;

	@Mock
	Selector mockSelector;

	AsyncClientHandler handler;

	@Test
	public void testRequest() throws IOException {

		// Set the behaviour of the dependencies of the handler
		context.checking(new Expectations() {
			{
				oneOf(mockSocketFactory).getSocketChannel(
						with(any(InetSocketAddress.class)));
				will(returnValue(mockSocketChannel));

				allowing(mockSocketChannel).register(with(any(Selector.class)),
						with(any(Integer.class)));
				will(returnValue(registerSelectionKey));

				// oneOf(mockWorker).DoWork(with(any(NIODataEvent.class)));
				// oneOf(mockWorker).processData(anything());
			}
		});

		handler = new AsyncClientHandler(mockWorker, mockSocketFactory);

		handler.setSelector(mockSelector);

		context.assertIsSatisfied();

		final NIODataEvent event = new NIODataEvent(mockSocketChannel,
				"REQUEST".getBytes(), null);

		event.inetAddress = InetAddress.getLocalHost();
		event.owner = "Owner";

		handler.receiveEvent(event);

		context.checking(new Expectations() {
			{
				oneOf(registerSelectionKey).attach(with(anything()));
			}
		});

		handler.handlePendingChanges();

		context.assertIsSatisfied();

		context.checking(new Expectations() {
			{
				oneOf(mockSocketChannel).finishConnect();
				will(VoidAction.INSTANCE);
				oneOf(connectSelectionKey).channel();
				will(returnValue(mockSocketChannel));

				oneOf(connectSelectionKey).interestOps(
						with(SelectionKey.OP_WRITE));
			}
		});

		handler.handleConnection(connectSelectionKey);

		context.assertIsSatisfied();

		context.checking(new Expectations() {
			{
				oneOf(mockSocketChannel).write(with(any(ByteBuffer.class)));

				oneOf(writeSelectionKey).channel();
				will(returnValue(mockSocketChannel));

				oneOf(writeSelectionKey)
						.interestOps(with(SelectionKey.OP_READ));
			}
		});

		handler.handleWrite(writeSelectionKey);

		context.assertIsSatisfied();
	}
}
