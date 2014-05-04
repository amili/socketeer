package socketeer.plugins.genericStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLClassLoader;
import java.util.Properties;

import socketeer.sterlogger;
import socketeer.plugins.connection.sterchannel;

public class gsLoader implements gsInterface {

	gsLoader gl = null;
	String classname = null;
	
	public boolean init(String classname) {
		sterlogger.getLogger().info("classname to load for gsloader:"+classname);
		this.classname = classname;
		try {
			ClassLoader myClassLoader = ClassLoader.getSystemClassLoader();
			Class myClass = myClassLoader.loadClass(classname);
			Object whatInstance = myClass.newInstance();
			gl = (gsLoader)whatInstance;
			sterlogger.getLogger().info("name of the class is:"+gl);
		} catch (SecurityException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean setParameters(Properties p) {
		return gl.setParameters(p);
	}

	public void setStreamsFromInputAndOutput(InputStream is, OutputStream os) {
		gl.setStreamsFromInputAndOutput(is, os);
	}

	public InputStream getInputStream() {
		return gl.getInputStream();
	}

	public OutputStream getOutputStream() {
		return gl.getOutputStream();
	}

	public String getDynamicClassName() {
		return classname;
	}
	
}