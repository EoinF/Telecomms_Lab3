/**
 * Name1 StudentNumber1
 * Name2 StudentNumber2
 * Name3 StudentNumber3
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
	
	MulticastSocket socket;
	InetAddress address;
	int port;
	
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
			sc = new Scanner(System.in);
			this.port= port;
			address = InetAddress.getByName(addr);
			socket = new MulticastSocket(port);
			socket.joinGroup(address);
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
	 * This method sends a datagram with the strnig "Data?" to a server and
	 * then enters an endless loop in which it attempts to receive datagrams
	 * and prints the content of received datagrams.
	 */
	public void run()
	{
		int input;
		
		//NOTE: There is no user input yet apart from choosing a name
		//		We need to get joining the group done first
		
		
		//
		//First connect to the multicast group
		//
		System.out.println("Attempting to connect to the multicast group...");
		queueMessage(MulticastPeer.UPDATE, MulticastPeer.getNodeList());

		/*
		try 
		{
			do
			{	
				System.out.print("0) End the program\n 1) Send a Command\n");
				
				input = sc.nextInt();

				switch(input)
				{
					case 0://End the program
						break;
					case 1://Send a command
						sendCommand();
						break;
					default://Not a valid option
						System.out.print("This is not a valid option.\n");
						break;
					
				}
				
			}while(input != 0);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		*/
	}
	
	public void sendCommand()
	{
		System.out.println("Choose a command: ");
		System.out.println("1) Date?");
		System.out.println("2) Test");
		System.out.println();
		System.out.println("0) Cancel");
		System.out.println();
		
		int input = sc.nextInt();
		
		switch(input)
		{
			case 1:
				System.out.println("Not supported yet");
				break;
			case 2:
				System.out.println("Sending test message");
				break;
			case 0:
				break;
			default:
				System.out.println("Not a valid command!");
		}
	}
	
	
	/*
	 * 
	 */
	public void sendMessage(byte[] buffer)
	{
		try
		{
			//Send datagram to the rest of the group
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, 
					address, port);
			socket.send(packet);
			System.out.println("Send Msg");
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void queueMessage(byte type, String data)
	{
		byte[] databuffer = data.getBytes();
		byte[] buffer = new byte[MulticastPeer.HEADER_SIZE + databuffer.length];
		
		buffer[0] = type;
		buffer[1] = 0;
		java.lang.System.arraycopy(databuffer, 0, buffer, MulticastPeer.HEADER_SIZE, databuffer.length);
		
		sendMessage(buffer);
	}
	
}