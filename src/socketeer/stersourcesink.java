package socketeer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import socketeer.plugins.connection.sterplugin;
import socketeer.plugins.genericStream.genericStream;
import socketeer.plugins.genericStream.gsLoader;

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
	
	Listener l = null;
	
	sterplugin plugin = null;
	
	gsLoader gsl = new gsLoader();
	
	void setClusterID(String clusterID) {
		this.clusterID = clusterID;
	}
	
	public String getClusterID() {
		return clusterID;
	}
	
	void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}
	
	public String getNodeID() {
		return nodeID;
	}
	
	public String getResourceID() {
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
	
	gsLoader getGenericStreamInterface() {
		return gsl;
	}
	
	Client cli = null;
	
	boolean isSinkInsteadSource = false;
	
	Client initClient() {
		try {
			sterlogger.getLogger().info("init client for channel:"+
								getConfig().getChannelHost(getProfile())+","+
								getConfig().getChannelPort(getProfile())+" "+
								getConfig().getChannelUsername(getProfile())+" "+
								getConfig().getChannelPassword(getProfile())
								);
			cli = new Client( getConfig().getChannelHost(getProfile()),
							getConfig().getChannelPort(getProfile()),
							getConfig().getChannelUsername(getProfile()), 
							getConfig().getChannelPassword(getProfile())
							);
			sterlogger.getLogger().info("client inited."+cli);
			return cli;
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	void sendSinkCloseMessageToHostBecauseOfError(stermessage tsm) {
		stermessage sm = new stermessage();
		sm.setCryptography(getConfig().getChannelEncryptionKey(profile), getConfig().getChannelEncryptionIterations(profile));
		sm.setMessage(-1, 
					stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(),getResourceID()),
					stermessage.joinClusterNodeSubnode(tsm.getFromCluster(), tsm.getFromNode(), tsm.getFromResourceID())
					, sm.getNextSeqID(), sterconst.MESSAGE_CLOSE+"");
		sm.setCommandNameConstant(sterconst.MESSAGE_CLOSE+"");
		sm.setCommandParameter(sterconst.MESSAGE_CLOSE_RESOURCE+"", tsm.getFromResourceID());
		sterlogger.getLogger().info("!! error happened."+sm.getAsSerial());
		channelSend(getConfig().getChannelTopic(profile),
				sm.getAsEncryptedSerial(
						conf.getChannelEncryptionKey(profile),
						conf.getChannelEncryptionIterations(profile)
						)
				);
		sterlogger.getLogger().info("!! close message sent");
	}
	
	void sendSelfCloseMessageToHostBecauseOfError(String to, String groupid) {
		stermessage sm = new stermessage();
		sm.setCryptography(getConfig().getChannelEncryptionKey(profile), getConfig().getChannelEncryptionIterations(profile));
		sm.setMessage(-1, 
					stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(),getResourceID()),
					to,
					sm.getNextSeqID(), sterconst.MESSAGE_CLOSE+"");
		sm.setCommandNameConstant(sterconst.MESSAGE_CLOSE+"");
		//sm.setCommandParameter(sterconst.MESSAGE_CLOSE_RESOURCE+"", resourceid);
		if (groupid != null) {
			sm.setCommandParameter(sterconst.MESSAGE_PARAMETER_GROUPID+"",groupid);
			}
		sterlogger.getLogger().info("!! self error happened to self close."+sm.getAsSerial());
		channelSend(getConfig().getChannelTopic(profile),
				sm.getAsEncryptedSerial(
						conf.getChannelEncryptionKey(profile),
						conf.getChannelEncryptionIterations(profile)
						)
				);
		sterlogger.getLogger().info("!! self close message sent");
	}
	
	static boolean writeHexToOutputStream(OutputStream os, String hexstring) {
		sterlogger.getLogger().info("to be written hex ("+hexstring.length()+") !="+hexstring);
		if (hexstring.length() > 2) {
			sterlogger.getLogger().info("tbwh>");
		}
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
	
	void channelSend(String destination, String message) {
		if (plugin == null) {
			cli.send(destination, message);
		} else {
			plugin.send(destination, message);
		}
	}
	
	boolean init (String channel,
			genericStream s,
			sterconfig conf, String profile,
			Client cli) {
		
		this.cli = cli;
		this.conf = conf;
		this.profile = profile;
		
		if (cli == null) {cli = initClient();}

		setClusterID(conf.getClusterName(profile));
		setNodeID(conf.getNodeName(profile));
		this.resourceID = getNodeID()+stermessage.getUniqueID();

		if (conf.getChannelProtocol(profile).equalsIgnoreCase("stomp") == false) {
			// load and init dynamic plugins by class
			int pos = -1;
			for (int i=0;i<conf.getPluginClassProfiles().length;i++) {
				sterplugin sp = new sterplugin();
				sp.addAndInitPlugin(conf.getPluginClassProfiles()[i]);
				if (sp.getProtocolID().equals(conf.getChannelProtocol(profile)) == true) {
					pos = i;
					i = conf.getPluginClassProfiles().length;
					plugin = sterpool.addNewPlugin(
							conf.getPluginClassProfiles()[i],
							stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(),resourceID)
							);
				}
			}
			
		}
		
		sterlogger.getLogger().info("sink not source (0):"+isSinkInsteadSource+" "+s+ "cli:"+cli);

		if (s!= null) {
			if (s.isSocket() == true) {
				sourceTCPsock = s.getSocketRef();
			}
		}
		if (s == null) {isSinkInsteadSource = true;}

		sterlogger.getLogger().info("sink not source:"+isSinkInsteadSource);

		handler (null);

		if (isSinkInsteadSource == false) { // source

			// check accesslist
			if (s.isSocket() == true) {
				if (
						(conf.isAccessListAuthorised(profile, s.getSocketRef().getInetAddress().getHostAddress()) == false) &&
						(conf.isAccessListAuthorised(profile, s.getSocketRef().getInetAddress().getHostName()) == false)
						){
					return false;
				}	
			}
			
			// init connection parameters
			String targethost = conf.getFixedHost(profile);
			int targetport = conf.getFixedPort(profile);
			int targettype = conf.getFixedSocketType(profile);

			// handle SOCKS, if enabled
			sterproxy sp = new sterproxy();
			if (targethost == null) {
				// start socks resolve, which result is host,port and type
				//try {
					sterlogger.getLogger().info("((((((((((((((proxy");
					boolean proxysucess = sp.initProxy(s.getInputStream(), s.getOutputStream(), conf, profile);
					if (proxysucess == false) {
						return false;
					}
					targethost = sp.getHost();
					targetport = sp.getPort();
					targettype = sp.getType();
					sterlogger.getLogger().info("((((((((((((((hpt:"+targethost + ","+targetport+","+targettype);
				/*} catch (IOException e) {
					e.printStackTrace();
				}*/
			}

			if (targettype != sterconst.SOCKET_TYPE_UDP) {	
				// Check black and white list
				boolean auth =
						conf.isAuthorisedCombination(profile, getClusterID(), conf.getPeerNodeName(profile), targethost, targetport);
				if (auth == false) {
					sterlogger.getLogger().info("%%%%% Unathorised:"+targethost + ","+targetport+","+targettype);
					return false;
				}

				// Remapping
				String hostport = conf.getRemappingForProfileAsHostPortPair(profile, targethost, targetport);
				if (hostport != null) {
					sterlogger.getLogger().info("%%%%% Remapping:"+hostport);
					targethost = hostport.split(" ")[0];
					targetport = Integer.parseInt(hostport.split(" ")[1]);
					sterlogger.getLogger().info("%%%%% Remapped to:"+profile+","+targethost+","+targetport);
				}

				// init the communication by sending message
				stermessage sm = new stermessage();
				String uniqueid = stermessage.getUniqueID();
				String groupid = stermessage.getUniqueID();
				sterlogger.getLogger().info("&&&id,uid::"+uniqueid+","+groupid);// debug
				sm.setMessage(-1,
						stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(),resourceID),
						stermessage.joinClusterNodeSubnode(getClusterID(), conf.getPeerNodeName(profile), uniqueid),
						stermessage.getNextSeqID(), sterconst.MESSAGE_OPEN+"");
				sm.setCommandNameConstant(sterconst.MESSAGE_OPEN+"");
				sm.setCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_HOST+"", targethost);
				sm.setCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_PORT+"", ""+targetport);
				sm.setCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_TYPE+"", ""+targettype);
				sm.setCommandParameter(sterconst.MESSAGE_PARAMETER_GROUPID+"", ""+groupid);
				if (sp.getSecondSocksBindReply() != null) {
					sm.setCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_SECONDREPLY+"", ""+sp.getSecondSocksBindReply());
				}
				if (sp.getForwardRelay() != null) {
					sm.setCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_FORWARDONRELAY+"", ""+sp.getForwardRelay());
				}
				sterlogger.getLogger().info("message is:"+sm.getAsSerial());

				// test
				sterlogger.getLogger().info("init message passed with command :"+sm.getCommandNameConstant()+" for profile:"+getConfig().getChannelTopic(profile)+ " for cli:"+cli);
				//cli.send(getConfig().getChannelTopic(profile), sm.getAsSerial());
				channelSend(getConfig().getChannelTopic(profile),
						sm.getAsEncryptedSerial(
								conf.getChannelEncryptionKey(profile),
								conf.getChannelEncryptionIterations(profile)
								)
						);
				
				// connect backwards
				sterrelaythread bst = sterpool.relayfactory(getThisOne(),"bc"+stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(),resourceID));
				try {
					InputStream is = null;
					if (getSourceTCPsock() == null) {
						is = System.in;
					} else {
						is = getSourceTCPsock().getInputStream();
					}
					bst.initTCP(is,
							stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(),resourceID),
							stermessage.joinClusterNodeSubnode(getClusterID(), conf.getPeerNodeName(profile), uniqueid),
							getConfig(),profile,groupid);
					new Thread(bst).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
				sterlogger.getLogger().info("relay reference(1):"+bst+","+bst.getFrom()+","+bst.getTo());
				sterpool.debugRelayThread();		

			} else {	// TODO: UDP

				// TODO: some kind of remote UDP assotiation initing
				
				// connect backwards
				/*sterUDPrelay udr = new sterUDPrelay();
				udr.init(targethost, targetport,
						s.getLocalSocketAddress().toString() , s.getLocalPort(),
						getThisOne(),
						resourceID,
						isSinkInsteadSource);
				Thread udpt = new Thread(udr);
				udpt.start();*/
				
			}
		} else {	// sink
			// TODO: generic stream plugin handling
			sterlogger.getLogger().info("generic plugin ["+profile+","+conf.getFixedSteamClass(profile)+"]:");
			if (conf.getFixedSteamClass(profile) != null) {
				sterlogger.getLogger().info("generic plugin:"+conf.getFixedSteamClass(profile));
				gsl.init(conf.getFixedSteamClass(profile));
				gsl.setParameters(conf.getFixedStreamClassProperties(profile));
			}
		}
		return true;
	}
	
	/**
	 * To be used by plugins for callback message
	 */
	public void callbackmessage(Map header, String body) {
		l.message(header, body);
	}
	
	void handler (stermessage initmessage) {
		
		l = new Listener() {

			public void message(Map header, String body) {
				stermessage sm = new stermessage();
				
				sterlogger.getLogger().info("body is:"+body);
				//sm.setFromSerial(body);
				sm.setFromEncryptedSerial(body, getConfig().getChannelEncryptionKey(profile), 
						getConfig().getChannelEncryptionIterations(profile));
				
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
							OutputStream os;
							if (sms == null) {
								os = System.out;
							} else {
								os = sms.getOutputStream();	
							}
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
							if (getSourceTCPsock() != null) {
								getSourceTCPsock().close();	
							}
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
						sterdnsresolver resolv = new sterdnsresolver(hostname);
						// TODO: form and send the message
						stermessage dnsres = new stermessage();
						dnsres.setMessage(-1,
								stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(), getResourceID()),
								stermessage.joinClusterNodeSubnode(sm.getFromCluster(), sm.getFromNode(), sm.getFromResourceID() ),
								stermessage.getNextSeqID(), sterconst.MESSAGE_RESOLVE_RESPONSE+"");
						dnsres.setCommandNameConstant(sterconst.MESSAGE_RESOLVE_RESPONSE+"");
						dnsres.setCommandParameter(sterconst.MESSAGE_RESOLVE_IP+"", resolv.getFirstIPV4Address());
						//cli.send(getConfig().getChannelTopic(profile), dnsres.getAsSerial());
						channelSend(getConfig().getChannelTopic(profile),
								dnsres.getAsEncryptedSerial(
										conf.getChannelEncryptionKey(profile),
										conf.getChannelEncryptionIterations(profile)
										)
								);
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
								sendSinkCloseMessageToHostBecauseOfError(sm);
							}
							String groupid = sm.getCommandParameter(sterconst.MESSAGE_PARAMETER_GROUPID+"");
							sterrelaythread st = sterpool.relayfactory(getThisOne(),"si"+stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(), resource ));
							if (st == null) {
								// TODO: error handling by message
								sendSinkCloseMessageToHostBecauseOfError(sm);
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
												getConfig(), getProfile(), groupid
												);
								new Thread(st).start();
								String forwardrelay = sm.getCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_FORWARDONRELAY+"");
								if (forwardrelay != null) {
									try {
										rse.getOutputStream().flush();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									writeHexToOutputStream(rse.getOutputStream(), forwardrelay);
									try {
										rse.getOutputStream().flush();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									sterlogger.getLogger().info("To write forward!");
								}
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
						else if (sm.getCommandParameter(sterconst.MESSAGE_OPEN_PARAMETER_TYPE+"").equals(sterconst.SOCKET_TYPE_GENERICSTREAM+"")) {
							if ((getGenericStreamInterface() != null) && getGenericStreamInterface().getDynamicClassName() != null) {
								// TODO
							}
						}
						else {
							// TODO
						}
					}
					else if ( sm.getCommandNameConstant() == sterconst.MESSAGE_SEND) {
						String resource = sm.getToResourceID();
						sterlogger.getLogger().info("gsl for profile:"+profile+","+getGenericStreamInterface());
						if ((getGenericStreamInterface() != null) && (getGenericStreamInterface().getDynamicClassName() != null) ) { // TODO better
							sterlogger.getLogger().info("send message ["+getGenericStreamInterface()+"]!"+resource+","+sm.getToCluster()+","+sm.getToNode());
							OutputStream os = getGenericStreamInterface().getOutputStream();
							writeHexToOutputStream(os, sm.getCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+""));
							sterlogger.getLogger().info("sent"+getGenericStreamInterface()+"!="+sm.getCommandParameter(sterconst.MESSAGE_SEND_PAYLOAD+""));
						} else {
							sterlogger.getLogger().info("send message!"+resource+","+sm.getToCluster()+","+sm.getToNode());
							Socket ms = sterpool.getSocketByName( stermessage.joinClusterNodeSubnode(sm.getToCluster(), sm.getToNode(), resource ) );
							if (ms == null) {
								// TODO: error handling by message
								sendSinkCloseMessageToHostBecauseOfError(sm);
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
					}
					else if ( sm.getCommandNameConstant() == sterconst.MESSAGE_CLOSE) {
						sterlogger.getLogger().info("!!!Socket close message!!!"); //debug
						if ((getGenericStreamInterface() != null)  && (getGenericStreamInterface().getDynamicClassName() != null)) {
							sterlogger.getLogger().info("!!!Generic stream message:"+getGenericStreamInterface());
						} else {
							String resource = sm.getToResourceID();
							Socket s = sterpool.getSocketByName( stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(), resource ) );
							sterlogger.getLogger().info("!!!Socket close message is!!!"+s); //debug
							if (s == null) {
								// TODO: error handling by message
								sendSinkCloseMessageToHostBecauseOfError(sm);
							}
							try {
								if (s != null) {s.close();
								sterlogger.getLogger().info("!!!Socket closed!!!"+stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(), resource ));}
							} catch (IOException e) {
								e.printStackTrace();
							}
							sterpool.nullifySocketByName(stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(), resource ));
							sterlogger.getLogger().info("!!!Socket nullified!!!"+stermessage.joinClusterNodeSubnode(getClusterID(), getNodeID(), resource ));
					
							// TODO: better release
							String groupid = sm.getCommandParameter(sterconst.MESSAGE_PARAMETER_GROUPID+"");
							sterlogger.getLogger().info("!!!Socket groupid!!!"+groupid);
							if (groupid != null) {
								sterpool.debugRelayThread();
								sterpool.relayreleasebygroup(groupid);
								sterpool.debugRelayThread();
							}
							
						}				
					} else {
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
