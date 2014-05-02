package socketeer.plugins.connection;

import java.util.Map;

import socketeer.sterconfig;
import socketeer.stersourcesink;

public class sterchannel implements stergenericcon {

	sterconfig conf;
	String profile;
	stersourcesink sosi;
	
	public boolean setConfigProfileSourceSink(stersourcesink sosi, sterconfig conf, String profile) {
		this.conf = conf;
		this.profile = profile;
		this.sosi = sosi;
		return true;
	}
	
	public void send(String destination, String message) {
		// to be implemented by pluging;
	}

	public String getProtocolID() {
		// to be implemented by plugin
		return null;
	}
	
}
