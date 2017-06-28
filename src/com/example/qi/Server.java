package com.example.qi;

public class Server {

	public static void main(String[] args) {
		start();
	}
	
	private static void start() {
		ServerHandler serverHandler = new ServerHandler(5555);
		new Thread(serverHandler).start();
	}
	
}
