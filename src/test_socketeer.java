import java.io.IOException;
import java.net.Socket;

import javax.security.auth.login.LoginException;

import net.ser1.stomp.Client;

/**
 * 
 * @author Alen Milincevic
 *
 * direct tester for sourcesink spawning
 *
 */

public class test_socketeer {

	public static void main(String[] args) {

		// load the config
		sterconfig c = new sterconfig();
		c.getConfigFromFile("/home/user/workspace/socketeer/socketeer.config");
		
		stersourcesink s = new stersourcesink();
		try {
			Client cli = new Client( c.getChannelHost("0"), c.getChannelPort("0"),
					c.getChannelUsername("0"), c.getChannelPassword("0") );
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//s.init("bla", new Socket(), "", "", 0, c, "0", null);

	}
	
}
