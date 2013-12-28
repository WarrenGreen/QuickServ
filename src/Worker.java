import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

import javax.imageio.ImageIO;


public class Worker implements Runnable{
	private Socket c_sock;
	
	public Worker(Socket c_sock){
		this.c_sock = c_sock;
	}
	
	@Override
	public void run() {
		try{
			BufferedReader in  = new BufferedReader(new InputStreamReader(c_sock.getInputStream()));
			c_sock.setKeepAlive(true);
			
						
			while(!c_sock.isClosed() && c_sock.getKeepAlive()){
				HashMap<String, String> header = parse(in);
				
				sendResponse(header, c_sock.getOutputStream());
				
				if(header.get("connection") == null || header.get("connection").compareToIgnoreCase("keep-alive") != 0){
					c_sock.setKeepAlive(false);
				}
				
			}
			c_sock.close();
			in.close();

			
		}catch(IOException ex){
			ex.printStackTrace();
		}

		return;
		
	}
	
	
	public void sendResponse(HashMap<String, String> header, OutputStream out) throws IOException{
		int response = 200;
		String respString = Server.STRING200;
		String contentType = "text/html";
		byte[] content = new byte[5];
		String connection = "close";
		
		
		if(header.get("file") == null){
			response = 501;
			respString = Server.STRING501;
			content = Server.CONTENT501.getBytes();
		}else if(! new File(header.get("file")).exists()){
			response = 404;
			respString = Server.STRING404;
			content = Server.CONTENT404.getBytes();
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
	
	public HashMap<String, String> parse(BufferedReader in){
		HashMap<String, String> header = new HashMap<String, String>();
		try{

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

}
