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
		
		ServerClientAcceptor clientAcceptor = new ServerClientAcceptor(serverSocket);
		clientAcceptor.setDaemon(true);
		clientAcceptor.start();
		while (true)
			mainLoop();
	}
	
	private static void mainLoop() {
		for (int i = 0; i < ServerUsers.numberOfActiveUsers(); i++) {
			ServerUser u = ServerUsers.getActive(i);
			if (u == null) 
				break;
			
			if (!u.isOnline()) {
				ServerUsers.makeInactive(u);
				Report.behaviour("User " + u.getName() + " has gone offline");
			}
			
			ServerIncomingMessage newMsg = u.process();
			if (newMsg != null) {
				boolean found = false;
				for (int j = 0; j < ServerUsers.numberOfActiveUsers(); j++) {
					ServerUser jUser = ServerUsers.getActive(j);
					if (jUser == null) 
						break;
					if (jUser.getName().equals(newMsg.getRecipient())) {
						jUser.sendMessage(u, newMsg.getText());
						found = true;
						break;
					}
				}
				
				if (!found) {
					for (int j = 0; j < ServerUsers.numberOfInactiveUsers(); j++) {
						ServerUser jUser = ServerUsers.getInactive(j);
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
					newMsg.sendRejection();
				}
			}
		}
	}
}
