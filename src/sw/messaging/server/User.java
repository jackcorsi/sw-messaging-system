package sw.messaging.server;
import sw.messaging.*;



public class User {
		
	private String name;
	private SenderReceiver senderReceiver;
	
	
	public User (String name) {
		this.name = name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void connectDevice(SenderReceiver dev) {
		this.senderReceiver = dev;
	}
	
	public void sendMessage(User sender, String text) {
		senderReceiver.send(new String[] {sender.getName(), text});
	}
	
	public void sendMessage(String sender, String text) {
		senderReceiver.send(new String[] {sender, text});
	}
	
	public boolean isConnected() {
		return senderReceiver.isConnected();
	}
	
	public String getName() {
		return name;
	}
	
	public void kick() {
		//TODO: stub
		senderReceiver.disconnect();
	}
	
	public void kickWithMessage(String msg) {
		senderReceiver.send(new String [] {SharedConst.QUIT_STRING, msg});
		senderReceiver.disconnect();
	}
	
	public IncomingMessage process() {
		String[] packet = senderReceiver.receive();
		if (packet == null)
			return null;
		if (packet.length == 1) {
			//TODO Interpret singular packets 
			senderReceiver.disconnect();
			return null;
		} else {
			return new IncomingMessage(packet[0], packet[1]);
		}	
	}
	
	public boolean equals(User other) {
		return other.getName().equals(name);
	}
}
