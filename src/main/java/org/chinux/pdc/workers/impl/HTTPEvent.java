package org.chinux.pdc.workers.impl;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import org.chinux.pdc.http.api.HTTPRequest;
import org.chinux.pdc.http.api.HTTPResponse;
import org.chinux.pdc.server.Configuration;
import org.chinux.pdc.server.ConfigurationProvider;

/**
 * Represents a unique httprequest sent by a client. When receiving a new
 * httpresponse object, we know which request is the owner by this object And we
 * also know which is the client to answer to
 * 
 * @author cris
 */
public class HTTPEvent {
	private HTTPRequest request;
	private HTTPResponse response;
	private SocketChannel socketChannel;
	private Charset dataCharset = Charset.forName("ISO-8859-1"); // Por ahora re
																	// va

	private Configuration eventConfiguration = ConfigurationProvider
			.getConfiguration();

	private StringBuilder builder = new StringBuilder();
	private boolean canSend;
	private boolean canClose;
	private InetAddress address;

	public HTTPEvent(final HTTPRequest request, final SocketChannel channel) {
		this.request = request;
		this.socketChannel = channel;
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
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final HTTPEvent other = (HTTPEvent) obj;
		if (this.getSocketChannel() == null) {
			if (other.getSocketChannel() != null) {
				return false;
			}
		} else if (!this.getSocketChannel().equals(other.getSocketChannel())) {
			return false;
		}
		if (this.getRequest() == null) {
			if (other.getRequest() != null) {
				return false;
			}
		} else if (!this.getRequest().equals(other.getRequest())) {
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
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.getSocketChannel() == null) ? 0 : this
						.getSocketChannel().hashCode());
		result = prime
				* result
				+ ((this.getRequest() == null) ? 0 : this.getRequest()
						.hashCode());
		return result;
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
		return eventConfiguration;
	}
}