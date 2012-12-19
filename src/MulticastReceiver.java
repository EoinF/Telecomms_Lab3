/**
 * Name1 StudentNumber1
 * Name2 StudentNumber2
 * Name3 StudentNumber3
 */

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;

/**
 * Server 
 * Skeleton code for Multicast server
 */
public class MulticastReceiver implements Runnable
{
	
	public static final String MCAST_ADDR = "230.0.0.1";	// Hardcoded address for the multicast group
	public static final int MCAST_PORT = 9013; 				// Hardcoded port number for the multicast group
	
	public static final int MAX_BUFFER = 1024; 				// Maximum size for data in a packet
	
	MulticastSocket socket;
	InetAddress address;
	int port;
	
	/**
	 * Default Constructor
	 * 
	 * Fills an instance with the hardcoded values
	 */
	public MulticastReceiver() {
		this(MCAST_ADDR, MCAST_PORT);
	}
	
	/**
	 * Constructor
	 * 
	 * Creates an instance with specific values for the 
	 * address and port of the multicast group 
	 * 
	 * @param addr Address of the multicast group as string
	 * @param port Port number of the server 
	 */
	public MulticastReceiver(String addr, int port) 
	{
		try
		{
			this.port= port;
			address = InetAddress.getByName(addr);
			socket = new MulticastSocket(port);
			socket.joinGroup(address);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Run method
	 *
	 * This method is continuously looking to receive messages from clients.
	 * The method will reply with a message containing the current date information
	 * if a client sends a message that contains the string "Date?". 
	 */
	public void run() {
		DatagramPacket packet= null;
		byte[] buffer= null;
		String msg= null;
		
		
		/*
			Firstly, an UPDATE command is sent to the multicast group
			If there are users already connected, an UPDATE command should be returned
			If this program receives an UPDATE command it should set isConnected to true
			
			If the username entered is already in use, a REJECT command should be used
			
			NOTE: We can't actually find our own ip address as seen from outside the local network
			due to NAT translation. This address is discovered, however, when the first UPDATE command is received
			containing the list of nodes in the network. If there is a user with the same username, then the REJECT
			command should be returned. Otherwise it's impossible to tell your own address from theirs
		*/
		
		
		//Make sure this program can't receive its own commands
		
		
		try {
			while (true) {
				System.out.println("Waiting");
				
				// receive message from client
				buffer = new byte[MAX_BUFFER];
				packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				
				
				msg= new String(buffer, MulticastPeer.HEADER_SIZE, packet.getLength());
				
				
				System.out.println("Received: " + msg);
				System.out.println("From: " + packet.getAddress() + ":" + packet.getPort());
				
				if (msg.equalsIgnoreCase("Date?")) {
					// send reply to everyone
					msg = new Date().toString();
					buffer = msg.getBytes();
					packet = new DatagramPacket(buffer, buffer.length, 
							address, port);
					System.out.println("Sending: " + new String(buffer));
					socket.send(packet);
				}
			}				
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}