package socketeer;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;

/**
 * 
 * @author Alen Milincevic
 *
 */

public class sterconfig {

	Properties p = new Properties();
	
	Properties temporaryp = new Properties();
	
	public void getConfigFromStream(InputStream is) {
		try {
			p.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getConfigFromFile(String filename) {
		try {
			FileInputStream fileInput = new FileInputStream(new File(filename));
			getConfigFromStream(fileInput);
			fileInput.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getConfigFromURL(String uri) {   
		HttpURLConnection urlConnection = null;
		try 
	    {     
	    	URL url = new URL(uri);   
		    urlConnection = (HttpURLConnection) url.openConnection();
	        InputStream is = new BufferedInputStream(urlConnection.getInputStream());     
	        getConfigFromStream(is);
	    } catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    finally 
	    {     
	        if (urlConnection != null) {urlConnection.disconnect();}   
	    } 
	}

	public void getConfigFromString(String configstring,boolean urldecode) {
		 try {
			 if (urldecode == false) {
				 p.load(new StringReader(configstring));
			 } else {
				 p.load(new StringReader(URLDecoder.decode(configstring,"UTF-8")));
			 }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getConfigFromResourceStream(String name) {
		try {
			p.load(sterconfig.class.getResourceAsStream(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// TODO: better testing
	public String getConfigAsString(boolean urlencode) {
		String result = null;
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			p.store(os, ""+System.currentTimeMillis());
			result = new String(os.toByteArray(),"UTF-8");
			if (urlencode == true) {result = URLEncoder.encode(result,"UTF-8");}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


	boolean isMessagingServerEnabled() {
		String msval = p.getProperty("socketeer.general.messagingserverenabled");
		if (msval.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}
	
	int getMessagingServerPort() {
		String msp = p.getProperty("socketeer.general.messagingserverport");
		if (msp == null) return 61626;
		return Integer.parseInt(msp);
	}
	
	int getConnectionTimeoutInMsec() {
		String msp = p.getProperty("socketeer.general.connectiontimeout");
		if (msp != null) {Integer.parseInt(msp);}
		return 2000;
	}
	
	String[] getProfileNames(String beginWith) {
		Properties props = new Properties();
		Enumeration enuKeys = p.keys();
		while (enuKeys.hasMoreElements()) {
			String key = (String) enuKeys.nextElement();
			String value = p.getProperty(key);
			if (beginWith == null) {beginWith = "socketeer.sourcesink.";}
			if (key.startsWith(beginWith)) {
				int posend = key.substring(beginWith.length(),key.length()).indexOf(".");
				if (posend >= 0) {
					String profname = key.substring(beginWith.length(),beginWith.length()+posend);
					props.setProperty(profname, "");
				}
			}
		}
		String[] rets = new String[props.size()];
		int counter = 0;
		Enumeration pKeys = props.keys();
		while (pKeys.hasMoreElements()) {
			String key = (String) pKeys.nextElement();
			rets[counter] = key;
			counter++;
		}
		
		return rets;
	}
	
	clusternodeipport getBlackList(String profile) {
		clusternodeipport bl = new clusternodeipport();
		bl.cluster = p.getProperty("socketeer.blacklist."+profile+".cluster");
		bl.node = p.getProperty("socketeer.blacklist."+profile+".node");
		bl.host = p.getProperty("socketeer.blacklist."+profile+".host");
		bl.port = Integer.parseInt(p.getProperty("socketeer.blacklist."+profile+".port"));
		return bl;
	}
	
	clusternodeipport[] getBlackListForProfile(String profile) {
		String blprofiles = p.getProperty("socketeer.sourcesink."+profile+".blacklist");
		if (blprofiles == null) {return new clusternodeipport[0];}
		String[] blparted = blprofiles.split(",");
		if (blprofiles.equals("")) {return new clusternodeipport[0];}
		clusternodeipport[] res = new clusternodeipport[blparted.length];
		for (int i=0;i<blparted.length;i++) {
			res[i] = getBlackList(blparted[i]);
		}
		return res;
	}
	
	clusternodeipport getWhiteList(String profile) {
		clusternodeipport wl = new clusternodeipport();
		wl.cluster = p.getProperty("socketeer.whitelist."+profile+".cluster");
		wl.node = p.getProperty("socketeer.whitelist."+profile+".node");
		wl.host = p.getProperty("socketeer.whitelist."+profile+".host");
		wl.port = Integer.parseInt(p.getProperty("socketeer.whitelist."+profile+".port"));
		return wl;
	}

	clusternodeipport[] getWhiteListForProfile(String profile) {
		String wlprofiles = p.getProperty("socketeer.sourcesink."+profile+".whitelist");
		if (wlprofiles == null) {return new clusternodeipport[0];}
		String[] wlparted = wlprofiles.split(",");
		if (wlprofiles.equals("")) {return new clusternodeipport[0];}
		clusternodeipport[] res = new clusternodeipport[wlparted.length];
		for (int i=0;i<wlparted.length;i++) {
			res[i] = getWhiteList(wlparted[i]);
		}
		return res;
	}
	
	boolean isAuthorisedCombination(String profile, String cluster, String node, String host, int port) {
		if ((getWhiteListForProfile(profile).length < 1) && (getBlackListForProfile(profile).length < 1)) {
			return true;
		}
		clusternodeipport[] bl = getBlackListForProfile(profile);
		for (int i=0;i<bl.length;i++) {
			if (
					( (bl[i].cluster).equals(cluster) == true ) &&
					( (bl[i].node).equals(node) == true ) &&
					( (bl[i].host).equals(host) == true ) &&
					(bl[i].port == port)
				)
			{
				sterlogger.getLogger().info("%%%%%% in blacklist!"+port);
				return false;
			}
		}
		if (bl.length > 0) return true;
		clusternodeipport[] wl = getWhiteListForProfile(profile);
		for (int i=0;i<wl.length;i++) {
			if (
					( (wl[i].cluster).equals(cluster) == true ) &&
					( (wl[i].node).equals(node) == true ) &&
					( (wl[i].host).equals(host) == true ) &&
					(wl[i].port == port)
				)
			{
				sterlogger.getLogger().info("%%%%%% in whitelist!");
				return true;
			}
		}
		return false;
	}
	
	maplist getMapList(String profile) {
		maplist ml = new maplist();
		ml.orghost = p.getProperty("socketeer.maplist."+profile+".orghost");
		ml.orgport = Integer.parseInt(p.getProperty("socketeer.maplist."+profile+".orgport"));
		ml.maphost = p.getProperty("socketeer.maplist."+profile+".maphost");
		ml.mapport = Integer.parseInt(p.getProperty("socketeer.maplist."+profile+".mapport"));
		return ml;
	}
	
	maplist[] getMapListForProfile(String profile) {
		String mlprofiles = p.getProperty("socketeer.sourcesink."+profile+".maplist");
		if (mlprofiles == null) {return new maplist[0];}
		String[] mlparted = mlprofiles.split(",");
		if (mlprofiles.equals("")) {return new maplist[0];}
		maplist[] res = new maplist[mlparted.length];
		for (int i=0;i<mlparted.length;i++) {
			res[i] = getMapList(mlparted[i]);
		}
		return res;
	}

	maplist getRemappingForProfile(String profile, String host, int port) {
		maplist[] mlp = getMapListForProfile(profile);
		for (int i=0;i<mlp.length;i++) {
			sterlogger.getLogger().info("%%%%% remap"+ mlp[i].orghost+","+mlp[i].orgport);
			if ( (mlp[i].orghost.equals(host) == true) && (mlp[i].orgport == port) ) {
				return mlp[i]; 
			}
		}
		maplist res = new maplist();
		res.orghost = host; res.orgport = port; res.maphost = host; res.mapport = port;
		return res;
	}

	String getRemappingForProfileAsHostPortPair(String profile, String host, int port) {
		String res = null;
		maplist ml = getRemappingForProfile(profile, host, port);
		try {
			res = URLEncoder.encode(ml.maphost,"UTF-8")+" "+ml.mapport;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public boolean isSocks4UserIDValid(String profile, String userID) {
		String[] s4v = getAuthorisation4Items(profile);
		for (int j=0;j<s4v.length;j++) {
			String[] uidnames = getProfileNames("socketeer.authorisation4."+s4v[j]);
			if (uidnames.length < 1) {return true;}
			usernamepassword[] uid = new usernamepassword[uidnames.length];
			for (int i=0;i<uidnames.length;i++) {
				uid[i].username = p.getProperty("socketeer.authorisation4."+profile+".userID");
				uid[i].password = null;
				if ( (userID.equals(uid[i].username) == true)) {
					return true;
				}
			}	
		}
		return false;
	}

	public boolean isSocks5UsernamePasswordValid(String profile, String username, String password) {
		String[] s5v = getAuthorisation5Items(profile);
		for (int j=0;j<s5v.length;j++) {
			String[] unpwdnames = getProfileNames("socketeer.authorisation5."+s5v[j]);
			if (unpwdnames.length < 1) {return true;}
			usernamepassword[] unpwd = new usernamepassword[unpwdnames.length];
			for (int i=0;i<unpwdnames.length;i++) {
				unpwd[i].username = p.getProperty("socketeer.authorisation5."+profile+".username");
				unpwd[i].password = p.getProperty("socketeer.authorisation5."+profile+".password");
				if ( (username.equals(unpwd[i].username) == true) && (password.equals(unpwd[i].password) == true) ) {
					return true;
				}
			}	
		}
		return false;
	}
	
	String getClusterName(String profile) {
		return p.getProperty("socketeer.sourcesink."+profile+".clustername");
	}
	
	String getNodeName(String profile) {
		return p.getProperty("socketeer.sourcesink."+profile+".nodename");
	}
	
	public String getChannel(String profile) {
		return p.getProperty("socketeer.sourcesink."+profile+".channel");
	}
	
	public String getChannelProtocol(String profile) {
		String[] res = getChannel(profile).split("/"); 
		return res[0].split(":")[0];
	}
	
	public String getChannelHost(String profile) {
		String[] res = getChannel(profile).split("/"); 
		try {
			return URLDecoder.decode(res[0].split(":")[1],"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int getChannelPort(String profile) {
		String[] res = getChannel(profile).split("/"); 
		return Integer.parseInt(res[0].split(":")[2]);
	}
	
	public String getChannelTopic(String profile) {
		String[] res = getChannel(profile).split("/"); 
		try {
			return URLDecoder.decode(res[1],"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getChannelUsername(String profile) {
		String un = getChannelParameterByKey(profile, "username");
		if (un == null) return "";
		return un;
	}
	
	public String getChannelPassword(String profile) {
		String pass = getChannelParameterByKey(profile, "password");
		if (pass == null) return "";
		return pass;
	}
	
	String getChannelEncryptionKey(String profile) {
		String ck = getChannelParameterByKey(profile, "codekey");
		//if (ck == null) return "";
		return ck;
	}
	int getChannelEncryptionIterations(String profile) {
		String iter = getChannelParameterByKey(profile, "keyiteration");
		if (iter == null) return 1;
		return Integer.parseInt(iter);
	}
	
	public String getChannelParameterByKey(String profile, String key) {
		String[] res = getChannel(profile).split("\\?");
		if (res.length < 2) return null;
		String[] kvpairs = res[res.length-1].split("&");
		for (int i=0;i<kvpairs.length;i++) {
			String[] kv = kvpairs[i].split("=");
			try {
				if (URLDecoder.decode(kv[0],"UTF-8").equals(key)) {
					return URLDecoder.decode(kv[1],"UTF-8");
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	int getServerPort(String profile) {
		return Integer.parseInt(p.getProperty("socketeer.sourcesink."+profile+".serverport"));
	}
	
	String getPeerNodeName(String profile) {
		return p.getProperty("socketeer.sourcesink."+profile+".peernode");
	}
	
	String getFixedHost(String profile) {
		return p.getProperty("socketeer.sourcesink."+profile+".fixedHost");
	}
	
	int getFixedPort(String profile) {
		String fp = p.getProperty("socketeer.sourcesink."+profile+".fixedPort");
		if (fp == null) return 0;
		return Integer.parseInt(fp);
	}
	
	int getFixedSocketType(String profile) {
		String fsp = p.getProperty("socketeer.sourcesink."+profile+".fixedType");
		if (fsp == null) return sterconst.SOCKET_TYPE_CONNECTION;
		return Integer.parseInt(fsp);
	}
	
	String[] getBlackListItems(String profile) {
		String bli = p.getProperty("socketeer.sourcesink."+profile+".blacklist");
		String[] blis = bli.split(",");
		return blis;
	}
	
	String[] getWhiteListItems(String profile) {
		String wli = p.getProperty("socketeer.sourcesink."+profile+".whitelist");
		String[] wlis = wli.split(",");
		return wlis;
	}
	
	String[] getMapListItems(String profile) {
		String mli = p.getProperty("socketeer.sourcesink."+profile+".maplist");
		String[] mlis = mli.split(",");
		return mlis;
	}
	
	String[] getAuthorisation4Items(String profile) {
		String a4i = p.getProperty("socketeer.sourcesink."+profile+".authorisation4");
		if (a4i == null) return new String[0];
		String[] a4is = a4i.split(",");
		return a4is;
	}
	
	String[] getAuthorisation5Items(String profile) {
		String a5i = p.getProperty("socketeer.sourcesink."+profile+".authorisation5");
		if (a5i == null) return new String[0];
		String[] a5is = a5i.split(",");
		return a5is;
	}
	
	boolean isSOCKSV4enabled(String profile) {
		String socksval = p.getProperty("socketeer.sourcesink."+profile+".socks4enabled");
		if (socksval == null) return false;
		if (socksval.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}
	
	boolean isSOCKSV5enabled(String profile) {
		String socksval = p.getProperty("socketeer.sourcesink."+profile+".socks5enabled");
		if (socksval == null) return false;
		if (socksval.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}
	
	boolean isHTTPenabled(String profile) {
		String httpval = p.getProperty("socketeer.sourcesink."+profile+".httpenabled");
		if (httpval == null) return false;
		if (httpval.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}
	
	boolean isSpanningAndResolvingSink(String profile) {
		String socksval = p.getProperty("socketeer.sourcesink."+profile+".isSpawningAndResolvingSink");
		if (socksval == null) return false;
		if (socksval.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}

	
	boolean isContentRedirectingenabled(String profile) {
		String cfval = p.getProperty("socketeer.sourcesink."+profile+".contentredirectenabled");
		if (cfval == null) return false;
		if (cfval.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}

	boolean isInContentRedirectRules(String profile, int firstchar) {
		if ((getContentRedirectHPorLine(profile,firstchar,null,true)) == null) return false;
		return true;
	}
	
	String getContentRedirectRule(String profile, String line) {
		return getContentRedirectHPorLine(profile,0,line,false);
	}
	
	// dual purpose function
	// to be used by content dependant routing
	// returns either the host and the port or the whole line
	String getContentRedirectHPorLine(String sosiprofile,
								int rulelinebegin,
								String ruleline,
								boolean returnContentLineInsteadHostAndPort) {
		String crprofiles = p.getProperty("socketeer.sourcesink."+sosiprofile+".contentredirect");
		if (crprofiles == null) {return null;}
		String[] crparted = crprofiles.split(",");
		for (int i=0;i<crparted.length;i++) {
			contentredirectlist rl = getContentRedirect(crparted[i]);
			sterlogger.getLogger().info("rl="+crparted[i]+","+rl.host+","+rl.port+","+rl.contentbeginswith);// debug
			if (returnContentLineInsteadHostAndPort == true) {
				if (rl.encoding != null) {
					String toDecode = rl.contentbeginswith;
					try {
						toDecode = URLDecoder.decode(toDecode,rl.encoding);
						if (toDecode.getBytes()[0] == rulelinebegin) {
							return rl.contentbeginswith;
						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						return null;
					}
				} else {
					if ((rl.contentbeginswith).getBytes()[0] == rulelinebegin) {
						return rl.contentbeginswith;
					}	
				}
			} else {
				if (rl.contentbeginswith.equals(ruleline) == true) {
					try {
						return URLEncoder.encode(rl.host,"UTF-8")+" "+rl.port;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						return null;
					}
				}	
			}
		}
		return null;
	}
	
	contentredirectlist getContentRedirect(String profile) {
		contentredirectlist rl = new contentredirectlist();
		rl.contentbeginswith = p.getProperty("socketeer.contentredirect."+profile+".ruleline");
		rl.host = p.getProperty("socketeer.contentredirect."+profile+".host");
		rl.port = p.getProperty("socketeer.contentredirect."+profile+".port");
		rl.encoding = p.getProperty("socketeer.contentredirect."+profile+".encoding");
		return rl;
	}
	
	clusternodeipport getAccessList(String profile) {
		clusternodeipport bl = new clusternodeipport();
		//bl.cluster = p.getProperty("socketeer.accesslist."+profile+".cluster");
		//bl.node = p.getProperty("socketeer.accesslist."+profile+".node");
		bl.host = p.getProperty("socketeer.accesslist."+profile+".host");
		//bl.port = Integer.parseInt(p.getProperty("socketeer.accesslist."+profile+".port"));
		return bl;
	}
	
	String[] getUsedAccessLists(String profile) {
		String acl = p.getProperty("socketeer.sourcesink."+profile+".accesslist");
		if (acl == null) return new String[0];
		String[] aclis = acl.split(",");
		return aclis;
	}
	
	public boolean isAccessListAuthorised(String profile, String host) {
		String[] acl = getUsedAccessLists(profile);
		if (acl.length < 1) return true;
		for (int i=0;i<acl.length;i++) {
			clusternodeipport hostport = getAccessList(acl[i]);
			if (hostport.host.equals(host) == true) {
				return true;
			}
		}
		return false;
	}

	public String[] getPluginClassProfiles() {
		String[] profiles =  getProfileNames("socketeer.channel.");
		String[] cns = new String[profiles.length];
		for (int i=0;i<profiles.length;i++) {
			cns[i] = p.getProperty("socketeer.channel."+profiles[i]+".classname");
		}
		return cns;
	}
	
	public boolean useConsoleInsteadSocket(String profile) {
		String sc = p.getProperty("socketeer.sourcesink."+profile+".useconsole");
		if (sc == null) return false;
		if (sc.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}
	
	public boolean disableConsole() {
		String nc = p.getProperty("socketeer.general.noconsole");
		if (nc == null) return false;
		if (nc.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}
	
	public String getFixedSteamClass(String profile) {
		String sc = p.getProperty("socketeer.sourcesink."+profile+".fixedStreamClass");
		return sc;
	}
	
	public Properties getFixedStreamClassProperties(String profile) {
		Properties fscp = new Properties();
		String[] pn = getProfileNames("socketeer.sourcesink."+profile+".streamClassProperties.");
		for (int i=0;i<pn.length;i++) {
			String fcp = p.getProperty("socketeer.sourcesink."+profile+".streamClassProperties."+pn[i]+".");			
			fscp.setProperty(pn[i], fcp);
		}
		return fscp;
	}
	
	void setTemporaryProperty(String key, String value) {
		temporaryp.setProperty(key, value);
	}
	
	String getTemporaryProperty(String key) {
		return temporaryp.getProperty(key);
	}	

}

class clusternodeipport {
	
	public String cluster;
	public String node;
	public String host;
	public int port;
	
}

class usernamepassword {
	
	public String username;
	public String password;
	
}

class maplist {
	
	public String orghost;
	public int orgport;
	public String maphost;
	public int mapport;
	
}

// TODO
class contentredirectlist {
	public String contentbeginswith;
	public String host;
	public String port;
	public String encoding;
}
