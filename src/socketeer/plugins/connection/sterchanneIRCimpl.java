package socketeer.plugins.connection;
import java.io.*;
import java.net.*;

public class sterchanneIRCimpl implements Runnable {

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
	
	public boolean conect(String host, int port) {
		try {
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
	
	public void send(String destination, String message) {
		try {
			writer.write("PRIVMSG #"+destination+" "+message+'\r'+'\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public void handle() {

    	String line;
    	
        try {
			while ((line = reader.readLine( )) != null) {
			    if (line.toLowerCase( ).startsWith("PING ")) {
			    	writer.write("PONG " + line.substring(5) + "\r\n");
			        writer.write("PRIVMSG " + channel + " :I got pinged!\r\n");
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
			        // The raw line from bot
			        System.out.println(line);
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