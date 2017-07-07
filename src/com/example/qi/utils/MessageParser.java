package com.example.qi.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map.Entry;

public class MessageParser {

	public static final HashMap<String, SocketChannel> socketChannels = new HashMap<>();

	/**
	 * 解析消息,并传递消息
	 * 
	 * @param sb
	 *            获取的消息
	 * @param sc
	 *            发送客户端的SocketChannel
	 * @throws Exception
	 */
	public static void parse(StringBuffer sb, SocketChannel sc) throws Exception {
		String[] messages = sb.toString().split("\n\n");
		String[] headers = messages[0].split("\n");
		Pair<String, String> typePair = null;
		Pair<String, String> fromPair = null;
		Pair<String, String> toPair = null;
		for (String string : headers) {
			String[] header = string.split(":");
			if (!string.contains(":")) {
				sendError(sc, "消息头格式不正确");
				return;
			}
			if (header[0].equals("type")) {
				// 如果类型是save，则账号为消息发送者的账号。保存账号及其对应的SocketChannel
				typePair = new Pair<>(header[0], header[1]);
			} else if (header[0].equals("from")) {
				fromPair = new Pair<>(header[0], header[1]);
			} else if (header[0].equals("to")) {
				toPair = new Pair<String, String>(header[0], header[1]);
			}
		}
		if (typePair.second().equals("login")) {
			socketChannels.put(fromPair.second(), sc);
		} else if (typePair.second().equals("chat")) {
			// 如果类型为send，则代表客户端想发送消息给他人，账号为其他客户端账号
			SocketChannel dsc = socketChannels.get(toPair.second());
			String message = fromPair.second() +":"+ messages[1];
			sendMessage(dsc, message);
		}
	}

	private static void sendError(SocketChannel sc, String string) {
		try {
			sendMessage(sc, string);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static boolean sendMessage(SocketChannel sc, String message) throws IOException {
		if (sc == null || !sc.isOpen())
			return false;
		int write = sc.write(ByteBuffer.wrap(message.getBytes()));
		return true;
	}
}
