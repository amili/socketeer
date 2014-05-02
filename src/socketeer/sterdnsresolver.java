package socketeer;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 
 * @author Alen Milincevic
 *
 */

public class sterdnsresolver {

	String address;
	
	public sterdnsresolver(String address) {
		this.address = address;
	}
	
	public byte[][] getaddress() {
		byte[][] res = new byte[0][0];
		try {
			InetAddress aa[] = InetAddress.getAllByName(address);
			res = new byte[aa.length][16];
			for (int i=0;i< aa.length;i++) {
				res[i] = aa[i].getAddress();
			}
			return res;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return res;
		}
	}
	
	public String getFirstIPV4Address() {
		String ipv4 = new String();
		byte[][] ips = getaddress();
		for (int i=0;i < ips.length;i++) {
			if (ips[i].length == 4) {
				for (int j=0;j<ips[i].length;j++) {
					 ipv4 = ipv4+"."+(ips[i][j] & 0xFF);
				}
				return ipv4.substring(1,ipv4.length());
			}
		}
		return null;
	}
	
	public String getFirstIPV6Address() {
		String ipv6 = new String();
		byte[][] ips = getaddress();
		for (int i=0;i < ips.length;i++) {
			if (ips[i].length == 16) {
				for (int j=0;j<ips[i].length;j=j+2) {
					 ipv6 = ipv6+":"+(ips[i][j] & 0xFF);
					 ipv6 = ipv6+":"+(ips[i][j] & 0xFF);
				}
				return ipv6.substring(1,ipv6.length());
			}
		}
		return null;
	}
	
}