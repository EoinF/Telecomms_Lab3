
public class Node 
{
	String ipaddress;
	String name;
	boolean gotAck;
	
	public Node(String name, String ipaddress)
	{
		this.ipaddress = ipaddress;
		this.name = name;
		this.gotAck = false;
	}
	
	public boolean equals(Node n)
	{
		return this.name.equals(n.name)
				&&
				this.ipaddress.equals(n.ipaddress);
	}
}
