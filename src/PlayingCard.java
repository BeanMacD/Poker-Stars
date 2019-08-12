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
 * PlayingCard.java
 */

// Implements Comparable so that cards can be compared to each other 
// and sorted by Game Value
public class PlayingCard implements Comparable<PlayingCard> {
	
	static public final char HEARTS = 'H', DIAMONDS = 'D', CLUBS = 'C', SPADES = 'S';
	
	private String type;
	private char suit;
	private int faceValue, gameValue;
	
	public PlayingCard(String type, char suit, int faceValue, int gameValue) {
		this.type = type;
		this.suit = suit;
		this.faceValue = faceValue;
		this.gameValue = gameValue;
	}
		
	public String getType() {
		return type;
	}
	
	public char getSuit() {
		return suit;
	}
	
	public int getFaceValue() {
		return faceValue;
	}
	
	public int getGameValue() {
		return gameValue;
	}
	
	/**
	 * @return -1 if this card has a lower Game Value than other card
	 * 		   0 if both cards have the same Game Value
	 *         +1 if this card has a greater Game Value than other card
	 */        
	@Override
	public int compareTo(PlayingCard other) {					 
		return Integer.compare(this.gameValue, other.gameValue);	
	}																
	
	/**
	 * @return String representation of the card in the form of "<type><suit>"
	 * 		   e.g. "AH", "4S", "10D", "KC"
	 */
	public String toString() {
		return type + suit;
	}

}
