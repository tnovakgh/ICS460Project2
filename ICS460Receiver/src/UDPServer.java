// receiver 2 in my projects

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class UDPServer {
	
	private final String RECV = "RECV";
	private final String DUPL = "DUPL";
	private final String CRPT = "CRPT";
	private final String SEQ_ERR = "!Seq";//WONT BE USED
	private final String ACKKNOWLEDGE = "ACK";
	private final String SENT = "SENT";
	private final String ERR = "ERR";
	private final String DROP = "DROP";
	private final String TIMEOUT = "TimeOut";//Server cant timeout.  Doesnt Make sense
	private final String SEND_MESSAGE = "SENDing";
	
	// instantiate constants
	private final int ACK_PORT = 9587;
	private final int BUFFER_SIZE = 100;
	private final int HEADER_SIZE = 12;
	private final int ACK_SIZE = 8;
	
	// declare instance variables
	private byte[] buf;
	private boolean running;
	private int receiverPort;
	private DatagramSocket packetSocket;
	private DatagramSocket ackSocket;
	private byte[] header;
	private byte[] fileData;
	private byte[] acknoHeader = new byte[2];		// used for ackno header
	private byte[] ackPacket;		// used for ack packet
	private String receivedPacketCondition = RECV;
	private String ackPacketCondition = SENT;
	private float badPackets;

	
	private short chksum = 0;
	private short len = ACK_SIZE;
	private int ackno = 1;
	private int seqno = 0;
	private boolean isDuplicate;
	private int currentWrite = -1;
	
	
	// added for ack testing
	private InetAddress address;
	private boolean goodPacket = true;
	
	// class constructor
	public UDPServer(String address, int portNum, float badPackets) {
		try {
			
			// open socket to receive packets
			// (socket to receive does need port as parameter)
			this.packetSocket = new DatagramSocket(portNum);
			
			// open socket used to receive acknowledgments\             //This says receive but I think its suppposed to be send
			// (socket to send doesn't need port as parameter)
			this.ackSocket = new DatagramSocket();
			
			// specify host's IP address
			this.address = InetAddress.getByName(address);
			
			// specify the port number for receiver to find packets in
			this.receiverPort = portNum;
			
			this.badPackets = badPackets;
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void receive() {
		
		int numPackets = 0;		// packet counter
		running = true;			// keeps program running until file has been fully sent
		
		// open byte output stream to allow file to be compiled when received
		ByteArrayOutputStream byteOStream = new ByteArrayOutputStream();
		
		// add + HEADER_SIZE when ready
		buf = new byte[BUFFER_SIZE + HEADER_SIZE];		// initialize buffer array for incoming data to be held
		header = new byte[HEADER_SIZE];		// initialize buffer array for header data
		fileData = new byte[BUFFER_SIZE];	// initialize buffer array for file data
		ackPacket = new byte[ACK_SIZE];
		
		// pre-test continues until termination code has been received
		while(running) {
			try {
				
				// create packet to hold packet object being received
				DatagramPacket dgPacket = new DatagramPacket(buf, buf.length);
				
				// receive incoming packet
				packetSocket.receive(dgPacket);
				Date dateReceived = new Date();

				long timeReceived = dateReceived.getTime();

			//	System.out.println("Receiving packet...");
				
				
				segmentPacketArray(dgPacket);
//				System.out.println("header buffer being received- " + Arrays.toString(header));//This just shows the header so we can check if corrupt

				
				//Getting Bytes so we can run tests
				byte[] test = dgPacket.getData();
				
				//Create chksum
				ByteBuffer chksumBuffer = ByteBuffer.wrap(test,0,2);
				short chksum = chksumBuffer.getShort();
				
				//creating the ackno
				ByteBuffer acknoBuffer = ByteBuffer.wrap(test,4,4);
				int tempAckno = acknoBuffer.getInt();
//				System.out.println("tempAckno: " + tempAckno);
				
				
				//creating the seqno
				ByteBuffer seqnoBuffer = ByteBuffer.wrap(test,8,4);
				seqno = seqnoBuffer.getInt();
//				System.out.println("ByteBuffer.array(): " + Arrays.toString(seqnoBuffer.array()));
//				System.out.println("seqno: " + seqno);
				
				//Printing Corruption
//				System.out.println("chksum: " + test[1] + "\n");

				//Testing if packet is corrupt //This comes last because Corrupt files dont care if there is a duplicate because seqno could be affected
				//Testing if packet is a duplicate
				//Setting Ackno to Next Packet needed regardless of duplicate //no need to reset seqno because we update it when we receive our packet. Not necessary to keep value
				if (chksum == 1) {    					
					//goodPacket = false;
					receivedPacketCondition = CRPT;
					if (tempAckno == ackno) {
						isDuplicate = true;
						System.out.println(DUPL + " " + timeReceived + " " + seqno + " " + receivedPacketCondition);

					} else {
						isDuplicate = false;

						System.out.println(RECV + " " + timeReceived + " " + seqno + " " + receivedPacketCondition);
					}
					this.ackno = tempAckno;

				} else {
					receivedPacketCondition = RECV;
					
					if (tempAckno == ackno) {
						isDuplicate = true;

						//receivedPacketCondition = DUPL;
						System.out.println(DUPL + " " + timeReceived + " " + seqno + " " + receivedPacketCondition);

					} else {
						isDuplicate = false;

						System.out.println(RECV + " " + timeReceived + " " + seqno + " " + receivedPacketCondition);

					}
					this.ackno = tempAckno;
//					System.out.println();
					acknowledge();
					
					// appends packet's data to output stream
					//byteOStream.write(fileData);
					
					numPackets++;						// increments packet counter
					
					// check if termination code has been received
					// if so, negate loop's pre-test condition
					// (converts data to String, checks if equal to termination code)
					if(new String(dgPacket.getData(), 0, dgPacket.getLength()).trim().equals("stop")){
						running = false;
						byteOStream.close();// negates loop's pre-test condition
					}
					// appends packet's data to output stream
					if (seqno > currentWrite) {
						byteOStream.write(fileData);
						currentWrite = seqno;

					}
				}
				
				goodPacket = true;

				
				
				goodPacket = true;

				
				
				// packet trimming happens here
				segmentPacketArray(dgPacket);
				
				
				
				
				//System.out.println("This is the header" + Arrays.toString(header));    Just tests
				
				
				
				
				
				//System.out.println("This is the file data" + Arrays.toString(fileData));

				// for testing purposes
				//System.out.println(Arrays.toString(header));

				//System.out.println(Arrays.toString(fileData));

				
				// implement when ready
				//byteOStream.write(trimData(dgPacket));
				
			
				
				// output format [packet #][start byte offset][end byte offset]
//				System.out.println(String.format("[%d][%d][%d]", seqno-1, (seqno-1) * BUFFER_SIZE, (seqno-1) * BUFFER_SIZE + fileData.length) + "possible end\n");
				
				
				buf = new byte[BUFFER_SIZE + HEADER_SIZE];		// clears buffer
				
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		writeFileToDisk(byteOStream);
		
		// close socket and byte output stream
		try {
			packetSocket.close();
			ackSocket.close();
			byteOStream.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	
	}
	
	public void writeFileToDisk(ByteArrayOutputStream byteOStream) {
		// write compiled packets(file) to system
		try {
			
			// compile byte output stream
			byte[] file = byteOStream.toByteArray();
					
			// open file output stream that will write file to system
			OutputStream oStream = new BufferedOutputStream(new FileOutputStream("heart.jpg"));
					
			// write file to system
			oStream.write(file);
					
			// close file output stream
			oStream.close();
					
				
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	// just testing so far
	public void acknowledge() {
		
		try {
			createAckPacket();
			DatagramPacket acknowledge = new DatagramPacket(ackPacket, ackPacket.length, address, ACK_PORT);
			Date ackDateSent = new Date();

			long ackSendTime = ackDateSent.getTime();
			if (ackPacketCondition != DROP) {
//				ackSocket.send(acknowledgeWithCondition);
				ackSocket.send(acknowledge);
				
//				System.out.println("Ack being sent- " + Arrays.toString(ackPacket));//This just shows the header so we can check if corrupt

//				System.out.println("chksum being sent to Client: " + ackPacket[1]);
//				System.out.println("ackno being sent to client: " + ackPacket[7]);
//				System.out.println("Sending Ack to Client...\n");
			}
			
			if (isDuplicate) {
				System.out.println("ReSend. ACK " + seqno + " " + ackSendTime + " " + ackPacketCondition);

			} else {
				System.out.println("SENDing ACK " + seqno + " " +ackSendTime + " " + ackPacketCondition);

			}
			ackPacketCondition = SENT;
		
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void createAckPacket() {
		ByteBuffer buf = ByteBuffer.wrap(ackPacket);
		
		// create and add chksum header
		byte[] chksumHeader = new byte[2];
		ByteBuffer chksumBuf = ByteBuffer.wrap(chksumHeader);
		chksumBuf.putShort(chksum);
		chksumBuf.rewind();
		// create and add len header
		byte[] lenHeader = new byte[2];
		ByteBuffer lenBuf = ByteBuffer.wrap(lenHeader);
		lenBuf.putShort(len);
		lenBuf.rewind();
		// create and add ackno header
		byte[] acknoHeader = new byte[4];
		ByteBuffer acknoBuf = ByteBuffer.wrap(acknoHeader);
		acknoBuf.putInt(ackno);
		acknoBuf.rewind();
		
		// add chksum header to full header
		buf.put(chksumHeader);
		// add len header to full header
		buf.put(lenHeader);
		// add ackno header to full header
		buf.put(acknoHeader);
		
		buf.rewind();
		
	}
	
	
	
	
	public void createAckPacket() {
		ByteBuffer buf = ByteBuffer.wrap(ackPacket);
		
		// create and add chksum header
		byte[] chksumHeader = new byte[2];
		ByteBuffer chksumBuf = ByteBuffer.wrap(chksumHeader);
		chksumBuf.putShort(proxy());
		chksumBuf.rewind();
		
		// create and add len header
		byte[] lenHeader = new byte[2];
		ByteBuffer lenBuf = ByteBuffer.wrap(lenHeader);
		lenBuf.putShort(len);
		lenBuf.rewind();
		
		// create and add ackno header
		byte[] acknoHeader = new byte[4];
		ByteBuffer acknoBuf = ByteBuffer.wrap(acknoHeader);
		acknoBuf.putInt(ackno);
		acknoBuf.rewind();
		
		// add chksum header to full header
		buf.put(chksumHeader);
		// add len header to full header
		buf.put(lenHeader);
		// add ackno header to full header
		buf.put(acknoHeader);
		
		buf.rewind();
		
	}
	
	
	
	
	
	public void segmentPacketArray(DatagramPacket packet) {
		byte[] buffer = packet.getData();
		header = Arrays.copyOfRange(buffer, 0, HEADER_SIZE);
		acknoHeader = Arrays.copyOfRange(buffer, 4, 8);
		fileData = Arrays.copyOfRange(buffer, HEADER_SIZE, buffer.length);
		
	}
	
	
	
	
	public short proxy() {
		
		// decides if packet is corrupt, delayed, dropped, etc?
		//Chance < 50 needs to be changed to have user specify valeue
		//creates random number
		Random rng = new Random();
		int chance = rng.nextInt((int)(100));
		
		
		int badPacketsInt = (int) (badPackets * 100);
		int badPacketsPercentage = (int) (badPacketsInt / 2);
		
		
		// corrupt packet
		if(chance < badPacketsPercentage) {
	
			ackPacketCondition = ERR;
			return 1;
		}
		// drop packet
		else if(chance > 100 - badPacketsPercentage) {
			ackPacketCondition = DROP;
			return 0;
		}
		
		
		return 0;
		
	}
	
	
}