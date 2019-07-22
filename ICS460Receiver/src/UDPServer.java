import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class UDPServer {
	
	// instantiate constants
	private final int ACK_PORT = 9587;
	private final int BUFFER_SIZE = 16;
	
	// Yuan
	// output string constants
	private final String RECV = "RECV";
	private final String DUPL = "DUPL";
	private final String CRPT = "CRPT";
	private final String SeqEr = "!Seq";
	private final String ACK = "ACK";
	private final String SENT = "SENT";
	private final String ERR = "ERR";
	private final String DROP = "DROP";
	private final String timeout = "TimeOut";
	private final String sendFirstMessage = "SENDing";
	
	// added for ack testing
	private final byte[] ACK_PACKET = "ack".getBytes();
	
	// declare instance variables
	private byte[] buf;
	private boolean running;
	private int receiverPort;
	private DatagramSocket packetSocket;
	private DatagramSocket ackSocket;
	private short packetLen;
	
	// instance variables for ack packet
	private short chksum = 0;
	byte[] chksumBytes = new byte[2];
	private short len = 8;
	byte[] lenBytes = new byte[2];
	private int ackNum;
	byte[] ackNumBytes = new byte[4];
	private int seqNum;
	byte[] seqNumBytes = new byte[4];
	
	byte[] packetBuf;
	
	// added for ack testing
	private InetAddress address;
	
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
		buf = new byte[BUFFER_SIZE];		// initialize buffer array for incoming data to be held
		
		// pre-test continues until termination code has been received
		while(running) {
			try {
				
				// create packet to hold packet object being received
				DatagramPacket dgPacket = new DatagramPacket(buf, buf.length);
				// receive incoming packet
				packetSocket.receive(dgPacket);
				
				// trim dgPacket array down.
				trimPacket(dgPacket);
				System.out.println(Arrays.toString(chksumBytes));
				System.out.println(Arrays.toString(lenBytes));
				System.out.println(Arrays.toString(ackNumBytes));
				System.out.println(Arrays.toString(seqNumBytes));
				
				dgPacket = new DatagramPacket(buf, buf.length);
				
				// added for ack testing
				// acknowledge that packet has been received
				acknowledge();
				
				// check if termination code has been received
				// if so, negate loop's pre-test condition
				// (converts data to String, checks if equal to termination code)
				if(new String(dgPacket.getData(), 0, dgPacket.getLength()).trim().equals("stop")){
					running = false;			// negates loop's pre-test condition
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
		
		// call method to write the packets parsed payload data to file
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
	
	
	public void acknowledge() {
		
		try {
			// create ack packet
			DatagramPacket acknowledge = new DatagramPacket(ACK_PACKET, ACK_PACKET.length, address, ACK_PORT);
			// send ack packet over different port
			ackSocket.send(acknowledge);
			// confirm ack packet has been sent
			System.out.println("ack sent back to client");
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void trimPacket(DatagramPacket packet) {
		// place packet data in to byte array
		packetBuf = packet.getData();
		
		/**
		// place array into buffer to parse
		ByteBuffer byteBuffer = ByteBuffer.wrap(packetBuf);
		
		// separate out packet headers
		chksum = byteBuffer.getShort();
		packetLen = byteBuffer.getShort();
		ackNum = byteBuffer.getInt();
		seqNum = byteBuffer.getInt();
		*/
		
		for(int i = 0; i < packetBuf.length; i++) {
			
			/**
			byte[] chksumBytes = new byte[2];
			byte[] lenBytes = new byte[2];
			byte[] ackNumBytes = new byte[4];
			byte[] seqNumBytes = new byte[4];*/
			
			if(i <= 1) {
				chksumBytes[i] = packetBuf[i];
			//}else if(i > 1 && i <= 3) {
				//lenBytes[i-2] = packetBuf[i];
			}else if(i > 1 && i <= 5) {
				ackNumBytes[i-2] = packetBuf[i];
			}else if(i > 5 && i <= 9) {
				seqNumBytes[i-6] = packetBuf[i];
			}else {
				buf[i-10] = packetBuf[i];
			}
		}
		
		/**
		// create temp array for packet payload
		byte[] temp = Arrays.copyOfRange(packetBuf, 11, packetBuf.length - 1);
		// create packet to return from method
		DatagramPacket newPacket = new DatagramPacket(temp, temp.length);*/
		
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
		test
	}*/
	
}
