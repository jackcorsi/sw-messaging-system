package sw.messaging.server;

import java.util.ArrayList;

public class Users {

	private static ArrayList <User> active = new ArrayList <User> ();
	private static ArrayList <User> inactive = new ArrayList <User> ();
	
	public static synchronized boolean newActive(User u) {
		for (User existing : active) {
			if (existing.equals(u))
				return false;
		}
		active.add(u);
		return true;
	}
	
	public static synchronized void makeInactive(User u) {
		active.remove(u);
		inactive.add(u);
	}
	
	public static synchronized void makeActive(User u) {
		inactive.remove(u);
		active.add(u);
	}
	
	public static synchronized void deleteActive(User u) {
		active.remove(u);
	}
	
	public static synchronized void deleteInactive(User u) {
		inactive.remove(u);
	}
	
	public static synchronized int numberOfActiveUsers() {
		return active.size();
	}
	
	public static synchronized int numberOfInactiveUsers() {
		return inactive.size();
	}
	
	public static synchronized User getActive(int i) {
		if (i >= active.size())
			return null;
		else
			return active.get(i);
	}
	
	public static synchronized User getInactive(int i) {
		if (i >= inactive.size()) {
			return null;
		} else
			return inactive.get(i);
	}
}
