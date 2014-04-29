import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.SocketHandler;
import java.util.logging.StreamHandler;

/**
 * 
 * @author Alen Milincevic
 *
 */

public class sterlogger {

	private final static Logger LOGGER = Logger.getLogger("socketeerlogger");
	private static boolean isInited = false;
	static Handler h = null;
	
	static Handler addConsoleHandler() {
		h = new ConsoleHandler();
		h.setFormatter(new SimpleFormatter());
		return h;
	}
	
	static Handler addFileHandler(String filename, boolean append) {
		try {
			h = new FileHandler(filename, append);
			h.setFormatter(new SimpleFormatter());
		} catch (SecurityException e) {
			h = null;
			e.printStackTrace();
		} catch (IOException e) {
			h = null;
			e.printStackTrace();
		}
		return h;
	}
	
	static Handler addStreamHandler(OutputStream os) {
		h = new StreamHandler(os, new SimpleFormatter());
		return h;
	}
	
	static Handler addSocketHandler(String host, int port) {
		try {
			h = new SocketHandler(host, port);
			h.setFormatter(new SimpleFormatter());
		} catch (IOException e) {
			h = null;
			e.printStackTrace();
		}
		return h;
	}
	
	static Handler addMemoryHandler(String host, int port) {
		h = new MemoryHandler();
		h.setFormatter(new SimpleFormatter());
		return h;
	}
	
	static public void setLevel(Level l) {
		LOGGER.setLevel(l);
	}
	
	static public void removeHandler(Handler han) {
		LOGGER.removeHandler(han);
	}
	
	static public Logger getLogger() {
				if (isInited == false) {
					try {
						if (h==null) {
							FileHandler fh = new FileHandler("/tmp/MyLogFile.log");
							SimpleFormatter formatter = new SimpleFormatter();
							fh.setFormatter(formatter);
							LOGGER.addHandler(fh);	
						} else {
							LOGGER.addHandler(h);
						}
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}		
				}
				isInited = true;
				return LOGGER;
	}
	
}