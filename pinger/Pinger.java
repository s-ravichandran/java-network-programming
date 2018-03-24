import java.io.*;
import java.net.*;
import java.util.Random;

class Pinger 
{
	public static byte[] intToByte(int a)
	{
		byte[] seq = new byte[4];
		seq[0] = (byte)((a >> 24) & 0xff);
		seq[1] = (byte)((a >> 16) & 0xff);
		seq[2] = (byte)((a >> 8) & 0xff);
		seq[3] = (byte)((a >> 0) & 0xff);
		return seq;
	}

	public static int byteToInt(byte[] seq)
	{
		if (seq == null || seq.length != 4) 
			return 0x0;
		return (int)((0xff & seq[0]) << 24 |
				(0xff & seq[1]) << 16 |
				(0xff & seq[2]) << 8 |
				(0xff & seq[3]) << 0
				);
	}

	public static byte[] longToByte(long timestamp)
	{
		byte[] time = new byte[12];
		time[0] = (byte)((timestamp >> 56) & 0xff);
		time[1] = (byte)((timestamp >> 48) & 0xff);
		time[2]	= (byte)((timestamp >> 40) & 0xff);
		time[3]	= (byte)((timestamp >> 32) & 0xff);
		time[4]	= (byte)((timestamp >> 24) & 0xff);
		time[5]	= (byte)((timestamp >> 16) & 0xff);
		time[6]	= (byte)((timestamp >> 8) & 0xff);
		time[7]	= (byte)((timestamp >> 0) & 0xff);
		return time;
	}

	public static long byteToLong(byte[] time)
	{
		 if (time == null || time.length != 8) 
		 	return 0x0;
		 return (long)(
					(long)(0xff & time[0]) << 56 |
					(long)(0xff & time[1]) << 48 |
					(long)(0xff & time[2]) << 40 |
					(long)(0xff & time[3]) << 32 |
					(long)(0xff & time[4]) << 24 |
					(long)(0xff & time[5]) << 16 |
					(long)(0xff & time[6]) << 8 |
					(long)(0xff & time[7]) << 0
					);
	}

	public static void main(String[] args) 	throws Exception
	{
		if((args.length == 8)&&(args[0].equals("-l"))&&(args[2].equals("-h"))&&(args[4].equals("-r"))&&(args[6].equals("-c"))) 		//Client Mode with correct options
		{
			int localPort = Integer.parseInt(args[1]);							//Client's local port number
			int num_sent=0, num_recv=0;									//number of packets sent/received
			long total_rtt=0, avg_rtt=0, max_rtt=0, min_rtt=100000;						//RTTs min_rtt is set to arbitrarily large value
			String remoteName = args[3];									//Name of remote server. (IP)
			InetAddress remoteHost = InetAddress.getByName(remoteName);		
			int remotePort = Integer.parseInt(args[5]);							//Port number of server process
			int packCount = Integer.parseInt(args[7]);							//Number of packets to be sent
			DatagramSocket clientSocket = new DatagramSocket(localPort);					//DGram Socket at client
			Random rand = new Random();
			int init_seq_num = rand.nextInt(Integer.MAX_VALUE);						//Generate Random number for ISS.
			for (int i=0; i<packCount; i++)
			{
				byte[] sent = new byte[12];
				byte[] seq = new byte[4];
				byte[] time = new byte[8];
				byte[] recv_seq = new byte[4];
				byte[] recv_time = new byte[8];
				byte[] recv = new byte[12];
				int seq_num = init_seq_num + i;
				long timestamp = System.currentTimeMillis();
				seq = intToByte(seq_num);
				time = longToByte(timestamp);
				for(int j=0;j<4;j++)
				{
					sent[j] = seq[j];
				}
				for(int k=4;k<12;k++)
				{
						sent[k] = time[k-4];
				}
				DatagramPacket tx = new DatagramPacket(sent, sent.length,remoteHost, remotePort);
				try
				{
					clientSocket.send(tx);
					num_sent++;
				}catch(Exception e) {e.printStackTrace();}
				DatagramPacket rx = new DatagramPacket(recv, recv.length);
				try
				{
					clientSocket.receive(rx);
					num_recv++;
				}catch(Exception e) {e.printStackTrace();}

				for(int j=0;j<4;j++)
				{
					recv_seq[j] = recv[j];
				}

				for(int k=4;k<12;k++)
				{
					recv_time[k-4] = recv[k];
				}

				long rtt = (System.currentTimeMillis()-byteToLong(recv_time));
				System.out.println("size="+sent.length+" from="+remoteHost.getHostAddress()
						+" seq="+byteToInt(recv_seq)+" rtt="+rtt+"ms");
				
				if(rtt <= min_rtt)
					min_rtt = rtt;
				if(rtt > max_rtt)
					max_rtt = rtt;
				total_rtt+=rtt;
				//Sleep for a second
				try {
    				Thread.sleep(1000);                 
				} catch(InterruptedException ex) {
    				Thread.currentThread().interrupt();
				}
			}
			clientSocket.close();
			avg_rtt = total_rtt/(long)(packCount);
			System.out.println("sent=" + num_sent +" received=" + num_recv 
				+" lost="+((num_sent-num_recv)/packCount)*100 + "% rtt min/avg/max="+
				min_rtt + "/" + avg_rtt + "/" + max_rtt +" ms");
		}
		else if((args.length == 2)&&(args[0].equals("-l")))			//Server mode with correct options
		{
			int localPort = Integer.parseInt(args[1]);
			DatagramSocket serverSocket = new DatagramSocket(localPort);
			byte[] recv = new byte[12];
			byte[] seq = new byte[4];
			byte[] time = new byte[8];
			while(true)
			{
				DatagramPacket recv_packet = new DatagramPacket(recv, recv.length);
				try
				{
					serverSocket.receive(recv_packet);
				}catch(Exception e) {e.printStackTrace();}
				for(int j=0;j<4;j++)
				{
					seq[j] = recv[j];
				}

				for(int k=4;k<12;k++)
				{
					time[k-4] = recv[k];
				}
				System.out.println("time=" + byteToLong(time) + 
					" from=" + recv_packet.getAddress().toString().substring(1) + " seq="+byteToInt(seq));
				DatagramPacket send_packet = new DatagramPacket(recv, recv.length, 
					recv_packet.getAddress(), recv_packet.getPort());
				try
				{
					serverSocket.send(send_packet);
				}catch(Exception e) {e.printStackTrace();}
			}
		}
		else
		{
			System.out.println("Error: missing or additional arguments");				// Change to correct error message
		}
	}
}
