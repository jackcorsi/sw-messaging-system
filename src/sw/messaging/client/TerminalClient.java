package sw.messaging.client;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import sw.messaging.*;

public class TerminalClient {
	
	private static SenderReceiver senderReceiver;

	public static void main(String[] args) {
		
		// Check correct usage:
	    if (args.length != 2) {
	      Report.error("Usage: java Client user-nickname server-hostname");
	      return;
	    }

	    // Initialize information:
	    String nickname = args[0];
	    String hostname = args[1];

		Socket socket;
		try {
			socket = new Socket(hostname, SharedConst.PORT_NUMBER);
		} catch (UnknownHostException e) {
			Report.error("Could not connect to server : unknown host!");
			return;
		} catch (IOException e) {
			Report.error("Could not connect to server!");
			return;
		}
		
		senderReceiver = new SenderReceiver(socket);
		mainLoop();
	}
	
	public static void mainLoop() {
		while (true) {
			//TODO: this
		}
	}
}
