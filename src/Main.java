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
 * Main.java
 */

import java.io.IOException;
import java.util.Scanner;

public class Main {
		
	public static void main(String[] args) throws IOException {
		boolean playThroughTwitter = true;			// Change to false to play through console
		
		if (playThroughTwitter) new TwitterBot();
		else new GameOfPoker(getPlayerNameThroughConsole());
	}
	
	@SuppressWarnings("resource")							// Cannot close scanner as it is used in consoleCommunicator too
	public static String getPlayerNameThroughConsole() {	// Closing it causes problems
		System.out.print("Enter your name: ");
		String name = "";
		Scanner scanner = new Scanner(System.in);
		name = scanner.nextLine();
		//scanner.close();
		return name;
	}
}
