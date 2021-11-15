import java.io.*;
import java.net.*;
import java.util.*;

public class ClientThread extends Thread {
	
	private String serverName;
	private int serverPort;
	private String fileName;
	private String server;
	private int T;
	// private String[] files;

	public ClientThread(String server,String serverName,int serverPort,String fileName,int T){
		this.server = server;
		this.serverName = serverName;
		this.serverPort = serverPort;
		this.fileName = fileName;
		this.T = T;
	}
  
    public void run() {
		try{
			InetAddress serverIPAddress = InetAddress.getByName(server);
    		// Socket clientSocket = new Socket(serverIPAddress, serverPort);//建立连接

			long startTime = System.currentTimeMillis(); 

			FileInputStream inputStream = new FileInputStream("./" + fileName);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String file = null;
			file = bufferedReader.readLine();

	    	while (System.currentTimeMillis()-startTime< T) {
				Socket clientSocket = new Socket(serverIPAddress, serverPort);
				if(file == null)
				{
					inputStream.close();
					bufferedReader.close();
					inputStream = new FileInputStream("./" + fileName);
					bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
					file = bufferedReader.readLine();   // 有待修改,感觉很耗时
				}
				String request = "GET" + " " + file + " " + "HTTP/1.0\r\nHost: " + serverName + "\r\n";

				DataOutputStream outToServer 
		   		= new DataOutputStream(clientSocket.getOutputStream());
				outToServer.writeBytes(request + '\n');
				Date sendTime = new Date();


				BufferedReader inFromServer 
			 	= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				Date archieveTime = new Date();
				
				String sentenceFromServer = inFromServer.readLine();;
				while(sentenceFromServer != null){
					System.out.println(sentenceFromServer);
					sentenceFromServer = inFromServer.readLine();
				}
				File fileInfo = new File("./file/" + file);
				TCPClient.statistics(1,(int) fileInfo.length(),archieveTime.getTime() -sendTime.getTime() );
				System.out.println("\n");
				file = bufferedReader.readLine();
		    } // end while(true)
			// clientSocket.close();
		}catch(Exception e){}
		
    } // end method run
} // end ServiceThread