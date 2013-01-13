import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;


public class MulticastPeer {

	//Command Type Masks
	public static final int ACK = 0x01;
	public static final int TEST = 0x02;
	public static final int REJECT = 0x04;
	public static final int LEAVE = 0x08;
	public static final int UPDATE = 0x10;
	public static final int REQUEST = 0x20; //Asks for a certain item
	public static final int CHAT = 0x40;
	
	public static final int HEADER_SIZE = 1 /*Command Type*/ + 1 /*datalength*/;
	
	public static ArrayList<Node> Nodes;
	public static Node MyNode;
	public static boolean isConnected = false;
	

	public static MulticastSender sender = null;
	public static MulticastReceiver receiver = null;

	
	private static Scanner sc;
	
	/**
	 * Main method
	 * Start the sending and receiving threads
	 * 
	 * @param args 	[0] IP address the client should send to 
	 * 				[1] Port number the client should send to
	 */
	public static void main(String[] args)                                                                                    
	{
		int port = 0;
		String address = "";
		Nodes = new ArrayList<Node>();
		
		System.out.println("Program start");
		try
		{
			if (args.length == 2)
			{
				address = args[0];
				port = Integer.parseInt(args[1]);
				
				
				sender = new MulticastSender(address, port);
				receiver= new MulticastReceiver(address, port);
			}
			else
			{
				receiver= new MulticastReceiver();
				sender = new MulticastSender();
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
			
			new Thread(receiver).start();
			new Thread(sender).start();
			
			while(!isConnected)
				Thread.sleep(100);//Don't start the user interface until the user has connected to the multicast group
			
			System.out.println("Connected!");
			userInterface();
		}	
		catch(Exception e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("Program end");
	}

	public static void userInterface()
	{
		int input;
		sc = new Scanner(System.in);
		try 
		{
			do
			{	
				System.out.print("0) End the program\n 1) Send a Command\n 2) Enter Chat Mode\n");
				
				input = sc.nextInt();

				switch(input)
				{
					case 0://End the program
						break;
					case 1://Send a command
						sendCommand();
						break;
					case 2://Send a command
						chatMode();
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
		
	}

	public static void chatMode()
	{
		System.out.println("Type /exit to exit");
		
		String input;
		while (!(input = sc.nextLine()).equals("/exit"))
		{
			sender.queueMessage(CHAT, input);
		}
	}
	
	public static void sendCommand()
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
				sender.queueMessage(MulticastPeer.TEST, "Hello world");
			case 0:
				break;
			default:
				System.out.println("Not a valid command!");
		}
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
			
			if(properties.length < 2)
				nodelist.add(new Node(properties[0], ""));
			else
				nodelist.add(new Node(properties[0], properties[1]));
		}
		return nodelist;
	}
	
	public static void DeleteNode(String input){
		//Will find the node of the person that left the group, and delete their node.
		String[] nodes_txt = input.split(":");
		boolean found = false;
		for(int i = 0; i < Nodes.size() && !found; i++)
		{//Check the nodes for the persons name who left.
			if(nodes_txt[1].equals(Nodes.get(i).name))//Once found, delete his node.
			{
				found = true;
				Nodes.remove(i);//Remove the Node.
			}
		}
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
	
	public static boolean isDifferentList(ArrayList<Node> newNodeList)
	{
		if (Nodes.size() != newNodeList.size())
			return true;
		
		boolean foundMatch;
		for (int i = 0; i < Nodes.size(); i++)
		{
			foundMatch = false;
			for (int j = 0; j < newNodeList.size(); j++)
			{
				if (Nodes.get(i).equals(newNodeList.get(j)))
				{
					foundMatch = true;
				}
			}
			
			if (!foundMatch)
			{
				return true;
			}
		}
		return false;
	}

	public static Node getNodeByAddress(String ipaddress)
	{
		for (int i = 0; i < Nodes.size(); i++)
		{
			if (Nodes.get(i).ipaddress.equals(ipaddress))
				return Nodes.get(i);
		}
		return null;
	}
	
	public static Node getNodeByName(String name)
	{
		for (int i = 0; i < Nodes.size(); i++)
		{
			if (Nodes.get(i).name.equals(name))
				return Nodes.get(i);
		}
		return null;
	}
	
	public static boolean receivedAllAcks()
	{
		for (int i = 0; i < Nodes.size(); i++)
		{
			if (!Nodes.get(i).equals(MyNode)) //Don't need to receive an ACK from myself
			{
				if (!Nodes.get(i).gotAck)
					return false;
			}
		}
		return true;
	}
	
	public static void printDebug(String msg)
	{
		System.err.println(msg);
	}
}
