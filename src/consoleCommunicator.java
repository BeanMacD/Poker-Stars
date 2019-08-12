/**
 * COMP30050 - Software Engineering Project 3
 * 
 * PokerStars
 * 
 * Ciarán Connolly – 14445308
 * Kevin O’Brien – 12498432
 * Ben MacDonagh – 14398516
 * Alan Doyle – 14401758
 * 
 * ConsoleCommunicator.java
 */

import java.util.Scanner;

// Implementes the methods defined in Communicator in such a way
// that the communication is done through the console

public class consoleCommunicator implements Communicator {
	
	public void sendInstructionToPlayer(String instruction,String twitterAddition, boolean withImage) {
		System.out.println("\n" + instruction);
	}
	
	@SuppressWarnings("resource")					// Cannot close scanner as it is used in Main too
	public String getPlayerInput() {				// closing it causes problems
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		//scanner.close();
		return input;
	}

}
