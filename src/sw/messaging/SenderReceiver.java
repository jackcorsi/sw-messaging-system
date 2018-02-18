package sw.messaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * Class for communication with a server or client via a socket. Runs as a thread, receiving messages from the 
 * socket and adding them to an internal queue for processing
 */

public class SenderReceiver extends Thread {
	
	private BufferedReader in;
	private BlockingQueue <String> queue = new LinkedBlockingQueue <String> ();
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
	
	public void send(String msg) {
		out.println(msg);
	}
	
	public String receive() {
		return queue.poll();
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public String waitForMessage(long timeout) throws InterruptedException {
		return queue.poll(timeout, TimeUnit.MILLISECONDS);
	}
	
	public void run() {
		while (isInterrupted()) {
			String msg;
			try {
				msg = in.readLine();
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
			//Do nothing
		}
	}
	
}
