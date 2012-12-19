
public class Node 
{
	String ipaddress;
	String name;
	
	public Node(String ipaddress, String name)
	{
		this.ipaddress = ipaddress;
		this.name = name;
	}
	
	public boolean equals(Node n)
	{
		return this.name == n.name
				&&
				this.ipaddress == n.ipaddress;
	}
}
