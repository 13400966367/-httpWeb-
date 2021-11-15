/**
 ** XMU CNNS Class Demo Basic Web Server
 **/

import java.io.*;
import java.net.*;
import java.util.*;

class BasicWebServer{

    public static int serverPort = 3912;  
	public static String serverName = "localhost";  
    public static String WWW_ROOT = "./";// 默认
	public static int CacheSize = 1024;//默认cache大小
	public static int usedspace = 0;

	public static Map<String, byte[]> map = new HashMap<String, byte[]>();

	public static byte[] check(String fileName){
		File fileInfo = new File(fileName);
		int numOfBytes = (int) fileInfo.length();
		if(map.get(fileName) == null) //不在cache里面
		{
			if(usedspace + numOfBytes <= CacheSize){ // cache还没满
				try {
					FileInputStream fileStream  = new FileInputStream (fileName);
					byte[] fileInBytes = new byte[numOfBytes];
					map.put(fileName,fileInBytes);
					usedspace += numOfBytes;
					return null;//表示check不在cache里面
				} catch (Exception e) {
				}
			}
			return null;
		}
		else{
			return map.get(fileName);
		}
	}

    public static void main(String args[]) throws Exception  {

	// create server socket
	if(!args[0].equals("-config")){
		System.out.println("Wrong format\nPlease refer to: \n\n\tjava <servername> -config <config_file_name>");
		System.exit(0);
	}

	String ConfFile = args[1];
	FileInputStream inputStream = new FileInputStream("./conf/" + ConfFile);
	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	String lines = null;
	lines = bufferedReader.readLine();

	Map<String, String> ServernameMapRoot = new HashMap<String, String>();//ServerName和DocumentRoot的映射
	String temp1 = "file/";
	String temp2 = "localhost";
	// 读取配置文件
	while(lines != null){


		String[] keywords = lines.split("\\s+");
		// System.out.println(keywords.length);

		if(keywords[0].equals("Listen")){
			serverPort = Integer.parseInt(keywords[1]);
		}
		else if(keywords[0].equals("CacheSize")){
			CacheSize = Integer.parseInt(keywords[1]) * 1024;
		}
		else if(keywords.length == 3 && keywords[1].equals("DocumentRoot")){
			temp1 = keywords[2];
			
		}
		else if(keywords.length == 3 && keywords[1].equals("ServerName")){
			temp2 = keywords[2];
			
		}
		else if(keywords[0].startsWith("<") || keywords[0].startsWith("#")){}

		if(ServernameMapRoot.get(temp2) == null){
			ServernameMapRoot.put(temp2,temp1);//将Servername和WWW_ROOT做一个映射
		}

		// if(keywords.length == 3){
		// 	System.out.println(keywords[1]);
		// 	System.out.println(keywords[2]);
		// }

		lines = bufferedReader.readLine();
	}

	for (Map.Entry<String, String> entry : ServernameMapRoot.entrySet()) {
 
		System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
	 
	}

	// System.out.println("CacheSize: " + CacheSize);
	inputStream.close();
	bufferedReader.close();
	
	ServerSocket listenSocket = new ServerSocket(serverPort);
	System.out.println("server listening at: " + listenSocket);
	System.out.println("server www root: " + WWW_ROOT);

	while (true) {

	    try {

		    // take a ready connection from the accepted queue
		    Socket connectionSocket = listenSocket.accept();

		    System.out.println("\nReceive request from " + connectionSocket);
	
		    // process a request
		    WebRequestHandler wrh = 
		        new WebRequestHandler( connectionSocket, WWW_ROOT ,ServernameMapRoot);

		    wrh.processRequest();
			// System.out.println("nowblock: " + nowBlock);
	    } catch (Exception e)
		{
		}
	} // end of while (true)
	
    } // end of main

} // end of class WebServer
