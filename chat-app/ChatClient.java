import java.io.*;//Gives input and output streams
import java.net.*;//Low level communication details
import java.util.*;

public class ChatClient
{
	//needed instance variables
	private String hostName;
	private int port;
	private String userName;

	//Set constructor for first initialization during run time
	public ChatClient(String hostName, int port)
	{
		this.hostName = hostName;
		this.port = port;
	}

	//Get Username
	public String getUserName()
	{
		return userName;
	}

	//Set UserName
	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	//We start by running java file
	public static void main(String[] args) 
	{	
		//Set syntax for Java RunTime
		if(args.length != 2)
		{
			System.out.println("\nUSE THE SYNTAX BELOW:\n\nJava ChatClient <hostName> <port-number>");
		}


		//Enter first argument
		String hostName = args[0];
		//Enter 2nd argument
		int port = Integer.parseInt(args[1]);
		//IPAdrress
		InetAddress host = null;
		try
		{
		 	host = InetAddress.getByName(hostName);
		}

		catch(UnknownHostException ex)
		{
			System.out.println("Error in identifying host: " + ex.getMessage());
			ex.printStackTrace();
		}

		//Print HostName and IPAddress
		System.out.println(host.getHostName() + " = " + host.getHostAddress());

		

		//Call on the client class object to be able to execute using execute method
		ChatClient client = new ChatClient(hostName, port);
		client.execute();
	}

	public void execute()
	{
		try
		{
			//create a new Socket object to enable socket methods(to execute connection, we need host and port)
			Socket socket = new Socket(hostName, port);
			System.out.println("Connected to the ChatServer:");

			//Now we start our two threads
			new ReadThread(this, socket).start();
			new WriteThread(this, socket).start();
		}

		catch(UnknownHostException ex)
		{
			System.out.println("Cannot find server: " + ex.getMessage());
			ex.printStackTrace();
		}

		catch(IOException ex)
		{
			System.out.println("I/O Error: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public class ReadThread extends Thread
	{
		//Our needed instance variables
		private ChatClient client;
		private Socket socket;
		private BufferedReader reader;


		//Constructors for first initialization
		public ReadThread(ChatClient client, Socket socket)
		{
			this.client = client;
			this.socket = socket;

			try
			{
				InputStream input = socket.getInputStream();
				reader = new BufferedReader(new InputStreamReader(input));
			}

			catch(IOException ex)
			{
				System.out.println("Error getting input stream: " + ex.getMessage());
				ex.printStackTrace();
			}
		}

		public void run()
		{
			while(true)
				try
				{
					String response = reader.readLine();
					System.out.println("\n" + response);

					//prints the username after displaying the server's message
					if(client.getUserName() != null)
					{
					System.out.Print("[" + client.getUserName() + "]: ");
					//System.out.println("\n");
					}
				}

				catch(IOException ex)
				{
					System.out.println("Error in reading from serer: " + ex.getMessage());
					ex.printStackTrace();
					break;
				}
			}

		}

	public class WriteThread extends Thread
	{
		private ChatClient client;
		private Socket socket;
		private PrintWriter writer;

		public WriteThread(ChatClient client, Socket socket)
		{
			this.client = client;
			this.socket = socket;

			try
			{
				OutputStream output = socket.getOutputStream();
				writer = new PrintWriter(output, true);
			}

			catch(IOException ex)
			{
				System.out.println( "Error in getting output stream: " + ex.getMessage());
				ex.printStackTrace();
			}
		}

		public void run()
		{
			Console console = System.console();
			String userName = console.readLine("\n Enter your user name: ");
			client.setUserName(userName);
			writer.println(userName);

			String text;

			do
			{
				text = console.readLine("[" + userName + "]");
				writer.println(text);
			}
			while(!text.equals("exit"));

			try
			{
				socket.close();
			}

			catch(IOException ex)
			{
				System.out.println("Error writing to server");
			}
		}
	}
}