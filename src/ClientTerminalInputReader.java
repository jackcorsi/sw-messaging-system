

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientTerminalInputReader extends Thread {

	private BlockingQueue<String> queue = new LinkedBlockingQueue <String> ();
	private BufferedReader in;
	private boolean stop = false;

	public void run() {
		stop = false;
		in = new BufferedReader(new InputStreamReader(System.in));
		while (!stop) {
			try {
				queue.put(in.readLine());
			} catch (InterruptedException e) {
				if (!stop)
					Report.error("Fatal input reading exception!");
			} catch (IOException e) {
				Report.error(e.getMessage());
			}
		}
	}
	
	public void stopThread() {
		stop = true;
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
