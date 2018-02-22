
import java.util.ArrayList;

public class ServerUser {

	private String name;
	private ArrayList<Device> devices = new ArrayList<Device>();
	private ServerLinkedMessage headMessage;

	private class Device {
		public SenderReceiver senderReceiver;
		public ServerLinkedMessage currentMessage;

		public Device(SenderReceiver senderReceiver, ServerLinkedMessage currentMessage) {
			this.senderReceiver = senderReceiver;
			this.currentMessage = currentMessage;
		}
	}

	public ServerUser(String name) {
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void connectDevice(SenderReceiver senderReceiver) {
		devices.add(new Device(senderReceiver, headMessage));
	}

	public void sendMessage(ServerUser sender, String text) {
		sendMessage(sender.getName(), text);
	}

	public void sendMessage(String sender, String text) {
		ServerLinkedMessage msg = new ServerLinkedMessage(headMessage, null, sender, text);
		if (headMessage != null)
			headMessage.setNext(msg);
		headMessage = msg;

		for (Device device : devices) {
			if (device.currentMessage == headMessage.getPrevious()) {
				device.currentMessage = msg;
				device.senderReceiver.send(new String[] { msg.getSender(), msg.getText() });
				msg.setRead(true);
			} else {
				device.senderReceiver.send(new String[] { SharedConst.EVENT_MSG_NOTIFICATION });
			}
		}
	}

	public boolean isOnline() {
		return (devices.size() > 0);
	}

	public String getName() {
		return name;
	}

	public ServerIncomingMessage process() {
		for (Device device : devices) {
			String[] incoming = device.senderReceiver.receive();

			if (incoming == null)
				continue;

			if (incoming.length == 1) {
				String command = incoming[0];
				switch (command) {
				case SharedConst.COMMAND_PREVIOUS:
					commandPrevious(device);
					break;
				case SharedConst.COMMAND_NEXT:
					commandNext(device);
					break;
				case SharedConst.COMMAND_DELETE:
					commandDelete(device);
					break;
				case SharedConst.COMMAND_LATEST:
					commandLatest(device);
					break;
				case SharedConst.COMMAND_NEW:
					commandNew(device);
					break;
				case SharedConst.COMMAND_LOGOUT:
					commandLogout(device);
					break;
				default:
					kickDevice(device, "Invalid data received from your client");
				}
				return null;
			} else if (incoming.length == 2) {
				return new ServerIncomingMessage(incoming[0], incoming[1], device.senderReceiver);
			} else {
				kickDevice(device, "Invalid data received from your client");
				return null;
			}
		}
		return null;
	}

	public boolean equals(ServerUser other) {
		return other.getName().equals(name);
	}

	private void kickDevice(Device device, String message) {
		devices.remove(device);
		device.senderReceiver.send(new String[] { "SERVER", message });
		device.senderReceiver.send(new String[] { SharedConst.EVENT_KICKED });
		device.senderReceiver.disconnect();
	}

	private void commandPrevious(Device device) {
		if (device.currentMessage == null) {
			device.senderReceiver.send(new String[] { SharedConst.EVENT_NO_MSGS });
		} else if (device.currentMessage.getPrevious() == null) {
			device.senderReceiver.send(new String[] { SharedConst.EVENT_NO_PREVIOUS_MSG });
		} else {
			ServerLinkedMessage prev = device.currentMessage.getPrevious();
			device.currentMessage = prev;
			device.senderReceiver.send(new String[] { prev.getSender(), prev.getText() });
			prev.setRead(true);
		}
	}

	private void commandNext(Device device) {
		if (device.currentMessage == null) {
			device.senderReceiver.send(new String[] { SharedConst.EVENT_NO_MSGS });
		} else if (device.currentMessage.getNext() == null) {
			device.senderReceiver.send(new String[] { SharedConst.EVENT_NO_NEXT_MSG });
		} else {
			ServerLinkedMessage next = device.currentMessage.getNext();
			device.currentMessage = next;
			device.senderReceiver.send(new String[] { next.getSender(), next.getText() });
			next.setRead(true);
		}
	}

	private void commandDelete(Device device) {
		if (device.currentMessage == null) {
			device.senderReceiver.send(new String[] { SharedConst.EVENT_NO_MSGS });
		} else if (device.currentMessage.isHead()) {
			headMessage = device.currentMessage.getPrevious();
			device.currentMessage.delete();
			device.currentMessage = device.currentMessage.getPrevious();
		} else {
			device.currentMessage.delete();
			device.currentMessage = device.currentMessage.getNext();
		}
	}

	private void commandLatest(Device device) {
		device.currentMessage = headMessage;
		if (headMessage == null)
			device.senderReceiver.send(new String[] { SharedConst.EVENT_NO_MSGS });
		else {
			device.senderReceiver.send(new String[] { headMessage.getSender(), headMessage.getText() });
			headMessage.setRead(true);
		}
	}

	private void commandNew(Device device) {
		ServerLinkedMessage msg = headMessage;
		boolean foundNew = false;
		
		while (true) {
			if (msg == null)
				break;
			if (msg.getRead())
				break;
			else {
				msg = msg.getPrevious();
				foundNew = true;
			}
		}
		if (foundNew) {
			while (true) {
				msg = msg.getNext();
				if (msg == null)
					break;
				device.senderReceiver.send(new String[] { msg.getSender(), msg.getText() });
				msg.setRead(true);
			}
		} else {
			device.senderReceiver.send(new String[] { SharedConst.EVENT_NO_MSGS });
		}
	}

	private void commandLogout(Device device) {
		device.senderReceiver.disconnect();
		devices.remove(device);
		Report.behaviour("Device signed out for user " + name);
	}
}
