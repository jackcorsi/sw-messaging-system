

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * Class that wraps a socket and handles sending and receiving lines of text. Runs as a thread, receiving messages from the 
 * socket and adding them to an internal queue ready for processing by the object's owner
 */

public class SenderReceiver extends Thread {
	
	private BufferedReader in;
	private BlockingQueue <String[]> queue = new LinkedBlockingQueue <String[]> ();
	private PrintStream out;
	private Socket socket;
	private boolean isConnected = false;
	
	
	
	public SenderReceiver(Socket socket) {
		this.socket = socket;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintStream(socket.getOutputStream());
			isConnected = true;
		} catch (IOException e) {
			//Do nothing
		}
	}
	
	public void send(String[] lines) {
		if (lines.length > SharedConst.MAX_MESSAGE_LINES)
			throw new RuntimeException("Illegal number of lines in message attempted to be sent");
		
		for (int i = 0; i < lines.length; i++)
			out.println(SharedConst.NOT_SEPARATOR_CHAR + lines[i]);
		out.println(SharedConst.SEPARATOR_CHAR);
	}
	
	public String[] receive() { //Collect a single incoming message from the socket
		return queue.poll();
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public String[] waitForMillis(long timeout) throws InterruptedException {
		return queue.poll(timeout, TimeUnit.MILLISECONDS);
	}
	
	public void disconnect() {
		isConnected = false;
	}
	
	public void run() {
		while (isConnected) {
			try {
				String[] lineHolder = new String[SharedConst.MAX_MESSAGE_LINES];
				int i = 0;
				while (true) {
					String line = in.readLine();
					
					if (line == null) { 
						disconnect();
						continue;
					}
					
					if (line.length() < 1) {
						Report.error("SenderReceiver disconnecting: Message length 0"); //TODO remove
						isConnected = false;
						break;
					}
					if (line.charAt(0) != SharedConst.SEPARATOR_CHAR) {
						if (i >= lineHolder.length) {
							Report.error("SenderReceiver disconnecting: Too many lines in message"); //TODO remove
							isConnected = false;
							break;
						}
						lineHolder[i] = line.substring(1);
						i++;
					} else 
						break;	
				}
				String[] msg = new String[i];
				for (int j = 0; j < i; j++) 
					msg[j] = lineHolder[j];
				
				queue.put(msg);
			} catch (IOException e) {
				isConnected = false;
				break;
			} catch (InterruptedException e) {
				isConnected = false;
				Report.error("Blocking queue interrupted exception occurred");
				break;
			}
		}
		isConnected = false;
		try {
			socket.close();
		} catch (IOException e) {
			Report.error("SenderReceiver failed to close socket!");
		}
	}
	
}
