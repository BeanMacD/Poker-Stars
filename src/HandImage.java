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
 * HandImage.java
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

// Static methods that create and store an image at 'cardImages/hand.png'
// of either a single hand of cards, or a list of several players' cards

public class HandImage {
	
	private static final int CARD_WIDTH = 500;
	private static final int CARD_HEIGHT = 726;
	private static final double ROTATION = 20.0;
	private static final int X_OFFSET = 249;		// x and y offsets are coded in for now, but need to be calculated
	private static final int Y_OFFSET = 140;		// to allow for different sized cards etc.
	private static final int HAND_SPACING = 50;
	private static final String DIRECTORY = "cardImages/";
	private static final String EXTENSION = ".png";
	
	public static void create(HandOfCards hand) throws IOException {
		// Width and height of entire image
		int handImageWidth = (int) (2 * (Math.sin(Math.toRadians(ROTATION)) * CARD_HEIGHT + Math.cos(Math.toRadians(ROTATION)) * CARD_WIDTH)- CARD_WIDTH + 2 * X_OFFSET);
		int handImageHeight = (int) (CARD_HEIGHT + Math.sin(Math.toRadians(ROTATION)) * CARD_WIDTH + Y_OFFSET); 
		BufferedImage handImage = new BufferedImage(handImageWidth, handImageHeight, BufferedImage.TYPE_INT_ARGB);	// Entire image
		Graphics2D graphics = (Graphics2D) handImage.getGraphics();													// Used to paint each card image ont this image
		
		int xPos = handImageWidth / 2 - CARD_WIDTH / 2;			// Top left point of each card before rotation
		int yPos = 0;											
		int xAnchor = 0; 										// Point around which the card is rotated
		int yAnchor = CARD_HEIGHT;
		

		double rotation = -ROTATION;															// Set rotation for first card
		for (int i = 0; i < HandOfCards.HAND_SIZE; i++) {										// Iterate through each card
			PlayingCard card = hand.getCard(i);														
			BufferedImage cardImage = ImageIO.read(new File(DIRECTORY + card + EXTENSION));		// Load image of this card
			
			AffineTransform transform = new AffineTransform();
		    transform.translate(X_OFFSET, Y_OFFSET);											// Allows for negative rotations (CCW)
		    transform.rotate(Math.toRadians(rotation), xAnchor, yAnchor);						// Rotate card
			AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);	// Apply translation and rotation
			cardImage = transformOp.filter(cardImage, null);									// Set as image
			
			graphics.drawImage(cardImage, xPos, yPos, null);									// Paint to entire image
			rotation += ROTATION / 2;															// Increment rotation for next card
		}
										// Hardcoded values again, must be calculated
		handImage = handImage.getSubimage(2 * X_OFFSET - 30, 0, 966, handImageHeight);			// Crop image after translation occurred
		ImageIO.write(handImage, "png", new File(DIRECTORY + "hand" + EXTENSION));				// Write to "cardImages/hand.png"
	}
	
	public static void createRoundHands(ArrayList<PokerPlayer> players) throws IOException {		//Similar algorithm to above, execpt it prints all the players' hands in the game with
		int handsImageWidth = CARD_WIDTH + CARD_WIDTH * HandOfCards.HAND_SIZE;						// each of the 5 cards side-by-side, no rotations
		int handsImageHeight = CARD_HEIGHT * players.size() + HAND_SPACING * (players.size() - 1);	// and the player's name beside each hand
		BufferedImage handsImage = new BufferedImage(handsImageWidth, handsImageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D handsImageGraphics = (Graphics2D) handsImage.getGraphics();
		handsImageGraphics.setPaint (new Color(0, 0, 0));
		handsImageGraphics.fillRect(0, 0, handsImageWidth, handsImageHeight);
		
		int xPosHand = 0;
		int yPosHand = 0;
		
		for (PokerPlayer player : players) {
			HandOfCards hand = player.getHand();
			BufferedImage handImage = new BufferedImage(CARD_WIDTH * (HandOfCards.HAND_SIZE + 1), CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			Graphics2D handImageGraphics = (Graphics2D) handImage.getGraphics();

			int xPosCard = CARD_WIDTH;
			int yPosCard = 0;
			
			for (int i = 0; i < HandOfCards.HAND_SIZE; i++) {
				PlayingCard card = hand.getCard(i);
				BufferedImage cardImage = ImageIO.read(new File(DIRECTORY + card + EXTENSION));
				handImageGraphics.drawImage(cardImage, xPosCard, yPosCard, null);
				xPosCard += CARD_WIDTH;
			}
			
			handImageGraphics.setPaint(Color.WHITE);
			handImageGraphics.setFont(new Font("SansSerif", Font.PLAIN, (int) (CARD_WIDTH * 0.2)));
			handImageGraphics.drawString(player.getName(), 0, (int) (CARD_HEIGHT * 0.55));
			
			handsImageGraphics.drawImage(handImage, xPosHand, yPosHand, null);

			yPosHand += CARD_HEIGHT + HAND_SPACING;
		}
		
		ImageIO.write(handsImage, "png", new File(DIRECTORY + "hand" + EXTENSION));
	}
}
