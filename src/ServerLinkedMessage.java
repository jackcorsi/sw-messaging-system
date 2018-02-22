

//Allows creation of a manually implemented doubly linked list structure (because the standard library one doesn't have the 
// required functionality) for storing messages

public class ServerLinkedMessage {

	private String sender;
	private String text;
	private boolean read = false;
	private ServerLinkedMessage next;
	private ServerLinkedMessage previous;

	public ServerLinkedMessage() {
		
	}

	public ServerLinkedMessage(ServerLinkedMessage previous, ServerLinkedMessage next, String sender, String text) {
		this.previous = previous;
		this.next = next;
		this.sender = sender;
		this.text = text;
	}

	public ServerLinkedMessage getNext() {
		return next;
	}
	
	public ServerLinkedMessage getPrevious() {
		return previous;
	}

	public void setNext(ServerLinkedMessage next) {
		this.next = next;
	}

	public void setPrevious(ServerLinkedMessage previous) {
		this.previous = previous;
	}
	
	public boolean getRead() {
		return read;
	}
	
	public void setRead(boolean read) {
		this.read = read;
	}
	
	public String getSender() {
		return sender;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public synchronized boolean isHead() {
		return (next == null);
	}
	
	public synchronized void delete() {
		if (previous != null)
			previous.setNext(next);
		if (next != null)
			next.setPrevious(previous);
	}
}
