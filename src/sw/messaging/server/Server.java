package sw.messaging.server;
import sw.messaging.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	
	private static ArrayList <Client> clients = new ArrayList <Client> ();

	public static void main(String[] args) {
		Report.behaviour("Server starting");
		ServerSocket serverSocket;
		
		try {
			serverSocket = new ServerSocket(SharedConst.PORT_NUMBER);
		} catch (IOException e) {
			Report.error("Failed to create sever socket");
			return;
		}
		
		while (true) {
			Socket socket;
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				continue;
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
				} else {
					client.kickWithMessage("A user already exists with the name " + name);
				}
			}
			
		}
	}

}
