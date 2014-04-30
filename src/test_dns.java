/**
 * 
 * @author Alen Milincevic
 *
 * test of dns
 *
 */

public class test_dns {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		sterdnsresolver dns = new sterdnsresolver("www.google.com");
		System.out.println("Resolvance:"+dns.getFirstIPV4Address());
		
	}

}
