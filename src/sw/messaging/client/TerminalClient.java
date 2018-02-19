package sw.messaging.client;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import sw.messaging.*;

public class TerminalClient {
	
	private static SenderReceiver senderReceiver;
	private static TerminalInputReader in;
	private static String sendRecipient;

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
		}//TODO: this
		
		senderReceiver = new SenderReceiver(socket);
		senderReceiver.send(nickname);
		in = new TerminalInputReader();
		in.setName("Terminal input reader thread");
		in.start();
		mainLoop();
		in.interrupt();
	}
	
	public static void mainLoop() {
		while (true) {
			if (!senderReceiver.isConnected()) {
				Report.error("Server disconnected unexpectedly! Terminating");
				return;
			}
			
			//Send messages
			String inString = in.get();
			if (inString != null) {
				if (sendRecipient != null) {
					senderReceiver.send(sendRecipient);
					senderReceiver.send(inString);
					sendRecipient = null;
				}  else 
					sendRecipient = inString;
			}
			
			//Receive messages
			String[] strings = senderReceiver.receive(2); //Get 2 messages from the server 
			if (strings != null) 
				System.out.println(strings[0] + ": " + strings[2]);
		}
	}
}
