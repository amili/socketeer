package socketeer.plugins.connection;
import java.io.*;
import java.net.*;

import socketeer.sterlogger;

public class sterchanneIRCimpl implements Runnable {

	String memhost = null;
	int memport = 0;
	
	Socket socket;
	String nick;
	String login;
	String channel;
	String uid;
	sterchannelRC scI;
	BufferedWriter writer;
	BufferedReader reader;
	
	public sterchanneIRCimpl(sterchannelRC scI) {
		this.scI = scI;
	}
	
	public void setNickLoginChannel(String nick, String login, String channel, String uid) {
		this.nick = nick;
		this.login = login;
		this.channel = channel;
		this.uid = uid;
	}
	
	public boolean connect(String host, int port) {
		try {
			if (host == null) {host = memhost;}
			if (memport > 0) {port = memport;}
			memhost = host; memport = port;
			socket = new Socket(host, port);
			writer = new BufferedWriter(
	                new OutputStreamWriter(socket.getOutputStream( )));
	        reader = new BufferedReader(
	                new InputStreamReader(socket.getInputStream( )));
	        writer.write("NICK " + nick + "\r\n");
	        writer.write("USER " + login + " 8 * :"+uid+'\r'+'\n');
	        writer.flush( );
	        String line = null;
	        while ((line = reader.readLine( )) != null) {
	            if (line.indexOf("004") >= 0) {
	                return true;
	            }
	            else if (line.indexOf("433") >= 0) {
	                System.out.println("Nickname is already in use.");
	                return false;
	            }
	        }
	        writer.write("JOIN " + channel + "\r\n");
	        writer.flush( );
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
        return true;
	}
	
	private boolean tryResend(String destination, String message) {
		try {
			if ( connect(null,0) == true) {
				writer.write("PRIVMSG #"+destination+" "+message+'\r'+'\n');
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void send(String destination, String message) {
		try {
			writer.write("PRIVMSG #"+destination+" "+message+'\r'+'\n');
		} catch (IOException e) {
				int counter = 0;
				boolean isLooping = true;
				while ((counter < 10) && isLooping) {
					boolean sendsucceeded = tryResend(destination, message);
					if (sendsucceeded == true) {isLooping = false;}
					if (isLooping == true) {
						long beginPause = System.currentTimeMillis();
						while (System.currentTimeMillis()-beginPause < 3000) {
							int pausePayload = 10 | 10; // little pausing with dummy payload
						}
					}
				}
				if (counter >=10) e.printStackTrace();
		}
	}
	
    public void handle() {

    	String line;
    	
        try {
			while ((line = reader.readLine( )) != null) {
			    if (line.toLowerCase( ).startsWith("PING ")) {
			    	writer.write("PONG " + line.substring(5) + "\r\n");
			        writer.flush( );
			    }
			    else if (line.toLowerCase( ).startsWith(":")) {
			    	// example: http://calebdelnay.com/blog/2010/11/parsing-the-irc-message-format-as-a-client
			    	String[] messageparts = line.split(" ");
			    	String from = messageparts[0];
			    	String command = messageparts[1];
			    	String params = messageparts[2];
			    	String trailing = messageparts[3];
			    	for (int i=0;i<messageparts.length;i++) {
			    		trailing = trailing + " ";
			    	}
			    	trailing = trailing.substring(0,trailing.length()-1);
			    	if (command.equals("PRIVMSG")) {
			    		if (params.startsWith("#") == true) {
			    			// TODO, to distinguish channel from user
			    		}
			    		if (trailing.startsWith(":") == true) {
			    			trailing = trailing.substring(1,trailing.length()); //  message itself
			    		}
			    		scI.sosi.callbackmessage(null, trailing);
			    	}
			    }
			    else {
			        sterlogger.getLogger().info(this.getClass().getCanonicalName()+": Unkown message type from server:"+line);
			    }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args) throws Exception {
    	
    }

	public void run() {
		handle();
	}

}