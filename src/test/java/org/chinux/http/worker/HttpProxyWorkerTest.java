package org.chinux.http.worker;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import junit.framework.Assert;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.nio.receivers.api.DataReceiver;
import org.chinux.pdc.workers.impl.HttpProxyWorker;
import org.chinux.pdc.workers.impl.HttpProxyWorker.HTTPEvent;
import org.chinux.util.TestUtils;
import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("unchecked")
public class HttpProxyWorkerTest {

	final SocketChannel channel = Mockito.mock(SocketChannel.class);

	final Socket socket = Mockito.mock(Socket.class);

	@Test
	public void testBasicGetResponse() throws UnknownHostException {
		final String response = TestUtils
				.stringFromFile("http/responses/response4.txt");

		final InetAddress address = InetAddress.getLocalHost();
		final HttpProxyWorker worker = new HttpProxyWorker();

		final HTTPEvent event = new HTTPEvent(null, null);

		final DataReceiver<DataEvent> clientReceiver = Mockito
				.mock(DataReceiver.class);

		worker.setClientDataReceiver(clientReceiver);

		Mockito.when(this.channel.socket()).thenReturn(this.socket);
		Mockito.when(this.socket.getInetAddress()).thenReturn(address);

		final ClientDataEvent clientEvent = new ClientDataEvent(
				response.getBytes(), event);

		final ServerDataEvent answer = (ServerDataEvent) worker
				.DoWork(clientEvent);

		Assert.assertEquals(response, new String(answer.getData()));

	}

	@Test
	public void testBasicGetRequest() throws UnknownHostException {
		final String get = "GET / HTTP/1.0\r\n\r\n";

		final InetAddress address = InetAddress.getLocalHost();
		final HttpProxyWorker worker = new HttpProxyWorker();
		final ClientDataEvent answer = this.testWorkerRequest(get, address,
				worker);

		Assert.assertEquals(get, new String(answer.getData()));
		Assert.assertEquals(true, answer.canSend());
		Assert.assertNotNull(answer.getOwner());
	}

	@Test
	public void testBasicGetRequestWithHost() throws UnknownHostException {
		final String get = "GET / HTTP/1.0\r\nHost: localhost\r\n\r\n";

		final InetAddress address = InetAddress.getByName("localhost");
		final HttpProxyWorker worker = new HttpProxyWorker();

		final ClientDataEvent answer = this.testWorkerRequest(get, address,
				worker);

		Assert.assertEquals(get, new String(answer.getData()));
		Assert.assertEquals(true, answer.canSend());
		Assert.assertNotNull(answer.getOwner());
	}

	@Test
	public void testBasicPostRequestWithData() throws UnknownHostException {
		final String get = "POST / HTTP/1.0\r\nHost: localhost\r\nContent-Length: 10\r\n\r\n0123456789\r\n";

		final InetAddress address = InetAddress.getByName("localhost");
		final HttpProxyWorker worker = new HttpProxyWorker();

		final ClientDataEvent answer = this.testWorkerRequest(get, address,
				worker);

		Assert.assertEquals(get, new String(answer.getData()));
		Assert.assertEquals(true, answer.canSend());
		Assert.assertNotNull(answer.getOwner());
	}

	@Test
	public void testSplittedPostRequestWithData() throws UnknownHostException {
		final String get = "POST / HTTP/1.0\r\nHost: localhost\r\n";
		final String get2 = "Content-Length: 10\r\n\r\n0123456789\r\n";

		final InetAddress address = InetAddress.getByName("localhost");
		final HttpProxyWorker worker = new HttpProxyWorker();

		final ClientDataEvent a = this.testWorkerRequest(get, address, worker);

		Assert.assertEquals(false, a.canSend());

		final ClientDataEvent answer = this.testWorkerRequest(get2, address,
				worker);

		Assert.assertEquals(get + get2, new String(answer.getData()));
		Assert.assertEquals(true, answer.canSend());
		Assert.assertNotNull(answer.getOwner());
	}

	private ClientDataEvent testWorkerRequest(final String get,
			final InetAddress address, final HttpProxyWorker worker) {
		final DataReceiver<DataEvent> clientReceiver = Mockito
				.mock(DataReceiver.class);

		worker.setClientDataReceiver(clientReceiver);

		Mockito.when(this.channel.socket()).thenReturn(this.socket);
		Mockito.when(this.socket.getInetAddress()).thenReturn(address);

		final ServerDataEvent event = new ServerDataEvent(this.channel,
				get.getBytes());

		final ClientDataEvent answer = (ClientDataEvent) worker.DoWork(event);

		Assert.assertEquals(address, answer.getAddress());
		Assert.assertEquals(clientReceiver, answer.getReceiver());
		return answer;
	}
}
