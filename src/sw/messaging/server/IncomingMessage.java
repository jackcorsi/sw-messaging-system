package sw.messaging.server;

public class IncomingMessage {
	
	private String recipientName;
	private String message;
	
	public IncomingMessage(String recipientName, String message) {
		this.recipientName = recipientName;
		this.message = message;
	}
	
	public String getText() {
		return message;
	}
	
	public String getRecipient() {
		return recipientName;
	}
}
