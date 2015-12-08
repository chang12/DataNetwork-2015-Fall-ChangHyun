import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class PingClient {

	private static final int PING_MESSAGES = 10;
	private static final int TOKEN_TIMESTAMP = 2;
	private static final int MAX_WAIT_TIME = 1000;
	private static boolean[] checkArray;
	private static long[] rttArray;
	private static int receivedNum;
	// private static final int MAX_WAIT_TIME = 10000;
	private static final String CRLF = "\r\n";

	private static InetAddress host;
	private static int portNumber;
	private static DatagramSocket socket;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		// If user doesn't input both port number and ip address, the program
		// will display an error message.
		if (args.length != 2) {
			System.out.println("usage: java PingClient <Host> <Port>");
			// return;
		}

		try {
			if (!args[0].isEmpty()) ;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Host name and ip address please");
			System.exit(1);
		}
		host = InetAddress.getByName(args[0]);

		portNumber = Integer.parseInt(args[1]);

		/************************************************************************************************/
		/************************************************************************************************/
		/*********************************** MAIN PART START !!!!!!! **************************************/
		/************************************************************************************************/
		/************************************************************************************************/

		// Create a datagram socket used for sending and receiving UDP packets
		socket = new DatagramSocket();

		checkArray = new boolean[PING_MESSAGES];
		rttArray = new long[PING_MESSAGES];
		receivedNum = 0;
		for (int i = 0; i < 10; i++) {
			checkArray[i] = false;
			rttArray[i] = 0;
		}

		Timer timer = new Timer();

		// Schedule 10 ping sending tasks(SendJob) and time-out checking tasks(CheckJob) 
		for (int sequence_num = 0; sequence_num < PING_MESSAGES; sequence_num++) {
			
			SendJob sendjob = new SendJob(sequence_num);
			CheckJob checkjob = new CheckJob(sequence_num);
			timer.schedule(sendjob, 1000 * sequence_num);
			timer.schedule(checkjob, 1000 * sequence_num + MAX_WAIT_TIME);
			
		}

		String message = generatePing(-1);

		while (true) {
			DatagramPacket ping_response = new DatagramPacket(
					new byte[message.length()], message.length());
			socket.receive(ping_response);
			printData(ping_response);
		}

		// // Wait for ping response from server
		// try {
		// socket.receive(ping_response);
		// printData(ping_response);
		// } catch (SocketTimeoutException e) {
		// System.out.println("Ping "+sequence_num+" Time out !!!");
		// } catch (Exception e) {
		// // Another unknown error may have occured that can't be handled
		// e.printStackTrace();
		// return;
		// }
	}

	private static String generatePing(int sequence_num) {
		// For getting current date and time
		SimpleDateFormat sdfNow = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String strNow = sdfNow.format(new Date(System.currentTimeMillis()));
		return "PING #" + sequence_num + " " + System.currentTimeMillis()
				+ " (" + strNow + ")";
	}

	// Print ping page to standard output stream
	private static void printData(DatagramPacket request) throws Exception {

		String response = new String(request.getData());
		String[] tokens = response.split(" ");

		// Create sent and received timestamps for RTT
		long sent_timestamp = new Long(tokens[TOKEN_TIMESTAMP]);
		int sequence_num = Integer.parseInt((tokens[1].substring(1)));
		long received_timestamp = System.currentTimeMillis();

		// RTT
		long rtt = received_timestamp - sent_timestamp;
		
		if(rtt < MAX_WAIT_TIME)
		{
			// Increase # ping response 
			receivedNum++;
			// Display results
			System.out.print("PING " + sequence_num+" Received from "
					+ request.getAddress().getHostAddress() + ": " + "RTT=" + rtt
					+ "ms"+CRLF);
			checkArray[sequence_num]=true;
			rttArray[sequence_num]=rtt;
		}
	}

	public static class SendJob extends TimerTask {
		int sequence_num;

		public SendJob(int num) {
			sequence_num = num;
		}

		public void run() {
			String message = generatePing(sequence_num);

			DatagramPacket ping_request = new DatagramPacket(
					message.getBytes(), message.length(), host, portNumber);

			// Send a ping request
			try {
				socket.send(ping_request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Send: PING " + sequence_num);
		}
	}

	public static class CheckJob extends TimerTask {
		int sequence_num;

		public CheckJob(int num) {
			sequence_num = num;
		}

		public void run() {
			if (!checkArray[sequence_num])
				System.out.println("Ping " + sequence_num + " Time out !!!");
			if (sequence_num==PING_MESSAGES-1)
			{
				showStat();
				System.exit(1);
			}
		}
	}
	
	private static void showStat()
	{
		System.out.println();
		System.out.println("Ping statistics for 127.0.0.1:");
		System.out.println("\tPackets: Sent = "+PING_MESSAGES+", "+ "Received = "
				+receivedNum+", Lost = "+(PING_MESSAGES-receivedNum)+" ("
				+(PING_MESSAGES-receivedNum)*100/PING_MESSAGES+"% loss)");
		long min = MAX_WAIT_TIME;
		long max = 0;
		long sum = 0;
		for(int i=0;i<PING_MESSAGES;i++)
		{
			if(rttArray[i]!=0)
			{
				if(min>rttArray[i]) min = rttArray[i];
				if(max<rttArray[i]) max = rttArray[i];
				sum += rttArray[i];
			}
		}
		long avg = sum / receivedNum; 
		
		System.out.println("Approximate round trip times in milli-seconds:");
		System.out.println("\tMinimum = "+min+"ms, Maximum = "+max+"ms, Average = "+avg+"ms");
	}
}
