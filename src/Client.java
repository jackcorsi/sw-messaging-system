import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	private static SenderReceiver senderReceiver;
	private static ClientTerminalInputReader in;

	public static void main(String[] args) {
		in = new ClientTerminalInputReader();
		in.start();
		// Check correct usage:
		if (args.length != 1) {
			Report.error("Usage: java Client server-hostname");
			return;
		}

		while (true) {
			while (!loginLoop(args[0])) {
			}

			boolean relog = mainLoop();
			if (!relog)
				exit();
		}
	}

	public static boolean loginLoop(String hostname) {
		System.out.println("Begin login loop");
		String input = in.block();
		input = input.trim();
		String arg1;

		switch (input) {
		case "login":
			arg1 = SharedConst.CONNECT_LOGIN_STRING;
			break;
		case "register":
			arg1 = SharedConst.CONNECT_REGISTER_STRING;
			break;
		default:
			System.out.println("USAGE: enter login or register followed by the username you wish to use");
			return false;
		}

		String username = in.block();

		Socket socket;
		try {
			socket = new Socket(hostname, SharedConst.PORT_NUMBER);
		} catch (UnknownHostException e) {
			Report.error("Could not connect to server : unknown host!");
			return false;
		} catch (IOException e) {
			Report.error("Could not connect to server!");
			return false;
		}

		senderReceiver = new SenderReceiver(socket);
		senderReceiver.start();
		senderReceiver.send(new String[] { arg1, username }); // AAAAA

		String[] response;
		try {
			response = senderReceiver.waitForMillis(3000);
		} catch (InterruptedException e) {
			System.exit(0);
			return false;
		}
		//TODO handle no response
		
		if (response == null) {
			Report.error("Timed out waiting for server response");
			return false;
		}
		
		if (response.length != 1) {
			System.exit(0);
			return false;
		}

		switch (response[0]) {
		case SharedConst.TAKEN_USERNAME_STRING:
			System.out.println("Rejected - username already taken");
			return false;
		case SharedConst.EVENT_INVALID_MSG:
			System.out.println("Rejected - username invalid or does not exist");
			return false;
		case SharedConst.CONNECT_ACCEPT_STRING:
			System.out.println("Logged in successfully");
			break;
		default:
			Report.error("Received illegal response from server!");
			System.exit(0);
			return false;
		}

		return true;
	}

	private static boolean mainLoop() {
		while (true) {
			if (!senderReceiver.isConnected()) {
				Report.error("Server connection dropped!");
				return false;
			}

			String command = in.get();
			if (command != null) {
				command = command.trim();
				switch (command) {
				case "next":
					commandNext();
					break;
				case "previous":
					commandPrevious();
					break;
				case "delete":
					commandDelete();
					break;
				case "latest":
					commandLatest();
					break;
				case "send":
					commandSend();
					break;
				case "logout":
					System.out.println("Logging out");
					senderReceiver.send(new String[] {SharedConst.COMMAND_LOGOUT});
					return true;
				default:
					System.out.println("Available commands - next, previous, delete, latest, send, logout");
				}
			}

			String[] incoming = senderReceiver.receive();

			if (incoming == null)
				continue;
			
			if (incoming.length == 1) {
				String event = incoming[0];
				switch (event) {
				case SharedConst.EVENT_NO_NEXT_MSG:
					noNextEvent();
					break;
				case SharedConst.EVENT_NO_PREVIOUS_MSG:
					noPreviousEvent();
					break;
				case SharedConst.EVENT_NO_MSGS:
					noMessagesEvent();
					break;
				case SharedConst.EVENT_MSG_NOTIFICATION:
					messageNotificationEvent();
					break;
				case SharedConst.EVENT_INVALID_MSG:
					invalidMessageEvent();
					break;
				case SharedConst.EVENT_KICKED:
					kicked();
					break;
				default:
					Report.error("Received invalid data from server!");
					exit();
				}
			} else if (incoming.length == 2) {
				System.out.println(incoming[0] + ":\t" + incoming[1]);
			} else {
				Report.error("Received invalid data from server!");
				exit();
			}
		}
	}

	public static void commandNext() {
		senderReceiver.send(new String[] { SharedConst.COMMAND_NEXT });
	}

	public static void commandPrevious() {
		senderReceiver.send(new String[] { SharedConst.COMMAND_PREVIOUS });
	}

	public static void commandDelete() {
		senderReceiver.send(new String[] { SharedConst.COMMAND_DELETE });
	}

	public static void commandLatest() {
		senderReceiver.send(new String[] { SharedConst.COMMAND_LATEST });
	}

	public static void commandSend() {
		String recipient = in.block();
		String text = in.block();
		senderReceiver.send(new String[] { recipient, text });
	}

	public static void messageNotificationEvent() {
		System.out.println("New message received - use latest to see it");
	}

	public static void invalidMessageEvent() {
		System.out.println("Message rejected - invalid formatting or recipient does not exist");
	}

	public static void noPreviousEvent() {
		System.out.println("Reached least recent message");
	}

	public static void noNextEvent() {
		System.out.println("Reached most recent message");
	}
	
	public static void noMessagesEvent() {
		System.out.println("Your message queue is empty");
	}

	public static void kicked() {
		System.out.println("Kicked from server");
		exit();
	}

	public static void exit() {
		in.stopThread();
		senderReceiver.disconnect();
		System.exit(0);
	}
}
