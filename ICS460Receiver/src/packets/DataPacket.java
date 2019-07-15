package packets;

import java.net.DatagramPacket;

public class DataPacket {
	
	private short chksum; 	//16-bit 2-byte. Use 0 for good, 1 for bad.
	private short len;		//16-bit 2-byte. For data packets, this is 12 + payload size:
										  // 2 for cksum, 2 for len, 4 for ackno, 4 for seqno,
										  // and as many bytes are there in data[].
	private int ackNum;		//32-bit 4-byte. is the sequence number you are waiting for,
										  // that you have not received yet – it is the equivalent of
										  // Next Frame Expected. This says that the sender of a packet
										  // has received all packets with sequence numbers earlier than
										  // ackno, and is waiting for the packet with a seqno of ackno. 
										  // The first sequence number in any connection is 1, so if you
										  // have not received any packets yet, you should set ackno to 1.

	private int seqNum ; 	//32-bit 4-byte. The first packet in a stream has a seqno of 1.
										  // This protocol numbers packets.
	private byte[] data; 	//0-500 bytes. Variable. Contains (len - 12) bytes of payload data for the
										// application. To conserve packets, a sender should not send
										// more than one unacknowledged Data frame with less than the
										// maximum number of bytes (500)

	public DataPacket() {
		// set chksum to good
		chksum = 0;
		// set acknum
		ackNum = 1;
		// set seqNum
		seqNum = 1;
	}
	
	public DatagramPacket createPacket() {
		DatagramPacket packet = new DatagramPacket();
		
		
	}

}
