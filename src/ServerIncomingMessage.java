

public class ServerIncomingMessage {
	
	private String recipientName;
	private String message;
	
	public ServerIncomingMessage(String recipientName, String message) {
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
