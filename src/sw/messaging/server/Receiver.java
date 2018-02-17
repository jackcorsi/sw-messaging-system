package sw.messaging.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class Receiver extends Thread {
	
	private BufferedReader in;
	private BlockingQueue <String> queue;
	
	public Receiver(Socket socket) {
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String get() {
		//TODO : stub
		return null;
	}
	
	public void run() {
		//TODO: stub
	}
	
}
