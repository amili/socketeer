import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.Random;

/**
 * 
 * @author Alen Milincevic
 *
 */

public class stermessage {

	long time;
	String from;
	String to;
	int sequence;
	String command;
	
	String cryptkey;
	int cryptiter;
	
	static protected int newseed = 0;
	static protected int seqid = 0;
	
	long getTime() {
		return time;
	}
	
	String getFromCluster() {
		if (from == null) return null;
		String[] p = from.split("/");
		if (p.length < 1) return null;
		try {
			return URLDecoder.decode(p[0],"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	String getFromNode() {
		if (from == null) return null;
		String[] p = from.split("/");
		if (p.length < 2) return null;
		try {
			return URLDecoder.decode(p[1],"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	String getFromResourceID() {
		if (from == null) return null;
		String[] p = from.split("/");
		if (p.length < 3) return null;
		try {
			return URLDecoder.decode(p[2],"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	String getToCluster() {
		if (to == null) return null;
		String[] p = to.split("/");
		if (p.length < 1) return null;
		try {
			return URLDecoder.decode(p[0],"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	String getToNode() {
		if (to == null) return null;
		String[] p = to.split("/");
		if (p.length < 2) return null;
		try {
			return URLDecoder.decode(p[1],"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	String getToResourceID() {
		if (to == null) return null;
		String[] p = to.split("/");
		if (p.length < 3) return null;
		try {
			return URLDecoder.decode(p[2],"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	int getSequence() {
		return sequence;
	}
	
	int getCommandNameConstant() {
		return Integer.parseInt(command.substring(0, 1));
	}
	
	void setCryptography(String password, int iterations) {
		this.cryptkey = password;
		this.cryptiter = iterations;
	}
	
	void setCommandNameConstant(String comnconst) {
		if (command == null) {command = "0";}
		if (command.length() < 1) {command = "0";}
		String comconst = command.substring(0,1);
		command = comconst + command.substring(1, command.length());
	}
	
	String getCommandParameter(String parameter) {
		Properties p = new Properties();
		 try {
			String paramspart = new String(command.substring(1, command.length()));
			p.load(new StringReader(URLDecoder.decode(paramspart,"UTF-8")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p.getProperty(parameter);
	}
	
	void deleteParameter(String parameter) {
		setCommandParameter(parameter, null);
	}
	
	void setCommandParameter(String parameter, String value) {
		Properties p = new Properties();
		try {
			if (command == null) {command = "0";}
			if (command.length() > 1) {
				String paramspart = new String(command.substring(1, command.length()));
				p.load(new StringReader(URLDecoder.decode(paramspart,"UTF-8")));	
			}
			if (value == null) {
				p.remove(value);
			} else {
				p.setProperty(parameter, value);	
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			p.store(os, "");
			command = command.substring(0, 1) + URLEncoder.encode(new String(os.toByteArray(),"UTF-8"),"UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void setMessage(long time, String from, String to, int sequence, String command) {
		this.time = time;
		if (time < 0) {
			this.time = System.currentTimeMillis();
		}
		this.from = from;
		this.to = to;
		this.sequence = sequence;
		this.command = command;
	}
	
	void setFromSerial(String serial) {
		//TODO: check validity (i.e., via a hash!?)
		String[] parts = serial.split(" ");
		time = Long.parseLong(parts[0]);
		from = parts[1];
		to = parts[2];
		sequence = Integer.parseInt(parts[3]);
		command = parts[4];
	}
	
	void setFromEncryptedSerial(String serial, String password) {
		if ((password == null) && (cryptkey == null) ){
			setFromSerial(serial);
		} else {
			if (password != null) {
				setFromSerial(stercrypt.DecryptWithPassword(serial, password));
				return;
			}
		}
		setFromSerial(stercrypt.DecryptWithPassword(serial, cryptkey));
	}
	
	void setFromEncryptedSerial(String serial, String password, int successive) {
		if ((password == null) && (cryptkey == null)) {
			setFromSerial(serial);
		} else {
			if (password != null) {
				setFromSerial(stercrypt.DecryptWithPassword(serial, password, successive));
				return;
			}
		setFromSerial(stercrypt.DecryptWithPassword(serial, cryptkey, successive));	
		}
	}
	
	String getAsSerial() {
		return time+" "+from+" "+to+" "+sequence+" "+command;
	}
	
	String getAsEncryptedSerial(String password) {
		if ((password == null) && (cryptkey == null)) {
			return getAsSerial();
		} else {
			if (password != null) {
				return stercrypt.EncryptWithPassword(getAsSerial(), password);	
			}
			return stercrypt.EncryptWithPassword(getAsSerial(), cryptkey);
		}
	}
	
	String getAsEncryptedSerial(String password,int successive) {
		if ((password == null) && (cryptkey == null)) {
			return getAsSerial();
		} else {
			if (password != null) {
				return stercrypt.EncryptWithPassword(getAsSerial(), password, successive);
			}
			return stercrypt.EncryptWithPassword(getAsSerial(), cryptkey, successive);	
		}
	}
	
	static String joinClusterNode(String cluster, String node) {
		try {
			return URLEncoder.encode(cluster,"UTF-8")+"/"+URLEncoder.encode(node,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static String joinClusterNodeSubnode(String cluster, String node, String subnode) {
		try {
			if (cluster == null) {cluster = "";}
			if (node == null) {node = "";}
			if (subnode == null) {subnode = "";}
			return URLEncoder.encode(cluster,"UTF-8")+"/"+URLEncoder.encode(node,"UTF-8")+"/"+URLEncoder.encode(subnode,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	static synchronized String getUniqueID(){
		return getUniqueID(10);
	}
	
	static synchronized String getUniqueID(int length){
		newseed++;
		String r = new String(""+newseed);
		Random rand = new Random();
		for (int i=0;i<length;i++) {
			int  n = rand.nextInt(99) + 0;
			r = r + Integer.toHexString(n);
		}
		return r;
	}
	
	public static synchronized int getNextSeqID() {
		seqid++;
		return seqid;
	}
}
