package app.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.res.Contents;

public class Server implements Runnable {
	private final static String DEFAULT_HOST = "0.0.0.0";
	private final static int DEFAULT_PORT = 80;

	private String host;
	private int port;
	private Selector selector;
	private boolean listened;
	private boolean hasThread;
	private Thread thread;
	private List<MiddleWare> middleWareList = Collections.synchronizedList(new ArrayList<>());
	private ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	public Server() throws IOException {
		this(DEFAULT_PORT);
	}

	public Server(int port) throws IOException {
		this(DEFAULT_HOST, port);
	}

	public Server(String host, int port) throws IOException {
		this.host = host;
		this.port = port;
		listened = false;
		hasThread = false;
		selector = Selector.open();
	}

	public void use(MiddleWare mw) {
		middleWareList.add(mw);
	}

	public void bind(int port) {
		this.port = port;
	}

	public void bind(String host, int port) {
		this.host = host;
		bind(port);
	}

	public void listen() throws IOException {
		listen(false);
	}

	public void listen(int port) throws IOException {
		this.port = port;
		listen();
	}

	public void listen(int port, boolean block) throws IOException {
		this.port = port;
		listen(block);
	}

	public void listen(String host, int port) throws IOException {
		this.host = host;
		listen(port);
	}

	public void listen(String host, int port, boolean block) throws IOException {
		this.host = host;
		listen(port, block);
	}

	public void listen(boolean block) throws IOException {
		doListen();
		if (!listened) {
			listened = true;
			if (block) {
				run();
			} else {
				hasThread = true;
				thread = new Thread(this);
				thread.start();
			}
		}
	}

	private void doListen() throws IOException {
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.socket().bind(new InetSocketAddress(host, port));
		ssc.register(selector, SelectionKey.OP_ACCEPT);
	}

	@Override
	public void run() {
		try {
			if (!listened)
				doListen();
			if (hasThread)
				return;
			while (true) {
				if (selector.select() <= 0)
					continue;
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
					if (key.isAcceptable()) {
						ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
						SocketChannel sc = ssc.accept();
						sc.configureBlocking(false);
						sc.register(selector, SelectionKey.OP_READ, Contents.get(middleWareList, sc));
					} else if (key.isValid()) {
						if (key.isReadable()) {
							key.interestOps(0);
							threadPool.execute(new ReadHandler(key));
						} else if (key.isWritable()) {
							key.interestOps(0);
							threadPool.execute(new WriteHandler(key));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}