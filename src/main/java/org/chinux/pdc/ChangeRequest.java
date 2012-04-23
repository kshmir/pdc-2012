package org.chinux.pdc;

import java.nio.channels.SocketChannel;

public class ChangeRequest {
	public static final int REGISTER = 1;
	public static final int CHANGEOPS = 2;
	public static final int CLOSE = 3;

	public SocketChannel socket;
	public int type;
	public int ops;

	public ChangeRequest(final SocketChannel socket, final int type,
			final int ops) {
		this.socket = socket;
		this.type = type;
		this.ops = ops;
	}
}
