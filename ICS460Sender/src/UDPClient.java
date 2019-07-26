// sender 2 in git ** works properly **  **TEST COMMITT ***

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.Random; 

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
	
	
	private short chksum = 0;
	private short len = HEADER_SIZE;
	private int ackno = 1;
	private int seqno = 0;
	//private boolean dropped = true;
	private String condition = "";
	Date date = new Date();
	int numPackets = 0;		// packet counter

	
	
	
	
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
	public void sendFile(String file) {
		
		
		try {
			
			// open file input stream with file to be read into buffer
			InputStream iStream = new BufferedInputStream(new FileInputStream(file));
			
			buf = new byte[bufferSize];		// initialize buffer array for data to be read to
			fileBuffer = new byte[bufferSize];		// initialize buffer array for data to be read to
			stopBuffer = new byte[bufferSize];		// initialize buffer array for stop message
			ackBuffer = new byte[bufferSize];		// initialize buffer array for ack data
			headerBuffer = new byte[HEADER_SIZE];	// initialize buffer array for header data
			
			packetBuffer = new byte[headerBuffer.length + fileBuffer.length];	// add + HEADER_SIZE
			
			
			// pre-test reads file from input stream and writes BUFFER_SIZE number of bytes to buffer to be sent in packets
			while(iStream.read(fileBuffer) != -1) {
				
				
				
				// for testing
				//System.out.println(Arrays.toString(packetBuffer));
				
				
				
				
				// create new packet each iteration to be sent with(packet data, packet length, destination address, destination port)
//				headerBuffer = createHeader();
//				addHeader();
//				DatagramPacket dgPacket = new DatagramPacket(packetBuffer, packetBuffer.length, address, receiverPort);
				
				
				
				//This will take good packet and return a packet that might be corrupted or dropped
				//so we need to figure out where this needs to take place whether it is in the send function or before
				headerBuffer = createHeader();
				addHeader();
				DatagramPacket dgPacketWithCondition = new DatagramPacket(packetBuffer, packetBuffer.length, address, receiverPort);
				
		
				
				// send packet via open socket //I think this the best solution to stop a dropped packet
				if (condition != DROP) { 
//					packetSocket.send(dgPacketWithCondition); //This could be the best place to put the error chance but we need to know what the error is too
					packetSocket.send(dgPacket);
					
//					packetSocket.send(dgPacket);
					
				}	//															   Sequence#   startByte	           EndByte	 ALSO NEEDS TIMESTAMP AND CONDITION
					Date packetDateSent = new Date();
					long time = packetDateSent.getTime();
					System.out.println(String.format("%s %d %d:%d %s","SENDing", seqno, numPackets * bufferSize, numPackets * bufferSize + fileBuffer.length,time,condition));
//					System.out.println("header buffer being sent- " + Arrays.toString(headerBuffer));//This just shows the header so we can check if corrupt
					condition = SENT;
				
		
				
				//This is the response for when no acknowledgement is received. This occurs after timeout or Corrupt Ack.
				while (receivedAck() == false) { 
					
					
					
					//Run Proxy again because Resend can also be corrupt of dropped
					headerBuffer = createHeader();
					addHeader();
					DatagramPacket dgPacketResendWithCondition = new DatagramPacket(packetBuffer, packetBuffer.length, address, receiverPort);
					
					
					//Send non dropped packets
					if (condition != DROP) { 
					//	packetSocket.send(dgPacketResendWithCondition); //This could be the best place to put the error chance but we need to know what the error is too
					}
					
					//Creating array to show header
				//	System.out.println("header buffer being ReSent- " + Arrays.toString(headerBuffer));//This just shows the header so we can check if corrupt

					
					//Displaying 
//					System.out.println("Should be header of ReSend Packet" + Arrays.toString(bb.array()));
//
//					byte oneTest = 1;
//					
//					System.out.println("dgPacketResendWithCondition[1] " + test[1]);
//					dgPacket.setData(data);
//					packetSocket.send(dgPacket);
					Date packetDateReSend = new Date();

					long timeResend = packetDateReSend.getTime();
					//System.out.println(String.format("[%s][%d][%d][%d]","ReSend", numPackets, numPackets * bufferSize, numPackets * bufferSize + fileBuffer.length,timeResend,condition));
//					System.out.println("header buffer - " + Arrays.toString(packetBuffer));
//					System.out.println("dgPacket data" + dgPacket.getDa);
					System.out.println(String.format("%s %d %d:%d %s","ReSend.", seqno, numPackets * bufferSize, numPackets * bufferSize + fileBuffer.length,timeResend,condition));

					condition = SENT;
				}
				
				//Happens once Good Ack comes back
				//System.out.println("ack received, packet successfully sent" + "\n");
				
				//increases What packet we are sending
				numPackets++;		//increment packet counter
				seqno++;
				ackno++;

				
				

				
			}
			
			// for testing
			//System.out.println("packetBuffer: " + Arrays.toString(packetBuffer) + "\n");
			
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
	
	
	
	public short proxy() {
		
		
		
		
		//creates random number
		Random rng = new Random();
		int chance = rng.nextInt((int)(100));
		System.out.println("Chance is: " + chance);
		
		
		int badPackets = (int) (percentBadPackets * 100);
		int badPacketsPercentage = badPackets / 2;
		
		// corrupt packet
		if(chance < badPacketsPercentage) {
			condition = ERR;
			return 1;

		}
		// drop packet
		else if(chance > 100 - badPacketsPercentage) {
			condition = DROP;
			return 0;
		}
		
		
		return 0;
		
	}
	
	// added for ack testing
	//Need lots of Daylns help here
	//Need to figure out how to do Timeout correctly
	//I dont understand the timing of this.  When does it start? When does it end?
	//Also need to test here for corruption
	public boolean receivedAck() {
		
		DatagramPacket acknowledged = new DatagramPacket(ackBuffer, ackBuffer.length);
		
		try {
			ackSocket.setSoTimeout(timeoutLength);
		//	System.out.println("Trying to receive ack");

			ackSocket.receive(acknowledged);
			ackSocket.setSoTimeout(0);


			byte[] test = acknowledged.getData();
//			ByteBuffer acknoBuffer = ByteBuffer.wrap(test,4,4);
//			int tempAckno = acknoBuffer.getInt();
//			System.out.println("tempAckno: " + tempAckno);
			ByteBuffer chksumBufferTest = ByteBuffer.wrap(test,0,2);
			short tempChksum = chksumBufferTest.getShort();
			
			
			
		//	System.out.println("ack was received");
		//	System.out.println("ack chksum received: " + tempChksum);
			
			if (tempChksum == 1 ) {
				System.out.println("AckRcvd " + seqno + " ErrAck.");
				return false;
			}
			
			//Getting bytes to test
			byte[] acknoTest = acknowledged.getData();
			
			ByteBuffer acknoBuffer = ByteBuffer.wrap(test,4,4);
			int tempAckno = acknoBuffer.getInt();
		//	System.out.println("tempAckno: " + tempAckno);
		//	System.out.println("numPackets: " + seqno);

			
			
			if(tempAckno == seqno+1){
				System.out.println("AckRcvd " + seqno + " MoveWnd");
				return true;			// negates loop's pre-test condition
			}
			
		}catch(IOException e) {
			if(e instanceof SocketTimeoutException) {
				System.out.println("TimeOut " + seqno);
				return false;
			}
//			e.printStackTrace();
		}
		
		return false;
	}
	
	
	public byte[] createHeader() { //partially dont know whats going on
		ByteBuffer buf = ByteBuffer.allocate(HEADER_SIZE);
		
		// create and add chksum header
		byte[] chksumHeader = new byte[2];
		ByteBuffer chksumBuf = ByteBuffer.wrap(chksumHeader);
		chksumBuf.putShort(proxy());
//		chksumBuf.putShort((short)1);

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
	
	public void addHeader() {//idk whats going on //Just modifying global variables
		ByteBuffer buf = ByteBuffer.wrap(packetBuffer);
		
		buf.put(headerBuffer);
		buf.put(fileBuffer);
	}
	
}