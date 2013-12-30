package com.green.quickserv;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;


public class Server implements Runnable{
	
	protected final static String CONTENT404 = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1>The requested resource could not be found but may be available again in the future.</html>";
	protected final static String CONTENT501 = "<html><head><title>501 Not Implemented</title></head><body><h1>501 Not Implemented</h1>The server either does not recognise the request method, or it lacks the ability to fulfill the request.</body></html>";

	protected final static String STRING200 = "OK";
	protected final static String STRING404 = "Not Found";
	protected final static String STRING501 = "Not Implemented";
	
	private int port;
	public static boolean running = false;
	public ServerSocket s_sock = null;
		
	public Server(int port){
		this.port = port;
		
	}
	
	/**
	 * Starts the server by accepting incoming connections
	 */
	
	
	
	public static void main(String[] args){
		
		if(args.length < 1){
			System.out.println("Usage: QuickServ.jar [port number]");
			System.exit(1);
		}
		
		int port = Integer.parseInt(args[0]);
		Server serv = new Server(port);
		serv.run();
		
	}

	@Override
	public void run() {
		running = true;
		ArrayList<Worker> clients = new ArrayList<Worker>();

		//Open ports.
		try{
			s_sock = new ServerSocket(port);
			
			while(running){
				//Spawn new thread for every connection
				Worker r = new Worker(s_sock.accept());
				new Thread(r).start();
				clients.add(r);
				//clients.get(clients.size()-1).start();
			}
			
		}catch(IOException ex){
			if(running) System.out.println("Port already in use.");
			//ex.printStackTrace();
		}catch(SecurityException ex){
			System.out.println("Security manager does not allow this.");
			ex.printStackTrace();
		}catch(IllegalArgumentException ex){
			System.out.println("Invalid port number. Range: [1025, 65535]");
			ex.printStackTrace();
		}

		try{
			for(Worker r: clients){
				r.c_sock.close();
			}
			
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
		return;
		
	}
}
