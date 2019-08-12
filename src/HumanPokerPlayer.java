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
 * HumanPokerPlayer.java
 */

import java.io.IOException;

// Implements the abstract methods of PokerPlayer in such a way that gives/receives
// informtion/input from a human player either through the console, or Twitter

public class HumanPokerPlayer extends PokerPlayer {
	
	private Communicator communicator;
	
	public HumanPokerPlayer(String name, Communicator comm) {
		super(name);
		communicator = comm;
	}
	
	@Override
	public void dealNewHand(DeckOfCards deck) {
		hand = new HandOfCards(deck);
		displayHand = new HandOfCards(hand.getDisplayHand(), false);
		createHandImage();
		communicator.sendInstructionToPlayer("You have been dealt the following hand: " + displayHand,"", true);
	}
	
	public boolean isHuman() {
		return true;
	}
	
	@Override
	public int discard() {
		communicator.sendInstructionToPlayer("Which cards would you like to discard? (e.g. \"1, 3\" or \"0\" to not discard any) ","Tweet #DiscardPS followed by your choice.", false);
		boolean validAnswer = false;
		String[] discardedCards = null;

		while (!validAnswer) {												// Loop until a valid answer is received
			String input = communicator.getPlayerInput();
			discardedCards = input.split(",");								// Create array of numbers given
			
			if (discardedCards[0].trim().equalsIgnoreCase("0") && discardedCards.length == 1) {	// If they decide not to dicard any
				return 0;
			}
			
			if (discardedCards.length < 4) {								// Iterate through the list of numbers and discard that card
				for (String card : discardedCards) {
					try {
						int displayDiscardNum = Integer.parseInt(card.trim()) - 1;
						hand.discard(getActualDiscardNum(displayDiscardNum));
						validAnswer = true;
					} catch (NumberFormatException e) {
						communicator.sendInstructionToPlayer("Invalid input. Please enter numbers between 0 and 5","", false);
					} catch(ArrayIndexOutOfBoundsException e) {
						communicator.sendInstructionToPlayer("Invalid input. Please enter numbers between 0 and 5","", false);
					}
				}
			} else {
				communicator.sendInstructionToPlayer("You can only discard up to 3 cards. Please enter a different value.","", false);
			}
			
		}
		
		updateHand();		// Sort hand and recompute new hex values
		createHandImage();	// Send image if playing on Twitter
		communicator.sendInstructionToPlayer("Your new hand is: " + displayHand + "\n","", true);
		return discardedCards.length;	
	}
	
	private void updateHand() {
		hand.update();
		displayHand = new HandOfCards(hand.getDisplayHand(), false);
	}
	
	/**
	 * Finds the position of the card that the player has chosen to discard 
	 * in the display hand that they see, in the actual hand
	 * @param displayDiscardNum
	 * @return
	 */
	private int getActualDiscardNum(int displayDiscardNum) {
		for (int i = 0; i < HandOfCards.HAND_SIZE; i++) {
			if (hand.getCard(i) == displayHand.getCard(displayDiscardNum)) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public boolean fold(int minimumBet, int numOfPlayers, int pot) {
		communicator.sendInstructionToPlayer("Would you like to fold? (y/n) ", "Tweet #FoldPS followed by your choice.", false);
		
		while(true) {										
			String input = communicator.getPlayerInput();
			
			if(input.trim().equalsIgnoreCase("y") || input.trim().equalsIgnoreCase("yes")) {
				return true;
			}
			else if(input.trim().equalsIgnoreCase("n") || input.trim().equalsIgnoreCase("no")) {
				return false;
			}
			else{
				communicator.sendInstructionToPlayer("Invalid input. Please reply with 'y' or 'n'","", false);
			}
		}
	}
	
	@Override
	public int bet(int minimumBet, int maximumBet, int numOfPlayers, int pot) {
		communicator.sendInstructionToPlayer("You have " + bank + " chips. You must bet at least " + minimumBet +
							" and no more than " + maximumBet + ". What would you like to bet? ", "Tweet #BetPS and your bet", false);
		while(true) {
			String input = communicator.getPlayerInput();
			
			try {
				int bet = Integer.parseInt(input.replace(" ", ""));
				
				if(bet < minimumBet || bet > maximumBet) {
					communicator.sendInstructionToPlayer("You must bet at least " + minimumBet + " chips to match the other "
							+ "player(s), and no more than " + maximumBet + ".","", false);
				}
				else if(bet >= minimumBet && bet <= bank) {
					bank -= bet;
					return bet;
				}
			} catch (NumberFormatException e) {
				communicator.sendInstructionToPlayer("Invalid input. Please enter numbers between " + minimumBet + " and " +  maximumBet + ".","", false);
			}
		}
	}
	
	private void createHandImage() {
		try {
			HandImage.create(displayHand);
		} catch (IOException e) {
			System.out.println("ERROR: Image of player's hand not created.");
			e.printStackTrace();
		}
	}
}