package org.chinux.pdc.nio.services.util;

import java.nio.channels.SocketChannel;

public class ChangeRequest {
	public static final int REGISTER = 1;
	public static final int CHANGEOPS = 2;
	public static final int CLOSE = 3;
	public static final int MAKE_NEW = 4;

	public SocketChannel socket;
	public int type;
	public int ops;
	public Object attachment;

	public ChangeRequest(final SocketChannel socket, final int type,
			final int ops) {
		this(socket, type, ops, null);
	}

	public ChangeRequest(final SocketChannel socket, final int type,
			final int ops, final Object attachment) {
		this.socket = socket;
		this.type = type;
		this.ops = ops;
		this.attachment = attachment;
	}
}
