package sw.messaging.server;
import sw.messaging.*;

import java.net.Socket;


public class Client implements Runnable {
		
	private Socket socket;
	private String name;
	private SenderReceiver senderReceiver;
	private String incomingRecipient;
	
	public Client (Socket socket) {
		this.socket = socket;
		senderReceiver = new SenderReceiver(socket);
		try {
			name = senderReceiver.waitForMessage(200);
			if (name == null) {
				Report.error("Received no username from the client");
				senderReceiver.interrupt();
				return;
			}
			if (name.equals(SharedConst.QUIT_STRING))
				senderReceiver.interrupt();
		} catch (InterruptedException e) {
			//Do nothing
		}
		
	}
	
	public void startThread() {
		senderReceiver.start();
	}
	
	public void sendMessage(Client sender, String text) {
		senderReceiver.send(sender.getName());
		senderReceiver.send(text);
	}
	
	public boolean isConnected() {
		return senderReceiver.isConnected();
	}
	
	public String getName() {
		return name;
	}
	
	public void kick() {
		//TODO: stub
		senderReceiver.interrupt();
	}
	
	public void kickWithMessage(String msg) {
		senderReceiver.send(SharedConst.QUIT_STRING);
		senderReceiver.send(msg);
		senderReceiver.interrupt();
	}
	
	public IncomingMessage getNextMessage() {
		String str = senderReceiver.receive();
		if (str != null) {
			String recipient = incomingRecipient;
			if (recipient != null) {
				incomingRecipient = null;
				return new IncomingMessage(incomingRecipient, str);
			} 
			incomingRecipient = str;
			if (incomingRecipient.equals(SharedConst.QUIT_STRING)) { 
				senderReceiver.interrupt();
				return null;
			}
			String text = senderReceiver.receive();
			if (text != null) {
				return new IncomingMessage(incomingRecipient, text);
			} else
				return null;
			
		} else
			return null;
	}
	
	public void run() {
		startThread();
	}
	
}
