package socketeer.plugins.genericStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public interface gsInterface {

	public boolean init(String classname);
	
	public String getDynamicClassName();
	
	public boolean setParameters(Properties p);
	
	public void setStreamsFromInputAndOutput(InputStream is, OutputStream os);
	
	public InputStream getInputStream();
	
	public OutputStream getOutputStream();
}
