import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random; 

public class UDPClient {
	
	// instantiate constants
	private final int ACK_PORT = 9587;
	
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
    
	// declare instance variables
	private int receiverPort;
	private int bufferSize;
	private int timeoutLength;
	private float percentBadPackets;
	private DatagramSocket packetSocket;
	private DatagramSocket ackSocket;
	private InetAddress address;
	private byte[] packet;
	
	// instance variables for data packet
	private short chksum = 0;
	byte[] chksumBytes = new byte[2];
	private ByteBuffer chksumBuf = ByteBuffer.wrap(chksumBytes);
	private short len = 12;
	//private byte[] lenBytes = new byte[2];
	//private ByteBuffer lenBuf = ByteBuffer.wrap(lenBytes);
	private int ackNum = 1;
	byte[] ackNumBytes = new byte[4];
	private ByteBuffer ackNumBuf = ByteBuffer.wrap(ackNumBytes);
	private int seqNum = 1;
	byte[] seqNumBytes = new byte[4];
	private ByteBuffer seqNumBuf = ByteBuffer.wrap(seqNumBytes);
	private byte[] data;
	
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
			this.percentBadPackets = badPackets * 100;
			
			// set packet len
			//this.len = (short)(12 + bufSize);
			this.packet = new byte[(len+bufSize)];
			
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
			data = new byte[bufferSize];		// initialize buffer array for data to be read to
			
			// pre-test reads file from input stream and writes BUFFER_SIZE number of bytes to buffer to be sent in packets
			while(iStream.read(data) != -1) {
				
				// Yuan
				// decides if packet is corrupt, delayed, dropped, etc?
				Random rng = new Random();
				int chance = rng.nextInt((int)(100));
				//
				// corrupt packet
				if(chance < percentBadPackets && chance % 2 == 0) {
					//chksum = 1;
				}
				// drop packet
				else if(chance < percentBadPackets && chance % 3 == 0) {
					//continue;	// jump to next iteration of loop. !may need to move!
				}
				// delay packet
				else if(chance < percentBadPackets && chance % 4 == 0) {
					
				}
				
				// create the byte array to write in to the packet
				createPacket(chksum, ackNum, seqNum, data);
				
				// create new packet each iteration to be sent with(packet data, packet length, destination address, destination port)
				DatagramPacket dgPacket = new DatagramPacket(packet, packet.length, address, receiverPort);
				// send packet via open socket
				packetSocket.send(dgPacket);
				
				// added for ack testing
				if(receivedAck()) {
					System.out.println("ack received, packet successfully sent");
				}
				
				// output format [packet #][start byte offset][end byte offset]
				System.out.println(String.format("[%d][%d][%d]", numPackets, numPackets * bufferSize, numPackets * bufferSize + packet.length));
				
				numPackets++;		//increment packet counter
				ackNum++;
				seqNum++;
			}
			
			// send termination code
			packet = "stop".getBytes();
			DatagramPacket dgPacket = new DatagramPacket(packet, packet.length, address, receiverPort);
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
		
		DatagramPacket acknowledged = new DatagramPacket(packet, packet.length);
		
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
	
	// review before testing *IF REVERTING BACK CHANGE TO BYTE[] INSTEAD OF VOID*
	public byte[] createPacket(short chksum, int ackNum, int seqNum, byte[] data) {
		
		len += (short)(bufferSize);
		
		chksumBuf.putShort(chksum);
    	chksumBuf.rewind();
    	//lenBuf.putShort(len);
    	//lenBuf.rewind();
    	ackNumBuf.putInt(ackNum);
    	ackNumBuf.rewind();
    	seqNumBuf.putInt(seqNum);
    	seqNumBuf.rewind();
    	
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	
    	/**
    	for(int i = 0; i < len+bufferSize; i++) {
    		if(i <= 1) {
				packet[i] = chksumBytes[i];
			//}else if(i > 1 && i <= 3) {
				//lenBytes[i-2] = packetBuf[i];
			}else if(i > 1 && i <= 5) {
				packet[i] = ackNumBytes[i-2];
			}else if(i > 5 && i <= 9) {
				packet[i] = seqNumBytes[i-6];
			}else {
				packet[i] = data[i-10];
			}
    	}*/
    	
    	
    	try {
    		baos.write(chksumBuf.array());
    		//baos.write(lenBuf.array());
    		baos.write(ackNumBuf.array());
    		baos.write(seqNumBuf.array());
    		baos.write(data);
    		
    	}catch(IOException e) {
    		e.printStackTrace();
    	}
    	
    	return baos.toByteArray();
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
