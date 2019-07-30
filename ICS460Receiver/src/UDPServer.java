import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
/**
 * Receives datagram packets from a sender using UDP stop and wait protocol.
 * Involves sending Acknowledments back to sender so the sender can send next
 * packet.
 * 
 * @author Troy Novak, Brent Windham, Abdullah Almamun, Yuan Lu
 *
 */
public class UDPServer {
	
	private final String RECV = "RECV";
	private final String DUPL = "DUPL";
	private final String CRPT = "CRPT";
	private final String SENT = "SENT";
	private final String ERR = "ERR";
	private final String DROP = "DROP";
	
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

	
	//private short chksum = 0;
	private short len = ACK_SIZE;
	private int ackno = 1;
	private int seqno = 0;
	private boolean isDuplicate;
	private int currentWrite = -1;
	
	// added for ack testing
	private InetAddress address;
	private boolean goodPacket = true;
	
	/**
	 * Constructor for UDPServer class, instantiates necessary variables.
	 * 
	 * @param address
	 * @param portNum
	 * @param badPackets
	 */
	public UDPServer(String address, int portNum, float badPackets) {
		try {
			
			// open socket to receive packets
			// (socket to receive does need port as parameter)
			this.packetSocket = new DatagramSocket(portNum);
			
			// open socket used to send acknowledgments
			this.ackSocket = new DatagramSocket();
			
			// specify host's IP address
			this.address = InetAddress.getByName(address);
			
			this.badPackets = badPackets;
			
		}catch(IOException e) {
			
			e.printStackTrace();
			
		}
	}
	
	/**
	 * Method used to receive packets and implements part of a proxy to
	 * potentially corrupt the acknowledgements being sent back.
	 */
	public void receive() {
		
		int numPackets = 0;		// packet counter
		running = true;			// keeps program running until file has been fully sent
		
		// open byte output stream to allow file to be compiled when received
		ByteArrayOutputStream byteOStream = new ByteArrayOutputStream();
		
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

				segmentPacketArray(dgPacket);
				
				//Getting Bytes so we can run tests
				byte[] test = dgPacket.getData();
				
				//Create chksum
				ByteBuffer chksumBuffer = ByteBuffer.wrap(test,0,2);
				short chksum = chksumBuffer.getShort();
				
				//creating the ackno
				ByteBuffer acknoBuffer = ByteBuffer.wrap(test,4,4);
				int tempAckno = acknoBuffer.getInt();
				
				//creating the seqno
				ByteBuffer seqnoBuffer = ByteBuffer.wrap(test,8,4);
				seqno = seqnoBuffer.getInt();

				//Testing if packet is corrupt //This comes last because Corrupt files dont care if there is a duplicate because seqno could be affected
				//Testing if packet is a duplicate
				//Setting Ackno to Next Packet needed regardless of duplicate //no need to reset seqno because we update it when we receive our packet. Not necessary to keep value
				if (chksum == 1) {   
					
					//goodPacket = false;
					receivedPacketCondition = CRPT;
					
					// test for duplicate ack
					if (tempAckno == ackno) {
						
						isDuplicate = true;
						System.out.println(DUPL + " " + timeReceived + " " + seqno + " " + receivedPacketCondition);
						
					// if not a duplicate
					} else {
						
						isDuplicate = false;

						System.out.println(RECV + " " + timeReceived + " " + seqno + " " + receivedPacketCondition);
						
					}
					
					// set ackno to next ackno
					this.ackno = tempAckno;

				} else {
					
					receivedPacketCondition = RECV;
					
					// test for duplicate ack
					if (tempAckno == ackno) {
						
						isDuplicate = true;

						//receivedPacketCondition = DUPL;
						System.out.println(DUPL + " " + timeReceived + " " + seqno + " " + receivedPacketCondition);

					} else {
						
						isDuplicate = false;

						System.out.println(RECV + " " + timeReceived + " " + seqno + " " + receivedPacketCondition);

					}
					
					this.ackno = tempAckno;
					
					acknowledge();
					
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
	
	/**
	 * Writes the bytestream to server's storage so it can be accessed by
	 * the user.
	 * 
	 * @param byteOStream
	 */
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
	
	/**
	 * Sends acknowledgment back to sender. Checks for duplicate
	 * acknowledgments, as well as makes sure that the acknowledgment hasn't
	 * been dropped by the server.
	 */
	public void acknowledge() {
		
		try {
			
			createAckPacket();
			
			DatagramPacket acknowledge = new DatagramPacket(ackPacket, ackPacket.length, address, ACK_PORT);
			Date ackDateSent = new Date();

			long ackSendTime = ackDateSent.getTime();
			
			if (ackPacketCondition != DROP) {
				
				ackSocket.send(acknowledge);
				
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
	
	/**
	 * Creates the acknowledgment packet to be sent back to the sender.
	 */
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
	
	/**
	 * Segments out incoming packets from sender so headers can be checked
	 * for corruption.
	 * 
	 * @param packet
	 */
	public void segmentPacketArray(DatagramPacket packet) {
		
		byte[] buffer = packet.getData();
		header = Arrays.copyOfRange(buffer, 0, HEADER_SIZE);
		acknoHeader = Arrays.copyOfRange(buffer, 4, 8);
		fileData = Arrays.copyOfRange(buffer, HEADER_SIZE, buffer.length);
		
	}
	
	/**
	 * Determines if an acknowledgment packet has been either dropped or
	 * corrupted.
	 * 
	 * @return 0 if packet is not corrupt
	 * @return 1 if packet has been corrupt
	 */
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