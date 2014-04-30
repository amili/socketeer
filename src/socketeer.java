import java.io.IOException;

import javax.security.auth.login.LoginException;

import net.ser1.stomp.Client;
import net.ser1.stomp.Server;

/**
 * 
 * Socketeer
 * 
 * A simulated socks4/(a)5 proxy, which tunnels the connection over a STOMP channel
 * using a configuration file
 * 
 * The tool is ment to go through a firewall by simulating a proxy/direct connection.
 * The building blocks are SourceSinks, composed of at least one source and sink
 * declared by a node name and combined in a cluster.
 * 
 * !!!!!!! ALPHA !!!!!!!, more a POC (proof of concept) 
 * 
 * @author Alen Milincevic
 *
 * Uses: gozirra - http://www.germane-software.com/software/Java/Gozirra/ (LGPL license)
 * 
 * TODO:
 * - UDP, proper SOCKS 4 bind support, proper SOCKS 5 support
 * - authorisation, black/white lists
 * - better proxy support
 * - other protocol support (HTTP, e-mail, RPC)
 * - better documentation
 * ...
 * 
 */

public class socketeer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// load the config
		sterconfig c = new sterconfig();
		if (args.length > 1) {
			if (args[0].equalsIgnoreCase("console")) {
				c.getConfigFromStream(System.in);
			}
			else if (args[0].equalsIgnoreCase("file")) {
				c.getConfigFromFile(args[1]);	
			}
			else if (args[0].equalsIgnoreCase("url")) {
				c.getConfigFromURL(args[1]);
			}
			else if (args[0].equalsIgnoreCase("line")) {
				c.getConfigFromString(args[1], true);
			}
			else if (args[0].equalsIgnoreCase("resource")) {
				c.getConfigFromResourceStream(args[1]);
			}
			else {
				c.getConfigFromFile(args[1]);	
			}
		} else if (args.length > 0) {
			c.getConfigFromFile(args[0]);
		} else {
			c.getConfigFromFile("/home/user/workspace/socketeer/socketeer.config");	
		}
		
		// init the general stuff (server)
		if (c.isMessagingServerEnabled() == true) {
			try {
				Server s = new Server( c.getMessagingServerPort() );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// TODO: init the "glue code, if wanted", TBD...
		
		String[] pns = c.getProfileNames(null);
		
		// init first sinks, than sources
		for (int i=0;i<pns.length;i++) { // sinks
			if (c.isSpanningAndResolvingSink(pns[i]) == true) {
				stersourcesink sosi = sterpool.sourcesinkfactory();
				sosi.init(c.getChannel(pns[i]), null,
						  c, pns[i], null);
			}
		}
		for (int i=0;i<pns.length;i++) { // sources
			if (c.isSpanningAndResolvingSink(pns[i]) == false) {
				stersourcespawner spawn = new stersourcespawner();
				spawn.setPort(c.getServerPort(pns[i]));
				spawn.setData(c.getChannel(pns[i]),
						  c, pns[i], null);
				Thread t = new Thread(spawn);
				t.start();
			}
		}
	}

}
