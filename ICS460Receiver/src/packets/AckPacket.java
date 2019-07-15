package packets;
import java.net.DatagramPacket;

public class AckPacket {
	
	private short chksum; 	//16-bit 2-byte, . Use 0 for good, 1 for bad.
	private short len;		//16-bit 2-byte, For Ack packets this is 8
										  // 2 for cksum, 2 for len, and 4 for ACK no
	private int ackNum;		//32-bit 4-byte. is the sequence number you are waiting for,
	  									  // that you have not received yet – it is the equivalent of
	  									  // Next Frame Expected. This says that the sender of a packet
										  // has received all packets with sequence numbers earlier than
	  									  // ackno, and is waiting for the packet with a seqno of ackno. 
	  									  // The first sequence number in any connection is 1, so if you
	  									  // have not received any packets yet, you should set ackno to 1.

	public AckPacket() {
		// set checksum to good
		chksum = 0;
		// set length of AckPacket
		len = 8;
		// set ackNum
		ackNum = 4;
	}

}
