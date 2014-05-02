package socketeer;
/**
 * 
 * @author Alen Milincevic
 *
 * Generic pool of lock resources
 * TODO
 *
 */

public class sterlockpool {

	protected static String[] lockid = new String[100];
	
	synchronized static void lock(String id) {
		for (int i=0;i< lockid.length;i++) {
			if (lockid[i] != null) {
				if (lockid[i].equals(id)) {
					while (lockid[i] != null) {}
				}				
			}
		}
	}
	
	synchronized static void unlock(String id) {
		for (int i=0;i< lockid.length;i++) {
			if (lockid[i] != null) {
				if (lockid[i].equals(id)) {
					lockid[i] = null;
					return;
				}	
			}
		}		
	}
	
}
