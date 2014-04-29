import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Vector;

/**
 * 
 * @author Alen Milincevic
 *
 * Remote HTTP RPC test
 *
 */

public class test_httprpc {

	static protected boolean isLocked;
	
	static Vector messages = new Vector();

	static synchronized boolean deleteMessagesForID(String id) {
		isLocked = true;
		Vector res = new Vector();
		for (int i=messages.size()-1;i>0;i--) {
			String[] mesparts = ((String)messages.get(i)).split("/");
			try {
				mesparts[0] = URLDecoder.decode(mesparts[0],"UTF-8");
				if (id == null) {
					res.remove(i);	
				} else {
					if (mesparts[0].equals(id)) {
						res.remove(i);
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		isLocked = false;
		return true;
	}
	
	static synchronized String[] getMessagesForID(String id) {
		while (isLocked == true) {} // delete lock
		Vector res = new Vector();
		for (int i=0;i<messages.size();i++) {
			String[] mesparts = ((String)messages.get(i)).split("/");
			try {
				mesparts[0] = URLDecoder.decode(mesparts[0],"UTF-8");
				if (id == null) {
					res.add(mesparts[1]);
				}
				else {
					if (mesparts[0].equals(id)) {
						res.add(mesparts[1]);
					}	
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		String[] resstr = new String[res.size()];
		for (int i=0;i<resstr.length;i++) {
			resstr[i] = (String)res.get(i);
		}
		return resstr;
	}
	
	static synchronized String[] getAndDeleteMessagesForID(String id) {
		String[] res = getMessagesForID(id);
		deleteMessagesForID(id);
		return res;
	}
	
	static synchronized boolean SendMessage(String id, String message) {
		while (isLocked == true) {} // delete lock
		try {
			if (id == null) {id = "";}
			if (message == null) {return false;}
			messages.add(URLEncoder.encode(id,"UTF-8")+"/"+message);
			return true;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO: a simple HTTP relay of messages
		try {
			ServerSocket servs = new ServerSocket(8888);
			boolean isRunning = true;
			while (isRunning == true) {
				Socket s = servs.accept();
				InputStream is = s.getInputStream();
				OutputStream os = s.getOutputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line = "";
				String firstline = null;
				int counter = 0;
				boolean isInnerRunning = true;
				System.out.println("connected!");
				while (isInnerRunning == true) {
					if (isInnerRunning == true) {line = br.readLine();}
					if (firstline == null) {
						firstline = line;
						System.out.println("first request is:"+firstline);
					}
					if (line.equals("")) counter++;
					System.out.println("counter="+counter+isInnerRunning);
					if (counter > 1){
						System.out.println("request is:"+firstline);
						line = br.readLine();
						if (line != null) {
							String[] idmessage = line.split(" ");
							idmessage[0] = URLDecoder.decode(idmessage[0],"UTF-8");
							SendMessage(idmessage[0],idmessage[1]);
							String[] msgs = getAndDeleteMessagesForID("get from request for whom");
							for (int i=0;i<msgs.length;i++) {
								BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
								bw.write(msgs[i]+'\n');
							}
						}
						s.close();
						isInnerRunning = false;
						// servs.close();
						// isRunning = false;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
