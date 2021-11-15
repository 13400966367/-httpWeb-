/*
 *
 *  client for TCPClient from Kurose and Ross
 *
 *  * Usage: java TCPClient [server addr] [server port]
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class TCPClient {


    private static ClientThread[] threads;
    private static int filenum = 0;
    private static int bytenum = 0;
    private static long waittime = 0;

    public static void statistics(int fn,int bn,long wt){
        filenum += fn;
        bytenum += bn;
        waittime += wt;
    }
	public static void main(String[] args) throws Exception {

		// get server address
        String server = "127.0.0.1";
        if (args.length >= 1)
		   { server = args[0];}

		String serverName = "localhost";
		if (args.length >= 2)
		   { serverName = args[1];}
		// InetAddress serverIPAddress = InetAddress.getByName(serverName);

		// get server port
		int serverPort = 3912;
		if (args.length >= 3)
		    {serverPort = Integer.parseInt(args[2]);}
        
        // get # of threads
        int parallel = 1;
        if (args.length >= 4)
		    {parallel = Integer.parseInt(args[3]);}

        // get filename
        String fileName = "1.txt";
		if (args.length >= 5)
		    {fileName = args[4];}
        
        // get T
        int T = 1000;
		if (args.length >= 6)
		    {T = Integer.parseInt(args[5]);}

        
        try{
            threads = new ClientThread[parallel];
            for (int i = 0; i < parallel; i++) {
                threads[i] = new ClientThread(server,serverName,serverPort,fileName,T); 
                threads[i].start();
            }
        }catch(Exception e){}
        Thread.currentThread().sleep(2 * T);//休眠一下让进程执行完毕
        System.out.println("the total transaction throughput: " + filenum * 1000 / T + " file/s");
        System.out.println("data rate throughput: " + bytenum * 8 * 1000 / T + " bps");
        if(filenum == 0)
        {
            System.out.println("the average of wait time: ∞ ms");
        }
        else
            System.out.println("the average of wait time: " + waittime / filenum + " ms");
        
	} // end of main

} // end of class TCPClient