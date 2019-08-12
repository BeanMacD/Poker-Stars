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
 * DeckOfCards.java
 */

import java.util.Random;

public class DeckOfCards {
	
	static public final int DECK_SIZE = 52;
	static private final int SUIT_SIZE = 13;
	static private final int ACE_GAME_VALUE = 14;	
	static private String[] TYPES = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
	static private char[] SUITS = {PlayingCard.HEARTS, PlayingCard.DIAMONDS, PlayingCard.CLUBS, PlayingCard.SPADES};
	
	private PlayingCard[] deck = new PlayingCard[DECK_SIZE];
	private int nextCardIndex, returnCardIndex;
	
	public DeckOfCards() {
		reset();
	}
	
	public int getNumCardsAvailable() {
		return returnCardIndex;
	}
	
	/**
	 * Reinitialises a full deck of cards and shuffles it
	 */
	public void reset() { 
		int suitCounter = 0, typeCounter = 0;			 	// pointers to the element currently being used in TYPES and SUITS arrays
		
		for (int i = 0; i < DECK_SIZE; i++) {				// Iterate 52 times
			if (typeCounter == SUIT_SIZE) { 				// Every 13 cards (each suit)
				suitCounter++;								// Iterate to next element of array 'suits'
				typeCounter = 0;							// Reset current element of array 'types' to "A"
			}
			
			int faceV = typeCounter + 1;
			int gameV = (typeCounter == 0) ? ACE_GAME_VALUE : faceV; 	// If current card is an Ace, Game Value is 14, otherwise Game Value = Face Value
	
			deck[i] = new PlayingCard(TYPES[typeCounter], SUITS[suitCounter], faceV, gameV);	// Add current card to deck
			typeCounter++;																		// Repeat loop for next card
		}
		
		nextCardIndex = DECK_SIZE - 1;					// Reset pointers to top of deck
		returnCardIndex = DECK_SIZE;
		
		shuffle();
	}
	
	/**
	 * Shuffles the cards that are still available to be dealt as well as the ones that have been returned to the decks
	 */
	private void shuffle() {						
		Random random = new Random();
		for (int i = 0; i < DECK_SIZE*DECK_SIZE; i++) {
			int card1 = random.nextInt(returnCardIndex), card2 = random.nextInt(returnCardIndex);	// Picks two random cards, including those that have been
			PlayingCard temp = deck[card1];															// returned to the deck, and swaps them
			deck[card1] = deck[card2];				
			deck[card2] = temp;
		}
		nextCardIndex = returnCardIndex - 1;		// Resets next card pointer to include all shuffled cards 
	}
	
	/**
	 * Deals next card, while checking if all cards have already been dealt
	 * @return null if all cards have been dealt, top card of deck if not
	 */
	public synchronized PlayingCard dealNext() {
		if (nextCardIndex < 0) {													// Checks if all cards have been dealt
			System.out.println("All cards have been dealt - deck is empty.");
			return null;
		} 
		else {
			PlayingCard card = deck[nextCardIndex];									// Stores card to be dealt
			if (returnCardIndex == nextCardIndex + 2) {								// If only one card has been returned to the 
				deck[nextCardIndex] = deck[nextCardIndex + 1];						// deck so far, move it into position that
				deck[nextCardIndex + 1] = null;										// card was just dealt from
			}
			else {
				deck[nextCardIndex] = null;											// Else Sets card's position in deck to null
				for (int i = nextCardIndex; i < returnCardIndex - 2; i++) {			// and bubble this null card to the end of the
					PlayingCard nullCard = deck[i];									// returned cards, moving each one back one
					deck[i] = deck[i + 1];											// position so that available cards and
					deck[i + 1] = nullCard;											// returned cards are all in succession
				}																	// to allow for a shuffle
			}
			nextCardIndex--;
			returnCardIndex--;
			return card;
		}
	}
	
	/**
	 * Returns cards to the deck, not allowing them to be dealt until the deck is shuffled. Checks if deck is full before card is returned.
	 * @param discarded
	 */
	public synchronized void returnCard(PlayingCard discarded) {					// Checks if deck is full
		if (returnCardIndex > DECK_SIZE - 1) {										// This can only happen if a card is created outside of the deck
			System.out.println("All cards have been returned - deck is full.");		// (May not be necessary)
		} 
		else {
			deck[returnCardIndex] = discarded;
			returnCardIndex++;
		}
	}
	
}
