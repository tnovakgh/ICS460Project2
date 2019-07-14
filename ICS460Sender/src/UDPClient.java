import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress; 

public class UDPClient {
	
	// instantiate constants
	private final int ACK_PORT = 9587;
	
	// declare instance variables
	private int receiverPort;
	private int bufferSize;
	private int timeoutLength;
	private float percentBadPackets;
	private DatagramSocket packetSocket;
	private DatagramSocket ackSocket;
	private InetAddress address;
	private byte[] buf;
	
	// class constructor
	public UDPClient(String address, int portNum, int bufSize, int timeout, float badPackets) {
		try {
			
			// open socket used to send packets
			// (socket to send doesn't need port as parameter)
			this.packetSocket = new DatagramSocket();
			// open socket used to receive acknowledgments
			// (socket to receive does need port as parameter)
			this.ackSocket = new DatagramSocket(ACK_PORT);
			// specify host's IP address
			this.address = InetAddress.getByName(address);
			// specify the port number for receiver to find packets in
			this.receiverPort = portNum;
			// specify buffer/packet size
			this.bufferSize = bufSize;
			// specify how long before client re-sends packet
			this.timeoutLength = timeout;
			// specify percentage of packets to corrupt, delay, or drop
			this.percentBadPackets = badPackets;
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	// will send file using UDP DatagramPackets
	public void send(String file) {
		
		int numPackets = 0;		// packet counter
		
		try {
			
			// open file input stream with file to be read into buffer
			InputStream iStream = new BufferedInputStream(new FileInputStream(file));
			buf = new byte[bufferSize];		// initialize buffer array for data to be read to
			
			// pre-test reads file from input stream and writes BUFFER_SIZE number of bytes to buffer to be sent in packets
			while(iStream.read(buf) != -1) {
				
				// create new packet each iteration to be sent with(packet data, packet length, destination address, destination port)
				DatagramPacket dgPacket = new DatagramPacket(buf, buf.length, address, receiverPort);
				// send packet via open socket
				packetSocket.send(dgPacket);
				
				// added for ack testing
				if(receivedAck()) {
					System.out.println("ack received, packet successfully sent");
				}
				
				// output format [packet #][start byte offset][end byte offset]
				System.out.println(String.format("[%d][%d][%d]", numPackets, numPackets * bufferSize, numPackets * bufferSize + buf.length));
				
				numPackets++;		//increment packet counter
			}
			
			// send termination code
			buf = "stop".getBytes();
			DatagramPacket dgPacket = new DatagramPacket(buf, buf.length, address, receiverPort);
			packetSocket.send(dgPacket);
			
			// close the open socket and the input stream
			packetSocket.close();
			ackSocket.close();
			iStream.close();
			
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	// added for ack testing
	public boolean receivedAck() {
		
		DatagramPacket acknowledged = new DatagramPacket(buf, buf.length);
		
		try {
			ackSocket.receive(acknowledged);
			
			if(new String(acknowledged.getData(), 0, acknowledged.getLength()).trim().equals("ack")){
				return true;			// negates loop's pre-test condition
			}
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	public static void main(String[] args) {
		
		// make sure that a file(parameter) has been passed
		// if not, program terminates
		if(args.length < 1) {
			System.out.println("Please provide a file to send... TERMINATING");
			System.exit(0);
		}
		
		UDPClient client = new UDPClient();		// create client object
		client.send(args[0]);					// begin process of sending file
		
	}*/
	
}
