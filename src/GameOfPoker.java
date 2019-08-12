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
 * GameOfPoker.java
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import twitter4j.Status;

public class GameOfPoker implements Runnable {
	
	/*
	 * Show chips in bank
	 * Deal hands
	 * Check who can open
	 * Discard cards
	 * Betting/Folding
	 */

	private static final int BAD_BOT_ORIGIN = 0;
	private static final int BAD_BOT_BOUND = 7;
	private static final int AVERAGE_BOT_ORIGIN = 7;
	private static final int AVERAGE_BOT_BOUND = 11;
	private static final int GOOD_BOT_ORIGIN = 11;
	private static final int GOOD_BOT_BOUND = 16;
	
	private Thread thread;											// Thread that this game is run in
	private ArrayList<PokerPlayer> players = new ArrayList<>();		// List of all players
	private ArrayList<PokerPlayer> remainingPlayers;				// List of players who havent folded or gona bankrupt
	private HumanPokerPlayer humanPlayer;							
	private int pot;	
	private boolean gameOver = false;
	
	private Communicator communicator;
	private TwitterBot twitterBot;
	private Status tweet;
	
	public GameOfPoker(TwitterBot twitter, Status twt) {	// Constructor when played through Twitter
		twitterBot = twitter;
		tweet = twt;
		if (thread == null) {			
			thread = new Thread(this);		// Start it in a new thread
			thread.start();
		}
	}
	
	public GameOfPoker(String name) {						// Constructor when played through Console
		communicator = new consoleCommunicator();
		addPlayers(name);
		newGame();
	}
	
	/**
	 * Creat bots and add them and human player to the game
	 * @param name	Name of human players
	 */
	private void addPlayers(String name) {					// To do, ask the user for difficulty level and choose based on that
		humanPlayer = new HumanPokerPlayer(name, communicator);
		AutomatedPokerPlayer bot1 = new AutomatedPokerPlayer(Bot.values()[ThreadLocalRandom.current().nextInt(BAD_BOT_ORIGIN, BAD_BOT_BOUND)]);				// Add one player of low intelligence
		AutomatedPokerPlayer bot2 = new AutomatedPokerPlayer(Bot.values()[ThreadLocalRandom.current().nextInt(AVERAGE_BOT_ORIGIN, AVERAGE_BOT_BOUND)]);		// one of average
		AutomatedPokerPlayer bot3 = new AutomatedPokerPlayer(Bot.values()[ThreadLocalRandom.current().nextInt(GOOD_BOT_ORIGIN, GOOD_BOT_BOUND)]);			// one og high
		
		players.add(humanPlayer);
		players.add(bot1);
		players.add(bot2);
		players.add(bot3);
	}
	
	/**
	 * Begin a new game
	 */
	private void newGame() {
		communicator.sendInstructionToPlayer("Let's play Poker!", "", false);
		
		PokerPlayer winner;
		
		while(!gameOver) {
			// Start a new round
			// If someone can open then get the winner after the round
			// If no one can open then skip the rest of the loop and start a new round
			if(newRound(new DeckOfCards()))
				winner = getWinner();
			else {
				communicator.sendInstructionToPlayer("No one can open. The deck will be reshuffled and new hands will be dealt", "", false);
				continue;
			}
			
			// Show hands
			showHands();

			// Declare winner of the round
			communicator.sendInstructionToPlayer(winner.getName() + " wins this round! Let's deal the next round.", "\n\nRemember, you can quit at anytime using #QuitPS", false);
			
			// Add pot to winners bank
			winner.addWinnings(pot);
			
			// Remove bankrupt players
			players.removeAll(bankruptPlayers());

			// Check if the game is over
			gameOver = checkGameOver();
			
			// Change the dealer
			players.add(players.remove(0));
		}
	}

	/**
	 * @return	last remaining player, or winner of last round if human is eliminated
	 */
	private PokerPlayer getWinner() {
		PokerPlayer winner = remainingPlayers.get(0);		// Get the first player of the list 
		
		for(PokerPlayer player : remainingPlayers) {		// If there is more than one player, get the plyayer that one the last round
			if(player.getHand().getGameValue() > winner.getHand().getGameValue()) 
				winner = player;
		}
		
		return winner;
	}
	
	/**
	 * @return	the player who currently has the most chips - used to declare the winner when
	 * 			the human is eliminated
	 */
	private PokerPlayer getPlayerWithMostChips() {
		PokerPlayer mostChips = remainingPlayers.get(0);	// Get the first player of the list 
		
		for(PokerPlayer player : remainingPlayers) {		// If there is more than one player, get the plyayer that one the last round
			if(player.getBank() > mostChips.getBank()) 		// Only used to give a winner when human goes bankrupt
				mostChips = player;
		}
		
		return mostChips;
	}
	
	/**
	 * Display hands of all players when round is over
	 */
	private void showHands() {
		String hands = "";
		for(PokerPlayer player : players) {
			hands += player.getName() + " had " + player.getHand() + "\n"; 
		}
		createHandsImage();
		communicator.sendInstructionToPlayer(hands,"", true);
	}

	/**
	 * @return 	list of all players who have no more chips left
	 */
	private ArrayList<PokerPlayer> bankruptPlayers() {
		ArrayList<PokerPlayer> bankruptPlayers = new ArrayList<>();
		
		String bankruptString = "";
		for(PokerPlayer player : players) {
			if(player.isBankrupt()) {
				bankruptString += player.getName() + " is bankrupt and has been eliminated! ";
				bankruptPlayers.add(player);
			}
		}
		
		if(bankruptPlayers.size() != 0) {
			communicator.sendInstructionToPlayer(bankruptString,"", false);
		}
		
		return bankruptPlayers;
	}

	/**
	 * @return	true if there is one player left, or if human has been eliminated
	 */
	private boolean checkGameOver() {
		
		if(players.size() == 1) {
			communicator.sendInstructionToPlayer("***GAME OVER*** " + players.get(0).getName() + " wins the game!","", false);
			return true;
		}
		
		// Check if the human player is bankrupt and end the game if they are
		if(humanPlayer.isBankrupt()) {
			communicator.sendInstructionToPlayer("***GAME OVER***\nYou have gone bankrupt. Goodbye! " + getPlayerWithMostChips().getName() + " wins the game!","", false);
			return true;
		}
		
		return false;
	}
	
	/**
	 *	Begin a new round with new hands dealt
	 * @param deck
	 * @return	false if no one can open with a pair or higher
	 */
	private boolean newRound(DeckOfCards deck) {
		// Initialise remainingPlayers and the pot
		remainingPlayers = new ArrayList<>(players);
		pot = 0;
		
		// Show Chips
		showChips();
		
		// Deal Hands
		for(PokerPlayer player : players)
			player.dealNewHand(deck);
		
		// Everyone opens with one chip
		if(canOpen())
			open();	
		else
			return false;

		// Discarding
		String discardString = "";
		for(PokerPlayer player : remainingPlayers) {
			discardString += player.getName() + " discards " + player.discard() + " card(s).\n";
		}
		communicator.sendInstructionToPlayer(discardString,"", false);
		
		// Betting
		bet();
		
		return true;
	}

	/**
	 * Output the amount of chips that each player has
	 */
	private void showChips() {
		String bankString = "";
		for(PokerPlayer player : players) {
			bankString += player.getName() + " has got " + player.getBank() + " chip(s).\n";
		}
		bankString = bankString.substring(0, bankString.length() - 1);	// Removes last \n
		communicator.sendInstructionToPlayer(bankString,"", false);
	}
	
	/**
	 * @return true if any player has a pair or better
	 */
	private boolean canOpen() {
		for(PokerPlayer player : players) {
			if(player.canOpen()) {
				communicator.sendInstructionToPlayer(player.getName() + " can open. Everyone bets 1 chip to start.","", false);
				remainingPlayers.remove(player);
				remainingPlayers.add(0, player);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Each player opens with one chip
	 */
	private void open() {
		for(PokerPlayer player : remainingPlayers) {
			pot += player.open();
		}
	}
	
	/**
	 * Sequence of betting until no player raises
	 */
	private void bet() {
		HashMap<PokerPlayer, Integer> bets = new HashMap<>();
		int bet, minimumBet, originalMaximumBet = getMaxBet(), maximumBet, highestBet = 0;
		int playerIndex = 0;
		boolean firstRoundOver = false; // Ensures betting doesn't finish until everybody has been asked to bet or fold at least once
		PokerPlayer player;
		pot = 0;
		
		// Intialise all bets to 0
		for(PokerPlayer remainingPlayer : remainingPlayers)
			bets.put(remainingPlayer, 0);
		
		// Continue betting until everyone calls or folds
		while(!bettingFinished(bets, firstRoundOver)) {
			player = remainingPlayers.get(playerIndex);
			minimumBet = highestBet - bets.get(player);
			maximumBet = originalMaximumBet - bets.get(player);
			
			// Ask the player is they want to fold
			// Remove them, update and skip to next player if they do
			if(player.fold(minimumBet, remainingPlayers.size(), pot)) {
				communicator.sendInstructionToPlayer(player.getName() + " has chosen to fold.","", false);
				remainingPlayers.remove(player);
				bets.remove(player);
				
				if(remainingPlayers.size() == 1)
					break;
				
				if(playerIndex == remainingPlayers.size()) {
					playerIndex = 0;
					firstRoundOver = true;
				}
				
				continue;
			}

			bet = player.bet(minimumBet, maximumBet, remainingPlayers.size(), pot);
			bets.put(player, bets.get(player) + bet);
			pot += bet;
			
			communicator.sendInstructionToPlayer(player.getName() + " bets " + bet + " chip(s)","", false);

			// Update highestBet
			if(bet > highestBet)
				highestBet = bet;

			// If each player has bet or folded then go back to the first player again
			// Otherwise, go to the next player
			if(playerIndex == remainingPlayers.size() - 1) {
				playerIndex = 0;
				firstRoundOver = true;
			}
			else
				playerIndex++;
		}
	}
	
	/**
	 * @return the bank of the player with the least amount of chips
	 */
	private int getMaxBet() {
		int maximumBet = remainingPlayers.get(0).getBank();
		
		for(PokerPlayer player : remainingPlayers) {
			if(player.getBank() < maximumBet) 
				maximumBet = player.getBank();
		}
		
		return maximumBet;
	}

	/**
	 * @param bets
	 * @param firsRoundOver
	 * @return true if all players have been asked to bet in this round of betting
	 */
	private boolean bettingFinished(HashMap<PokerPlayer, Integer> bets, boolean firsRoundOver) {
		
		if(!firsRoundOver)
			return false;
		
		for(int bet : bets.values()) {
			if(bet != bets.get(remainingPlayers.get(0))) 
				return false;
		}
		
		return true;
	}
	
	@Override
	/**
	 * Launch new game in this thread with its own Communicator 
	 */
	public void run() {
		communicator = new TwitterCommunicator(twitterBot, tweet);
		addPlayers(tweet.getUser().getName());
		newGame();
	}
	
	public HumanPokerPlayer getHumanPlayer() {
		return humanPlayer;
	}
	
	/**
	 * Creates and stores image of all hands at the end of the round
	 */
	private void createHandsImage() {
		try {
			HandImage.createRoundHands(players);
		} catch (IOException e) {
			System.out.println("ERROR: Image of all hands not created.");
			e.printStackTrace();
		}
	}
}