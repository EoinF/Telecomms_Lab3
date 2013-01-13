/**
 * Name1 StudentNumber1
 * Name2 StudentNumber2
 * Name3 StudentNumber3
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
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
	
	static MulticastSocket socket;
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
			If there are users already connected, an UPDATE|ACK command should be returned
			If this program receives an UPDATE|ACK command it will then create a boolean set
			to record which nodes it has received an ACK from.
			
			When all UPDATE|ACK commands are received, isConnected is set to true
			
			If the username entered is already in use, a REJECT command should be used
			
			NOTE: We can't actually find our own ip address as seen from outside the local network
			due to NAT translation. This address is discovered, however, when the first UPDATE command is received
			containing the list of nodes in the network. If there is a user with the same username, then the REJECT
			command should be returned. Otherwise it's impossible to tell your own address from theirs
		*/
		
		
		//Make sure this program can't receive its own commands
		
		
		try {
			while (true) {
				MulticastPeer.printDebug("Waiting Receiver.");
				
				// receive message from client
				buffer = new byte[MAX_BUFFER];
				packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				msg= new String(buffer, MulticastPeer.HEADER_SIZE, buffer[1]);
				
				MulticastPeer.printDebug("Received: " + msg);
				MulticastPeer.printDebug("From: " + packet.getAddress() + ":" + packet.getPort());
				
				if (msg.equalsIgnoreCase("Date?")) {
					// send reply to everyone
					msg = new Date().toString();
					buffer = msg.getBytes();
					packet = new DatagramPacket(buffer, buffer.length, 
							address, port);
					MulticastPeer.printDebug("Sending: " + new String(buffer));
					socket.send(packet);
				}
				
				
				//Check if the packet was sent by this program
				//if (!packet.getAddress().toString().equals(MulticastPeer.MyNode.ipaddress))
				{	
					//
					//Depending on the header of the msg, do one of the below:
					//

					if ((buffer[0] & MulticastPeer.ACK) == MulticastPeer.ACK)
					{
						MulticastPeer.getNodeByAddress(packet.getAddress().toString()).gotAck = true;
					}

					if ((buffer[0] & MulticastPeer.TEST) == MulticastPeer.TEST)
					{
						if (!((buffer[0] & MulticastPeer.ACK) == MulticastPeer.ACK))
							MulticastPeer.sender.sendMessage(MulticastPeer.ACK, null);//A request has been sent to send out an ACK to test connection.
						
						MulticastPeer.printDebug("TEST.\n");
					}
					else if ((buffer[0] & MulticastPeer.CHAT) == MulticastPeer.CHAT)
					{
						MulticastPeer.printDebug("CHAT.\n");
						if (!((buffer[0] & MulticastPeer.ACK) == MulticastPeer.ACK))
						{
							MulticastPeer.sender.sendMessage(MulticastPeer.ACK | MulticastPeer.CHAT, null);//A request has been sent to send out an ACK to test connection.

							String name = MulticastPeer.getNodeByAddress(packet.getAddress().toString()).name;
								
							System.out.println(name + ": " + msg);
						}
					}
					else if ((buffer[0] & MulticastPeer.REJECT) == MulticastPeer.REJECT)
					{
						ChangeName(msg);//After sending out a test for the name, send a rejection as the name has been taken.
						MulticastPeer.printDebug("REJECT.\n");
					}
					else if ((buffer[0] & MulticastPeer.LEAVE) == MulticastPeer.LEAVE)
					{
						MulticastPeer.DeleteNode(msg);//Delete a persons Node as they have left the group.
						MulticastPeer.printDebug("LEAVE.\n");
					}
					else if ((buffer[0] & MulticastPeer.UPDATE) == MulticastPeer.UPDATE)
					{
						boolean isACK = ((buffer[0] & MulticastPeer.ACK) == MulticastPeer.ACK);
						
						Update(MulticastPeer.stringToNodeList(msg), packet.getAddress(), isACK);//Update the NodeList.
						
						
						System.out.println(MulticastPeer.getNodeList());
						MulticastPeer.printDebug("UPDATE.\n");
					}
					else if ((buffer[0] & MulticastPeer.REQUEST) == MulticastPeer.REQUEST)
					{
						MulticastPeer.printDebug("REQUEST.\n");
					}
					else
					{
						MulticastPeer.printDebug("Not a valid command. Error has occured. (Blame Troy)\n(He'll tell you to blame Jason (You really should))\n");
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

/*
public void SendACK(){
	
	try{
	byte[] databuffer = MulticastPeer.getNodeList().getBytes();
	byte[] buffer = new byte[MulticastPeer.HEADER_SIZE + databuffer.length];
	
	buffer[0] = MulticastPeer.ACK;
	buffer[1] = 0;
	java.lang.System.arraycopy(databuffer, 0, buffer, MulticastPeer.HEADER_SIZE, databuffer.length);
	
	DatagramPacket packet = new DatagramPacket(buffer, buffer.length);//Create a packet
	socket.send(packet);
	}catch(Exception e){
		e.printStackTrace();
	}
}
*/

public void ChangeName(String input)
{
	//If the chosen name already exists, change it.
	
	String[] nodes_txt = input.split(":");
	
	if(nodes_txt[0].equals(MulticastPeer.MyNode.ipaddress))
	{
		try{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Username: ");
		String name = "";
		//Keep reading in a name until something has been entered
		while ((name = reader.readLine()).equals(""));
		
		MulticastPeer.MyNode.name = name;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}

	public void Update(ArrayList<Node> newNodeList, InetAddress senderIpAddress, boolean isACK){//Update the NodeList
		
		if(newNodeList.size() == 1 && newNodeList.get(0).ipaddress.equals(""))
		{
			//If only one node is in the list, the person is joining, otherwise it's just a normal update.
			
			
			if (newNodeList.get(0).name.equals(MulticastPeer.MyNode.name))
			{
				//It is my own joining message
				MulticastPeer.MyNode.ipaddress = senderIpAddress.toString();
				
				MulticastPeer.getNodeByName(MulticastPeer.MyNode.name).ipaddress = MulticastPeer.MyNode.ipaddress;
			}
			else if(MulticastPeer.getNodeByName(newNodeList.get(0).name) == null || !MulticastPeer.isConnected)
			{
				//If the name doesn't exist already or we aren't connected yet
				newNodeList.get(0).ipaddress = senderIpAddress.toString();
				MulticastPeer.mergeLists(newNodeList);
				
				if (!isACK)
					MulticastPeer.sender.sendMessage(MulticastPeer.ACK | MulticastPeer.UPDATE, MulticastPeer.getNodeList());
			}
			else
			{
				//If the message is from someone trying to take your name, then reject them.
				MulticastPeer.sender.queueMessage(MulticastPeer.REJECT, null);
			}
		}
		//Only receive UPDATE requests when connected
		else if ((MulticastPeer.isConnected | isACK)
			&& MulticastPeer.isDifferentList(newNodeList))
		{
			//If the message sent does not match the Node list that you have, then update your list.
			
			MulticastPeer.mergeLists(newNodeList);
			
			if (!isACK)
				MulticastPeer.sender.sendMessage((MulticastPeer.ACK | MulticastPeer.UPDATE), MulticastPeer.getNodeList());
			
			
			/*
			 Why do we need to receive ACKs? We just need to queue an update|ack message
			 
			byte[] buffer = new byte[MAX_BUFFER];//Craete a buffer.
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);//Create a packet
			
			//socket.setSoTimeout(1000);//set socket to stop receiving after x milliseconds.(This causes the exception)
			for(int i = 1; i < MulticastPeer.Nodes.size(); i++){//i = 1, because it had to receive the first to start this up.
				//socket.receive(packet);
			}
			*/
			
		}
	}
	
}