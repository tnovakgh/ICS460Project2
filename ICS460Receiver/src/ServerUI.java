
public class ServerUI {
	public static void main(String[] args) {
		
		final String PERCENTAGE_KEY = args[0];
		final float PERCENTAGE = Float.parseFloat(args[1]);
		final String RECEIVER_ADDRESS = args[2];
		final int RECEIVER_PORT = Integer.parseInt(args[3]);
		
		
		// just to test to make sure arguments are being passed through cmd correctly
    	System.out.println("\npercentage(" + PERCENTAGE_KEY + "): " + PERCENTAGE +
    					   "\nIP Address: " + RECEIVER_ADDRESS +
    					   "\nPort #: " + RECEIVER_PORT);
    	
    	// create UDPServer object to open socket(port)
    	new UDPServer(RECEIVER_ADDRESS, RECEIVER_PORT, PERCENTAGE).receive();
	}

}
