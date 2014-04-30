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

import net.ser1.stomp.Client;

/**
 * 
 * @author Alen Milincevic
 *
 * Remote HTTP RPC and message storage
 * 
 * id is usually a combination of cluster and node
 *
 *server part
 */

public class sterbridges implements Runnable{

	protected boolean isLocked;
	
	Vector messages = new Vector();

	sterconfig conf = null;
	String encoding = "UTF-8";
	
	// linkage with Gozirra
	Client cli = null;
	String topic = null;
	
	void setConfig(sterconfig conf) {
		this.conf = conf;
	}
	
	void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	void setClientAndTopic(Client cli, String topic) {
		this.cli = cli;
		this.topic = topic;
	}
	
	Client getClient() {
		return cli;
	}
	
	String getTopic() {
		return topic;
	}
	
	synchronized boolean deleteMessagesForID(String id) {
		isLocked = true;
		Vector res = new Vector();
		for (int i=messages.size()-1;i>0;i--) {
			String[] mesparts = ((String)messages.get(i)).split(" ");
			try {
				mesparts[0] = URLDecoder.decode(mesparts[0],encoding);
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
	
	synchronized String[] getMessagesForID(String id) {
		while (isLocked == true) {} // delete lock
		Vector res = new Vector();
		for (int i=0;i<messages.size();i++) {
			String[] mesparts = ((String)messages.get(i)).split(" ");
			try {
				mesparts[0] = URLDecoder.decode(mesparts[0],encoding);
				if (id == null) {
					res.add(mesparts[1]);
				}
				else {
					if (mesparts[0].equals(id)) {
						res.add(URLDecoder.decode(mesparts[1],encoding));
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
	
	synchronized String[] getAndDeleteMessagesForID(String id) {
		String[] res = getMessagesForID(id);
		deleteMessagesForID(id);
		return res;
	}
	
	synchronized boolean SendMessage(String id, String message) {
		while (isLocked == true) {} // sending lock
		try {
			if (id == null) {id = "";}
			if (message == null) {return false;}
			messages.add(URLEncoder.encode(id,encoding)+" "+URLEncoder.encode(message,encoding));
			return true;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public String[] handleRequest(InputStream is, OutputStream os, String line) {
		String[] parts = line.split(" ");
		String method = parts[0];
		String parameters = parts[1];
		String version = parts[3];
		if (method.equalsIgnoreCase("HEAD")) {
			// TODO
		}
		else if (method.equalsIgnoreCase("GET")) {
			// TODO: decode parameters from request and return reply
		}
		else if (method.equalsIgnoreCase("POST")) {
			// TODO: decode parameters from request body and return reply
			String[] parsplit = parameters.split("?");
			String url = parsplit[0];
			String pars = parsplit[1];
			String[] keyvalues = pars.split("&");
			for (int i=0;i<keyvalues.length;i++) {
				String[] kv = keyvalues[i].split("=");
				String key = kv[0];
				String value = kv[1];
			}
		}
		else {
			// TODO: illegal method, form a 404 response
		}
		return null;
	}
	
	public void startServer(int port) {
		try {
			ServerSocket servs = new ServerSocket(port);
			boolean isRunning = true;
			while (isRunning == true) {
				Socket s = servs.accept();
				System.out.println("connected!");
				InputStream is = s.getInputStream();
				OutputStream os = s.getOutputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				handleRequest(is, os, br.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public sterbridges getThis() {
		return this;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO: a simple HTTP relay of messages
		//startServer(8888);
		Thread t = new Thread(new sterbridges());
		t.start();
	}

	public void run() {
		// TODO Auto-generated method stub
		startServer(8888);
	}

}
