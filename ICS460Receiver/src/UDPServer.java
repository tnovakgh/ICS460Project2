// receiver 2 in my projects

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class UDPServer {
	
	private final String RECV = "RECV";
	private final String DUPL = "DUPL";
	private final String CRPT = "CRPT";
	private final String SEQ_ERR = "!Seq";
	private final String ACKKNOWLEDGE = "ACK";
	private final String SENT = "SENT";
	private final String ERR = "ERR";
	private final String DROP = "DROP";
	private final String TIMEOUT = "TimeOut";
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
	
	
	private short chksum = 0;
	private short len = ACK_SIZE;
	private int ackno = 1;
	
	// added for ack testing
	private InetAddress address;
	private boolean goodPacket = true;
	
	// class constructor
	public UDPServer(String address, int portNum, float badPackets) {
		try {
			
			// open socket to receive packets
			// (socket to receive does need port as parameter)
			this.packetSocket = new DatagramSocket(portNum);
			// open socket used to receive acknowledgments\
			// (socket to send doesn't need port as parameter)
			this.ackSocket = new DatagramSocket();
			// specify host's IP address
			this.address = InetAddress.getByName(address);
			// specify the port number for receiver to find packets in
			this.receiverPort = portNum;
			
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
				byte[] test = dgPacket.getData();
				
				System.out.println(test[1]);
				
				
				
				
				
				if (test[1] == 1) {    					//This is a big issue here
					goodPacket = false;
				}
				// test output
//				System.out.println(Arrays.toString(dgPacket.getData()));
				
				// added for ack testing
				// acknowledge that packet has been received

				if (goodPacket) {
					acknowledge();

				}

				
				// check if termination code has been received
				// if so, negate loop's pre-test condition
				// (converts data to String, checks if equal to termination code)
				if(new String(dgPacket.getData(), 0, dgPacket.getLength()).trim().equals("stop")){
					running = false;			// negates loop's pre-test condition
				}
				
				// packet trimming happens here
				segmentPacketArray(dgPacket);
				
				// for testing purposes
				System.out.println(Arrays.toString(header));

//				System.out.println(Arrays.toString(fileData));

				
				// implement when ready
				//byteOStream.write(trimData(dgPacket));
				
				// appends packet's data to output stream
				byteOStream.write(fileData);
				
				// output format [packet #][start byte offset][end byte offset]
				System.out.println(String.format("[%d][%d][%d]", numPackets, numPackets * BUFFER_SIZE, numPackets * BUFFER_SIZE + fileData.length));
				
				numPackets++;						// increments packet counter
				
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
			ackSocket.send(acknowledge);
			System.out.println("ack sent back to client");
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
	
	public void segmentPacketArray(DatagramPacket packet) {
		byte[] buffer = packet.getData();
		header = Arrays.copyOfRange(buffer, 0, HEADER_SIZE);
		acknoHeader = Arrays.copyOfRange(buffer, 4, 8);
		fileData = Arrays.copyOfRange(buffer, HEADER_SIZE, buffer.length);
		
	}
	
	/**
	public static void main(String[] args) {
		
		// create UDPServer object to open socket(port)
		new UDPServer().receive();
		
		put in own method outside of main()!
		int numPackets = 0;		// packet counter
		running = true;			// keeps program running until file has been fully sent
		
		// open byte output stream to allow file to be compiled when received
		ByteArrayOutputStream byteOStream = new ByteArrayOutputStream();
		buf = new byte[BUFFER_SIZE];		// initialize buffer array for incoming data to be held
		
		// pre-test continues until termination code has been received
		while(running) {
			try {
				
				// create packet to hold packet object being received
				DatagramPacket dgPacket = new DatagramPacket(buf, buf.length);
				// receive incoming packet
				dgSocket.receive(dgPacket);
				
				// check if termination code has been received
				// if so, negate loop's pre-test condition
				// (converts data to String, checks if equal to termination code)
				if(new String(dgPacket.getData(), 0, dgPacket.getLength()).trim().equals("stop")){
					running = false;			// negates loop's pre-test condition
					//continue;					// allows process to continue
				}
				
				// appends packet's data to output stream
				byteOStream.write(dgPacket.getData());
				
				// output format [packet #][start byte offset][end byte offset]
				System.out.println(String.format("[%d][%d][%d]", numPackets, numPackets * BUFFER_SIZE, numPackets * BUFFER_SIZE + buf.length));
				
				numPackets++;						// increments packet counter
				
				buf = new byte[BUFFER_SIZE];		// clears buffer
				
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		
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
		
		// close socket and byte output stream
		try {
			dgSocket.close();
			byteOStream.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}*/
	
}
