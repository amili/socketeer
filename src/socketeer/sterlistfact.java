package socketeer;

import net.ser1.stomp.Listener;

/*
 * Listener factory
 */

public class sterlistfact {

	static Listener[] l = new Listener[100];	// listener itself
	static String[] ln = new String[100];		// listener name
	static String[] lgid = new String[100];	// group id

	public sterlistfact() {
		setLength(100);
	}
	
	public sterlistfact(int length) {
		setLength(length);
	}

	private void setLength(int length) {
		l = new Listener[length];
		ln = new String[length];
		lgid = new String[length];
	}
	
	static public Listener getListener(String name) {
		return getListener(name, null, null);
	}
	
	static public Listener getListener(String name, String gid) {
		return getListener(name, gid, null);
	}
	
	static public Listener getListener(String name, Listener l) {
		return getListener(name, null, l);
	}
	
	static public Listener getListener(String name, String gid, Listener prel) {
		if (prel == null) {
			for (int i=0;i<ln.length;i++) {
				if (l[i] != null) {
					if (ln[i].equals(name) == true) {
						return l[i];	
					}

				}
			}
			return null;
		} else {
			for (int i=0;i<ln.length;i++) {
				if (l[i] == null) {
					l[i] = prel;
					ln[i] = name;
					lgid[i] = gid;
					return l[i];	
				}
			}
			return null;	
		}
	};
	
	static public boolean freeListener(String name) {
		for (int i=0;i<ln.length;i++) {
			if (l[i] != null) {
				if (ln[i].equals(name)) {
					l[i] = null;
					ln[i] = null;
					lgid[i] = null;
					return true;
				}	
			}
		}
		return false;
	}
	
	static public boolean freeListenerByGroupID(String gid) {
		for (int i=0;i<ln.length;i++) {
			if (l[i] != null) {
				if (lgid[i].equals(gid)) {
					l[i] = null;
					ln[i] = null;
					lgid[i] = null;
					return true;
				}	
			}
		}
		return false;
	}
	
	static void debugListeners() {
		sterlogger.getLogger().info("Listenerdebug----");
		for (int i=0;i<l.length;i++) {
			if (l[i] != null) {
				sterlogger.getLogger().info("Listener ["+i+"]:"+ln[i]+"="+l[i]);
			}
		}
	}

}
