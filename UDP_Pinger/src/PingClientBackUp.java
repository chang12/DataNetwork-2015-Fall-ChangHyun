import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PingClientBackUp
{
    private static final int PING_MESSAGES = 10;
    private static final int TOKEN_TIMESTAMP = 2;
    private static final int MAX_WAIT_TIME = 1000;
//    private static final int MAX_WAIT_TIME = 10000;
    private static final String CRLF = "\r\n";

    public static void main(String[] args) throws Exception
    {
        // If user doesn't input both port number and ip address, the program will display an error message.
        if (args.length != 2)
        {
            System.out.println("usage: java PingClient <Host> <Port>");
            //return;
        }
        
        try
        {
            if(!args[0].isEmpty())
                System.out.println("\nHost: "+args[1]+"\nIP address: "+args[0]+"\n");
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Host name and ip address please");
            System.exit(1);
        }
        InetAddress host = InetAddress.getByName(args[0]);
        
        int portNumber = Integer.parseInt(args[1]);
        
        //Create a datagram socket used for sending and receiving UDP packets
        DatagramSocket socket = new DatagramSocket();

        //Set up the maximum time the socket waits for responses
        socket.setSoTimeout(MAX_WAIT_TIME);

        //Construct a ping message to be sent to the Server
        for (int sequence_num = 0; sequence_num < PING_MESSAGES; sequence_num++)
        {
                String message = generatePing(sequence_num);

                DatagramPacket ping_request =
                    new DatagramPacket(message.getBytes(), message.length(), host, portNumber);

                //Send a ping request
                socket.send(ping_request);

                //Datagram packet to hold server response
                DatagramPacket ping_response =
                    new DatagramPacket(new byte[message.length()], message.length());

                //Wait for ping response from server
                try
                {
                        socket.receive(ping_response);
                        printData(ping_response);
                }
                catch (SocketTimeoutException e)
                {
                        System.out.println("No response was received from the server");
                }
                catch (Exception e)
                {
                        //Another unknown error may have occured that can't be handled
                        e.printStackTrace();
                        return;
                }
        }
    }

    private static String generatePing(int sequence_num)
    {
        // For getting current date and time 
        SimpleDateFormat sdfNow = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String strNow = sdfNow.format(new Date(System.currentTimeMillis()));
        return "PING #" + sequence_num + " " + System.currentTimeMillis() + " ("+strNow+")";
    }

    //Print ping page to standard output stream
    private static void printData(DatagramPacket request) throws Exception
    {
        String response = new String(request.getData());
        String[] tokens = response.split(" ");
        
        //Create sent and received timestamps for RTT
        long sent_timestamp = new Long(tokens[TOKEN_TIMESTAMP]);
        long received_timestamp = System.currentTimeMillis();

        //RTT
        long rtt = received_timestamp - sent_timestamp;

        //Display results
        System.out.print(response+" Received from "+
                request.getAddress().getHostAddress() + " "+"(RTT=" + rtt + "ms)"+CRLF);
    }
}