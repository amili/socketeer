package socketeer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
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
 * Ment to be used for internal HTTP protocol.
 * client part
 * 
 * http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/ - HTTP post and get
 * 
 *
 */

public class sterbridgec extends TimerTask {

	Timer timer = new Timer(true);
	long msecsheduletime = 1000;
	
	sterconfig conf = new sterconfig();
	String profile = null;
	
	boolean typeisrpc = true;
	
	boolean shouldBeStopped = false;
	
	stersourcesink sosi = null;
	
	void setConfig(sterconfig conf, String profile) {
		this.conf = conf;
		this.profile = profile;
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
	String[] makehttpPost(String url, String messages[]) {
		String charset = "UTF-8";
		// remote rpc via java
		try {
			HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true); // for starting POST.
			connection.setRequestProperty("User-Agent", "Socketeer");
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
			
			// prepare messages
			String whattopost = "";
			for (int i=0;i<messages.length;i++) {
				whattopost = whattopost + messages[i]+'\n';
			}
			whattopost = URLEncoder.encode(whattopost,"UTF-8");
			
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(whattopost);
			wr.flush();
			wr.close();

			InputStream response = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(response));
			String wholeline = "";
			String line = br.readLine();
			while (line != null) {
				wholeline = wholeline + line;
			}
			wholeline = URLDecoder.decode(wholeline,"UTF-8");
			String[] res = wholeline.split('\n'+"");
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
						+"/", new String[0]);
			// process all messages
		} else {
			// some other type,i.e. mail handling
			// IRC!?, XMPP!?
		}
		// reshedule (TODO: some breaking)
		if (shouldBeStopped == false) {timer.schedule(this, msecsheduletime);}

	}


}
