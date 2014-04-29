import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import net.ser1.stomp.Client;
import net.ser1.stomp.Listener;

/**
 * 
 * @author Alen Milincevic
 *
 * The main building block, source and sink
 * 
 */

public class stersourcesink implements Runnable {

	String clusterID = null;
	String nodeID = null;
	String resourceID = null;
	
	Socket sourceTCPsock = null;
	
	sterconfig conf = null;
	String profile = null;
	
	void setClusterID(String clusterID) {
		this.clusterID = clusterID;
	}
	
	String getClusterID() {
		return clusterID;
	}
	
	void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}
	
	String getNodeID() {
		return nodeID;
	}
	
	String getResourceID() {
		return resourceID;
	}
	
	stersourcesink getThisOne() {
		return this;
	}
	
	Socket getSourceTCPsock() {
		return sourceTCPsock;
	}
	
	sterconfig getConfig() {
		return conf;
	}
	
	String getProfile() {
		return profile;
	}
	
	Client cli = null;
	
	boolean isSinkInsteadSource = false;
	
	void setSinkCloseMessageToHostBecauseOfError(stermessage tsm) {
		stermessage sm = new stermessage();
		sm.setMessage(-1, 
					stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(),getResourceID()),
					stermessage.joinClusterNodeSubnode(tsm.getFromCluster(), tsm.getFromNode(), tsm.getFromResourceID())
					, sm.getNextSeqID(), sterconst.MESSAGE_CLOSE+"");
		sm.setCommandNameConstant(sterconst.MESSAGE_CLOSE+"");
		sm.setCommandParameter(sterconst.MESSAGE_CLOSE_RESOURCE+"", tsm.getFromResourceID());
		sterlogger.getLogger().info("!! error happened."+sm.getAsSerial());
	}
	
	static boolean writeHexToOutputStream(OutputStream os, String hexstring) {
		sterlogger.getLogger().info("to be written hex!="+hexstring);
		for (int i=0;i<hexstring.length()/2;i=i+2) {
			try {
				os.write(Integer.parseInt(hexstring.substring(i,i+2), 16));
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	boolean init (String channel,
				Socket s,
				sterconfig conf, String profile,
				Client cli) {
		
		this.cli = cli;
		this.conf = conf;
		this.profile = profile;
		
		setClusterID(conf.getClusterName(profile));
		setNodeID(conf.getNodeName(profile));
		this.resourceID = getNodeID()+stermessage.getUniqueID();
		
		sterlogger.getLogger().info("sink not source (0):"+isSinkInsteadSource+" "+s);
		
		sourceTCPsock = s;
		if (s == null) {isSinkInsteadSource = true;}
		
		sterlogger.getLogger().info("sink not source:"+isSinkInsteadSource);

		handler (null);
		
		if (isSinkInsteadSource == false) { // source

			// check accesslist
			if (
			   (conf.isAccessListAuthorised(profile, s.getInetAddress().getHostAddress()) == false) &&
			   (conf.isAccessListAuthorised(profile, s.getInetAddress().getHostName()) == false)
			   ){
				return false;
			}
			
			// init connection parameters
			String targethost = conf.getFixedHost(profile);
			int targetport = conf.getFixedPort(profile);
			int targettype = conf.getFixedSocketType(profile);
			
			// handle SOCKS, if enabled
			sterproxy sp = new sterproxy();
			if (targethost == null) {
				// start socks resolve, which result is host,port and type
				try {
					boolean proxysucess = sp.initProxy(s.getInputStream(), s.getOutputStream(), conf, profile);
					if (proxysucess == false) {
						return false;
					}
					targethost = sp.getHost();
					targetport = sp.getPort();
					targettype = sp.getType();
					sterlogger.getLogger().info("((((((((((((((hpt:"+targethost + ","+targetport+","+targettype);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// Check black and white list
			boolean auth =
			conf.isAuthorisedCombination(profile, getClusterID(), conf.getPeerNodeName(profile), targethost, targetport);
			if (auth == false) {
				sterlogger.getLogger().info("%%%%% Unathorised:"+targethost + ","+targetport+","+targettype);
				return false;
				}
			
			// TODO: remapping
			String hostport = conf.getRemappingForProfileAsHostPortPair(profile, targethost, targetport);
			if (hostport != null) {
				sterlogger.getLogger().info("%%%%% Remapping:"+hostport);
				targethost = hostport.split(" ")[0];
				targetport = Integer.parseInt(hostport.split(" ")[1]);
			}
			
			// init the communication by sending message
			stermessage sm = new stermessage();
			String uniqueid = stermessage.getUniqueID();
			sm.setMessage(-1,
					stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(),resourceID),
					stermessage.joinClusterNodeSubnode(getClusterID(), conf.getPeerNodeName(profile), uniqueid),
					stermessage.getNextSeqID(), sterconst.MESSAGE_OPEN+"");
			sm.setCommandNameConstant(sterconst.MESSAGE_OPEN+"");
			sm.setCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_HOST+"", targethost);
			sm.setCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_PORT+"", ""+targetport);
			sm.setCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_TYPE+"", ""+targettype);
			if (sp.getSecondSocksBindReply() != null) {
				sm.setCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_SECONDREPLY+"", ""+sp.getSecondSocksBindReply());
			}
			sterlogger.getLogger().info("message is:"+sm.getAsSerial());

			// test
			sterlogger.getLogger().info("init message passed with command :"+sm.getCommandNameConstant());
			cli.send(getConfig().getChannelTopic(profile), sm.getAsSerial());
			
			// connect backwards
			sterrelaythread bst = sterpool.relayfactory(getThisOne());
			try {
				bst.initTCP(getSourceTCPsock().getInputStream(),
						stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(),resourceID),
						stermessage.joinClusterNodeSubnode(getClusterID(), conf.getPeerNodeName(profile), uniqueid),
						cli,getConfig(),profile);
				new Thread(bst).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			sterlogger.getLogger().info("relay reference(1):"+bst+","+bst.getFrom()+","+bst.getTo());
			sterpool.debugRelayThread();
			
		} else {	// sink
			// nothing (for now)
		}
		return true;
	}
	
	void handler (stermessage initmessage) {
		
		Listener l = new Listener() {

			public void message(Map header, String body) {
				stermessage sm = new stermessage();
				sm.setFromSerial(body);
				sterlogger.getLogger().info("message handler:"+isSinkInsteadSource+","+sm.getAsSerial());

				// chek if correct address
				if (sm.getToCluster() != null) {
					if (getClusterID() != null) {
						if (getClusterID().equals(sm.getToCluster()) == false) {
							// not for this cluster
							sterlogger.getLogger().info("not for this cluster .["+sm.getToCluster()+"]["+getClusterID()+"]");
							return;
						}							
					}
				}
				if (sm.getToNode() != null) {
					if (getNodeID() != null) {
						if (getNodeID().equals(sm.getToNode()) == false) {
							// not for this node
							sterlogger.getLogger().info("not for this node .["+sm.getToNode()+"]["+getNodeID()+"]");
							return;
						}		
					}
				}

				
				// TODO: global response (pong)
				if ( sm.getCommandNameConstant() == sterconst.MESSAGE_PING ) {
					String pingpayload = sm.getCommandParameter(sterconst.MESSAGE_PING_PAYLOAD+"");
					stermessage pingpong = new stermessage();
					pingpong.setMessage(-1,
							stermessage.joinClusterNodeSubnode(sm.getToCluster(), sm.getToNode(), sm.getToResourceID()),
							stermessage.joinClusterNodeSubnode(sm.getFromCluster(), sm.getFromNode(), sm.getFromResourceID()),
							stermessage.getNextSeqID(), sterconst.MESSAGE_PONG+"");
					pingpong.setCommandNameConstant(sterconst.MESSAGE_PONG+"");
					pingpong.setCommandParameter(sterconst.MESSAGE_PONG_PAYLOAD+"", pingpayload);
				}
				
				//isSinkInsteadSource = true; // debug
				//sterlogger.getLogger().info("force mode:"+isSinkInsteadSource+","+sm.getCommandNameConstant()); //debug
				
				if (isSinkInsteadSource == false) { // source
					
					// resource is the own id
					if (sm.getToResourceID() != null) {
						if (getResourceID() != null) {
							if (getResourceID().equals(sm.getToResourceID()) == false) {
								// not for this node
								sterlogger.getLogger().info("not for source resource ["+sm.getToResourceID()+"]["+getResourceID()+"]");
								return;
							}		
						}
					}
					
					// TODO: resolving
					if (sm.getCommandNameConstant() == sterconst.MESSAGE_RESOLVE_RESPONSE) {
						
					}
					// open, close
					else if ( sm.getCommandNameConstant() == sterconst.MESSAGE_OPEN) {
						// TODO: confirmation of remote opened, should notify somewhere
					}
					else if ( sm.getCommandNameConstant() == sterconst.MESSAGE_SEND) {
						String resource = sm.getToResourceID();
						sterlogger.getLogger().info("Sourcesend message!"+resource+","+sm.getToCluster()+","+sm.getToNode());
						Socket sms = getSourceTCPsock();
						sterlogger.getLogger().info("Source sending socket is!"+sms);
						try {
							sterlogger.getLogger().info("Sourcesent!0"+sm.getFromNode());
							OutputStream os = sms.getOutputStream();
							sterlogger.getLogger().info("Sourcesent!1="+sm.getCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+""));
							/*os.write(
									Integer.parseInt(sm.getCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+""), 16)
									);*/
							sterlogger.getLogger().info("Sourcesentbefore!="+sm.getCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+""));
							writeHexToOutputStream(os, sm.getCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+""));
							sterlogger.getLogger().info("Sourcesentafter!="+sm.getCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+""));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else if ( sm.getCommandNameConstant() == sterconst.MESSAGE_CLOSE) {
						try {
							getSourceTCPsock().close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else {
						// unknown
					}
				} else { // sink
					
					// resource is the socket id
					/*if (sm.getResourceID() != null) {
						if (getResourceID() != null) {
							if (getResourceID().equals(sm.getResourceID()) == false) {
								// not for this node
								sterlogger.getLogger().info("not for resource");
								return;
							}		
						}
					}*/
					
					sterlogger.getLogger().info("--- command:"+sm.getCommandNameConstant());
					
					// resolve, open, close
					if ( sm.getCommandNameConstant() == sterconst.MESSAGE_RESOLVE_REQUEST) {
						String hostname = sm.getCommandParameter(sterconst.MESSAGE_RESOLVE_HOSTNAME+"");
						int restype = Integer.parseInt(sm.getCommandParameter(sterconst.MESSAGE_RESOLVE_TYPE+""));
						sterdnresolver resolv = new sterdnresolver(hostname);
						// TODO: form and send the message
						stermessage dnsres = new stermessage();
						dnsres.setMessage(-1,
								stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(), getResourceID()),
								stermessage.joinClusterNodeSubnode(sm.getFromCluster(), sm.getFromNode(), sm.getFromResourceID() ),
								stermessage.getNextSeqID(), sterconst.MESSAGE_RESOLVE_RESPONSE+"");
						dnsres.setCommandNameConstant(sterconst.MESSAGE_RESOLVE_RESPONSE+"");
						dnsres.setCommandParameter(sterconst.MESSAGE_RESOLVE_IP+"", resolv.getFirstIPV4Address());
						cli.send(getConfig().getChannelTopic(profile), dnsres.getAsSerial());
					}
					if ( sm.getCommandNameConstant() == sterconst.MESSAGE_OPEN) {
						sterlogger.getLogger().info("open message!"+sm.getCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_TYPE+""));
						if (sm.getCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_TYPE+"").equals(sterconst.SOCKET_TYPE_CONNECTION+"")){
							String host = sm.getCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_HOST+"");
							int port = Integer.parseInt(sm.getCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_PORT+""));
							String resource = sm.getToResourceID();
							Socket rse = sterpool.socketfactory(host, port,
									stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(), resource ), getConfig().getConnectionTimeoutInMsec());
							sterlogger.getLogger().info("socket ref:"+rse+
									" "+sterpool.getSocketByName(stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(), resource )));
							if (rse == null) {
								// TODO: create an error and return it!
								setSinkCloseMessageToHostBecauseOfError(sm);
							}
							sterrelaythread st = sterpool.relayfactory(getThisOne());
							if (st == null) {
								// TODO: error handling by message
								setSinkCloseMessageToHostBecauseOfError(sm);
							}
							try {
								st.initTCP(rse.getInputStream(),
										stermessage.joinClusterNodeSubnode(
												getClusterID(),
												getNodeID(),
												resource ),
										stermessage.joinClusterNodeSubnode(
												sm.getFromCluster(),
												sm.getFromNode(),
												sm.getFromResourceID() ),
												cli, getConfig(), getProfile()
												);
								new Thread(st).start();
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							sterlogger.getLogger().info("relay reference(0):"+st+","+st.getFrom()+","+st.getTo());
							sterpool.debugRelayThread();
							
							// TODO: send return confirmation message
							
						}
						else if (sm.getCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_TYPE+"").equals(sterconst.SOCKET_TYPE_BIND+"")) {
							int port = Integer.parseInt(sm.getCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_PORT+""));
							String secondreply = sm.getCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_SECONDREPLY+"");
							stersocketspawner spa = new stersocketspawner();
							spa.setParams(port, secondreply, getThisOne(),sm,cli,profile);
							Thread bt = new Thread(spa);
							bt.start();
							sterlogger.getLogger().info("binding finished:"+port+","+secondreply);
						}
						else if (sm.getCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_TYPE+"").equals(sterconst.SOCKET_TYPE_UDP+"")) {
							// TODO
						}
						else {
							// TODO
						}
					}
					else if ( sm.getCommandNameConstant() == sterconst.MESSAGE_SEND) {
						String resource = sm.getToResourceID();
						sterlogger.getLogger().info("send message!"+resource+","+sm.getToCluster()+","+sm.getToNode());
						Socket ms = sterpool.getSocketByName( stermessage.joinClusterNodeSubnode(sm.getToCluster(), sm.getToNode(), resource ) );
						if (ms == null) {
							// TODO: error handling by message
							setSinkCloseMessageToHostBecauseOfError(sm);
						}
						sterlogger.getLogger().info("sending socket is!"+ms);
						try {
							sterlogger.getLogger().info("sent!0");
							OutputStream os = ms.getOutputStream();
							sterlogger.getLogger().info("sent!1="+sm.getCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+""));
							/*os.write(
									Integer.parseInt(sm.getCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+""), 16)
									);*/
							writeHexToOutputStream(os, sm.getCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+""));
							sterlogger.getLogger().info("sent!="+sm.getCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+""));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else if ( sm.getCommandNameConstant() == sterconst.MESSAGE_CLOSE) {
						String resource = sm.getToResourceID();
						Socket s = sterpool.getSocketByName( stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(), resource ) );
						if (s == null) {
							// TODO: error handling by message
							setSinkCloseMessageToHostBecauseOfError(sm);
						}
						try {
							if (s != null) {s.close();}
						} catch (IOException e) {
							e.printStackTrace();
						}
						sterpool.nullifySocketByName(stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(), resource ));
					}
					else {
						// unknown
					}
				}
			}			
		};
		
		sterlogger.getLogger().info("*** channel is:"+ getConfig().getChannelTopic(getProfile()));
		if (getConfig().getChannelTopic(getProfile()) != null) {
			cli.subscribe( getConfig().getChannelTopic(getProfile()), l );
		}
		
		// simulate an init message
		if (initmessage != null) {
			sterlogger.getLogger().info("init is:"+initmessage.getAsSerial());
			isSinkInsteadSource = true; // debug
			l.message(null, initmessage.getAsSerial());
		}
		
	}

	public void run() {
		// TODO: start as thread
		
	}
	
}