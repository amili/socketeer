package socketeer.plugins.connection;

// component from: https://github.com/eriksson/smack-bosh

// TODO: http://www.igniterealtime.org/builds/smack/docs/latest/documentation/extensions/muc.html
// full MUC support and/or PubSub support

// smackx 3.2.2. extracted from:
//     http://code.google.com/p/openhab/source/browse/bundles/io/org.openhab.io.net/lib/smackx-3.2.2.jar?r=4279588f9cfdcf08b2f6530130612154a97aa04a

import org.jivesoftware.smack.BOSHConfiguration;
import org.jivesoftware.smack.BOSHConnection;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import socketeer.sterconfig;
import socketeer.stersourcesink;

public class sterchannelXMPP extends sterchannel {

	ChatManager chatManager;
	MessageListener messageListener;
	MultiUserChat muc;
	
	public boolean setConfigProfileSourceSink(stersourcesink sosi, sterconfig conf, String profile) {
	
		Connection connection;
		
		if (conf.getChannelParameterByKey(profile, "boshurl") != null) {
			BOSHConfiguration config = new BOSHConfiguration(false, 
					conf.getChannelHost(profile),
					conf.getChannelPort(profile),
					conf.getChannelParameterByKey(profile, "boshurl"),
					conf.getChannelTopic(profile),
					conf.getChannelParameterByKey(profile, "xmppurl")
					);
			connection = new BOSHConnection(config);		
		} else {
			ConnectionConfiguration config = new ConnectionConfiguration(
					conf.getChannelHost(profile),
					conf.getChannelPort(profile),
					conf.getChannelTopic(profile));
			connection = new XMPPConnection(config);
		}
		
		try {
			connection.connect();
			connection.login(
					conf.getChannelParameterByKey(profile, "username"),
					conf.getChannelParameterByKey(profile, "password"),
					conf.getChannelParameterByKey(profile, "resource")
					);
			chatManager = connection.getChatManager();
			messageListener = new MyMessageListener();
			if (conf.getChannelParameterByKey(profile, "conferenceroom") != null) {
				muc = new MultiUserChat(connection, conf.getChannelParameterByKey(profile, "conferenceroom"));
			    if (conf.getChannelParameterByKey(profile, "joinpassword") == null) {
			    	muc.join(conf.getChannelParameterByKey(profile, "joinnick"));	
			    } else {
			    	muc.join(conf.getChannelParameterByKey(profile, "joinnick"),conf.getChannelParameterByKey(profile, "joinpassword"));
			    }
			}

		} catch (XMPPException e) {
			e.printStackTrace();
			return false;
		}
		return super.setConfigProfileSourceSink(sosi, conf, profile);
	}
	
	public String getProtocolID() {
		return "XMPP";
	}
	
	public void send(String destination,String message) {
	      Chat chat = chatManager.createChat(destination, messageListener);
	      try {
			chat.sendMessage(message);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
	
}

class MyMessageListener implements MessageListener {

	stersourcesink sosi = new stersourcesink();
	
	public void setSourceSink(stersourcesink sosi) {
		this.sosi = sosi;
	}
	
    public void processMessage(Chat chat, Message message) {
        String from = message.getFrom();
        String body = message.getBody();
        System.out.println("received:"+from+","+body);
        sosi.callbackmessage(null, body);
    }
    
}