package socketeer;
import java.io.IOException;
import java.io.InputStream;

import net.ser1.stomp.Client;

/**
 * 
 * @author Alen Milincevic
 *
 * Data relay
 *
 */


public class sterrelaythread implements Runnable {

	boolean isUDPinsteadTCP = false;
	
	InputStream is = null;

	stersourcesink sosiref = null;
	
	String from = null;
	String to = null;
	
	//Client cli;
	sterconfig conf;
	String profile;
	
	// execution control
	boolean sheduledForExit = false;
	
	// TODO: group id, used for destroying
	String groupid = null;
	
	void sheduleForExit() {
		sheduledForExit = true;
	}
	
	void initTCP(InputStream is,String from, String to, sterconfig conf, String profile, String groupID) {
		this.is = is;
		this.from = from;
		this.to = to;
		//this.cli = cli;
		this.conf = conf;
		this.profile = profile;
		this.groupid = groupID;
	}
	
	void setSourceSink(stersourcesink sosi) {
		sosiref = sosi;
	}
	
	stersourcesink getSourceSink() {
		return sosiref;
	}
	
	String getFrom() {
		return from;
	}
	
	String getTo() {
		return to;
	}
	
	String getGroupID() {
		return groupid;
	}
	
	void startTCPrelay() {
		int c;
		stermessage sm = new stermessage();
		try {
			sterlogger.getLogger().info("$$$$ about to start..."+getFrom()+","+getTo()+","+profile+","+conf.getChannelTopic(profile));
			c = is.read();
			sterlogger.getLogger().info("$$$$ starting:"+getFrom()+","+getTo()+","+profile+","+conf.getChannelTopic(profile)+","+c);
			while (c > -1) {
				if (sheduledForExit == true) {return;}
				// create a message with from, to and the data
				sm.setMessage(-1, from, to, stermessage.getNextSeqID(), sterconst.MESSAGE_SEND+"");
				sm.setCommandNameConstant(sterconst.MESSAGE_SEND+"");
				String hexvalue = Integer.toHexString(c);
				if (hexvalue.length() < 2) {hexvalue = "0"+hexvalue;} 
				sm.setCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+"", hexvalue); 
				sterlogger.getLogger().info("to be sent ["+conf.getChannelTopic(profile)+"]:"+sm.getAsSerial());
				//cli.send(conf.getChannelTopic(profile), sm.getAsSerial());
				sosiref.channelSend(conf.getChannelTopic(profile),
						sm.getAsEncryptedSerial(conf.getChannelEncryptionKey(profile),
						conf.getChannelEncryptionIterations(profile)));
				sterpool.debugRelayThread();
				if (sheduledForExit == true) {return;}
				c = is.read();	
			}
			sterlogger.getLogger().info("$$$$ closing relay"+getFrom()+","+getTo()+","+profile+","+conf.getChannelTopic(profile));
		} catch (IOException e) {
			sterlogger.getLogger().info("$$$$ error:"+getFrom()+","+getTo()+","+profile+","+conf.getChannelTopic(profile)+","+e);			
			e.printStackTrace();
			if (sosiref != null) {
				if (getTo() != null) {
					sterlogger.getLogger().info("sending error to:"+getTo()+","+getGroupID());
					sosiref.sendSelfCloseMessageToHostBecauseOfError(getTo(),getGroupID());
				}
			}
		}
		sm.setMessage(-1, from, to, stermessage.getNextSeqID(), sterconst.MESSAGE_CLOSE+"");
		sm.setCommandNameConstant(sterconst.MESSAGE_CLOSE+"");
		sm.setCommandParameter(sterconst.MESSAGE_CLOSE_RESOURCE+"",from);
		sm.setCommandParameter(sterconst.MESSAGE_PARAMETER_GROUPID+"",getGroupID());
		sterlogger.getLogger().info("socket closed:"+sm.getAsSerial());
		//cli.send(conf.getChannelTopic(profile), sm.getAsSerial());
		sosiref.channelSend(conf.getChannelTopic(profile),
				sm.getAsEncryptedSerial(
						conf.getChannelEncryptionKey(profile),
						conf.getChannelEncryptionIterations(profile)
						)
				);
	}
	
	void setUDPinsteadTCP() {
		// todo;
	}
	
	void initUDP() {
		// TODO
	}

	public void run() {
		
		if (isUDPinsteadTCP == true) {
			
		} else {
			sterlogger.getLogger().info("$$$$ initing relay"+getFrom()+","+getTo());
			startTCPrelay();
		}
		
	}
	
}
