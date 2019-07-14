import java.util.Scanner;

public class ClientUI {
	
		
	public static void main(String[] args) {
		
		final String
		
		boolean running = true;
		// Using Scanner for Getting Input from User 
        Scanner in = new Scanner(System.in);
		
		while(running) {
			
	        // get input from user
	        String sizeInput = in.next(SIZE_INPUT_CHECK);
	        String timeoutInput = in.next(TIMEOUT_INPUT_CHECK);
	        String percentageInput = in.next(PERCENTAGE_INPUT_CHECK);
	        while(in.hasNext()) {
	        	s = s + in.nextByte(); 
	        }
	        System.out.println(s);
		}
		
	}

}
