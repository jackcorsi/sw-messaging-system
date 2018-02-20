package sw.messaging.server;
import sw.messaging.*;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {

	private static ServerSocket serverSocket;

	public static void main(String[] args) {
		Report.behaviour("Server starting");
		
		try {
			serverSocket = new ServerSocket(SharedConst.PORT_NUMBER);
		} catch (IOException e) {
			Report.error("Failed to create server socket");
			return;
		}
		
		ClientAcceptor clientAcceptor = new ClientAcceptor(serverSocket);
		clientAcceptor.setDaemon(true);
		clientAcceptor.start();
		while (true)
			mainLoop();
	}
	
	private static void mainLoop() {
		for (int i = 0; i < Users.numberOfActiveUsers(); i++) {
			User u = Users.getActive(i);
			if (u == null) 
				break;
			IncomingMessage newMsg = u.process();
			if (newMsg != null) {
				boolean found = false;
				for (int j = 0; j < Users.numberOfActiveUsers(); j++) {
					User jUser = Users.getActive(j);
					if (jUser == null)
						break;
					if (jUser.getName().equals(newMsg.getRecipient())) {
						jUser.sendMessage(u, newMsg.getText());
						found = true;
						break;
					}
				}
				
				if (!found) {
					for (int j = 0; j < Users.numberOfInactiveUsers(); j++) {
						User jUser = Users.getInactive(j);
						if (jUser == null)
							break;
						if (jUser.getName().equals(newMsg.getRecipient())) {
							jUser.sendMessage(u, newMsg.getText());
							found = true;
							break;
						}
					}
				}
				
				if (!found) {
					//TODO send error back to client
				}
			}
		}
	}
}
