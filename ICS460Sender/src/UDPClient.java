// sender 2 in my projects **added as test**

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer; 

public class UDPClient {
	
	// Yuan
	// output string constants
	private final String SEND_FIRST_MESSAGE = "SENDing ";
	private final String RESEND_MESSAGE = "ReSend. ";
	private final String SENT = "SENT";
	private final String DROP = "DROP";
	private final String ERR= "ERR";
	private final String ACK_RCVD = "AckRcvd";
	private final String DUPL_ACK = "DuplAck";
	private final String ERR_ACK = "ErrAck";
	private final String MOVE_WINDOW = "MoveWnd";
	private final String TIMEOUT = "TimeOut";
	
	// instantiate constants
	private final int ACK_PORT = 9587;
	private final int HEADER_SIZE = 12;
	
	// declare instance variables
	private int receiverPort;
	private int bufferSize;
	private int timeoutLength;
	private int window = 1;
	private float percentBadPackets;
	private DatagramSocket packetSocket;
	private DatagramSocket ackSocket;
	private InetAddress address;
	private byte[] buf;
	
	private byte[] fileBuffer;		// used for file data
	private byte[] stopBuffer;		// used for stop message
	private byte[] ackBuffer;		// used for ack packet
	private byte[] headerBuffer;	// used for header data
	private byte[] packetBuffer;	// used for entire packet
	
	private byte[] acknoHeader = new byte[2];		// used for ackno header
	
	
	private short chksum = 0;
	private short len = HEADER_SIZE;
	private int ackno = 1;
	private int seqno = 0;
	
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
			fileBuffer = new byte[bufferSize];		// initialize buffer array for data to be read to
			stopBuffer = new byte[bufferSize];		// initialize buffer array for stop message
			ackBuffer = new byte[bufferSize];		// initialize buffer array for ack data
			headerBuffer = new byte[HEADER_SIZE];	// initialize buffer array for header data
			packetBuffer = new byte[headerBuffer.length + fileBuffer.length];	// initialize buffer array for full packet
			
			// set timeout for socket
			//packetSocket.setSoTimeout(timeoutLength);
			
			// pre-test reads file from input stream and writes BUFFER_SIZE number of bytes to buffer to be sent in packets
			while(iStream.read(fileBuffer) != -1) {
				
				len = (short) (HEADER_SIZE + fileBuffer.length);
				boolean dropped = false;
				boolean delayed = false;
				
				// decides if packet is corrupt, delayed, dropped, etc?
				Random rng = new Random();
				int chance = rng.nextInt((int)(100));
				
				// corrupt packet
				if(chance < percentBadPackets / 3) {
					//chksum = 1;		// 1 = bad packet
				}
				// drop packet
				else if(chance < percentBadPackets && chance % 3 == 0) {
					//dropped = true;	// set drop condition
				}
				// delay packet
				else if(chance < percentBadPackets && chance % 4 == 0) {
					//delayed = true;	// set delayed condition
				}
				
				headerBuffer = createHeader();
				
				addHeader();
				// for testing
				System.out.println(Arrays.toString(packetBuffer));
				
				// set boolean loop tester
				//boolean socketTimedOut = true;
				
				// create new packet each iteration to be sent with(packet data, packet length, destination address, destination port)
				DatagramPacket dgPacket = new DatagramPacket(packetBuffer, packetBuffer.length, address, receiverPort);
					
				// send packet via open socket
				packetSocket.send(dgPacket);
						
				// if ack comes back in time
				if(receivedAck()) {
					System.out.println("ack received, packet successfully sent");
				}
				
				// output format [packet #][start byte offset][end byte offset]
				System.out.println(String.format("[%d][%d][%d]", numPackets, numPackets * bufferSize, numPackets * bufferSize + fileBuffer.length));
				
				numPackets++;		//increment packet counter
			}
			
			// for testing
			System.out.println(Arrays.toString(headerBuffer));
			
			// send termination code
			stopBuffer = "stop".getBytes();
			DatagramPacket dgPacket = new DatagramPacket(stopBuffer, stopBuffer.length, address, receiverPort);
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
		
		DatagramPacket acknowledged = new DatagramPacket(ackBuffer, ackBuffer.length);
		
		try {
			ackSocket.receive(acknowledged);
			
			segmentAckPacketArray(acknowledged);
			System.out.println(Arrays.toString(acknoHeader));
			
			if(new String(acknowledged.getData(), 0, acknowledged.getLength()).trim().equals("ack")){
				return true;			// negates loop's pre-test condition
			}
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void segmentAckPacketArray(DatagramPacket packet) {
		byte[] buffer = packet.getData();
		acknoHeader = Arrays.copyOfRange(buffer, 4, 8);
		
	}
	
	public byte[] createHeader() {
		ByteBuffer buf = ByteBuffer.allocate(HEADER_SIZE);
		
		// create and add chksum header
		byte[] chksumHeader = new byte[2];
		ByteBuffer chksumBuf = ByteBuffer.wrap(chksumHeader);
		chksumBuf.putShort(chksum);
		chksumBuf.rewind();
		// create and add chksum header
		byte[] lenHeader = new byte[2];
		ByteBuffer lenBuf = ByteBuffer.wrap(lenHeader);
		lenBuf.putShort(len);
		lenBuf.rewind();
		// create and add chksum header
		byte[] acknoHeader = new byte[4];
		ByteBuffer acknoBuf = ByteBuffer.wrap(acknoHeader);
		acknoBuf.putInt(ackno);
		acknoBuf.rewind();
		// create and add chksum header
		byte[] seqnoHeader = new byte[4];
		ByteBuffer seqnoBuf = ByteBuffer.wrap(seqnoHeader);
		seqnoBuf.putInt(seqno);
		seqnoBuf.rewind();
		
		// add chksum header to full header
		buf.put(chksumHeader);
		// add len header to full header
		buf.put(lenHeader);
		// add ackno header to full header
		buf.put(acknoHeader);
		// add seqno header to full header
		buf.put(seqnoHeader);
		
		
		buf.rewind();
		
		return buf.array();
		
	}
	
	public void addHeader() {
		ByteBuffer buf = ByteBuffer.wrap(packetBuffer);
		
		buf.put(headerBuffer);
		buf.put(fileBuffer);
	}
	
}
