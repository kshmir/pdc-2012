package org.chinux.pdc.workers.impl;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import org.chinux.pdc.http.api.HTTPRequest;
import org.chinux.pdc.http.api.HTTPResponse;
import org.chinux.pdc.server.Configuration;
import org.chinux.pdc.server.ConfigurationProvider;
import org.chinux.pdc.server.HTTPClientInfo;

/**
 * Represents a unique httprequest sent by a client. When receiving a new
 * httpresponse object, we know which request is the owner by this object And we
 * also know which is the client to answer to
 * 
 * @author cris
 */
public class HTTPProxyEvent {

	public HTTPProxyEvent next;

	private HTTPRequest request;
	private HTTPResponse response;
	private SocketChannel socketChannel;
	private ByteBuffer parseServerOffsetData;
	private ByteBuffer parseClientOffsetData;
	private Charset dataCharset = Charset.forName("ISO-8859-1"); // Por ahora re
																	// va

	private Configuration eventConfiguration = null;

	private StringBuilder builder = new StringBuilder();
	private boolean canSend;
	private boolean canClose;
	private InetAddress address;

	public HTTPProxyEvent(final HTTPRequest request, final SocketChannel channel) {
		this.request = request;
		this.socketChannel = channel;
		this.eventConfiguration = ConfigurationProvider
				.getConfiguration(new HTTPClientInfo(this));
	}

	public boolean canClose() {
		return this.canClose;
	}

	public boolean canSend() {
		return this.canSend;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		final HTTPProxyEvent other = (HTTPProxyEvent) obj;
		if (this.getSocketChannel() == null) {
			if (other.getSocketChannel() != null) {
				return false;
			}
		} else if (!this.getSocketChannel().equals(other.getSocketChannel())) {
			return false;
		}
		return true;
	}

	public InetAddress getAddress() {
		return this.address;
	}

	public StringBuilder getBuilder() {
		return this.builder;
	}

	public Charset getDataCharset() {
		return this.dataCharset;
	}

	public HTTPRequest getRequest() {
		return this.request;
	}

	public HTTPResponse getResponse() {
		return this.response;
	}

	public SocketChannel getSocketChannel() {
		return this.socketChannel;
	}

	@Override
	public int hashCode() {
		return this.getSocketChannel().hashCode();
	}

	public void setAddress(final InetAddress address) {
		this.address = address;
	}

	public void setBuilder(final StringBuilder builder) {
		this.builder = builder;
	}

	public void setCanClose(final boolean canClose) {
		this.canClose = canClose;
	}

	public void setCanSend(final boolean canSend) {
		this.canSend = canSend;
	}

	public void setDataCharset(final Charset dataCharset) {
		this.dataCharset = dataCharset;
	}

	public void setResponse(final HTTPResponse response) {
		this.response = response;
	}

	public Configuration getEventConfiguration() {
		return this.eventConfiguration;
	}

	public ByteBuffer getParseOffsetData() {
		return this.parseServerOffsetData;
	}

	public void setParseOffsetData(final ByteBuffer parseOffsetData) {
		this.parseServerOffsetData = parseOffsetData;
	}

	public ByteBuffer getParseClientOffsetData() {
		return this.parseClientOffsetData;
	}

	public void setParseClientOffsetData(final ByteBuffer parseClientOffsetData) {
		this.parseClientOffsetData = parseClientOffsetData;
	}
}