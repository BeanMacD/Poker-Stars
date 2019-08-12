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
 * Bot.java
 */
public enum Bot {
		
	//Predefined bots of which the constructor will only take
	DONALD("Donald", 0), LENNIE("Lennie", 10), HOMER("Homer", 10), BERT("Bert", 20), MARIO("Mario", 20), CALVIN("Calvin", 30), STARSKY("Starsky", 30), HUTCH("Hutch", 40), 
	ERNIE("Ernie", 50), LUIGI("Luigi", 60), HOBBES("Hobbes", 70), GEORGE("George", 80), BART("Bart", 80), Hillary("Hillary", 90), ALBERT("Albert", 90), DEXTER("Dexter", 100);
	
	private int intelligence;
	private String name;
	
	Bot(String name, int intelligence) {
		this.name = name;
		this.intelligence = intelligence;
	}
	
	public int getIntelligence() {
		return intelligence;
	}
	
	public String getName() {
		return name;
	}

}
