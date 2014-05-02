package socketeer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

// TODO - for UDP part

/**
 * 
 * @author Alen Milincevic
 *
 */

public class sterUDPrelay implements Runnable {
	
	String desthost = null;
	int destport = 0;
	String bindhost = null;
	int bindport = 0;
	stersourcesink sosi = null;
	String resourceid = null;
	
	boolean isSinkInsteadSource;
	
	void init (String desthost, int destport, String bindhost, int bindport,
			stersourcesink sosi, String resourceid, boolean isSinkInsteadSource) {
		this.desthost = desthost;
		this.destport = destport;
		this.bindhost = bindhost;
		this.bindport = bindport;
		this.sosi = sosi;
		this.resourceid = resourceid;
		this.isSinkInsteadSource = isSinkInsteadSource;
	}
	
	void startUDPRelay() {
		
		try {
			
			DatagramSocket serverSocket = sterpool.datagramfactory(
					null,bindport,
					stermessage.joinClusterNodeSubnode(sosi.getClusterID(), sosi.getNodeID(), resourceid)
					);
			if (serverSocket == null) {
				// TODO: error message, when failed
			}
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
			boolean isUDPactive = true;
			while (isUDPactive == true) {
				serverSocket.receive(receivePacket);
				
				// get data
				byte[] addr = receivePacket.getAddress().getAddress();
				int port = receivePacket.getPort();
				byte[] data = receivePacket.getData();
				// TODO: relay the packet when received, either forward over UDP or other TCP to another cluster
				if (isSinkInsteadSource == false) { // source
					// generate an encoded packet and send it
				} else { // sink
					//
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
		 
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
