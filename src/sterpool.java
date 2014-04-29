import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * @author Alen Milincevic
 *
 * Generic pool of resources
 *
 */

public class sterpool {

		// default sizes
		static stersourcesink[] sosi = new stersourcesink[10]; // sourcesink pool
		static sterrelaythread[] rt = new sterrelaythread[10]; // relay pool
		
		static Socket[] soc = new Socket[1000];	// socket pool
		static String[] socid = new String[1000]; // socket identifier string
		
		static DatagramSocket[] datagram = new DatagramSocket[1000]; // UDP datagram
		static String[] datagramid = new String[1000]; 					// UDP identifier string		
		
		static ServerSocket[] ssoc = new ServerSocket[10]; // TODO
		
		public sterpool(int sosilength, int rtlength, int soclength, int datagramlength) {
			sosi = new stersourcesink[sosilength];
			rt = new sterrelaythread[rtlength];
			soc = new Socket[soclength];
			socid = new String[soclength];
			datagram = new DatagramSocket[datagramlength];
			datagramid = new String[datagramlength];
			steerpoolinit();
		}
		
		public sterpool() {
			steerpoolinit();
		}
		
		void steerpoolinit() {
			for (int i=0;i<sosi.length;i++) {
				sosi[i] = null;
			}
			for (int i=0;i<rt.length;i++) {
				rt[i] = null;
			}
			for (int i=0;i<soc.length;i++) {
				soc[i] = null;
			}
			for (int i=0;i<socid.length;i++) {
				socid[i] = null;
			}
			for (int i=0;i<datagram.length;i++) {
				datagram[i] = null;
			}
			for (int i=0;i<datagramid.length;i++) {
				datagramid[i] = null;
			}
		}
		
		static stersourcesink sourcesinkfactory() {
			int first = getFreeSourceSink();
			if (first < 0) return null;
			sosi[first] = new stersourcesink();
			return sosi[first];
		}
		
		static int getFreeSourceSink() {
			for (int i=0;i<sosi.length;i++) {
				if (sosi[i] == null) {return i;}
			}
			return -1;
		}
		
		static stersourcesink getsourcesink(int i) {
			return sosi[i];
		}
		
		static int getFreeRelay() {
			for (int i=0;i<rt.length;i++) {
				if (rt[i] == null) {return i;}
			}
			return -1;
		}
		
		static sterrelaythread relayfactory(stersourcesink s) {
			int first = getFreeRelay();
			if (first < 0) return null;
			rt[first] = new sterrelaythread();
			rt[first].setSourceSink(s);
			return rt[first];
		}
		
		static sterrelaythread getrelay(int i) {
			return rt[i];
		}

		static int getFreeSocket() {
			for (int i=0;i<soc.length;i++) {
				if (soc[i] == null) {return i;}
			}
			return -1;
		}
		
		// client socket spawned
		static Socket socketfactory(String host, int port,String socName) {
			return socketfactory(null, host, port, socName);
		}
		
		static Socket socketfactory(String host, int port,String socName, int timeout) {
			return socketfactory(null, host, port, socName);
		}
		
		// server socket assigned
		static Socket socketfactory(Socket s, String host, int port, String socName) {
			return socketfactory(s, host, port, socName, 0);
		}
		
		static Socket socketfactory(Socket s, String host, int port,String socName, int timeout) {
			if (getSocketByName(socName) != null) {
				return null;	// only one with the name allowed
			}
			int first = getFreeSocket();
			if (first < 0) return null;
			try {
				if (s == null) {
					if (timeout > 0) {
						Socket socket = new Socket();
						socket.connect(new InetSocketAddress(host, port), timeout);
						soc[first] = socket;
					} else {
						soc[first] = new Socket(host, port);	
					}
				} else {
					soc[first] = s;
				}
				socid[first] = socName;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return soc[first];
		}
		
		static Socket getsocket(int i) {
			return soc[i];
		}
		
		static Socket getSocketByName(String socName) {
			for (int i=0;i<socid.length;i++) {
				if (socName.equals(socid[i])) {
					return soc[i];
				}
			}
			return null;
		}
		
		static void nullifySocketByName(String socName) {
			for (int i=0;i<socid.length;i++) {
				if (socName.equals(socid[i])) {
					soc[i] = null;
					socid[i] = null;
					return;
				}
			}
		}
		
		static int getFreeDatagram() {
			for (int i=0;i<datagram.length;i++) {
				if (datagram[i] == null) {return i;}
			}
			return -1;
		}
		
		static DatagramSocket datagramfactory(int port,String dgName) {
			return datagramfactory (null,port,dgName);
		}
		
		static DatagramSocket datagramfactory(DatagramSocket d, int port,String dgName) {
			if (getDatagramByName(dgName) != null) {
				return null;	// only one with the name allowed
			}
			int first = getFreeDatagram();
			if (first < 0) return null;
			try {
				if (d == null) {
					datagram[first] = new DatagramSocket(port);	
				} else {
					datagram[first] = new DatagramSocket();
				}
				datagramid[first] = dgName;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return datagram[first];
		}
		
		static DatagramSocket getDatagramByName(String dgName) {
			for (int i=0;i<datagramid.length;i++) {
				if (dgName.equals(datagramid[i])) {
					return datagram[i];
				}
			}
			return null;
		}
		
		static void nullifyDatagramByName(String dgName) {
			for (int i=0;i<datagramid.length;i++) {
				if (dgName.equals(datagramid[i])) {
					datagram[i] = null;
					datagramid[i] = null;
					return;
				}
			}
		}
		
		static void debugSourceSink() {
			sterlogger.getLogger().info("SourceSink debug----");
			for (int i=0;i<sosi.length;i++) {
				if (sosi[i] != null) {
					sterlogger.getLogger().info("SourceSink ["+i+"]="+sosi[i]);
				}
			}
		}
		
		static void debugRelayThread() {
			sterlogger.getLogger().info("RelayThread debug----");
			for (int i=0;i<rt.length;i++) {
				if (rt[i] != null) {
					sterlogger.getLogger().info("RelayThread ["+i+"]="+rt[i].getFrom()+" "+rt[i].getTo());
				}
			}
		}
		
		static void debugSockets() {
			sterlogger.getLogger().info("Socketdebug----");
			for (int i=0;i<soc.length;i++) {
				if (soc[i] != null) {
					sterlogger.getLogger().info("Socket ["+i+"]="+socid[i]);
				}
			}
		}

}
