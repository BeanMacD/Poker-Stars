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
 * PokerPlayer.java
 */

// Provides an interface where abstract methods must be overridden by subclasses
// and methods defined here do not need to be implemented in the subclass

public abstract class PokerPlayer {

	protected String name;
	protected HandOfCards hand;
	protected HandOfCards displayHand;
	protected int bank;
	static private final int HIGH_HAND_VALUE = 0;
	static private final int STARTING_BANK = 20;
	
	protected PokerPlayer(String name) {
		this.name = name;
		this.bank = STARTING_BANK;
	}
	
	public String getName() {
		return name;
	}
	
	public HandOfCards getHand() {
		return hand;
	}
	
	public HandOfCards getDisplayHand() {
		return displayHand;
	}
	
	public int getBank() {
		return bank;
	}
	
	public int open() {
		return --bank;
	}
	
	public boolean canOpen() {
		return hand.getHandValue() > HIGH_HAND_VALUE;	// Can open if hand is a One Pair or better
	}
	
	public boolean isBankrupt() {
		return bank == 0;
	}
	
	public void addWinnings(int pot) {
		bank += pot;
	}

	public abstract void dealNewHand(DeckOfCards deck);
	
	public abstract boolean isHuman();
	
	public abstract boolean fold(int minimumBet, int numOfPlayers, int pot);
	
	public abstract int bet(int minimumBet, int maximumBet,  int numOfPlayers, int pot);
	
	public abstract int discard();
}
