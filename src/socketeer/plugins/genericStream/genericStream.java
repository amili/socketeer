package socketeer.plugins.genericStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

// for now socket or console
// TODO: more other stream types

public class genericStream implements gsInterface {

	InputStream is = null;
	OutputStream os = null;
	
	boolean isSocket = false;
	Socket socketref = null;
	boolean isConsole = false;
	boolean isGenericStreamed = false;
	
	Properties p = new Properties();
	
	public boolean setParameters(Properties p) {
		this.p = p;
		return true;
	}
	
	public void setStreamsFromSocket(Socket s) {
		try {
			is = s.getInputStream();
			os = s.getOutputStream();
			isSocket = true;
			socketref = s;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setStreamsFromConsole() {
		is = System.in;
		os = System.out;
		isConsole = true;
	}
	
	public void setStreamsFromInputAndOutput(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
	}
	
	public boolean isSocket() {
		return isSocket;
	}
	
	public boolean isConsole() {
		return isConsole;
	}
	
	public boolean isGeneric() {
		return isGenericStreamed;
	}
	
	public Socket getSocketRef() {
		return socketref;
	}
	
	public InputStream getInputStream() {
		return is;
	}
	
	public OutputStream getOutputStream() {
		return os;
	}

	public boolean init(String classname) {
		return false; // not used here
	}

	public String getDynamicClassName() {
		return null; // not used here
	}
	
}
