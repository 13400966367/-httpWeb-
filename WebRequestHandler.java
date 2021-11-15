/**
 ** XMU CNNS Class Demo Basic Web Server
 **/
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

class WebRequestHandler {

    static boolean _DEBUG = true;
    static int     reqCount = 0;

    String WWW_ROOT;
    Socket connSocket;
	Map<String,String> ServernameMapRoot;
    BufferedReader inFromClient;
    DataOutputStream outToClient;

    String urlName;
    String fileName;
    File fileInfo;
	String rfc1123_3;
	String L_M;

    public WebRequestHandler(Socket connectionSocket, 
			     String WWW_ROOT,Map<String,String> ServernameMapRoot) throws Exception
    {
        reqCount ++;

	    this.WWW_ROOT = WWW_ROOT;
	    this.connSocket = connectionSocket;
		this.ServernameMapRoot = ServernameMapRoot;

	    inFromClient =
	      new BufferedReader(new InputStreamReader(connSocket.getInputStream()));

	    outToClient =
	      new DataOutputStream(connSocket.getOutputStream());

    }

    public void processRequest() 
    {

	try {
	    mapURL2File();

	    if ( fileInfo != null ) // found the file and knows its info
	    {
			Updatelastmodifiedtime();
		    outputResponseHeader();
		    outputResponseBody();
	    } // dod not handle error

	    connSocket.close();
	} catch (Exception e) {
	    outputError(400, "Server error");
	}
    } // end of processARequest

    private void mapURL2File() throws Exception     
    {	
	    String requestMessageLine = inFromClient.readLine();

	    DEBUG("Request " + reqCount + ": " + requestMessageLine);

	    // process the request
		
	    String[] request = requestMessageLine.split("\\s");

	    if (request.length < 2 || !request[0].equals("GET"))
	    {
		    outputError(500, "Bad request");
		    return;
	    }

	    // parse URL to retrieve file name
	    urlName = request[1];
	    
	    if ( urlName.startsWith("/") == true )
	       urlName  = urlName.substring(1);
        // debugging
		boolean ismobile = false;

        if (_DEBUG) {
           String line = inFromClient.readLine();
           while ( !line.equals("") ) {
			  if(line.startsWith("Host")){
				  String[] hoststring = line.split("\\s");
				//   for(int i = 0;i<hoststring.length;++i){
				// 	  System.out.println(hoststring[i]);
				//   }
				  WWW_ROOT = WWW_ROOT + ServernameMapRoot.get(hoststring[1]);
				  System.out.println("WWW_ROOT: " + WWW_ROOT);
			  }
			  
			  if(line.startsWith("User-Agent")){
				String[] headstring = line.split("\\s");
				for(int i = 1;i<headstring.length;++i){
					if(headstring[i].equals("Mobile")){//识别
						ismobile = true;
						break;
					}
				}
			  }
			  if(line.startsWith("If-Modified-Since")){//识别
				
			  }
              DEBUG( "Header: " + line );
              line = inFromClient.readLine();
           }
        }

		if(urlName.equals("")){
			if(ismobile)
			{
				urlName = "index_m.html";
			}
			else{
				urlName = "index.html";
			}
		}
	    // map to file name
	    fileName = WWW_ROOT + urlName;
	    DEBUG("Map to File name: " + fileName);



	    fileInfo = new File( fileName );
	    if ( !fileInfo.isFile() ) 
	    {
		    outputError(404,  "Not Found");
		    fileInfo = null;
	    }

    } // end mapURL2file


    private void outputResponseHeader() throws Exception 
    {
		InetAddress hostaddr = connSocket.getInetAddress();
	    outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n");
	    // outToClient.writeBytes("Set-Cookie: MyCool433Seq12345\r\n");
		outToClient.writeBytes("Date: " + rfc1123_3 + "\r\n");
		outToClient.writeBytes("Server: " + hostaddr.getHostAddress() + "\r\n");
		outToClient.writeBytes("Last-Modified: " + L_M + "\r\n");
	    if (urlName.endsWith(".jpg"))
	        outToClient.writeBytes("Content-Type: image/jpeg\r\n");
	    else if (urlName.endsWith(".gif"))
	        outToClient.writeBytes("Content-Type: image/gif\r\n");
	    else if (urlName.endsWith(".html") || urlName.endsWith(".htm"))
	        outToClient.writeBytes("Content-Type: text/html\r\n");
	    else
	        outToClient.writeBytes("Content-Type: text/plain\r\n");
    }

    private void outputResponseBody() throws Exception 
    {
	    int numOfBytes = (int) fileInfo.length();
	    outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
	    outToClient.writeBytes("\r\n");
		// send file content
		byte[] fileInBytes = new byte[numOfBytes];
		fileInBytes = BasicWebServer.check(fileName);
		// System.out.println(fileInBytes == null);
		if(fileInBytes == null)
	    {
			try{
	    		FileInputStream fileStream  = new FileInputStream (fileName);
				fileInBytes = new byte[numOfBytes];
				fileStream.read(fileInBytes);
			}
			catch(FileNotFoundException ex){}
		}
		// System.out.println(fileInBytes + " " + fileName);
	    outToClient.write(fileInBytes, 0, numOfBytes);
		outToClient.writeBytes("\r\n");
    }

	private void Updatelastmodifiedtime() throws Exception
	{

		FileInputStream inputStream = new FileInputStream("./conf/last_modified");
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		L_M = bufferedReader.readLine();
		inputStream.close();
		bufferedReader.close();

		SimpleDateFormat sdf3 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
        sdf3.setTimeZone(TimeZone.getTimeZone("GMT"));
    	rfc1123_3 = sdf3.format(new Date());

		BufferedWriter out = new BufferedWriter(new FileWriter("./conf/last_modified"));
		out.write(rfc1123_3);
		out.close();
	}

    void outputError(int errCode, String errMsg)
    {
	    try {
	        outToClient.writeBytes("HTTP/1.0 " + errCode + " " + errMsg + "\r\n");
	    } catch (Exception e) {}
    }

    static void DEBUG(String s) 
    {
       if (_DEBUG)
          System.out.println( s );
    }
}
