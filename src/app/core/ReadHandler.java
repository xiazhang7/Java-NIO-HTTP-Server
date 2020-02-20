package app.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import app.res.Contents;
import app.util.RequestBuilder;

public class ReadHandler implements Runnable {
	private SelectionKey key;

	public ReadHandler(SelectionKey key) {
		this.key = key;
	}

	@Override
	public void run() {
		SocketChannel sc = (SocketChannel) key.channel();
		Contents c = (Contents) key.attachment();
		RequestBuilder rb = c.getRequestBuilder();
		ByteBuffer bb = rb.getByteBuffer();
		int length = 0;
		try {
			while ((length = sc.read(bb)) > 0) {
				switch (rb.build(length)) {
				case RequestBuilder.DONE:
					key.interestOps(SelectionKey.OP_WRITE);
					key.selector().wakeup();
					return;
				case RequestBuilder.ERROR:
					c.back();
					return;
				}
				bb.clear();
			}
			if (length == -1) {
				c.back();
				return;
			}
			key.interestOps(SelectionKey.OP_READ);
			key.selector().wakeup();
		} catch (IOException e) {
			c.back();
		}
	}
}