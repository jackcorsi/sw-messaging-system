package sw.messaging.server;
import sw.messaging.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientAcceptor extends Thread {
	
	private static final long HANDSHAKE_WAIT_TIME = 200;
	
	private ServerSocket server;
	
	public ClientAcceptor(ServerSocket server) {
		this.server = server;
	}

	public void run() {
		while (true) {
			Socket socket;
			try {
				socket = server.accept();
			} catch (IOException e) {
				continue;
			}
			
			SenderReceiver senderReceiver = new SenderReceiver(socket);
			senderReceiver.start();
			String[] handshake;
			try {
				handshake = senderReceiver.waitForMillis(HANDSHAKE_WAIT_TIME); //AAAAA
			} catch (InterruptedException e) {
				senderReceiver.disconnect();
				continue;
			}
			
			if (handshake == null || handshake.length > 1) {
				senderReceiver.disconnect();
				continue;
			}
			
			String username = handshake[0];
			if (username.equals(SharedConst.QUIT_STRING)) {
				senderReceiver.disconnect();
				continue;
			}
			
			User newUser = new User(username);
			if (Users.newActive(newUser)) {
				newUser.connectDevice(senderReceiver);
				Report.behaviour(username + " has joined");
			}
		}
	}
	
}
