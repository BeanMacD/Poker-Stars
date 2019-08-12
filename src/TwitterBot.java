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
 * TwitterBot.java
 */

import java.io.File;
import java.util.ArrayList;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

public class TwitterBot {
	
	// Change key selector when rate limit reached
	private static final String[] CONSUMER_KEYS = {"rD7FHKyPWiCVGK3dzoirfyFIw", "4OsoICZReom479K9cZvKelguV", "NBzWyUa95oBMPv940WJt9ngFL"};
	private static final String[] CONSUMER_KEY_SECRETS = {"OHrNc7qpUar5cqcMoLTT1Y9C1NbsjrnhFx9T8f8YqnbY2sVRyy", "Q3IIsyqndxzNiy4rKTrql1XM2tAhEc6El8Hdz8hrqkyTJq2ah5", "BXZ61ZWXmKqWtyf9ppCTLR11FLSgj42PQisyZ95F7TjtT8W6hA"};
	private static final String[] ACCESS_TOKENS = {"852164097990905856-OeOeNBRkvxdD87pCXZep1IYWUmlDvpg", "852164097990905856-amxp7ZLDV3Cogc92Qg01Mq0yr1Tpn7c", "852164097990905856-xu1O8SLfqzBtKymAKeBSXHIeBv5bItM"};
	private static final String[] ACCESS_TOKEN_SECRETS = {"VIcNlZePoIq1Y9zadKeoDoIN0dhVkkkH9De7fQ59joah0", "nQFo8BtRtemcuiArpY2e33wNgRS7QjZoHp94YamiW4l0I", "TbmnN8CSQxKM68jKPeGuMXOnabuI9IuU20tD6G2rAQCtg"};
	private static int KEY_SELECTOR = 1;
	
	private static final String CONSUMER_KEY = CONSUMER_KEYS[KEY_SELECTOR]; 
	private static final String CONSUMER_KEY_SECRET = CONSUMER_KEY_SECRETS[KEY_SELECTOR];
	private static final String ACCESS_TOKEN = ACCESS_TOKENS[KEY_SELECTOR];
	private static final String ACCESS_TOKEN_SECRET = ACCESS_TOKEN_SECRETS[KEY_SELECTOR];
	
	private static final String HASHTAG = "#DealMeInPokerStars";		// Hashtag used to begin game
	public static final String STOP_HASHTAG = "#QuitPS";				// Hashtag used to quit game at any time
	
	private static final String[] QUERIES = {HASHTAG, STOP_HASHTAG, "#DiscardPS", "#FoldPS", "#BetPS"};	// Specific hashtags to filter 

	
	private Twitter twitter;				// Used to send tweets
	private TwitterStream twitterStream;	// Used to filter tweets
	private ArrayList<Status> tweets;		// List of all tweets filtered to be accessed and proccessed by Communicators
	
	public TwitterBot() {
		new TwitterFactory();
		new TwitterStreamFactory();
		twitter = TwitterFactory.getSingleton();
		twitterStream = TwitterStreamFactory.getSingleton();
		tweets = new ArrayList<Status>();
		setKeys();
		installListener();
		launchTwitterBot();
	}
	
	private void launchTwitterBot() {
		ArrayList<String> queries = new ArrayList<>();
		for (String query : QUERIES) {
			queries.add(query);					// List of hashtags with and without lower case letters
			queries.add(query.toLowerCase());	// to allow the user to enter either
		}
		FilterQuery filterQuery = new FilterQuery(queries.toArray(new String[0]));
		twitterStream.filter(filterQuery);		// Begin streaming for relevant hashtags
	}
	
	private void launchGame(Status tweet) {		// Launch a new game in a new thread 
		new GameOfPoker(this, tweet);
	}
	
	public ArrayList<Status> getTweets() {		// Return the current list of filtered tweets (used by Communicators)
		return tweets;
	}
	
	public void removeTweet(Status tweet) {		// Remove tweet from list (Used when tweet has been parsed by Communicator)
		tweets.remove(tweet);
	}
	
	public Status replyToTweet(Status tweetToReplyTo, String reply, boolean withImage) {		// Send reply to given tweet
		Status sentReply = null;																// with or without image 
		StatusUpdate replyTweet = new StatusUpdate(reply);
		replyTweet.inReplyToStatusId(tweetToReplyTo.getId());
		if (withImage) {
			replyTweet.setMedia(new File("cardImages/hand.png"));
		}
		try {
			sentReply = twitter.updateStatus(replyTweet);
			System.out.println("TWEET SENT: " + reply + "\n");		
		} catch (TwitterException e) {
			System.out.println("TWEET NOT SENT: " + reply);
			e.printStackTrace();
		}
		return sentReply;
	}
	
	private void installListener() {									// Add listener to TwitterStream
		twitterStream.addListener(new StatusListener() {
			@Override
			public void onStatus(Status tweet) {															// When tweet is found
				System.out.println("@" + tweet.getUser().getScreenName() + " " + tweet.getText());			// Print tweet to console
				if (tweet.getText().contains(HASHTAG) || tweet.getText().contains(HASHTAG.toLowerCase())) {	// Check if it is tweet to begin new game
					launchGame(tweet);
				} else {																					// Otherwise add to list to be processed by Communicators
					tweets.add(tweet); 
				}
			}
			@Override
			public void onException(Exception arg0) {}
			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {}
			@Override
			public void onScrubGeo(long arg0, long arg1) {}
			@Override
			public void onStallWarning(StallWarning arg0) {}
			@Override
			public void onTrackLimitationNotice(int arg0) {}
		});
	}
	
	private void setKeys() {
		twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_KEY_SECRET);
		twitter.setOAuthAccessToken(new AccessToken(ACCESS_TOKEN, ACCESS_TOKEN_SECRET));
		twitterStream.setOAuthConsumer(CONSUMER_KEY, CONSUMER_KEY_SECRET);
		twitterStream.setOAuthAccessToken(new AccessToken(ACCESS_TOKEN, ACCESS_TOKEN_SECRET));
	}
}
