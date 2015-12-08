import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

class WebServer {
	public static void main(String argv[]) throws Exception {
		
		String requestMessageLine;
		String fileName;
		String clientIP;
		int clientPort;
		
		ServerSocket ListenSocket = new ServerSocket(11858);
		int requestNum = 0;
		
		File file = new File("log.out");
		if(file.exists()){
			file.delete();
			file.createNewFile();
		}

		while(true){
			Socket connectionSocket = ListenSocket.accept();
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			requestMessageLine = inFromClient.readLine();
			
			clientIP = connectionSocket.getInetAddress().toString().substring(1);
			clientPort = connectionSocket.getPort();
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");
			Date currentTime = new Date();
			String currentTimeString = formatter.format(currentTime);
			
			StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);		
			if(tokenizedLine.nextToken().equals("GET")) {
				fileName = tokenizedLine.nextToken();
				if(fileName.startsWith("/")==true) 
					fileName = fileName.substring(1);
				
				if(fileName.equals("log.out")){
					requestNum++;
					
					String logContents = "#"+requestNum+"\tIP "+clientIP+",\t"+"Port "+clientPort+",\t"+currentTimeString;
					
					FileWriter filewriter = new FileWriter(file,true);
					BufferedWriter bufferedwriter = new BufferedWriter(filewriter);
					PrintWriter printwriter = new PrintWriter(bufferedwriter);
					printwriter.println(logContents+"<br/>");
					printwriter.close();
					bufferedwriter.close();
					filewriter.close();  
					int numOfBytes = (int) file.length();
					FileInputStream inFile = new FileInputStream (fileName);
					byte[] fileInBytes = new byte[numOfBytes];
					inFile.read(fileInBytes);
					outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n");
					outToClient.writeBytes("Content-Length: "+numOfBytes+"\r\n");
					outToClient.writeBytes("\r\n");
					outToClient.write(fileInBytes, 0, numOfBytes);
					
//					connectionSocket.close();
				}
				else{
					outToClient.writeBytes("HTTP/1.0 404 Not Found\r\n");
					outToClient.writeBytes("Content-Length: 0"+""+"\r\n");
					outToClient.writeBytes("\r\n");
				}
			}
			else System.out.println("Bad Request Message");	
		}
	}
}