/**
 * Name1 StudentNumber1
 * Name2 StudentNumber2
 * Name3 StudentNumber3
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

/**
 * Client 
 * Skeleton code for Multicast client
 */
public class MulticastSender implements Runnable
{
	public static final String MCAST_ADDR = "230.0.0.1"; // hardcoded address for the multicast group
	public static final int MCAST_PORT = 9013; // hardcoded port number for the multicast group
	
	public static final int MAX_BUFFER = 1024; // maximum size for data in a packet  

	public static final int DEFAULT_TIMEOUT = 1000;
	public static final int MAX_CONNECTION_ATTEMPTS = 3;
	
	MulticastSocket socket;
	InetAddress address;
	int port;
	int timeout;
	
	ArrayList<byte[]> messageQueue;
	
	Scanner sc;
	
	/**
	 * Default Constructor
	 * 
	 * Fills an instance with the hardcoded values
	 */
	public MulticastSender()
	{
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
	public MulticastSender(String addr, int port) 
	{
		try 
		{
			this.port= port;
			address = InetAddress.getByName(addr);
			socket = new MulticastSocket(port);
			socket.joinGroup(address);
			messageQueue = new ArrayList<byte[]>();
		}
		catch(Exception e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Run method
	 *
	 * This method sends a datagram with the string "Data?" to a server and
	 * then enters an endless loop in which it attempts to receive datagrams
	 * and prints the content of received datagrams.
	 */
	public void run()
	{
		//NOTE: There is no user input yet apart from choosing a name
		//		We need to get joining the group done first
		
		
		//
		//First connect to the multicast group
		//
		MulticastPeer.printDebug("Attempting to connect to the multicast group...");
		sendMessage(MulticastPeer.UPDATE, MulticastPeer.getNodeList());
		timeout = DEFAULT_TIMEOUT;
		int connectionAttempts = 0;
		
		while(true)
		{

			if (timeout <= 0)
			{
				timeout = 100;
			}
			
			try
			{
				Thread.sleep(timeout);
			}
			catch(InterruptedException ex)
			{
				ex.printStackTrace();
			}
			timeout = 0;
		
			
			if (!MulticastPeer.isConnected)
			{
				if (MulticastPeer.receivedAllAcks())
					System.out.println("Received all ACKs!");
				if (connectionAttempts >= MAX_CONNECTION_ATTEMPTS //If the max attempts have been reached
						//Or all ACKs have been received(Can't be 0 ACKs or this node may think it has connected to an empty group)
					|| (MulticastPeer.receivedAllAcks() && MulticastPeer.Nodes.size() > 1)
					&& !MulticastPeer.MyNode.ipaddress.equals(""))
				{
						MulticastPeer.isConnected = true;
				}
				else
				{
					connectionAttempts++;
					sendMessage(MulticastPeer.UPDATE, MulticastPeer.getNodeList());
				}
			}
			
			if (messageQueue.size() > 0)
			{
				if (MulticastPeer.receivedAllAcks())
				{
					//Only remove the message from the queue when it has been received by every other station
					messageQueue.remove(0);
					
					//Reset all ACKs for the next message
					for (int i = 0; i < MulticastPeer.Nodes.size(); i++)
					{
						MulticastPeer.Nodes.get(i).gotAck = false;
					}
					
					if (messageQueue.size() > 0)
					{
						timeout = DEFAULT_TIMEOUT;
						sendMessage(messageQueue.get(messageQueue.size() - 1));
					}
				}
				else
				{
					retryMessage();
				}
			}
			
		}
	}

	/**
	 * 
	 * @param flags The bits in the header that represent ACK, UPDATE, REJECT, etc.
	 * @param data The actual contents of the message
	 */
	public void sendMessage(int flags, String data)
	{
		byte[] databuffer;

		if (data == null)
			databuffer = new byte[0];
		else
			databuffer = data.getBytes();
		byte[] buffer = new byte[MulticastPeer.HEADER_SIZE + databuffer.length];
		
		buffer[0] = (byte)flags;
		
		buffer[1] = (byte)databuffer.length;
		java.lang.System.arraycopy(databuffer, 0, buffer, MulticastPeer.HEADER_SIZE, databuffer.length);

		sendMessage(buffer);
	}
	
	private void sendMessage(byte[] buffer)
	{
		try
		{
			//Send datagram to the rest of the group
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, 
					address, port);
			socket.send(packet);
			MulticastPeer.printDebug("Send Msg");
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param flags The bits in the header that represent ACK, UPDATE, REJECT, etc.
	 * @param data The actual contents of the message
	 */
	public void queueMessage(int flags, String data)
	{
		byte[] databuffer;

		if (data == null)
			databuffer = new byte[0];
		else
			databuffer = data.getBytes();
		byte[] buffer = new byte[MulticastPeer.HEADER_SIZE + databuffer.length];
		
		buffer[0] = (byte)flags;
		
		buffer[1] = (byte)databuffer.length;
		java.lang.System.arraycopy(databuffer, 0, buffer, MulticastPeer.HEADER_SIZE, databuffer.length);

		//If its an ACK, then just send it immediately
		if ((flags & MulticastPeer.ACK) == MulticastPeer.ACK)
			sendMessage(buffer); //Don't set a timeout here, because an ACK can be lost and it won't matter
		else
			messageQueue.add(buffer);
	}
	
	private void retryMessage()
	{
		timeout = DEFAULT_TIMEOUT;
		sendMessage(messageQueue.get(messageQueue.size() - 1));
	}
}