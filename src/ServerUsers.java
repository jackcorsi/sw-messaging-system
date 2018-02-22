

import java.util.ArrayList;

public class ServerUsers {

	private static ArrayList<ServerUser> active = new ArrayList<ServerUser>();
	private static ArrayList<ServerUser> inactive = new ArrayList<ServerUser>();

	public static synchronized boolean newActive(ServerUser u) {
		for (ServerUser existing : active) {
			if (existing.equals(u))
				return false;
		}
		active.add(u);
		return true;
	}

	public static synchronized void makeInactive(ServerUser u) {
		active.remove(u);
		inactive.add(u);
	}

	public static synchronized void makeActive(ServerUser u) {
		inactive.remove(u);
		active.add(u);
	}

	public static synchronized void deleteActive(ServerUser u) {
		active.remove(u);
	}

	public static synchronized void deleteInactive(ServerUser u) {
		inactive.remove(u);
	}

	public static synchronized int numberOfActiveUsers() {
		return active.size();
	}

	public static synchronized int numberOfInactiveUsers() {
		return inactive.size();
	}

	public static synchronized ServerUser getActive(int i) {
		if (i >= active.size())
			return null;
		else
			return active.get(i);
	}

	public static synchronized ServerUser getInactive(int i) {
		if (i >= inactive.size()) {
			return null;
		} else
			return inactive.get(i);
	}
}
