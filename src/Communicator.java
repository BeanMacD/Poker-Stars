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
 * Communicator.java
 */

// This provides an interface for communication between the game and the player
// either through the console, or Twitter
public interface Communicator {
	
	/**
	 * @param instruction 	message to send to player
	 * @param withImage		flag to send it with an image or not
	 */
	public void sendInstructionToPlayer(String instruction, String twitterAddition, boolean withImage);
	
	/**
	 * @return	input from player
	 */
	public String getPlayerInput();
}
