import java.util.Scanner;

public class ClientUI {
	
		
	public static void main(String[] args) {
		
		final String SIZE_KEY = args[0];
		final int SIZE = Integer.parseInt(args[1]);
		final String TIMEOUT_KEY = args[2];
		final int TIMEOUT = Integer.parseInt(args[3]);
		final String PERCENTAGE_KEY = args[4];
		final float PERCENTAGE = Float.parseFloat(args[5]);
		
		// just to test to make sure arguments are being passed through cmd correctly
    	System.out.println("size(" + SIZE_KEY + "): " + SIZE + 
    					   "\ntimeout(" + TIMEOUT_KEY + "): " + TIMEOUT + 
    					   "\npercentage(" + PERCENTAGE_KEY + "): " + PERCENTAGE);
		
	}

}
