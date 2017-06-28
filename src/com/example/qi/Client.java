package com.example.qi;

import java.util.Scanner;

public class Client {

	public static void main(String[] args) {
		start();
	}

	private static void start() {
		ClientHandler clientHandler = new ClientHandler("127.0.0.1", 5555);
		new Thread(clientHandler).start();
		while(true){
			try {
				clientHandler.sendMsg(new Scanner(System.in).nextLine());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
