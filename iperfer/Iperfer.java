import java.io.*;
import java.net.*;

class Iperfer
{
	public static void main(String[] args)	throws Exception
	{
		if(args.length == 7)	// Client Mode
		{
			int serverPort = Integer.parseInt(args[4]);
			String serverHost = args[2];
			int time = Integer.parseInt(args[6]);
			long num_sent = 0;
			Socket client = new Socket(serverHost, serverPort);
			DataOutputStream dos = new DataOutputStream(client.getOutputStream());
			long deadline = System.currentTimeMillis() + (time * 1000);
			while(deadline - System.currentTimeMillis() > 0)
			{
				byte[] data = new byte[1024];		
				dos.write(data);
				dos.flush();
				num_sent+=1024;					//Counts the number of  bytes sent
			}
			client.close();
			num_sent = num_sent/1024;				//Convert bytes to KB
			float rate = num_sent/time;
			rate = rate/1024;		// KBps to MBps
			rate = rate*8;			// MBps to Mbps
			System.out.println("sent="+num_sent+" KB rate="+rate+" Mbps");
		}
		else if(args.length == 3)	// Server Mode
		{
			int serverPort = Integer.parseInt(args[2]);
			ServerSocket serverSocket = new ServerSocket(serverPort);
			long num_bytes=0;
			while(true)
			{
				Socket clientSocket = serverSocket.accept();
				DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
				long startTime = System.currentTimeMillis();
				int num_read = 1;
				while(true)
				{
					byte[] data = new byte[1024];
					if((num_read = dis.read(data))!=-1)
						num_bytes+=num_read;		//Counts the total number of bytes received.
					else
						break;
				}
				long time = System.currentTimeMillis()-startTime;
				num_bytes = num_bytes/1024;			//Convert bytes to KB
				float rate = num_bytes/time;
				rate = rate * 1000;		// compensate for ms to s
				rate = rate/1024;		// KBps to MBps
				rate = rate*8;			// MBps to Mbps
				System.out.println("received="+num_bytes+" KB rate="+rate+" Mbps");
				num_bytes=0;
			}
		}
	}
}
