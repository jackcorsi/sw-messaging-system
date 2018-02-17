package sw.messaging.server;

import java.net.Socket;

public class Client implements Runnable {
	
	private Socket socket;
	private String name;
	private Sender sender;
	private Receiver receiver;
	private boolean isConnected = false;
	
	public Client (Socket socket) {
		this.socket = socket;
	}
	
	public void startThreads() {
		//TODO: stub
	}
	
	public void sendMessage(Client sender, String text) {
		//TODO: stub
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public String getName() {
		return name;
	}
	
	public void kick() {
		//TODO: stub
		sender.interrupt();
		receiver.interrupt();
	}
	
	public void kickWithMessage(String msg) {
		//TODO: stub
	}
	
	public IncomingMessage getNextMessage() {
		//TODO: stub
		return null;
	}
	
	public void run() {
		startThreads();
	}
	
}
