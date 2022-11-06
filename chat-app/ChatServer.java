import java.io.*;//Gives input and output streams
import java.net.*;//Low level communication details
import java.util.*;


public class ChatServer
{
	private int port;
	private int backlog;
	private Set<String> userNames = new HashSet<String>();
	private Set<UserThread> userThreads = new HashSet<UserThread>();

	public ChatServer(int port, int backlog)
	{
		this.port = port;
		this.backlog = backlog;
	}

	public static void main(String[] args) 
	{
		if (args.length != 2)
		{
			System.out.println("\nUSE THE SYNTAX BELOW FOR SERVER TO RUN: \n \njava ChatServer <port-number> <backlog>");
			System.exit(0);
		}

		int port = Integer.parseInt(args[0]);
		int backlog = Integer.parseInt(args[1]);

		ChatServer server = new ChatServer(port, backlog);
		server.execute();
	}

	public void execute()
	{
		try
		{
			ServerSocket serverSocket = new ServerSocket(port, backlog);
			System.out.println("Chat Server is listening on port " + port);

			while(true)
			{
				Socket socket =serverSocket.accept();
				System.out.println("New user connected");

				UserThread newUser = new UserThread(socket, this);
				userThreads.add(newUser);
				newUser.start();
			}
		}

		catch(IOException ex)
		{
			System.out.println("Error in the Server: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	Set<String> getUserNames()
	{
		return this.userNames;
	}

	//Stores username of the newly connected client.
	void addUserName(String userName)
	{
		userNames.add(userName);
	}

	//When a client is disconneted, removes the associated username and UserThread
	void removeUser(String userName, UserThread aUser)
	{
		boolean removed = userNames.remove(userName);
		if (removed)
		{
			userThreads.remove(aUser);
			System.out.println("The user "+ userName+" has quitted");
		}
	}

	//Returns true if there are other users connected (not count the currently connected user)
	boolean hasUsers()
	{
		return !this.userNames.isEmpty();
	}

	//Delivers a message from one user to others (broadcasting)
	void broadcast(String message, UserThread excludeUser)
	{
		for (UserThread aUser : userThreads)
		{
			if (aUser != excludeUser)
			{
				aUser.sendMessage(message);
			}
		}
	}


	//This thread handles connection for each connected client,
	//so the server can handle multiple clients at the same time.
	public class UserThread extends Thread
	{
		private Socket socket;
		private ChatServer server;
		private PrintWriter writer;

		public UserThread(Socket socket, ChatServer server)
		{
			this.server = server;
			this.socket = socket;
		}

		public void run()
		{
			try
			{
				InputStream input = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				
				OutputStream output = socket.getOutputStream();
			 	writer = new PrintWriter(output, true);

			 	printUsers();

			 	String userName = reader.readLine();
			 	server.addUserName(userName);

			 	String serverMessage = "New user connected: " + userName;
			 	server.broadcast(serverMessage, this);

			 	String clientMessage;

			 	do
			 	{
			 		clientMessage = reader.readLine();
                	serverMessage = "[" + userName + "]: " + clientMessage;
                	server.broadcast(serverMessage, this);
			 	}

			 	while (!clientMessage.equals("exit"));
 
            	server.removeUser(userName, this);
            	socket.close();
 
            	serverMessage = userName + " has quitted.";
            	server.broadcast(serverMessage, this);
            }

			catch (IOException ex)
			{
				System.out.println("Error in UserThread: " + ex.getMessage());
            	ex.printStackTrace();
			}
		}

		//Sends a list of online users to the newly connected user.
		void printUsers() 
		{
        	if (server.hasUsers()) 
        	{
            	writer.println("Connected users: " + server.getUserNames());
            	writer.println(userNames.size() + " are online");
        	} 
        	else 
        	{
            	writer.println("No other users connected");
        	}
    	}

    	//Sends a message to the client.
    	void sendMessage(String message) 
    	{
        	writer.println(message);
    	}
	}
}