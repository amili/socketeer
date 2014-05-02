package socketeer;
/**
 * 
 * @author Alen Milincevic
 *
 */

public class sterconst {

	// Socket types
	public static int SOCKET_TYPE_CONNECTION = 0;
	public static int SOCKET_TYPE_BIND = 1;
	public static int SOCKET_TYPE_UDP = 2;
	
	// Sourcesink types
	public static int SOURCESINK_TYPE_SOURCE = 1;
	public static int SOURCESINK_TYPE_SINK = 2;
	
	// message type
	public static int MESSAGE_OPEN = 1;
	public static int MESSAGE_CLOSE = 2;
	public static int MESSAGE_SEND = 3;
	public static int MESSAGE_RESOLVE_REQUEST = 4;
	public static int MESSAGE_RESOLVE_RESPONSE = 5;
	public static int MESSAGE_PING = 6;
	public static int MESSAGE_PONG = 7;
	public static int MESSAGE_ERROR = 99;

	// UDP relay to do
	public static int MESSAGE_UDP = 3;
	public static int MESSAGE_UDP_FROM = 4;
	public static int MESSAGE_UDP_TO = 5;
	public static int MESSAGE_UDP_DATAGRAM = 6;
	
	// message parameters
	public static int MESSAGE_OPEN_PARAMETER_HOST = 1;
	public static int MESSAGE_OPEN_PARAMETER_PORT = 2;
	public static int MESSAGE_OPEN_PARAMETER_TYPE = 3;
	public static int MESSAGE_OPEN_PARAMETER_SECONDREPLY=4;
	public static int MESSAGE_CLOSE_RESOURCE = 5;
	public static int MESSAGE_SEND_PAYLOAD = 6;
	public static int MESSAGE_ERROR_CODE = 7;
	public static int MESSAGE_RESOLVE_HOSTNAME = 8;
	public static int MESSAGE_RESOLVE_TYPE = 9;
	public static int MESSAGE_RESOLVE_IP = 10;
	public static int MESSAGE_PING_PAYLOAD = 11;
	public static int MESSAGE_PONG_PAYLOAD = 12;
	
	// IP type for resolving
	public static int MESSAGE_RESOLVE_TYPE_V4=1;
	public static int MESSAGE_RESOLVE_TYPE_V6=2;
	
	// Socket second bynd reply
	
}
