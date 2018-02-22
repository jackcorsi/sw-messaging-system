

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientTerminalInputReader extends Thread {

	private BlockingQueue<String> queue = new LinkedBlockingQueue <String> ();

	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (!isInterrupted()) {
			try {
				queue.put(in.readLine());
			} catch (InterruptedException e) {
				Report.error("Fatal input reading error 1");
			} catch (IOException e) {
				Report.error(e.getMessage());
			}
		}
	}
	
	public String get() {
		return queue.poll();
	}
	
	public String block() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			Report.error("Fatal input reading error 3");
			System.exit(0);
			return "";
		}
	}
}
