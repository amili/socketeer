import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import net.ser1.stomp.Client;
import net.ser1.stomp.Listener;

/**
 * 
 * @author Alen Milincevic
 *
 * Ment to be used for protocol other than STOMP (TBD).
 * client part
 *
 */

public class sterbridgec extends TimerTask {

	Timer timer = new Timer(true);
	long msecsheduletime = 1000;
	
	sterconfig conf = new sterconfig();
	String profile = null;
	
	boolean typeisrpc = true;
	
	boolean shouldBeStopped = false;
	
	Client cli = null;
	
	void setConfig(sterconfig conf, String profile) {
		this.conf = conf;
		this.profile = profile;
	}
	
	void setClient(String topic, Client cli) {
		this.cli = cli;
		
		Listener l = new Listener() {
			public void message(Map header, String body) {
				stermessage sm = new stermessage();
				sm.setFromSerial(body);
				// TODO: message parsing
			}
		};
		
		if (topic != null) {this.cli.subscribe(topic, l);}
	}
	
	void startTimer(long timertime) {
		msecsheduletime = timertime;
		timer.schedule(this, msecsheduletime);
		shouldBeStopped = false;
	}

	void shouldStop() {
		shouldBeStopped = true;
	}
	
	// the result are messages
	String[] makehttpPost(String url, String whattopost) {
		String charset = "UTF-8";
		// remote rpc via java
		try {
			URLConnection connection = new URL(url).openConnection();
			connection.setDoOutput(true); // for starting POST.
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
			OutputStream output = connection.getOutputStream();
			try {
				output.write(whattopost.getBytes("UTF-8"));
			} finally {
				try { output.close(); } catch (IOException logOrIgnore) {}
			}

			Vector lines = new Vector();
			InputStream response = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(response));
			String line = br.readLine();
			while (line != null) {
				lines.add(URLDecoder.decode(line,"UTF-8"));
			}
			String[] res = new String[lines.size()];
			for (int i=0;i<lines.size();i++) {
				res[i] = (String)lines.get(i);
			}
			return res;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void run() {
		// do the job, check all channels for messages, trigger and send pending messages
		String protocolis = conf.getChannelProtocol(profile);
		if (typeisrpc == true) {
			// remote rpc via java
			String[] lines = makehttpPost("http://"
						+conf.getChannelHost(profile)+":"
						+conf.getChannelPort(profile)
						+"/", "something to post URL encoded");
			// process all messages
		} else {
			// some other type,i.e. mail handling
			// IRC!?, XMPP!?
		}
		// reshedule (TODO: some breaking)
		if (shouldBeStopped == false) {timer.schedule(this, msecsheduletime);}

	}


}
