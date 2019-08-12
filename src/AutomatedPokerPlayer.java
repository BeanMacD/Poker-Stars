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
 * AutomatedPokerPlayer.java
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class AutomatedPokerPlayer extends PokerPlayer {
	
	// Order of arrays below
	// High Hand, One Pair, Two Pair, 3OAK,   Straight, Flush,  Full House, 4OAK,    Straight Flush, Royal Flush
	// Percentage of all Poker hands that the best hand of each type beats
	// 50.12%     92.37%    97.13%    99.24%  99.63%    99.83%  99.97%      99.998%  99.9998%        100%
	
	static private final int[] NUM_PER_HAND_TYPE = {1302540, 1098240, 123552, 54912, 10200, 5108, 3744, 624, 36, 4};		// Number of all possible hands per type
	static private final int   NUM_HANDS = 2598960;																			// Sum of the elements in NUM_PER_HAND_TYPE
	static private final int[] NUM_DISTINCT_PER_HAND_TYPE = {8, 13, 12, 13, 8, 8, 12, 13, 9, 1};							// Number of possible different hands for each type of hand, ignoring the suits 
	static private final int   NUM_CARD_VALUES = 13;																		
	static private final int   MAX_INTELLIGENCE = 100;
	
	static private Random randomNumGenerator = new Random();			// random.nextInt(max - min + 1) + min

	private ArrayList<PlayingCard> allOtherCards;						// All other cards in the deck that are not in this hand
	private HashMap<Integer, ArrayList<HandOfCards>> allPossibleHands;	// All possible combinations that could be made from this hand by discarding 1, 2, or 3 cards
	
	private int intelligence;											// This AIs intelligence level
	private double winPercentage;										// Percentage of all poker hands that the hand this AI is holding beats
	
	public AutomatedPokerPlayer(Bot bot) {
		super(bot.getName());
		intelligence = bot.getIntelligence();
	}
	
	/**
	 * Update all the data used in calculations
	 */
	private void update() {
		hand.update();													// Sorts the hand in descending order and computes hand value
		winPercentage = getWinPercentage();								
		sortBrokenHands();												// AI sorts their hand in order of "most" important to "least" important cards
		allOtherCards = findAllOtherCards();							// Compute all other cards in the deck
		allPossibleHands = findAllPossibleHands();						// Compute all possible hands when discarding the last 1, 2, or 3 cards - the "least" important cards
	}
	
	@Override
	public void dealNewHand(DeckOfCards deck) {
		hand = new HandOfCards(deck);
		displayHand = new HandOfCards(hand.getDisplayHand(), false);
		update();
	}
	
	public boolean isHuman() {
		return false;
	}
	
	@Override
	public boolean fold(int minimumBet, int numOfPlayers, int pot) {
		boolean correctChoice = false;
		int bet = getBet(minimumBet, numOfPlayers, pot);	// Get bet based on the expected value of placing the minimum bet and the current pot
		
		int choice = randomNumGenerator.nextInt(100) + 1;	// Random num from [1, 10]
		if (choice <= intelligence && intelligence != 0) {	// if it is <= bot's intelligence
			correctChoice = true;							// they make the correct choice 
		}													// -> intelligence 0 = always wrong, 3 = wrong 70% of the time, 10 = always right
		
		choice = randomNumGenerator.nextInt(100) + 1;		// Random num from [1, 100]
		if (choice <= winPercentage || bet < minimumBet) {	// if it is <= % of hands that this hand beats or the amount they're willing
			if (correctChoice)  return false;				// to bet is less than the minimum bet, and they are making the right 
			else				return true;				// choice, then don't fold if they are making the wrong choice, fold
		} 
		else {												// if num > % of hands beat
			if (correctChoice)  return true;				// and correct choice, fold
			else 				return false;				// if wrong choice, don't fold
		}
	}
	
	@Override
	public int bet(int minimumBet, int maximumBet, int numOfPlayers, int pot) {
		int bet = getBet(minimumBet, numOfPlayers, pot);														// Get bet based on expected value
		int choice = randomNumGenerator.nextInt(100) + 1;														// Random num from [1, 100]
		if (choice <= intelligence) {																			// if it is <= bot's intelligence
			bet = (int) ((double) bet * ( (double) MAX_INTELLIGENCE / (double) Math.max(intelligence, 10))); 	// For example, bot with intelligence of 30
		}																									 	// will multiply their bet by 10/3
		if (bet < minimumBet) bet = minimumBet;
		else if (bet > maximumBet) bet = maximumBet;
		bank -= bet;
		return bet;
	}
	
	/**
	 * 
	 * @param minimumBet	Lowest bet made by all previous players so far
	 * @param numOfPlayers	number of players who haven't folded
	 * @param pot			total amount currently bet by all players
	 * @return				Expected value when making the minimum bet
	 */
	private int getBet(int minimumBet, int numOfPlayers, int pot) {
		double probabilityOfWinning = Math.pow(getWinPercentage()/100, numOfPlayers);							// Probability of winning with the curent hand
		double expectedValue = (probabilityOfWinning * pot) - ((1 - probabilityOfWinning) * minimumBet);		// Expected value if the minimum bet is made
		return (int) Math.floor(expectedValue);																	// Under-estimate expected value
	}
	
	@Override
	public int discard() {
		Integer[][] discardNum_ExpectedValues = new Integer[4][2];		// 4x2 matrix in the form of:	[discard 0 cards] [expected hand value when discarding 0 cards]
																		//								...				  ...
		for (int i = 0; i < 4; i++) {									//								[discard 3 cards] [expected hand value from discarding 3 cards]
			discardNum_ExpectedValues[i][0] = i;
			discardNum_ExpectedValues[i][1] = getExpectedHandValue(i);
		}
		
		Arrays.sort(discardNum_ExpectedValues, (a, b) -> Integer.compare(b[1], a[1]));	// Sort matrix rows by expected values
		
		int numToDiscard;
		int choice = randomNumGenerator.nextInt(100) + 1;		// Random num from [1, 100]
		if (choice <= intelligence) {							// If it is <= intelligenceLevel
			numToDiscard = discardNum_ExpectedValues[0][0];		// Make the best choice
		} else if (choice <= intelligence * 2) {
			numToDiscard = discardNum_ExpectedValues[1][0];		// If it is <= 2xIntelligence
		} else if (choice <= intelligence * 3) {				// make the 2nd best choice
			numToDiscard = discardNum_ExpectedValues[2][0];
		} else {												// If it is <= 3xIntelligence
			numToDiscard = discardNum_ExpectedValues[3][0];		// Make the worst choice
		}														// -> intelligence 100 = always makes best choice
																// -> intelligence 0   = always makes worst choice
		if (numToDiscard > 0) {
			for (int i = 1; i <= numToDiscard; i++) {			// Discard chosen number of cards
				hand.discard(HandOfCards.HAND_SIZE - i);
			}
			update();											// Update hand data with new cards
		}
		return numToDiscard;
	}
	
	/**
	 * @param numToDiscard
	 * @return the probability of improving this hand if 0, 1, 2, or 3 cards are discarded
	 */
	public int getExpectedHandValue(int numToDiscard) {					// Because of the way that my hands are ordered from "best" cards to worst, the discarded cards will always be the last 1-3 cards
		if (numToDiscard < 0 || numToDiscard > 3) {						// This is why the number to discard is taken as a parameter
			return 0;
		}
				
		ArrayList<HandOfCards> possibleHands = new ArrayList<>();
		
		if (numToDiscard == 0) {
			return hand.getGameValue();									// No expected value if hand remains the same
		} else {
			possibleHands = allPossibleHands.get(numToDiscard);			// Get all possible hands if this number of cards are discarded
		}
		
		long sumOfPossibleHandValues = 0;								// Total sum of all possible hands when discarding 'numToDiscard' cards
		
		for (HandOfCards possibleHand : possibleHands)
			sumOfPossibleHandValues += possibleHand.getGameValue();
			
		return (int) (sumOfPossibleHandValues / possibleHands.size());	// Return the average value across all possible hands
	}
	
	/**
	 * @return the percentage of all possible poker hands that this hand beats
	 */
	private double getWinPercentage() {
		int numHandsBeat = 0;						// Total sum of all poker hands that the current hand beats
		int handType = hand.getHandValue();																		
		
		for (int i = 0; i < handType; i++) {
			numHandsBeat += NUM_PER_HAND_TYPE[i];	// Add number of hands per hand type that are worse than current hand type															
		}
		
		// This adds the number of worse hands of the same hand type that the current hand is
		int numDistinctSameHandTypeBeat = hand.getCard(0).getGameValue() - (NUM_CARD_VALUES - NUM_DISTINCT_PER_HAND_TYPE[handType]) - 1;
		double fractionDistinctSameHandTypeBeat = (double) numDistinctSameHandTypeBeat / (double) NUM_DISTINCT_PER_HAND_TYPE[handType];
		numHandsBeat += (int) (fractionDistinctSameHandTypeBeat * (double) NUM_PER_HAND_TYPE[handType]);
		
		return (((double) numHandsBeat / (double) NUM_HANDS) * 100.0);
	}
	
	/**
	 * @return a list of all cards excluding the cards in this hand
	 */
	private ArrayList<PlayingCard> findAllOtherCards() {
		ArrayList<PlayingCard> otherCards = new ArrayList<>();			// List of all cards not in this hand
		DeckOfCards tempDeck = new DeckOfCards();						// Deck to get the cards from
		
		for (int i = 0; i < DeckOfCards.DECK_SIZE; i++) {				// Iterate through tempDeck
			PlayingCard tempCard = tempDeck.dealNext();					
			boolean cardInHand = false;
			
			for (int j = 0; j < HandOfCards.HAND_SIZE; j++) {			// Check if current card is in this hand
				if (hand.getCard(j).getGameValue() == tempCard.getGameValue() && hand.getCard(j).getSuit() == tempCard.getSuit()) {
					cardInHand = true;
					break;
				}
			}
			
			if (!cardInHand) {											// If not, add to list
				otherCards.add(tempCard);
			}
		}
		
		return otherCards;
	}
	
	/** Finds all hands possible by replacing worst 1, 2, or 3 cards in the hand with all other cards in the deck
	 * @return a Map in the form < numCardsToDiscard (1, 2, 3) , List of all possible hands when these card(s) are discarded
	 */
	private HashMap<Integer, ArrayList<HandOfCards>> findAllPossibleHands() {
		HashMap<Integer, ArrayList<HandOfCards>> possibleHands = new HashMap<>();
		
		for (int discardNum = 1; discardNum <= HandOfCards.MAX_DISCARD_NUM; discardNum++) {		// Create a new list in the map for each discard number
			possibleHands.put(discardNum, new ArrayList<>());
			
			if (discardNum == 1) {
				for (PlayingCard otherCard : allOtherCards) {									// Iterate through cards not in this hand
					PlayingCard[] tempHand = new PlayingCard[HandOfCards.HAND_SIZE];
					for (int i = 0; i < HandOfCards.HAND_SIZE; i++) {							// make a new tempHand by replacing worst card with otherCard
						if (i == HandOfCards.HAND_SIZE - 1) {
							tempHand[i] = otherCard;
						} else {
							tempHand[i] = hand.getCard(i);
						}
					}
					ArrayList<HandOfCards> tempList = possibleHands.get(discardNum);			// Add this new hand to list
					tempList.add(new HandOfCards(tempHand, true));
					possibleHands.put(discardNum, tempList);
				}
			}
			
			if (discardNum == 2) {																// Same algorithm as above, just with replacing worst 2 or 3 cards
				for (int i = 0; i < allOtherCards.size() - 1; i++) {
					for (int j = i + 1; j < allOtherCards.size(); j++) {
						PlayingCard[] tempHand = new PlayingCard[HandOfCards.HAND_SIZE];
						for (int k = 0; k < HandOfCards.HAND_SIZE; k++) {
							if (k == HandOfCards.HAND_SIZE - 1) {
								tempHand[k] = allOtherCards.get(i);
							} else if (k == HandOfCards.HAND_SIZE - 2) {
								tempHand[k] = allOtherCards.get(j);
							} else {
								tempHand[k] = hand.getCard(k);
							}
						}
						ArrayList<HandOfCards> tempList = possibleHands.get(discardNum);
						tempList.add(new HandOfCards(tempHand, true));
						possibleHands.put(discardNum, tempList);
					}
				}
			}
			
			if (discardNum == 3) {
				for (int i = 0; i < allOtherCards.size() - 2; i++) {
					for (int j = i + 1; j < allOtherCards.size() - 1; j++) {
						for (int k = j + 1; k < allOtherCards.size(); k++) {
							PlayingCard[] tempHand = new PlayingCard[HandOfCards.HAND_SIZE];
							for (int l = 0; l < HandOfCards.HAND_SIZE; l++) {
								if (l == HandOfCards.HAND_SIZE - 1) {
									tempHand[l] = allOtherCards.get(i);
								} else if (l == HandOfCards.HAND_SIZE - 2) {
									tempHand[l] = allOtherCards.get(j);
								} else if (l == HandOfCards.HAND_SIZE - 3) {
									tempHand[l] = allOtherCards.get(k);
								} else {
									tempHand[l] = hand.getCard(l);
								}
							}
							ArrayList<HandOfCards> tempList = possibleHands.get(discardNum);
							tempList.add(new HandOfCards(tempHand, true));
							possibleHands.put(discardNum, tempList);
						}
					}
				}
			}
		}
		
		return possibleHands;
	}
	
	/**
	 * Sorts the hand in order of "most important" to "least important" cards
	 */
	private void sortBrokenHands() {
		hand.update();
		if 		(hand.isBrokenFlush()) 	  sortBrokenFlush();
		else if (hand.isBrokenStraight()) sortBrokenStraight();
	}
	
	/**
	 * Move the card breaking the straight to the last position
	 */
	private void sortBrokenStraight() {						
		
		// A6543, A6542, A6532, A6432, A7654, A7653, A7643, A7543
		if(hand.getCard(0).getGameValue() == 14 && (hand.getCard(1).getGameValue() == 6 || hand.getCard(1).getGameValue() == 7)) {
			for(int i = 0; i < HandOfCards.HAND_SIZE - 1; i++)
				hand.swapCards(i, i + 1);
		}
		else {
			for(int i = HandOfCards.HAND_SIZE - 1; i >= 0; i--) {
				if(hand.isCardBreakingStraight(i)) {
					for(int j = i; j < HandOfCards.HAND_SIZE - 1; j++)
						hand.swapCards(j, j + 1);
					
					break;
				}
			}
		}
	}
	
	/**
	 * Move the card breaking the straight to the last position
	 */
	private void sortBrokenFlush() {
		
		for(int i = 0; i < HandOfCards.HAND_SIZE - 1; i++) {
			if(hand.isCardBreakingFlush(i)) {
				for(int j = i; j < HandOfCards.HAND_SIZE - 1; j++)
					hand.swapCards(j, j + 1);
			}
		}
	}
}