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
 * HandOfCards.java
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class HandOfCards {
	

	static public final int    HAND_SIZE = 5;	
	static public final int    MAX_DISCARD_NUM = 3;
	static private final int   ROYAL_FLUSH_VALUE = 9, STRAIGHT_FLUSH_VALUE = 8, FOUR_OF_A_KIND_VALUE = 7, FULL_HOUSE_VALUE = 6, 
							   FLUSH_VALUE = 5, STRAIGHT_VALUE = 4, THREE_OF_A_KIND_VALUE = 3, TWO_PAIR_VALUE = 2, 
							   ONE_PAIR_VALUE = 1, HIGH_HAND_VALUE = 0;
	
	private DeckOfCards deck;
	private PlayingCard[] hand;
	private PlayingCard[] displayHand;
	private String[] hexHand;				// Unique hexadecimal representation for hand, used to value the hand
	
	public HandOfCards(DeckOfCards d) {		
 		hand = new PlayingCard[HAND_SIZE];
		deck = d;
		
		for (int i = 0; i < HAND_SIZE; i++) {	// 5 cards dealt from shuffled deck
			hand[i] = deck.dealNext();
		}
		
		update();
	}
												
	public HandOfCards(PlayingCard[] cards, boolean sort) {	// AI uses sorted version to create possible hands when discarding							
		hand = new PlayingCard[HAND_SIZE];					// Human player uses unsorted version to create display hand
		
		for (int i = 0; i < HAND_SIZE; i++) {	// Creates a hand from an array of 5 cards passed in.
			hand[i] = cards[i];
		}
		
		if (sort) update();
	}
	
	public int compareTo(HandOfCards other) {	//	Determines which hand is better or worse based on game value			 
		return Integer.compare(this.getGameValue(), other.getGameValue());	
	}
	
	public void update() {						// Sorts hand in descending order and computes hand value
		Arrays.sort(hand, Collections.reverseOrder());
		displayHand = hand.clone();
		sortHand();
		hexHand = createHexHand();
	}
	
	public DeckOfCards getDeck() {
		return deck;
	}
	
	public PlayingCard getCard(int cardPosition) {	// Return the card at a given position
		return hand[cardPosition];
	}
	
	public PlayingCard[] getDisplayHand() {			// Returns the cards ordered in descending order for display use
		return displayHand;
	}

	/**
	 * Gives a unique value to any hand, based firstly on the type of hand, and then on the cards it contains
	 * @return The Game Value of this hand
	 * 
	 * Firstly, a 6 digit hex string is created where the most significant digit represents the value of the type of hand
	 * (defined as constants private to this class), the last 5 digits represent the values of the 5 cards in the hand, 
	 * ordered in descending order
	 * 
	 * Examples:																																					Hand Value
	 * 																Hand Value     	Card 1 Value   	Card 2 Value   	Card 3 Value   	Card 4 Value   	Card 5 Value	Hex	  	 => Decimal
	 * Royal Flush 			AS, KS, QS, JS, 10S	 =>		Decimal		256			   	12			   	11			    10				9				8
	 * 													Hex			100		  		C            	B            	A            	9 				8			=> 	100CBA98 => 10,271,384
	 * 
	 * Full House 			7S, 7D, 7H, 2D, 2C	 =>		Decimal		6			   	5				5				5				0				0
	 * 													Hex			6				5				5				5				0				0 			=>	655500 	 => 6,640,896
	 */
	public int getGameValue() {
		String hexCardsValue = "";
		for (int i = 0; i < HAND_SIZE + 1; i++) {
			hexCardsValue += hexHand[i];
		}
		
		return Integer.parseInt(hexCardsValue, 16);				// Bases moves solely off the probabilities.
	}

	/**
	 * @return Default value for the type of hand
	 */
	public int getHandValue() {
		if (isRoyalFlush())	   return ROYAL_FLUSH_VALUE;
		if (isStraightFlush()) return STRAIGHT_FLUSH_VALUE;
		if (isFourOfAKind())   return FOUR_OF_A_KIND_VALUE;
		if (isFullHouse())	   return FULL_HOUSE_VALUE;
		if (isFlush()) 	   	   return FLUSH_VALUE;	
		if (isStraight()) 	   return STRAIGHT_VALUE;		
		if (isThreeOfAKind())  return THREE_OF_A_KIND_VALUE;
		if (isTwoPair()) 	   return TWO_PAIR_VALUE;
		if (isOnePair())	   return ONE_PAIR_VALUE;
							   return HIGH_HAND_VALUE;
	}
	
	/**
	 * @param card
	 * @return the value for a given card type, used to calculate the Hand's Game Value
	 * 2H = 0, 3D = 1, 4S = 2, ... , KC = 11, AH = 12
	 */
	private int getCardValue(PlayingCard card) {
		return card.getGameValue() - 2;
	}
	
	/**
	 * Replaces the chosen card with ones dealt from the deck
	 * @param numToDiscard
	 */
	public void discard(int numToDiscard) {
		deck.returnCard(hand[numToDiscard]);
		hand[numToDiscard] = deck.dealNext();
	}
		
	/**
	 * Creates a unique hexadecimal representation of the hand.
	 * Further details outlined in the comment to 'getGameValue()' below
	 */
	private String[] createHexHand() {
		String[] hex = new String[HAND_SIZE + 1];
		hex[0] = Integer.toHexString(getHandValue());
		
		for (int i = 0; i < HAND_SIZE; i++) {
			hex[i + 1] = Integer.toHexString(getCardValue(hand[i]));
		}

		return hex;
	}
	
//////////////////////////////////////////
//////////////////////////// SORTING  ////
//////////////////////////////////////////
	
	private void sortHand() {
		if 		(isFourOfAKind()) 	  sortFourOfAKind();
		else if (isFullHouse()) 	  sortFullHouse();
		else if (isThreeOfAKind())    sortThreeOfAKind();
		else if (isTwoPair()) 		  sortTwoPair();
		else if (isOnePair()) 		  sortOnePair();	
	}
	
	/**
	 * Put the 4 of a kind cards first
	 */
	private void sortFourOfAKind() {
		if (hand[0].getGameValue() != hand[HAND_SIZE - 4].getGameValue()) {		// If the four of a kind are the last four cards
			swapCards(0, HandOfCards.HAND_SIZE - 1);							// swap the first and last cards
		}																		// e.g. AS, 9D, 9H, 9S, 9C => 9C, 9D, 9H, 9S, AS
	}
	
	/**
	 * Put the 3 of a kind cards first, and the pair last
	 */
	private void sortFullHouse() {
		if (hand[0].getGameValue() != hand[HandOfCards.HAND_SIZE - 3].getGameValue()) {		// If the 3 of a kind cards are the last three
			swapCards(0, HandOfCards.HAND_SIZE - 2);										// swap cards 1 and 4, 2 and 5
			swapCards(HandOfCards.HAND_SIZE - 4, HandOfCards.HAND_SIZE - 1);				// e.g. 10D, 10C, 6H, 6S, 6D => 6S, 6D, 6H, 10D, 10C
		}
	}
	
	/**
	 * Put the 3 of a kind cards first
	 */
	private void sortThreeOfAKind() {																		
		if (hand[0].getGameValue() != hand[HandOfCards.HAND_SIZE - 3].getGameValue() 									// If the 3 of a kind cards are the middle 3 cards
				&& hand[HandOfCards.HAND_SIZE - 1].getGameValue() != hand[HandOfCards.HAND_SIZE - 3].getGameValue()) {	// swap cards 1 and 4
			swapCards(0, HandOfCards.HAND_SIZE - 2);																	// e.g. AH, 8D, 8S, 8C, 7D => 8C, 8D, 8S, AH, 7D
		} else if (hand[0].getGameValue() != hand[HandOfCards.HAND_SIZE - 3].getGameValue() 
				&& hand[HandOfCards.HAND_SIZE - 4].getGameValue() != hand[HandOfCards.HAND_SIZE - 3].getGameValue()) {
			swapCards(0, HandOfCards.HAND_SIZE - 2);																	// If the 3 of a kind cards are the last 3,
			swapCards(HandOfCards.HAND_SIZE - 4, HandOfCards.HAND_SIZE - 1);											// reverse the order (smallest game value to largest)
		}																												// then swap the last two cards (maintain the large to small ordering)
	}																													// e.g. KS, 9D, 3C, 3S, 3H => 3H, 3S, 3C, 9D, KS => 3H, 3S, 3C, KS, 9D
	
	/**
	 * Put the best pair first, and the other pair next
	 */
	private void sortTwoPair() {																																	
		if (hand[0].getGameValue() != hand[HandOfCards.HAND_SIZE - 4].getGameValue()) {									// If the two pairs are the last 4 cards
			for (int i = 0; i < HandOfCards.HAND_SIZE - 1; i++) {														// "bubble" the first card to the end
				swapCards(i, i + 1);																					// e.g. AH, 7D, 7S, 6H, 6C => 7D, 7S, 6H, 6C, AH
			}
		} else if (hand[0].getGameValue() == hand[HandOfCards.HAND_SIZE - 4].getGameValue() 
				&& hand[HandOfCards.HAND_SIZE - 2].getGameValue() == hand[HandOfCards.HAND_SIZE - 1].getGameValue()) {	// If the two pairs are the first and last two cards
			swapCards(HandOfCards.HAND_SIZE - 3, HandOfCards.HAND_SIZE - 1);											// swap the middle and last card
		}																												// e.g. JS, JC, 8D, 4H, 4S => JS, JC, 4S, 4H, 8D
	}
	
	/**
	 * Put the pair at the front
	 */
	private void sortOnePair() {																						// If the pair are cards 2 and 3, swap cards 1 and 3
		if (hand[HandOfCards.HAND_SIZE - 4].getGameValue() == hand[HandOfCards.HAND_SIZE - 3].getGameValue()) {			// e.g QD, 8H, 8C, 6S, 4J => 8C, 8H, QD, 6S, 4J
			swapCards(0, HandOfCards.HAND_SIZE - 3);
		} else if (hand[HandOfCards.HAND_SIZE - 3].getGameValue() == hand[HandOfCards.HAND_SIZE - 2].getGameValue()) {	// If the pair are cards 3 and 4, swap 1 and 3, 2 and 4 
			swapCards(0, HandOfCards.HAND_SIZE - 3);																	// e.g. 10S, 8D, 4C, 4H, 2H => 4C, 4H, 10S, 8D, 2H
			swapCards(HandOfCards.HAND_SIZE - 4, HandOfCards.HAND_SIZE - 2);
		} else if (hand[HandOfCards.HAND_SIZE - 2].getGameValue() == hand[HandOfCards.HAND_SIZE - 1].getGameValue()) {	// If the pair are the last 2 cards, reverse the order
			swapCards(0,  HandOfCards.HAND_SIZE - 1);																	// and then swap cards 3 and 5
			swapCards(HandOfCards.HAND_SIZE - 4, HandOfCards.HAND_SIZE - 2);
			swapCards(HandOfCards.HAND_SIZE - 3, HandOfCards.HAND_SIZE - 1);											// e.g. 9H, 7D, 6C, 3D, 3S => 3S, 3D, 6C, 7D, 9H => 3S, 3D, 9H, 7D, 6C 
		}
	}
				
	/**
	 * Swaps the two cards of the hand at the indexes taken as parameters
	 * @param index1
	 * @param index2
	 */
	public void swapCards(int index1, int index2) {
		PlayingCard temp = hand[index1];
		hand[index1] = hand[index2];
		hand[index2] = temp;
	}
	
//////////////////////////////////////////
//////////////////// HAND TYPE CHECKS ////
//////////////////////////////////////////
	
 	public boolean isRoyalFlush() {
		if (hand[0].getGameValue() != 14) {			// If 1st card isn't an A, then it can't be a Royal FLush
			return false;
		}
													// Check if all cards are not the same suit, or if cards aren't in
		for (int i = 0; i < HAND_SIZE - 1; i++) {	// descending continuous Game Value
			if (hand[i].getSuit() != hand[i + 1].getSuit() || hand[i].getGameValue() != hand[i + 1].getGameValue() + 1) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isStraightFlush() {
		return isStraight() && isFlush();
	}
	
	public boolean isFourOfAKind() {								
		if (hand[0].getGameValue() == hand[HAND_SIZE - 2].getGameValue() || hand[HAND_SIZE - 4].getGameValue() == hand[HAND_SIZE - 1].getGameValue()) {
			return true;							// Checks if [0] & [3] or [1] & [4] are the same 
		}

		return false;
	}
	
	public boolean isFullHouse() {								
		if (hand[0].getGameValue() == hand[HAND_SIZE - 4].getGameValue() && hand[HAND_SIZE - 3].getGameValue() == hand[HAND_SIZE - 1].getGameValue()
				|| hand[0].getGameValue() == hand[HAND_SIZE - 3].getGameValue() && hand[HAND_SIZE - 2].getGameValue() == hand[HAND_SIZE - 1].getGameValue()) {
			return true;							// Checks if ([0] & [1] and [2] & [4]) or ([0] & [2] and [3] & [4] are the same
		}
		
		return false;
	}
	
	public boolean isFlush() {
		for (int i = 0; i < HAND_SIZE - 1; i++) {				// If all suits aren't the same
			if (hand[i].getSuit() != hand[i + 1].getSuit()) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isBrokenFlush() {
		int[] suitCount = {0, 0, 0, 0}; // Count of H, D, S, C
		
		for (PlayingCard card : hand) {
			int suitIndex = 0;
			switch (card.getSuit()) {
			case 'H':
				suitIndex = 0;
				break;
			case 'D':
				suitIndex = 1;
				break;
			case 'S':
				suitIndex = 2;
				break;
			case 'C':
				suitIndex = 3;
				break;
			}
			suitCount[suitIndex]++;
		}
		
		Arrays.sort(suitCount);
		if (suitCount[3] == 4 && suitCount[2] == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isCardBreakingFlush(int cardPosition) {
		
		char suit;
		int flushCount = 0;
		
		if(cardPosition == 0)
			suit = hand[1].getSuit();
		else
			suit = hand[0].getSuit();
		
		for(int card = 0; card < HAND_SIZE; card++) {
			if(card != cardPosition && hand[card].getSuit() == suit)
				flushCount++; 
		}
		
		if(flushCount == 4 && hand[cardPosition].getSuit() != suit)
			return true;
		else
			return false;
	}
	
	public boolean isStraight() {
		
		// If the first card is an Ace
		if(hand[0].getGameValue() == 14) {
			
			// If the second card is a 5 or King
			if(hand[1].getGameValue() == 5 || hand[1].getGameValue() == 13) {		
				for (int i = HAND_SIZE - 4; i < HAND_SIZE - 1; i++) {
					if (hand[i].getGameValue() != hand[i + 1].getGameValue() + 1) 
						return false;
				}
			}
			else
				return false;
		}
		// If the first card isn't an Ace
		else {
			for (int i = 0; i < HAND_SIZE - 1; i++) {				// If Game Value or Face Value aren't in descending continuous order (allows for 5432A)
				if ((hand[i].getGameValue() != hand[i + 1].getGameValue() + 1) && (hand[i].getFaceValue() != hand[i + 1].getFaceValue() + 1))
					return false;
			}
		}
		
		return true;
	}
	
	public ArrayList<ArrayList<Integer>> getAllStraights() {
		
		ArrayList<ArrayList<Integer>> straights = new ArrayList<>();
		
		for(int card = 2; card <= 10; card++) {
			int cardValue = card;
			ArrayList<Integer> straight = new ArrayList<>();
			
			for(int cards = 0; cards < HAND_SIZE; cards++) {
				straight.add(cardValue++);
			}
			
			straights.add(straight);
		}
		
		ArrayList<Integer> specialCase = new ArrayList<>();
		specialCase.add(2);
		specialCase.add(3);
		specialCase.add(4);
		specialCase.add(5);
		specialCase.add(14);
		straights.add(specialCase);
		
		return straights;
	}
	
	// If hand[cardPosition] is the card that busts the straight, return true
	// Creates an arraylist storing all possible straights
	// It then checks if any straight contains all cards in the hand excluding hand[cardPosition]
	// If a straight is found containing the four cards then that means that the card at the given
	// position is responsible for the busted straight
	public boolean isBrokenStraight() {
		
		if(isStraight())
			return false;
		
		ArrayList<ArrayList<Integer>> straights = getAllStraights();
		
		for(ArrayList<Integer> straight : straights) { 
			int cardGameValue, count = 0;
			
			for(int card = 0; card < HAND_SIZE; card++) {
				cardGameValue = this.hand[card].getGameValue();
				
				if(straight.contains(cardGameValue)) {
					straight.remove(new Integer(cardGameValue));
					count++;
				}
			}
			
			if(count == 4)
				return true;
		}
		
		return false;
	}
	
	public boolean isCardBreakingStraight(int cardPosition) {
		
		ArrayList<ArrayList<Integer>> straights = getAllStraights();
		
		for(ArrayList<Integer> straight : straights) { 
			int cardGameValue, count = 0;
			
			for(int card = 0; card < HAND_SIZE; card++) {
				cardGameValue = this.hand[card].getGameValue();
				
				if(card != cardPosition && straight.contains(cardGameValue)) {
					straight.remove(new Integer(cardGameValue));
					count++;
				}
			}
			
			if(count == 4)
				return true;
		}
		
		return false;
	}
	
	public boolean isThreeOfAKind() {
		for (int i = 0; i < HAND_SIZE - 2; i++) {								// If any two cards 1 places away from each other are the same
			if (hand[i].getGameValue() == hand[i + 2].getGameValue()) {
				return true;
			}
		}
		
		return false;
		
	}
	
	public boolean isTwoPair() {
		for (int i = 0; i < HAND_SIZE - 3; i++) {								// If 2x (any two cards beside each other are the same)
			if (hand[i].getGameValue() == hand[i + 1].getGameValue()) {
				for (int j = i + 2; j < HAND_SIZE - 1; j++) {
					if (hand[j].getGameValue() == hand[j + 1].getGameValue()) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public boolean isOnePair() {
				
		for (int i = 0; i < HAND_SIZE - 1; i++) {								// If any two cards beside each other are the same
			if (hand[i].getGameValue() == hand[i + 1].getGameValue()) {
				return true;
			}
		}
		
		return false;
		
	}
	
	/**
	 * @return String representation of the hand in the form of "<card 1 type><card 1 suite> ... <card 5 type><card 2 suit>"
	 * 		   e.g. "4H 7D 10D QS AC"
	 */
	public String toString() {
		String string = "";
		for (PlayingCard card : hand) {
			string += card + " ";
		}
		return string;
	}

}


