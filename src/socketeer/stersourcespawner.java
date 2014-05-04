package socketeer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import socketeer.plugins.genericStream.genericStream;

import net.ser1.stomp.Client;

/**
 * 
 * @author Alen Milincevic
 *
 * New spawner
 *
 */

public class stersourcespawner implements Runnable {

	int port = 0;
	
	String channel = null;
	sterconfig conf = null;
	String profile = null;
	Client cli = null;
	
	boolean isConsoleInsteadSocket = false;
	
	void isConsoleInsteadSocket (boolean isConsoleInsteadSocket) {
		this.isConsoleInsteadSocket = isConsoleInsteadSocket;
	}
	
	void setPort(int port) {
		this.port = port;
	}
	
	void setData (String channel,
			sterconfig conf, String profile,
			Client cli) {
		this.channel = channel;
		this.conf = conf;
		this.profile = profile;
		this.cli = cli;
	}
	
	void startIt() {
		try {
			genericStream gt = new genericStream();
			stersourcesink sosi = sterpool.sourcesinkfactory();
			if (isConsoleInsteadSocket == true) {
				gt.setStreamsFromConsole();
				if (sosi.init(channel, gt, conf, profile, cli) == false) {
					// todo: something in error case
				}
			} else {
				ServerSocket source = new ServerSocket(port);
				boolean looping = true;
				while (looping == true) {
					Socket spawned = source.accept();
					sterlogger.getLogger().info("spawned:"+spawned);
					gt.setStreamsFromSocket(spawned);
					if (sosi.init(channel, gt, conf, profile, cli) == false) {
						spawned.close();
					}
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		startIt();
	}

}
