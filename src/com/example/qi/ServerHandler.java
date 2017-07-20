package com.example.qi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import com.example.qi.utils.MessageParser;

/**
 * 消息处理
 * 
 * @author Administrator
 *
 */
public class ServerHandler implements Runnable {

	private Selector selector;
	private InetSocketAddress inetSocketAddress;
	private Set<SocketChannel> sockets = new HashSet<SocketChannel>();

	public ServerHandler(int port) {
		inetSocketAddress = new InetSocketAddress("172.26.52.1", port);
	}

	@Override
	public void run() {
		try {
			ServerSocketChannel ssc = null;
			ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.bind(inetSocketAddress);
			selector = Selector.open();
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("服务器已启动。。。");
			while (true) {
				selector.select(1000);
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					if (!key.isValid()) {
						continue;
					}

					if (key.isAcceptable()) {
						accept(key);
					}
					if (key.isReadable()) {
						readable(key);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	/**
	 * 从通道中读取消息
	 * 
	 * @param key
	 * @throws Exception
	 */
	private void readable(SelectionKey key) throws Exception {
		Charset charset = Charset.defaultCharset();
		SocketChannel sc = (SocketChannel) key.channel();
		StringBuffer sb = new StringBuffer();
		ByteBuffer dst = ByteBuffer.allocate(4 * 1024);
		int len = 0;
		while ((len = sc.read(dst)) != 0) {
			dst.flip();
			sb.append(charset.decode(dst));
			dst.clear();
		}
		System.out.println("客户端：" + sb.toString());
		MessageParser.parse(sb, sc);
	}

	/**
	 * 接收客户端socket
	 * 
	 * @param key
	 * @throws IOException
	 */
	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
		SocketChannel sc = ssc.accept();
		sockets.add(sc);
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		System.out.println("连接到客户端：" + sc.getRemoteAddress());
	}

}
