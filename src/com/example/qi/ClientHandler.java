package com.example.qi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.example.qi.utils.MessageParser;

public class ClientHandler implements Runnable {

	private InetSocketAddress address;
	private Selector selector;
	SocketChannel sc = null;

	public ClientHandler(String hostname, int port) {
		address = new InetSocketAddress(hostname, port);
	}

	@Override
	public void run() {
		try {
			sc = SocketChannel.open();
			sc.configureBlocking(false);
			selector = Selector.open();
			sc.connect(address);
			sc.register(selector, SelectionKey.OP_CONNECT);
			while (!sc.finishConnect()) {
				Thread.sleep(500);
			}
			connect(sc);
			while (true) {
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					if (key.isWritable()) {
						System.out.println("客户端可写");
					}
					if (key.isReadable()) {
						read(key);
					}
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (sc != null && sc.isOpen()) {
				try {
					sc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		// 创建ByteBuffer，并开辟一个1M的缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		// 读取请求码流，返回读取到的字节数
		int readBytes = sc.read(buffer);
		// 读取到字节，对字节进行编解码
		if (readBytes > 0) {
			// 将缓冲区当前的limit设置为position=0，用于后续对缓冲区的读取操作
			buffer.flip();
			// 根据缓冲区可读字节数创建字节数组
			byte[] bytes = new byte[buffer.remaining()];
			// 将缓冲区可读字节数组复制到新建的数组中
			buffer.get(bytes);
			String result = new String(bytes, "UTF-8");
			System.out.println("客户端收到消息：" + result);
		}
	}

	private void connect(SocketChannel sc) throws IOException {
		if (sc.isConnected()) {
			System.out.println("连接服务器成功");
			String msg = "abc1\r\nsave";
			try {
				sendMsg(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
			sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
	}

	// 异步发送消息
	private void doWrite(SocketChannel sc, String message) throws IOException {
		MessageParser.sendMessage(sc, message);
	}

	public void sendMsg(String msg) throws Exception {
		if (sc.isConnected()) {
			sc.register(selector, SelectionKey.OP_READ);
			doWrite(sc, msg);
		} else {
			System.out.println("还未连接成功");
		}
	}

}
