import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;


public class Server {
	
	protected final static String CONTENT404 = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1>The requested resource could not be found but may be available again in the future.</html>";
	protected final static String CONTENT501 = "<html><head><title>501 Not Implemented</title></head><body><h1>501 Not Implemented</h1>The server either does not recognise the request method, or it lacks the ability to fulfill the request.</body></html>";

	protected final static String STRING200 = "OK";
	protected final static String STRING404 = "Not Found";
	protected final static String STRING501 = "Not Implemented";
	
	private int port;
	protected static volatile boolean running = true;
		
	public Server(int port){
		this.port = port;
	}
	
	public void start(){
		
		Socket c_sock = null;
		ServerSocket s_sock = null;
		ArrayList<Thread> clients = new ArrayList<Thread>();

		//Open ports.
		try{
			s_sock = new ServerSocket(port);
			
			while(running){
				Runnable r = new Worker(s_sock.accept());
				clients.add(new Thread(r));
				clients.get(clients.size()-1).start();
			}
			
			for(Thread th: clients){
				th.join();
			}
			
		}catch(IOException ex){
			System.out.println("Port already in use.");
			ex.printStackTrace();
		}catch(SecurityException ex){
			System.out.println("Security manager does not allow this.");
			ex.printStackTrace();
		}catch(IllegalArgumentException ex){
			System.out.println("Invalid port number. Range: [1025, 65535]");
			ex.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	
	
	public static void main(String[] args){
		/*final Thread mainThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				running = false;
				try {
					mainThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});*/
		
		if(args.length < 1){
			System.out.println("Usage: QuickServ.jar [port number]");
			System.exit(1);
		}
		
		int port = Integer.parseInt(args[0]);
		Server serv = new Server(port);
		serv.start();
		
	}
}
