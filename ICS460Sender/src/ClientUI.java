import java.util.Scanner;

public class ClientUI {
	
		
	public static void main(String[] args) {
		
		final String SIZE_KEY = args[1];
		final int PACKET_SIZE = Integer.parseInt(args[2]);
		final String TIMEOUT_KEY = args[3];
		final int TIMEOUT = Integer.parseInt(args[4]);
		final String PERCENTAGE_KEY = args[5];
		final float PERCENTAGE = Float.parseFloat(args[6]);
		final String RECEIVER_ADDRESS = args[7];
		final int RECEIVER_PORT = Integer.parseInt(args[8]);
		
		// just to test to make sure arguments are being passed through cmd correctly
    	System.out.println("size(" + SIZE_KEY + "): " + PACKET_SIZE + 
    					   "\ntimeout(" + TIMEOUT_KEY + "): " + TIMEOUT/1000 + "s" + 
    					   "\npercentage(" + PERCENTAGE_KEY + "): " + PERCENTAGE*100 + "%" +
    					   "\nIP Address: " + RECEIVER_ADDRESS +
    					   "\nPort #: " + RECEIVER_PORT);
    	
    	// make sure that a file(parameter) has been passed
    	// if not, program terminates
    	if(args.length < 1) {
    		System.out.println("Please provide a file to send... TERMINATING");
    		System.exit(0);
    	}
    	
    	UDPClient client = new UDPClient(RECEIVER_ADDRESS, RECEIVER_PORT, PACKET_SIZE, TIMEOUT, PERCENTAGE);
    	client.sendFile(args[0]);					// begin process of sending file
		
	}

}
