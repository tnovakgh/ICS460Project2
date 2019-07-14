import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {
	
	// instantiate constants
	private final int PACKET_PORT = 2201;
	private final int ACK_PORT = 9587;
	private final int BUFFER_SIZE = 16;
	
	// added for ack testing
	private final byte[] ACK = "ack".getBytes();
	private final String HOSTNAME = "localhost";
	
	// declare instance variables
	private byte[] buf;
	private boolean running;
	private DatagramSocket packetSocket;
	private DatagramSocket ackSocket;
	
	// added for ack testing
	private InetAddress address;
	
	// class constructor
	public UDPServer() {
		try {
			
			// open socket to receive packets
			// (socket to receive does need port as parameter)
			this.packetSocket = new DatagramSocket(PACKET_PORT);
			// open socket used to receive acknowledgments\
			// (socket to send doesn't need port as parameter)
			this.ackSocket = new DatagramSocket();
			// specify host's IP address
			this.address = InetAddress.getByName(HOSTNAME);
			
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
		
		writeFileToDisk(byteOStream);
		/** put in own method! called in line 69
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
		}*/
		
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
			DatagramPacket acknowledge = new DatagramPacket(ACK, ACK.length, address, ACK_PORT);
			ackSocket.send(acknowledge);
			System.out.println("ack sent back to client");
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
			
	public static void main(String[] args) {
		
		// create UDPServer object to open socket(port)
		new UDPServer().receive();
		
		/** put in own method outside of main()!
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
		}*/
		
	}
	
}
