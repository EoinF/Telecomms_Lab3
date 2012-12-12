
public class MulticastPeer {

	/**
	 * Main method
	 * Start the sending and receiving threads
	 * 
	 * @param args 	[0] IP address the client should send to 
	 * 				[1] Port number the client should send to
	 */
	public static void main(String[] args) {
		int port = 0;
		String address = null;
		MulticastSender client = null;
		MulticastReceiver receiver = null;
		
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
				receiver.run();
				client.run();
		}	
		catch(Exception e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("Program end");
	}
}
