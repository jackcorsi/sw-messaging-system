package sw.messaging.server;
import sw.messaging.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	
	private static ArrayList <Client> clients = new ArrayList <Client> ();
	private static ServerSocket serverSocket;

	public static void main(String[] args) {
		Report.behaviour("Server starting");
		
		try {
			serverSocket = new ServerSocket(SharedConst.PORT_NUMBER);
		} catch (IOException e) {
			Report.error("Failed to create sever socket");
			return;
		}
		
		//Main server loop, which includes accepting new clients and passing on client messages
		while (true) {
			acceptNewClient();
			pushMessages();
		}
	}
	
	private static void acceptNewClient() {
		Socket socket;
		try {
			socket = serverSocket.accept();
		} catch (IOException e) {
			return;
		}
		Client client = new Client(socket);
		
		if (client.isConnected()) {
			boolean allowClient = true;
			String name = client.getName();
			for (Client c : clients) {
				if (c.getName() == name) {
					allowClient = false;
					break;
				}
			}
			
			if (allowClient) {
				clients.add(client);
				Report.behaviour("Client " + client.getName() + " has joined");
			} else {
				client.kickWithMessage("A user already exists with the name " + name);
			}
		}
		
	}
	
	private static void pushMessages() {
		for (int i = 0; i < clients.size(); i ++) {
			Client client = clients.get(i);
			if (!client.isConnected()) {
				clients.remove(i);
				continue;
			}
			IncomingMessage msg = client.getNextMessage();
			boolean sent = false;
			for (Client recip : clients) {
				if ( recip.getName().equals(msg.getRecipient()) ) {
					recip.sendMessage(client, msg.getText());
					sent = true;
					break;
				}
			}
			
			if (!sent)
				Report.error("Client does not exist: " + msg.getRecipient());
			//TODO :  send error back to the client
		}
	}
}
