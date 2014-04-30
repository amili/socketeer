import net.ser1.stomp.Client;

/**
 * Apache channel connection to Gozirra STOMP server
 * 
 * TODO, planned
 * apache camel part
 */

public class sterbridgea {

	Client cli = null;
	
	void connectWithGozirra(Client cli) {
		this.cli = cli;
	}
	
	void setRoute(String from, String to) {
		
	}
	
}
