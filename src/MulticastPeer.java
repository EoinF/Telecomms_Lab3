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
	public static String MyName = "";
	
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
			MyName = name;
			
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
	
	public static ArrayList<Node> stringToNodeList(String input)
	{
		ArrayList<Node> nodelist = new ArrayList<Node>();
		String[] nodes_txt = input.split(",");
		
		for (int i = 0; i < nodes_txt.length; i++)
		{
			String[] properties = nodes_txt[i].split(":");
			nodelist.add(new Node(properties[0], properties[1]));
		}
		
		return nodelist;
	}
	
	public static void mergeLists(ArrayList<Node> newnodes)
	{
		for (int i = 0; i < newnodes.size(); i++)
		{
			//If the node doesn't already exist, then add it
			if (!nodeExists(newnodes.get(i)))
				Nodes.add(newnodes.get(i));
		}
	}
	
	private static boolean nodeExists(Node n)
	{
		for (int i = 0; i < Nodes.size(); i++)
		{
			if (Nodes.get(i).equals(n))
				return true;
		}
		return false;
	}
}
