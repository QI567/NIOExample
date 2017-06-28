package com.example.qi.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class MessageParser {
	
	public static final HashMap<String,SocketChannel> socketChannels = new HashMap<>();
	
	public static void parse(StringBuffer sb,SocketChannel sc) throws Exception {
		String[] messages = sb.toString().split("\r\n");
		if(messages==null || messages.length<2)throw new Exception("消息错误");
		String account = messages[0];
		String type = messages[1];
		if("save".equals(type)){
			//如果类型是save，则账号为消息发送者的账号。保存账号及其对应的SocketChannel
			socketChannels.put(account, sc);
		}else if("send".equals(type)){
			//如果类型为send，则代表客户端想发送消息给他人，账号为其他客户端账号
			SocketChannel dsc = socketChannels.get(account);
			sendMessage(dsc, messages[2]);
		}
	}
	
	public static boolean sendMessage(SocketChannel sc,String message) throws IOException{
		if(sc == null || !sc.isOpen())return false;
		int write = sc.write(ByteBuffer.wrap(message.getBytes()));
		return true;
	}
}
