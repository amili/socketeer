package socketeer.plugins.connection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Vector;

import socketeer.sterconfig;
import socketeer.stersourcesink;

public class sterplugin {

	sterchannel sc = null;
	
	public boolean addAndInitPlugin(String classname) {
		return addAndInitPlugin(classname,false);
	}
	
	public boolean addAndInitPlugin(String classname,boolean useURL) {
		
		try {
			ClassLoader myClassLoader = ClassLoader.getSystemClassLoader();
			if (useURL == true) {
				myClassLoader = URLClassLoader.getSystemClassLoader();		
			}
			Class myClass = myClassLoader.loadClass(classname);
			Object whatInstance = myClass.newInstance();
			sc = (sterchannel)whatInstance;
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
	
	public boolean initPlugin(stersourcesink sosi,sterconfig conf,String profile) {
		return sc.setConfigProfileSourceSink(sosi, conf, profile);
	}
	
	public void send (String destination,String message) {
		sc.send(destination,message);
	}
	
	public String getProtocolID() {
		return sc.getProtocolID();
	}
	
}
