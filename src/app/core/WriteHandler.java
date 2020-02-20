package app.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import app.res.Contents;
import app.util.ResponseBuilder;

public class WriteHandler implements Runnable {
	private SelectionKey key;

	public WriteHandler(SelectionKey key) {
		this.key = key;
	}

	@Override
	public void run() {
		SocketChannel sc = (SocketChannel) key.channel();
		Contents c = (Contents) key.attachment();
		ResponseBuilder rb = c.getResponseBuilder();
		try {
			ByteBuffer bb = rb.build();
			sc.write(bb);
			if (rb.build() != null) {
				key.interestOps(SelectionKey.OP_WRITE);
			} else if (rb.getStat() == ResponseBuilder.CONTINUE) {
				key.interestOps(SelectionKey.OP_READ);
			} else {
				c.back();
				return;
			}
			key.selector().wakeup();
		} catch (IOException e) {
			c.back();
		}
	}
}