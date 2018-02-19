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
 * socket and adding them to an internal queue ready for processing by the object's owner
 */

public class SenderReceiver extends Thread {
	
	private BufferedReader in;
	private BlockingQueue <String> queue = new LinkedBlockingQueue <String> ();
	private int unread = 0; //Keeps track of the number of unread messages we have in the queue
	private PrintStream out;
	private Socket socket;
	private boolean isConnected = false;
	
	public SenderReceiver(Socket socket) {
		this.socket = socket;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintStream(socket.getOutputStream());
			unread = 0;
			isConnected = true;
		} catch (IOException e) {
			//Do nothing
		}
	}
	
	public void send(String msg) {
		out.println(msg);
	}
	
	public String receive() { //Collect a single incoming message from the socket
		String next = queue.poll();
		if (next != null) 
			unread --;
		return next;
	}
	
	public String[] receive(int n) { //Collect n incoming messages from the socket, only if they are available
		if (n <=  unread) {
			String[] msgs = new String[n];
			for (int i = 1; i <= n; i++)
				msgs[i] = queue.poll();
			unread -= n;
			return msgs;
		} else
			return null;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public String waitForMessage(long timeout) throws InterruptedException {
		String next = queue.poll(timeout, TimeUnit.MILLISECONDS);
		if (next != null) 
			unread --;
		return next;
	}
	
	public void disconnect() {
		isConnected = false;
	}
	
	public void run() {
		while (isConnected) {
			String msg;
			try {
				msg = in.readLine();
				System.out.println("Message received by SenderReciever: " + msg); //TODO remove
				queue.put(msg);
				unread++; //TODO fix race condition here
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
		Report.behaviour("SenderReceiver disconnected");
		try {
			socket.close();
		} catch (IOException e) {
			//Do nothing
		}
	}
	
}
