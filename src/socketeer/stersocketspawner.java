package socketeer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.ser1.stomp.Client;

/**
 * 
 * @author Alen Milincevic
 *
 * Spawn new bind sockets
 *
 */

public class stersocketspawner implements Runnable {

	int port = 0;
	String secondReply = null;
	stersourcesink sosi = null;
	stermessage sm = null;
	//Client cli = null;
	String profile = null;
	
	void setParams(int port, String secondReply, stersourcesink sosi, stermessage sm, Client cli, String profile) {
		this.port = port;
		this.secondReply = secondReply;
		this.sosi = sosi;
		this.sm = sm;
		//this.cli = cli;
		this.profile = profile;
	}
	
	void handleBinding() {
		try {
			ServerSocket bins = new ServerSocket(port);
			Socket bs = bins.accept();
			bs = sterpool.socketfactory(bs,"", 0,
					stermessage.joinClusterNodeSubnode(sosi.getClusterID(), sosi.getNodeID(), sm.getToResourceID() ) );
			if (bs == null) {
				// TODO: error handling by message
			}
			sterrelaythread st = sterpool.relayfactory(sosi,"sp"+stermessage.joinClusterNodeSubnode(sosi.getClusterID(), sosi.getNodeID(), sm.getToResourceID() ));
			if (st == null) {
				// TODO: error handling by message
			}
			try {
				st.setSourceSink(sosi);
				st.initTCP(bs.getInputStream(),
						stermessage.joinClusterNodeSubnode(
								sosi.getClusterID(),
								sosi.getNodeID(),
								sm.getToResourceID() ),
						stermessage.joinClusterNodeSubnode(
								sm.getFromCluster(),
								sm.getFromNode(),
								sm.getFromResourceID() ),
								sosi.getConfig(), sosi.getProfile(), null
								);
				new Thread(st).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (secondReply != null) { 
				stermessage acm = new stermessage();
				acm.setMessage(-1,
						stermessage.joinClusterNodeSubnode(sosi.getClusterID(),sosi.getNodeID(),sm.getToResourceID()),
						stermessage.joinClusterNodeSubnode(sm.getFromCluster(),sm.getFromNode(),sm.getFromResourceID()),
						stermessage.getNextSeqID(), sterconst.MESSAGE_SEND+"");
				acm.setCommandNameConstant(sterconst.MESSAGE_SEND+"");
				acm.setCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+"", secondReply);
				//cli.send(sosi.getConfig().getChannelTopic(profile), acm.getAsSerial());
				sosi.channelSend(sosi.getConfig().getChannelTopic(profile),
						acm.getAsEncryptedSerial(
								sosi.getConfig().getChannelEncryptionKey(profile), 
								sosi.getConfig().getChannelEncryptionIterations(profile)
								)
						);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		handleBinding();
	}

}
