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
 * TwitterCommunicator.java
 */

import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.User;

// Implements the interface methods of Communicator in such a way that information is
// given/received through to Twitter

public class TwitterCommunicator implements Communicator {
	
	private TwitterBot twitterBot;		// TwitterBot to interact with to receive tweets and send replies through
	private Status mostRecentTweet;		// Last tweet sent/ received in this conversation
	private User twitterPlayer;			// Player that this Communicator is conversing with 

	public TwitterCommunicator(TwitterBot twitter, Status twt) {
		twitterBot = twitter;
		mostRecentTweet = twt;
		twitterPlayer = twt.getUser();
	}
	
	public void sendInstructionToPlayer(String instruction, String twitterAddition, boolean withImage) {		// Send tweet with/without image through TwitterBot
		String tweet = "@" + twitterPlayer.getScreenName() + " " + instruction + twitterAddition;
		if (withImage) mostRecentTweet = twitterBot.replyToTweet(mostRecentTweet, tweet, true);
		else mostRecentTweet = twitterBot.replyToTweet(mostRecentTweet, tweet, false);	// Store this as the last tweet in the conversation
		pause();
	}
	
	public String getPlayerInput() {
		String input = "";
		String[] inputSplit = {};
		boolean inputReceived = false;
		Status tweetToRemove = null;
		
		while (!inputReceived) {
			ArrayList<Status> tweets = twitterBot.getTweets();							// Get updated list of filtered tweets
			for (Status tweet : tweets) {												// Search through list
				if (tweet.getUser().equals(twitterPlayer) && tweet.getInReplyToStatusId() == mostRecentTweet.getId()) {	// If it is from the correct player and is in reply to the last tweet 
					inputReceived = true;
					mostRecentTweet = tweet;
					tweetToRemove = tweet;
					twitterBot.removeTweet(tweetToRemove);								// Remove tweet from filtered list as it is no longer needed
																						// If user has chosen to terminate the game
					if(tweet.getText().contains(TwitterBot.STOP_HASHTAG) || tweet.getText().contains(TwitterBot.STOP_HASHTAG.toLowerCase())) {
						terminateGame();
					}
					
					input = tweet.getText();
					inputSplit = input.split(" ");
					input = "";
					for (int i = 2; i < inputSplit.length; i++) {						// Parse necessary information in tweet
						input += inputSplit[i];
					}
					break;
				}
			}
			pause();
		}
		
		return input;
	}
	
	@SuppressWarnings("deprecation")	// Terminate the game by killing this thread at any point	
	private void terminateGame() {
		sendInstructionToPlayer("You have chosen to end the game. Goodbye.","", false);
		Thread.currentThread().stop();				// TO DO: Find a more suitable way to stop the game
	}
	
	private void pause() {				// Pauses execution of thread as send tweets in quick succession caused problems as mostRecentTweet
		try {							// was not updated quick enough
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("ERROR: Thread not sleeping.");
			e.printStackTrace();
		}
	}
	
}
