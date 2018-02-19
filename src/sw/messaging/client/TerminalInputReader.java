package sw.messaging.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import sw.messaging.Report;

public class TerminalInputReader extends Thread {

	private BlockingQueue<String> queue = new LinkedBlockingQueue <String> ();

	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (!isInterrupted()) {
			try {
				queue.put(in.readLine());
			} catch (InterruptedException e) {
				Report.error("Fatal input reading error!");
			} catch (IOException e) {
				Report.error("Fatal input reading error!");
			}
		}
	}
	
	public String get() {
		return queue.poll();
	}
}
