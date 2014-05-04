package socketeer.plugins.genericStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import socketeer.sterlogger;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

// http://code.google.com/p/java-simple-serial-connector/
// TODO: implement

public class genericStreamSerial extends gsLoader {

	SerialPort serialPort = null;
	jsscinputstream is = null;
	jsscoutputstream os = null;
	
	public boolean setParameters(Properties p) {
		sterlogger.getLogger().info("Enter setparams in :"+this.getClass().getCanonicalName());
		String portName = p.getProperty("portName");
		if (portName == null) {
			return false; // TODO: portname not specified
		}
		boolean portexists = false;
	    String[] portNames = SerialPortList.getPortNames();
	    sterlogger.getLogger().info("ports existing:"+portNames.length);
	    for (int i = 0; i < portNames.length; i++){
        	  sterlogger.getLogger().info("portName is:"+portNames[i]);
	          if (portNames[i].equals(portName)) {
	        	  portexists = true;
	          }
	    }
	    if (portexists == false) return false;
	    serialPort = new SerialPort(portName);
	    try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600, 
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE); // TODO: make configurable
            is.setPort(serialPort);
            os.setPort(serialPort);
            sterlogger.getLogger().info("Finished setparams in :"+this.getClass().getCanonicalName());   
            return true;
	    } catch (SerialPortException ex) {
            ex.printStackTrace();
            return false;
        }
	}

	public void setStreamsFromInputAndOutput(InputStream is, OutputStream os) {
		// not used
	}

	public InputStream getInputStream() {
		return (InputStream)is;
	}

	public OutputStream getOutputStream() {
		return (OutputStream)os;
	}

}

// Input and outputstreams for the ports
class jsscinputstream extends InputStream {
	
	SerialPort serialPort = null;
	
	void setPort(SerialPort serialPort) {
		this.serialPort = serialPort;
	}
	
	public int read() throws IOException {
		 try {
			byte[] buffer = serialPort.readBytes(1);
			return buffer[0];
		} catch (SerialPortException e) {
			e.printStackTrace();
			return -1;
		}
	}

}

class jsscoutputstream extends OutputStream {

	SerialPort serialPort = null;
	
	void setPort(SerialPort serialPort) {
		this.serialPort = serialPort;
	}
	
	public void write(int one) throws IOException {
		try {
			serialPort.writeInt(one);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}
	
}