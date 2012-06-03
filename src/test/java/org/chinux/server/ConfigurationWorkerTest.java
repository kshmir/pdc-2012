//package org.chinux.server;
//
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.net.Socket;
//import java.net.SocketAddress;
//import java.net.UnknownHostException;
//import java.nio.ByteBuffer;
//import java.nio.CharBuffer;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//import java.nio.channels.spi.SelectorProvider;
//import java.nio.charset.Charset;
//import java.nio.charset.CharsetDecoder;
//import java.nio.charset.CharsetEncoder;
//
//import org.chinux.pdc.nio.events.api.DataEvent;
//import org.chinux.pdc.nio.events.impl.ClientDataEvent;
//import org.chinux.pdc.workers.impl.ConfigurationWorker;
//import org.junit.Test;
//
//public class ConfigurationWorkerTest {
//
//	public Charset charset = Charset.forName("UTF-8");
//	public CharsetEncoder encoder = this.charset.newEncoder();
//	public CharsetDecoder decoder = this.charset.newDecoder();
//
//	private ConfigurationWorker worker;
//
//	public void init() {
//		final String propertiespath = "src/main/resources/users.properties";
//		this.worker = new ConfigurationWorker(propertiespath);
//	}
//
//	@Test
//	public void loginTest(){
//		final Selector socketSelector = SelectorProvider.provider()
//				.openSelector();
//		final ServerSocketChannel serverChannel = ServerSocketChannel.open();
//		serverChannel.configureBlocking(false);
//		final InetSocketAddress isa = new InetSocketAddress("localhost", 8080);
//		serverChannel.socket().bind(isa);
//		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
//		final ClientDataEvent dataEvent;
//		final ByteBuffer data;
//		/*Hello simulation*/
//		
//		data =  this.str_to_bb("HELO");
//		final SocketChannel channel = new MySocketChannel();
//		
//		this.worker.DoWork(new ClientDataEvent(data, attachment))
//		
//		login(final DataEvent dataEvent, final String command)
//	}
//
//	public ByteBuffer str_to_bb(final String msg) {
//		try {
//			return this.encoder.encode(CharBuffer.wrap(msg));
//		} catch (final Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public String bb_to_str(final ByteBuffer buffer) {
//		String data = "";
//		try {
//			final int old_position = buffer.position();
//			data = this.decoder.decode(buffer).toString();
//			// reset buffer's position to its original so it is not altered:
//			buffer.position(old_position);
//		} catch (final Exception e) {
//			e.printStackTrace();
//			return "";
//		}
//		return data;
//	}
//
// }
