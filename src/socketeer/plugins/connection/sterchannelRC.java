package socketeer.plugins.connection;

import socketeer.sterconfig;
import socketeer.stersourcesink;

public class sterchannelRC extends sterchannel {

	sterchanneIRCimpl sci;
	
	public boolean setConfigProfileSourceSink(stersourcesink sosi, sterconfig conf, String profile) {
		sterchanneIRCimpl sci = new sterchanneIRCimpl(this);
		sci.setNickLoginChannel(
				conf.getChannelParameterByKey(profile, "nick"),
				conf.getChannelParameterByKey(profile, "login"),
				conf.getChannelTopic(profile),
				conf.getChannelParameterByKey(profile, "uid")
				);
		if (sci.connect(conf.getChannelHost(profile), conf.getChannelPort(profile)) == true) {
			Thread irct = new Thread(sci);
			irct.start();
		} else {
			return false;
		}
		return super.setConfigProfileSourceSink(sosi, conf, profile);
	}
	
	public void send(String destination, String message) {
		sci.send(destination,message);
	}
	
	public String getProtocolID() {
		return "IRC";
	}
	
}
