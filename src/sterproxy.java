import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

/**
 * 
 * @author Alen Milincevic
 *
 * Proxy handling routines
 *
 */

public class sterproxy {

	boolean proxysocks4 = true;
	boolean proxysocks4a = false;
	boolean proxysocks5 = true;
	boolean proxyhttp = true;
	boolean proxybycontent = false;
	
	String host = null;
	int port = 0;
	int type = sterconst.SOCKET_TYPE_CONNECTION;
	
	String secondSocksBindHexReply = null;
	
	String getHost() {
		return host;
	}
	
	int getPort() {
		return port;
	}
	
	int getType() {
		return type;
	}
	
	boolean isSOCKS4enabled() {
		return proxysocks4;
	}
	
	boolean isSOCKS4aenabled() {
		return proxysocks4a;
	}
	
	boolean isSOCKS5enabled() {
		return proxysocks5;
	}
	
	String getSecondSocksBindReply() {
		return secondSocksBindHexReply;
	}
	
	boolean isHTTPenabled() {
		return proxyhttp;
	}
	
	boolean isContentBasedFilteringOn() {
		return proxybycontent;
	}
	
	// TODO: various HTTP(S) proxy modes
	
	boolean initProxy (InputStream is, OutputStream os, sterconfig conf, String profile) {
		
		proxysocks4 = conf.isSOCKSV4enabled(profile);
		proxysocks5 = conf.isSOCKSV5enabled(profile);
		proxyhttp = conf.isHTTPenabled(profile);
		proxybycontent = conf.isContentRedirectingenabled(profile);
		
		try {
			int one = is.read();			
			if (one < 0) {
				return false;
			}
			if ((one == 4) && ((isSOCKS4enabled() == true) || (isSOCKS4aenabled() == true)) ) {
				
				System.out.println("###### proxy: Socks4"); // DEBUG
				
				one = is.read(); // field 2
				if (one < 0) {
					return false;
				}
				boolean useBindingInsteadStream = false;
				if (one == 1) {
					useBindingInsteadStream = false;
					type = sterconst.SOCKET_TYPE_CONNECTION;
				}
				else if (one == 2) {
					useBindingInsteadStream = true;
					type = sterconst.SOCKET_TYPE_BIND;
				}
				else {
					return false;
				}

				one = is.read();  // field 3
				if (one < 0) {
					return false;
				}
				port = one;
				System.out.println("###### proxy: port0:"+port); // DEBUG
				one = is.read();
				if (one < 0) {
					return false;
				}
				port = one+255*port;
				System.out.println("###### proxy: port1:"+one); // DEBUG
				System.out.println("###### proxy: port:"+port); // DEBUG
				
				one = is.read(); // field 4
				if (one < 0) {
					return false;
				}
				host = ""+one;
				one = is.read();if (one < 0) {return false;}; host = host+"."+one;
				one = is.read();if (one < 0) {return false;}; host = host+"."+one;
				one = is.read();if (one < 0) {return false;}; host = host+"."+one;
				boolean shouldreaddomainname = false;
				if (isSOCKS4aenabled() == true) {
					if (host.startsWith("0.0.0.") == true) {
						if (host.substring(7, host.length()).equals("0") ) {
							shouldreaddomainname = true;
						}
					}
				}
				
				String userid = "";// field 5
				one = is.read();
				if (one < 0) {
					return false;
				}
				while(one != 0) {
					one = is.read();
					userid = userid + (char)one;
				}
				
				// TODO: authorisation				
				boolean isv4useridvalid = false;
				isv4useridvalid = conf.isSocks4UserIDValid(profile, userid);
				if (isv4useridvalid == false) {
					return false;
				}
				
				String domainname = "";
				if (shouldreaddomainname == true) { // field 6
					one = is.read();
					if (one < 0) {
						return false;
					}
					while(one != 0) {
						one = is.read();
						domainname = domainname + (char)one;
					}
				}
				
				// TODO: hostname to IP resolving
				
				// response
				os.write(0);
				os.write(0x5a);	// todo: other responses
				
				if (shouldreaddomainname == false) {
					os.write(0);os.write(0);os.write(0);os.write(0);os.write(0);os.write(0); // should be ignored	
				}
				else {
					byte[] portparts = new byte[2];
					portparts[0] = (byte) port;
					portparts[1] = (byte) (port >>> 8);
					os.write(portparts[0]); os.write(portparts[1]);
					host = domainname; //TODO: resolving domainname on sink and alter ip, and send ip
					os.write(127);os.write(0);os.write(0);os.write(1); // ugly hack...
				}
	
				// Second reply to bind (if this method is used)
				if (type == sterconst.SOCKET_TYPE_BIND) {
					secondSocksBindHexReply = "0"+Integer.toHexString(0);
					secondSocksBindHexReply = secondSocksBindHexReply + "90";
					if (port > 255) {
						secondSocksBindHexReply = Integer.toHexString(port);
						secondSocksBindHexReply = Integer.toHexString(port >>> 8);
					}
					else {
						secondSocksBindHexReply = "00";
						secondSocksBindHexReply = Integer.toHexString(port);					
					}
					secondSocksBindHexReply = secondSocksBindHexReply + "00";
					secondSocksBindHexReply = secondSocksBindHexReply + "00";
					secondSocksBindHexReply = secondSocksBindHexReply + "00";
					secondSocksBindHexReply = secondSocksBindHexReply + "00";	
				}
			}
			else if ((one == 5) && (isSOCKS5enabled() == true) ) {
				
				System.out.println("###### proxy: Socks5"); // DEBUG
				
				// initial greeting
				one = is.read(); // field 2
				if (one < 1) {
					return false;
				}
				
				int[] auths = new int[one];
				for (int i=0;i<one;i++) {
					auths[i] = is.read();	// field 3
					if (auths[i] < 0) {
						return false;
					}
				}
				
				// server's choice
				boolean hasnoauthentification = false;
				boolean hasusernamepassword = false;
				for (int i=0;i<auths.length;i++) {
					if (auths[i] == 0) {hasnoauthentification = true;}
					if (auths[i] == 2) {hasusernamepassword = true;}
				}
				if ((hasnoauthentification == false) && (hasusernamepassword == false)) {
					return false;
				}
				if ((hasnoauthentification == true) && (hasusernamepassword == true)) {
					hasnoauthentification = false;
				}
				if (hasnoauthentification == true) {
					os.write(0x05);
					os.write(0);	
				}
				else if (hasusernamepassword == true) {
					os.write(0x05);
					os.write(2);
					
					// username/password authentication
					one = is.read();
					if (one != 1) {
						return false;
					}
					String v5un = "";
					one = is.read();
					for (int i=0;i<one;i++) {
						v5un = v5un+(char)one;
					}
					String v5pass = "";
					one = is.read();
					for (int i=0;i<one;i++) {
						v5un = v5un+(char)one;
					}
					os.write(1);
					if ((conf.isSocks5UsernamePasswordValid(profile, v5un, v5pass)) == false) {
						os.write(1);
						return false;
					} else {
						os.write(0);
					}
					
				}
				else {
					// TODO: wrong request
				}
				
				// connection request
				one = is.read(); // field 1
				if (one != 0x05) {
					return false;
				}
				one = is.read(); // field 2
				if (one < 0) {
					return false;
				}
				
				boolean isTCPbindInsteadStream = false;
				boolean isUDPinsteadTCP = false;
				if (one == 1)  {
					isTCPbindInsteadStream = false;
					isUDPinsteadTCP = false;
					type = sterconst.SOCKET_TYPE_CONNECTION;
				}
				else if (one == 2) {
					isTCPbindInsteadStream = true;
					isUDPinsteadTCP = false;
					type = sterconst.SOCKET_TYPE_BIND;
				}
				else if (one == 3) {
					isTCPbindInsteadStream = false;
					isUDPinsteadTCP = true;
					type = sterconst.SOCKET_TYPE_UDP;
				}
				else {
					return false;
				}
				
				one = is.read(); // field 3
				if (one != 0) {
					return false;
				}
				one = is.read(); // field 4
				if (one < 0) {
					return false;
				}
				boolean isV6insteadV4 = false;
				boolean isdomaininsteadIP = false;
				if (one == 1) {
					isV6insteadV4 = false;
					isdomaininsteadIP = false;
				}
				else if (one == 3) {
					isV6insteadV4 = false;
					isdomaininsteadIP = true;
				}
				else if (one == 4) {
					isV6insteadV4 = true;
					isdomaininsteadIP = false;
				}
				else {
					return false;
				}
				int[] destaddr = new int[0];  // field 5
				if (isdomaininsteadIP == true) {
					one = is.read();
					if (one < 0) {
						return false;
					}
					destaddr = new int[one];
					for (int i=0;i<one;i++) {
						destaddr[i] = is.read();
						if (destaddr[i] < 0) {
							return false;
						}
					}
				}
				else if (isV6insteadV4 == true) {
					destaddr = new int[16];
					for (int i=0;i<16;i++) {
						destaddr[i] = is.read();
						if (destaddr[i] < 0) {
							return false;
						}						
					}
				}
				else if (isV6insteadV4 == false) {
					destaddr = new int[4];
					for (int i=0;i<4;i++) {
						destaddr[i] = is.read();
						if (destaddr[i] < 0) {
							return false;
						}						
					}
				}
				else {
					return false;
				}
				
				int[] rawport = new int[2];
				one = is.read();  // field 6
				if (one < 0) {
					return false;
				}
				rawport[0] = one;
				rawport[1] = is.read();
				port = rawport[1]+255*one;  // network order, reversed
				
				// response
				os.write(5); // field 1
				os.write(0); // field 2
				os.write(0); // field 3
				if (isdomaininsteadIP == true) { // field 4
					os.write(3);
					host = "";
					for (int i=0;i<destaddr.length;i++) {
						host = host + (char)destaddr[i];
					}
				}
				else if (isV6insteadV4 == true) {
					os.write(4);
					host = Integer.toHexString(destaddr[0]);
					host = host + Integer.toHexString(destaddr[1]);
					for (int i=2;i<15;i=i+2) {
						host = host + ":";
						host = host + Integer.toHexString(destaddr[i]);
						host = host + Integer.toHexString(destaddr[i+1]);
					}
				}
				else if (isV6insteadV4 == false) {
					os.write(1);
					host = ""+destaddr[0];
					host = host+"."+destaddr[1];
					host = host+"."+destaddr[2];
					host = host+"."+destaddr[3];
				}
				else {
					return false;
				}
				
				for (int i=0;i<destaddr.length;i++) {// field 5
					os.write(destaddr[i]);
				}

				byte[] portparts = new byte[2]; // field 6
				portparts[0] = (byte) port;
				portparts[1] = (byte) (port >>> 8);
				os.write(rawport[0]); os.write(rawport[1]);
				
				// for bind second reply
				if (type == sterconst.SOCKET_TYPE_BIND) {
					secondSocksBindHexReply = "0"+Integer.toHexString(5);
					secondSocksBindHexReply = secondSocksBindHexReply + "00";
					secondSocksBindHexReply = secondSocksBindHexReply + "00";
					if (isdomaininsteadIP == true) {
						secondSocksBindHexReply = secondSocksBindHexReply + "03";
					}
					else if (isV6insteadV4 == true) {
						secondSocksBindHexReply = secondSocksBindHexReply + "04";
					}
					else if (isV6insteadV4 == false) {
						secondSocksBindHexReply = secondSocksBindHexReply + "01";
					} else {
						// TODO: invalid atyp
					}
					for (int i=0;i<destaddr.length;i++) { // IP
						String padded = Integer.toHexString(destaddr[i]);
						if (padded.length() < 2) {padded = "0"+padded;}
						secondSocksBindHexReply = secondSocksBindHexReply + padded;
					}
					// PORT (TODO: check network order)
					String padded = Integer.toHexString((byte) port);
					if (padded.length() < 2) {padded = "0"+padded;}
					secondSocksBindHexReply = secondSocksBindHexReply + padded;
					padded = Integer.toHexString((byte) (port >>> 8));
					if (padded.length() < 2) {padded = "0"+padded;}
					secondSocksBindHexReply = secondSocksBindHexReply + padded;
				}
				
			}
			else if ( (isContentBasedFilteringOn() == true) && (conf.isInContentRedirectRules(profile, one) == true )) {
				sterlogger.getLogger().info("content based entry!");
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line = (char)one+br.readLine();
				sterlogger.getLogger().info("content based line to compare="+line);
				String hostport = conf.getContentRedirectRule(profile, line);
				if (hostport != null) {
					host = hostport.split(" ")[0];
					port = Integer.parseInt(hostport.split(" ")[1]);
				}
				sterlogger.getLogger().info("content based="+host+","+port);
			}
			else if ( (isHTTPenabled() == true) &&
					(one == 'C') || (one == 'G') || (one == 'P')) {
				sterlogger.getLogger().info("HTTP!");
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line = br.readLine();
				String firstline = new String(line);
				sterlogger.getLogger().info("HTTP1!"+line);
				String httpver = "";
				if ( ( (one == 'C') && (line.substring(0, 5).equals("ONNECT") ) == false)) {
					sterlogger.getLogger().info("isconnect");
					line = line.substring(line.indexOf(" ")+1);
					line = line.trim();
					String parts[] = line.split(" ");
					if (parts.length < 1) {
						return false;
					}
					String hostport[] = parts[0].split(":");
					if (hostport.length == 2) {
						host = hostport[0];
						port = Integer.parseInt(hostport[1]);
					} else {
						host = hostport[0];
						port = 80;
					}
					httpver = "HTTP/"+parts[parts.length-1];
				}
				if ( 
						( (one == 'G') && (line.substring(0, 1).equals("ET") ) == false) ||
						( (one == 'H') && (line.substring(0, 2).equals("EAD") ) == false) ||
						( (one == 'P') && (line.substring(0, 2).equals("OST") ) == false)
						) {
					sterlogger.getLogger().info("isget");
					line = line.substring(line.indexOf(" ")+1);
					line = line.trim();
					String parts[] = line.split("/");
					if (parts.length < 3) {
						return false;
					}
					String hostport[] = parts[2].split(":");
					if (hostport.length == 2) {
						host = hostport[0];
						port = Integer.parseInt(hostport[1]);
					} else {
						host = hostport[0];
						port = 80;
					}
					httpver = "HTTP/"+parts[parts.length-1];
				}
				sterlogger.getLogger().info("HTTP2!"+host+","+port);
				line = br.readLine();
				Vector headers = new Vector();
				while (line.equals("") == false) {
					line = br.readLine();
					headers.add(line);
					sterlogger.getLogger().info("line="+line+"("+line.length()+")");
				}
				sterlogger.getLogger().info("HTTP3!"+line+ ","+ httpver);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
				if (one == 'C') {
					bw.write(httpver+" 200 Connection established"+'\n');
					bw.write("Proxy-agent: Socketeer/1.1"+'\n');
					bw.write(""+'\n');
					bw.flush();	
				} else {
					// TODO, send the headers somehow, when opened
				}
				sterlogger.getLogger().info("HTTP4!");
			}
			else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
}