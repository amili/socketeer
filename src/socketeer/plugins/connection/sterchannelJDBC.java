package socketeer.plugins.connection;

import java.sql.SQLException;

import socketeer.sterconfig;
import socketeer.stersourcesink;

public class sterchannelJDBC extends sterchannel {

	sterchannelJDBCimpl ji = null;
	
	public boolean setConfigProfileSourceSink(stersourcesink sosi, sterconfig conf, String profile) {
		try {
			ji = new sterchannelJDBCimpl(
						conf.getChannelHost(profile),
						conf.getChannelParameterByKey(profile, "conurl"),
						conf.getChannelParameterByKey(profile, "username"),
						conf.getChannelParameterByKey(profile, "password")					
						);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return super.setConfigProfileSourceSink(sosi, conf, profile);
	}
	
	public String getProtocolID() {
		return "jdbc";
	}
	
	public void returner (String message[]) {
		for (int i=0;i<message.length;i++) {
			sosi.callbackmessage(null, message[i]);	
		}
	}
	
	public void send(String destination,String message) {
		ji.send(destination, message,sosi.getNodeID());
	}
	
}
