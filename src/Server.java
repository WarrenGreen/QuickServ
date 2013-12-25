import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.imageio.ImageIO;


public class Server {
	
	private final String CONTENT404 = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1>The requested resource could not be found but may be available again in the future.</html>";
	private final String CONTENT501 = "<html><head><title>501 Not Implemented</title></head><body><h1>501 Not Implemented</h1>The server either does not recognise the request method, or it lacks the ability to fulfill the request.</body></html>";

	private final String STRING200 = "OK";
	private final String STRING404 = "Not Found";
	private final String STRING501 = "Not Implemented";
	
	private int port;
		
	public Server(int port){
		this.port = port;
	}
	
	public void start(){
		
		Socket c_sock = null;
		ServerSocket s_sock = null;
		
		//Open ports.
		try{
			s_sock = new ServerSocket(port);
			c_sock = s_sock.accept();
			
		}catch(IOException ex){
			System.out.println("Port already in use.");
			ex.printStackTrace();
		}catch(SecurityException ex){
			System.out.println("Security manager does not allow this.");
			ex.printStackTrace();
		}catch(IllegalArgumentException ex){
			System.out.println("Invalid port number. Range: [1025, 65535]");
			ex.printStackTrace();
		}
		
		try{
			while(true){
				BufferedOutputStream out = new BufferedOutputStream(c_sock.getOutputStream());
				
				HashMap<String, String> header = parse(c_sock);
				
				sendResponse(header, out);
				
				if(header.get("connection") == null || header.get("connection").compareToIgnoreCase("keep-alive") != 0)
					break;
				
				c_sock = s_sock.accept();
			}
			
		}catch(IOException ex){
			//TODO: Fill Exceptions
			ex.printStackTrace();
		}

	}
	
	public void sendResponse(HashMap<String, String> header, BufferedOutputStream out) throws IOException{
		int response = 200;
		String respString = STRING200;
		String contentType = "text/html";
		byte[] content = new byte[5];
		String connection = "close";
		
		if(header.get("file") == null){
			response = 501;
			respString = STRING501;
			content = CONTENT501.getBytes();
		}else if(! new File(header.get("file")).exists()){
			response = 404;
			respString = STRING404;
			content = CONTENT404.getBytes();
		}else if(new File(header.get("file")).isDirectory()){
			content = generateIndex(header.get("file")).getBytes();
		}else{
			contentType = getFileType(header.get("file"));
			if(contentType.compareTo("application/octet-stream")!= 0){
				content = readImage(header.get("file"));
			}
		}
		
		if(header.get("connection") != null && header.get("connection").compareToIgnoreCase("keep-alive") == 0){
			connection = "keep-alive";
		}
		
		String headRet = String.format("HTTP/1.1 %d %s\r\nQuickServ\r\nMIME-version: 1.0\r\nContent-type: %s\r\nContent-Length: %d\r\nConnection: %s\r\n\r\n", response, respString, contentType, content.length, connection);
	
		out.write(headRet.getBytes());
		out.write(content);
		
		out.close();
	}
	
	public byte[] readImage(String filename){
		
		byte[] imgBytes = null;
		try {
			BufferedImage img = ImageIO.read(new File(filename));
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    ImageIO.write( img, "jpg", baos );
		    baos.flush();
		    imgBytes = baos.toByteArray();
		    baos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return imgBytes;
	}
	
	public String generateIndex(String filename){
		File file = new File(filename);
		String ret = "<html><body><h1>QuickServ</h1><br />";
		String path = "";
		
		if(filename.compareTo("./") != 0 ){
			path = filename.substring(1) +"/";
		}
		
		for(File child: file.listFiles()){
			ret = ret + String.format("<a href='%s'>%s</a><br />", path + child.getName(), child.getName());
		}
		
		ret.concat("</body></html>");
		return ret;
	}
	
	public HashMap<String, String> parse(Socket c_sock){
		HashMap<String, String> header = new HashMap<String, String>();
		try{
			BufferedReader in  = new BufferedReader(
					new InputStreamReader(c_sock.getInputStream()));
			
			String inputLine;
			
			if((inputLine = in.readLine()) != null){
				header.put("status", inputLine);
				String file = inputLine.split(" ")[1];
				if(file.indexOf("..") >=0 ) file = null;
				header.put("file", "."+file);
			}
			
			while((inputLine = in.readLine())!= null && !inputLine.isEmpty()){
				int sep = inputLine.indexOf(":");
				if(sep >0){
					header.put(inputLine.substring(0, sep).toLowerCase(), inputLine.substring(sep+2, inputLine.length()));
				}
			}
			
		}catch(IOException ex){
			//TODO: Fill Exceptions
			ex.printStackTrace();
		}
		
		return header;
	}
	
	public String getFileType(String file){
		String contentType = "";
		
		if(file.indexOf(".jpeg") >= 0 || file.indexOf(".jpg") >=0 ){
			contentType = "image/jpeg";
		}else if(file.indexOf(".png") >= 0){
			contentType = "image/png";
		}else{
			contentType = "application/octet-stream";
		}
		
		return contentType;
	}
	
	public static void main(String[] args){
		if(args.length < 1){
			System.out.println("Usage: QuickServ.jar [port number]");
			System.exit(1);
		}
		
		int port = Integer.parseInt(args[0]);
		Server serv = new Server(port);
		serv.start();
		
	}
}
