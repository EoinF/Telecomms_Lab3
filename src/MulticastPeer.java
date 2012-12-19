import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class MulticastPeer {

	//Command Type Masks
	public static final byte ACK = 0x01;
	public static final byte TEST = 0x02;
	public static final byte REJECT = 0x04;
	public static final byte LEAVE = 0x08;
	public static final byte UPDATE = 0x0F;
	public static final byte REQUEST = 0x10; //Asks for a certain item
	
	public static final int HEADER_SIZE = 1 /*Command Type*/ + 1 /*Command ID*/;
	
	//public static final byte TEST = (byte)(0xFF); //All bits are set
	
	public static ArrayList<Node> Nodes;
	public static Node MyNode;
	public static boolean isConnected = false;
	
	/**
	 * Main method
	 * Start the sending and receiving threads
	 * 
	 * @param args 	[0] IP address the client should send to 
	 * 				[1] Port number the client should send to
	 */
	public static void main(String[] args) {
		int port = 0;
		String address = "";
		MulticastSender client = null;
		MulticastReceiver receiver = null;
		Nodes = new ArrayList<Node>();
		
		System.out.println("Program start");
		try 
		{
			if (args.length == 2) 
			{
				address = args[0];
				port = Integer.parseInt(args[1]);
				
				
				client = new MulticastSender(address, port);
				receiver= new MulticastReceiver(address, port);
			}
			else
			{
				receiver= new MulticastReceiver();
				client = new MulticastSender();
			}
			
			
			//
			//Next, get the username for this session
			//
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Username: ");
			String name = "";
			
			//Keep reading in a name until something has been entered
			while ((name = reader.readLine()).equals(""));
			
			MyNode = new Node(name, "");
			Nodes.add(MyNode);
			
			(new Thread(receiver)).start();
			client.run();
		}	
		catch(Exception e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("Program end");
	}
	
	public static String getNodeList()
	{
		StringBuilder nodestext = new StringBuilder();
		for (int i = 0; i < Nodes.size(); i++)
		{
			nodestext.append(Nodes.get(i).name);
			nodestext.append(':');
			nodestext.append(Nodes.get(i).ipaddress);
			if (i < Nodes.size() - 1)
				nodestext.append(',');
		}
		
		return nodestext.toString();
	}
}
