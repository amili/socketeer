package socketeer.plugins.connection;

import java.util.Map;

import socketeer.sterconfig;
import socketeer.stersourcesink;

public interface stergenericcon {

	boolean setConfigProfileSourceSink(stersourcesink sosi, sterconfig conf, String profile);
	
	String getProtocolID();
	
	void send(String destination,String message);
	
}
