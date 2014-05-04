package socketeer.plugins.connection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Vector;

// base on: http://www.pretechsol.com/2013/11/java-database-connection-pooling-simple.html
// possibly usefull: http://javapapers.com/core-java/serialize-de-serialize-java-object-from-database/

public class sterchannelJDBCimpl {
	private String driverName;
	private String password;
	private String url;
	private String user;
	private Driver driver;
	private Vector freeConnections;
	private int maxConn;
	private int count;
	
	sterchannelJDBCimpl jdbc;
	
	sterchannelJDBC jref;
	
	/**
	 * DatabaseConnectionPool constructor.
	 * 
	 * @param drivername
	 * @param conUrl
	 * @param conuser
	 * @param conpassword
	 * @throws SQLException
	 */
	public sterchannelJDBCimpl(String drivername, String conUrl,
			String conuser, String conpassword) throws SQLException {
		freeConnections = new Vector();
		driverName = drivername;
		url = conUrl;
		user = conuser;
		password = conpassword;
		try {
			driver = (Driver) Class.forName(driverName).newInstance();
			DriverManager.registerDriver(driver);
		} catch (Exception _ex) {
			new SQLException();
		}
		count = 0;
		maxConn = 5;
	}
	/**
	 * Method to destroy all connections.
	 */
	public void destroy() {
		closeAll();
		try {
			DriverManager.deregisterDriver(driver);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	/**
	 * Method to add free connections in to pool.
	 * 
	 * @param connection
	 */
	public synchronized void freeConnection(Connection connection) {
		freeConnections.addElement(connection);
		count--;
		notifyAll();
	}
	
	/**
	 * Method to get connections.
	 * 
	 * @return Connection
	 */
	public synchronized Connection getConnection() {
		Connection connection = null;
		if (freeConnections.size() > 0) {
			connection = (Connection) freeConnections.elementAt(0);
			freeConnections.removeElementAt(0);
			try {
				if (connection.isClosed()) {
					connection = getConnection();
				}
			} catch (Exception e) {
				print(e.getMessage());
				connection = getConnection();
			}
			return connection;
		}
		if (count < maxConn) {
			connection = newConnection();
			print("NEW CONNECTION CREATED");
		}
		if (connection != null) {
			count++;
		}
		return connection;
	}
	/**
	 * Method to close all resources
	 */
	private synchronized void closeAll() {
		for (Enumeration enumeration = freeConnections.elements(); enumeration
				.hasMoreElements();) {
			Connection connection = (Connection) enumeration.nextElement();
			try {
				connection.close();
			} catch (Exception e) {
				print(e.getMessage());
			}
		}
		freeConnections.removeAllElements();
	}
	/**
	 * Method to create new connection object.
	 * 
	 * @return Connection.
	 */
	private Connection newConnection() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			print(e.getMessage());
			return null;
		}
		return connection;
	}
	private void print(String print) {
		System.out.println(print);
	}
	
	
	void setIRC(sterchannelJDBC jref) {
		this.jref = jref;
	}
	
	void getFromTableAndReturn() {
		Connection conn = jdbc.getConnection();
		Vector messages = new Vector();
		try {
			Statement stmt = conn.prepareStatement("");
			ResultSet rs = stmt.executeQuery("SELECT * FROM socketeertable WHERE destination='subscribed'");
			while (rs.next() == true) {
				String m = rs.getString("message");
				messages.add(m);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String[] message = new String[messages.size()];
		for (int i=0;i<message.length;i++) {
			message[i] = (String)messages.get(i);
		}
		jref.returner(message);
	}

	void purgeObsoleteMessagesResources(String resource) {
		Connection conn = jdbc.getConnection();
		// TODO: get all resource id's and lastaccess times
		// all the messages that are behind lastaccess times are purged
		 try {
			Statement stmt = conn.prepareStatement("");
			ResultSet rs = stmt.executeQuery("SELECT * FROM socketeerref WHERE destination='"+resource+"'");
			//int least = 0;
			while (rs.next() == true) {
				rs.getString("id");
				rs.getString("lastaccesstime");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		 
	}
	
	void send(String destination,String message, String fromID) {
		Connection conn = jdbc.getConnection();
		try {
			 Statement stmt = conn.prepareStatement("");
			 int ur = stmt.executeUpdate("INSERT INTO socketeertable VALUES ("+destination+","+message);
			 ur = stmt.executeUpdate("UPDATE socketeerref SET lastaccesstime="+
					 					System.currentTimeMillis()+" WHERE id='"+fromID+"' AND destination='"+destination+"'");
			 // update reference
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
}
