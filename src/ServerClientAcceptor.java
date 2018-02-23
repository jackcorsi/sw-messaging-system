import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerClientAcceptor extends Thread {
	
	private static final long HANDSHAKE_WAIT_TIME = 200;
	
	private ServerSocket server;
	
	public ServerClientAcceptor(ServerSocket server) {
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
			
			if (handshake == null || handshake.length > 2) {
				senderReceiver.disconnect();
				continue;
			}
			
			String command = handshake[0];
			String username = handshake[1].toLowerCase();
			
			if (username.equals(SharedConst.LOGOUT_INPUT_STRING) || username.equals("server") || username.equals(SharedConst.QUIT_INPUT_STRING)) {
				senderReceiver.send(new String[] {SharedConst.EVENT_INVALID_MSG});
				senderReceiver.disconnect();
				continue;
			}
			
			
			if (command.equals(SharedConst.CONNECT_LOGIN_STRING)) {
				boolean found = false;
				for (int i = 0; i < ServerUsers.numberOfInactiveUsers(); i++) {
					ServerUser user = ServerUsers.getInactive(i);
					if (user == null)
						break;
					if (user.getName().equals(username)) {
						found = true;
						user.connectDevice(senderReceiver);
						ServerUsers.makeActive(user);
						senderReceiver.send(new String[] {SharedConst.CONNECT_ACCEPT_STRING});
						Report.behaviour("Device logged in for user: " + username);
						break;
					}
				}
				
				if (!found) {
					for (int i = 0; i < ServerUsers.numberOfActiveUsers(); i++) {
						ServerUser user = ServerUsers.getActive(i);
						if (user == null)
							break;
						if (user.getName().equals(username)) {
							found = true;
							user.connectDevice(senderReceiver);
							senderReceiver.send(new String[] {SharedConst.CONNECT_ACCEPT_STRING});
							Report.behaviour("Device logged in for user: " + username);
							break;
						}
					}
					
				}
				
				if (!found) {
					senderReceiver.send(new String[] {SharedConst.EVENT_INVALID_MSG});
					senderReceiver.disconnect();
				}
				
			} else if (command.equals(SharedConst.CONNECT_REGISTER_STRING)) {
				ServerUser newUser = new ServerUser(username);
				newUser.connectDevice(senderReceiver);
				if (ServerUsers.newActive(newUser)) {
					senderReceiver.send(new String[] {SharedConst.CONNECT_ACCEPT_STRING});
					Report.behaviour("Device logged in for user: " + username);
				} else
					senderReceiver.send(new String[] {SharedConst.EVENT_INVALID_MSG});
			} else {
				senderReceiver.disconnect();
			}
			
		}
	}
	
}
