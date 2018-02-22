

public class ServerIncomingMessage {
	
	private String recipientName;
	private String message;
	private SenderReceiver callbackDevice;
	
	public ServerIncomingMessage(String recipientName, String message, SenderReceiver callbackDevice) {
		this.recipientName = recipientName;
		this.message = message;
		this.callbackDevice = callbackDevice;
	}
	
	public String getText() {
		return message;
	}
	
	public String getRecipient() {
		return recipientName;
	}
	
	public void sendRejection() {
		if (callbackDevice.isConnected()) {
			callbackDevice.send(new String[] {SharedConst.EVENT_INVALID_MSG});
		}
	}
}
