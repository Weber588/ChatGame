package wbs.chatgame.game;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import wbs.chatgame.ChatGame;
import wbs.chatgame.player.PlayerData;
import wbs.util.WbsStrings;

public class Game {
	
	/**************************** GAME SETTINGS ****************************/
	private static Map<GameType, GameSettings> settings = new HashMap<>();
	private static int betweenGames; // Number of ticks between rounds
	private static boolean doRewards;
	private static boolean doChallenges;
	
	public static boolean useGuessCommand;

	/**************************** WORD SETS ****************************/
	private static Map<String, Integer> unscramble = new HashMap<>();
	private static Set<MathType> math = new HashSet<>();
	private static Map<TriviaQuestion, Integer> trivia = new HashMap<>();
	private static Map<String, Integer> quicktype = new HashMap<>();
	private static Map<String, Integer> reveal = new HashMap<>();

	/**************************** GAME INFO ****************************/
	public static boolean isRunning = false;

	/**************************** CURRENT ROUND ****************************/
	private static GameType currentType;
	private static int currentTimerID;
	private static GameSettings currentSettings;
	private static boolean autoGeneratedWord = false;
	
	private static GameType nextType;
	
	public static boolean inRound;
	public static String currentQuestion;
	public static int currentPoints;
	public static Set<String> answers = new HashSet<>(); // The accepted answers
	public static LocalDateTime roundStartTime;

	/**************************** LAST ROUND ****************************/
	private static String lastWinner = "Nobody";
	private static Set<String> lastAnswers = new HashSet<>();

	/**************************** UTILITY ****************************/
	private final static Plugin pl = Bukkit.getPluginManager().getPlugin("ChatGame");
	private final static Logger logger = pl.getLogger();
	private final static Random random = new Random();

	/**************************** GAME STATES ****************************/

	public static void start() {
		if (isRunning) {
			logger.warning("Game attempted to start when in progress.");
			return;
		}
		ChatGame.broadcast("Game starting.");
		isRunning = true;
		nextRound();
	}
	public static void stop() {
		ChatGame.broadcast("Game stopping.");
		Bukkit.getScheduler().cancelTask(currentTimerID);
		isRunning = false;
		answers.clear();
	}
	
	// Get the remaining time until the start of the next round, or until the end of this round.
	public static String getTimeLeft() {
		return prettyTime(Duration.between(roundStartTime, LocalDateTime.now()));
	}
	
	/**************************** SETTERS ****************************/

	public static void nextType(GameType next) {
		nextType = next;
	}
	
	public static void clearLists() {
		unscramble.clear();
		math.clear();
		trivia.clear();
		quicktype.clear();
		reveal.clear();
	}
	
	/************ Global *************/
	public static void setTypeSettings(GameType type, GameSettings newSettings) {
		settings.put(type, newSettings);
	}
	/************ Game Types *************/
	public static void addUnscramble(String word, int points) {
		unscramble.put(word, points);
	}
	public static void addMath(MathType question) {
		math.add(question);
	}
	public static void addTrivia(TriviaQuestion question, int points) {
		trivia.put(question, points);
	}
	public static void addQuickType(String characterSet, int points) {
		quicktype.put(characterSet, points);
	}
	public static void addReveal(String word, int points) {
		reveal.put(word, points);
	}

	/************ Game *************/
	public static void setDurationBetweenGames(int seconds) {
		betweenGames = seconds * 20;
	}
	public static void toggleRewards(boolean rewards) {
		doRewards = rewards;
	}
	public static void toggleChallenges(boolean challenges) {
		doChallenges = challenges;
	}
	public static void toggleGuessCommand(boolean guessCommand) {
		useGuessCommand = guessCommand;
	}
	
	/**************************** PLAYER INTERACTION ****************************/
	
	public static String checkAnswer(String guess) {
		if (currentType == GameType.QUICKTYPE) {
			if ((boolean) settings.get(GameType.QUICKTYPE).getOption("match-case")) {
				for (String answer : answers) {
					if (guess.equals(answer)) { // Match case
						return answer;
					}
				}
				return null;
			}
		} else if (currentType == GameType.MATH) {
			for (String answer : answers) {
				try {
					if (Double.parseDouble(guess) == Double.parseDouble(answer)) {
						return answer;
					}
				} catch (NumberFormatException e) {
					continue;
				}
			}
			return null;
		}
		
		for (String answer : answers) {
			if (guess.equalsIgnoreCase(answer)) {
				return answer;
			}
		}
		return null;
	}
	
	public static void guess(Player player, String guess) {
		String answer = checkAnswer(guess);
		if (answer != null) {
			correct(player, answer);
		} else {
			incorrect(player);
		}
	}
	
	private static double roundTo(double number, int decimalPlaces) {
		return Math.round(number * (Math.pow(10, decimalPlaces)))/Math.pow(10, decimalPlaces);
	}
	
	private static String prettyTime(Duration duration) {
		String prettyTime = null;
		double inMillis = duration.toMillis();

		prettyTime = roundTo((inMillis % 60000)/1000, 2) + " seconds";
		if (inMillis > 60000) {
			int minutes = ((int) inMillis/60000);
			if (minutes == 1 || minutes == 0) {
				prettyTime = minutes + " minute and " + prettyTime;
			} else {
				prettyTime = minutes + " minutes and " + prettyTime;
			}
		}
		
		return prettyTime;
	}
	
	private static void correct(Player player, String answer) {
		int seconds = (int) Duration.between(roundStartTime, LocalDateTime.now()).toMillis()/1000;
		String prettyTime = prettyTime(Duration.between(roundStartTime, LocalDateTime.now()));
		switch (currentType) {
		case UNSCRAMBLE:
		case MATH:
		case TRIVIA:
		case REVEAL:
			ChatGame.broadcast(player.getName() + " won in " + prettyTime + "! The answer was: &h" + WbsStrings.capitalizeAll(answer));
			break;
		default:
			ChatGame.broadcast(player.getName() + " won in " + prettyTime + "!");
		}
		
		PlayerData data = PlayerData.getPlayerData(player);
		data.addWin(currentType, currentPoints, seconds);
		
		endRound(player.getName());
	}
	private static void incorrect(Player player) {
		if (useGuessCommand) {
			ChatGame.sendMessage("&wIncorrect.", player);

			PlayerData data = PlayerData.getPlayerData(player);
			data.addLoss(currentType); // Don't add incorrect if the guess command isn't required; otherwise chatting normally will affect scores
		}
	}

	/**************************** ROUNDS ****************************/
	public static void skipRound() {
		if (inRound) {
			endRound();
		}
		Bukkit.getScheduler().cancelTask(currentTimerID); // Cancel the current timer
		ChatGame.broadcast("Skipping to next round. New question:");
		nextRound();
	}
	
	private static void endRound() {
		endRound("Nobody");
	}
	
	private static void endRound(String winner) {
		lastAnswers.clear();
		lastAnswers.addAll(answers);
		
		answers.clear(); // Remove all answers in preparation for the next round.
		Bukkit.getScheduler().cancelTask(currentTimerID); // Stop the current rounds timeout
		inRound = false;
		
		lastWinner = winner;
		
		currentTimerID = new BukkitRunnable() {
            public void run() {
        		if (isRunning) {
        			nextRound();
        		}
            }
        }.runTaskLater(pl, betweenGames).getTaskId();
	}
	
	private static void nextRound() {
		roundStartTime = LocalDateTime.now();
		inRound = true;
		if (nextType == null) {
			
			/* Create a pool; The pool represents a line segment spanning 1 to n as the summation of the chances for each. Ideally this
			 * would sum to 1 if chances have been calculated correctly, or to 100 if using percentages, but by rescaling it means
			 * it doesn't matter what is used - it work with any numbers correctly.
			 * 
			 * A random point along the line segment will be chosen and the game type that was added at that point will be selected.
			 */
			double point = Math.random();
			
			double pool = 0;
			for (GameType type : GameType.values()) {
				pool += settings.get(type).chance;
			}
			double scaleFactor = pool;
			pool = 0;
			// Redo the pool creation but scaled so it'll end on 1. When it exceeds "chance", the random type has been selected.
			for (GameType type : GameType.values()) {
				pool += settings.get(type).chance/scaleFactor;
				if (pool > point) {
					currentType = type;
					break;
				}
			}
		} else {
			currentType = nextType;
			nextType = null;
		}
		startRound();
	}
	
	private static void startRound() {
		currentSettings= settings.get(currentType);
		
		switch (currentType) {
		case UNSCRAMBLE: 
			unscramble();
			break;
		case MATH: 
			math();
			break;
		case TRIVIA: 
			trivia();
			break;
		case QUICKTYPE: 
			quicktype();
			break;
		case REVEAL: 
			reveal();
			break;
		default:
			pl.getLogger().warning("Something went wrong. Defaulting to scramble");
			unscramble();
		}
		
		System.out.println(answers.toString());
	}

	/**************************** UTILITY ****************************/
	// returns random int in the range [0, max)
	private static int randomInt(int max) {
		return (int) Math.floor(Math.random() * max);
	}
	
	private static String pointsDisplay(int points) {
		if (points == -1 || points == 1) {
			return points + " point";
		}
		return points + " points";
	}
	
	private static String getRandomWord(Map<String, Integer> wordSet) {
		String genType = currentSettings.useGeneration();
		if (genType == null) { // Use provided list
			autoGeneratedWord = false;
			Set<String> words = wordSet.keySet();
			String[] wordArray = new String[words.size()];
			wordArray = words.toArray(wordArray);
			return wordArray[randomInt(words.size())];
		} else { // See what type of generation to use, and pull from RandomMinecraft
			String word = null;
			switch (genType.toLowerCase()) {
			case "random-material":
				boolean variations = (boolean) currentSettings.getOption("include-variations");
				boolean colours = (boolean) currentSettings.getOption("include-coloured");
				boolean tools = (boolean) currentSettings.getOption("verbose-tools");
				boolean armour = (boolean) currentSettings.getOption("verbose-armour");
				
				word = RandomMinecraft.getRandomMaterial(variations, colours, tools, armour);
				break;
			case "random-mob":
				word = RandomMinecraft.getRandomMob();
				break;
			case "random-enchantment":
				word = RandomMinecraft.getRandomEnchantment();
				break;
			case "random-biome":
				word = RandomMinecraft.getRandomBiome();
				break;
			default: // Use provided word list
				Set<String> words = wordSet.keySet();
				String[] wordArray = new String[words.size()];
				wordArray = words.toArray(wordArray);
				return wordArray[randomInt(words.size())];
			}
			
			autoGeneratedWord = true;
			return word;
		}
	}
	
	private static Player randomOnlinePlayer() {
		Collection<? extends Player> online = Bukkit.getOnlinePlayers();
		int chosen = randomInt(online.size());
		int index = 0;
		for (Player player : online) {
			if (index == chosen) {
				return player;
			}
			index++;
		}
		return null;
	}
	
	private static boolean chance(double percentage) {
		return (Math.random() < percentage/100);
	}

	/**************************** GAME HANDLERS ****************************/

	/************** Scramble *************/
	
	private static void unscramble() {
		String challenge = currentSettings.useChallenge();
		String scrambled = null;
		
		String challengeString = "";
		
		if (challenge != null) { // Use a challenge
			switch (challenge.toLowerCase()) {
			case "username":
				Player random = randomOnlinePlayer();
				if (random == null) {
					break;
				}
				scrambled = randomOnlinePlayer().getName();
				challengeString = " This scramble is an online players name!";
				currentPoints = 2;
				break;
			default: // Don't need to cancel as a normal game will occur.
				logger.warning("An unknown challenge was parsed into the " + currentType + " game (" + challenge + ")");
			}
		}
		if (scrambled == null) {
			scrambled = getRandomWord(unscramble);
			if (autoGeneratedWord) {
				scrambled = formatScramble(scrambled);
				scrambleCost(scrambled);
			} else {
				currentPoints = unscramble.get(scrambled);
			}
		}

		final String answer = scrambled;
		answers.add(answer); // Add before scrambling.
		scrambled = scrambleString(scrambled);
		
		currentQuestion = "Unscramble \"&h" + scrambled + "&r\" for " + pointsDisplay(currentPoints) + "!" + challengeString;
		ChatGame.broadcast(currentQuestion);

		int secondsUntilHint = (int) currentSettings.getOption("seconds-until-hint");
		if (secondsUntilHint == 0) {
			currentTimerID = new BukkitRunnable() {
	            public void run() {
	    			ChatGame.broadcast("Nobody got the word in time :(");
	    			endRound();
	            }
	        }.runTaskLater(pl, currentSettings.duration * 20).getTaskId();
		} else {
			currentTimerID = new BukkitRunnable() {
	            public void run() {
	            	if (answer.contains(" ")) {
	            		String[] words = answer.split(" ");
	            		for (int i = 0; i < words.length; i++) {
	            			words[i] = scrambleString(words[i]);
	            		}
	            		String hint = String.join(" ", words);
	            		ChatGame.broadcast("Too hard? Here are the words scrambled individually: \"&h" + hint + "&r\"");
	            		currentPoints--;
	            	} else {
	            		if (answer.toLowerCase().equals(answer)) { // If the answer has no upper case letters
	            			String hint = WbsStrings.capitalizeAll(answer);
	            			hint = scrambleString(hint);
		            		ChatGame.broadcast("Hint: \"&h" + hint + "&r\"");
	            		} else { // The answer is one word with the first letter capitalized already
	            			String start = answer.substring(0, 2);
	            			if (answer.length() > 6) {
	            				String end = answer.substring(answer.length() - 2);
			            		ChatGame.broadcast("Hint: \"&h" + start + answer.substring(2, answer.length() - 3).replaceAll(".?", "_") + end + "&r\"");
	            			} else {
			            		ChatGame.broadcast("Hint: \"&h" + start + answer.substring(2, answer.length() - 1).replaceAll(".?", "_") +"&r\"");
	            			}
	            		}
	            	}

	    			currentTimerID = new BukkitRunnable() {
	    	            public void run() {
	    	    			ChatGame.broadcast("Nobody got the word in time :(");
	    	    			endRound();
	    	            }
	    	        }.runTaskLater(pl, (currentSettings.duration - secondsUntilHint) * 20).getTaskId();
	            }
	        }.runTaskLater(pl, secondsUntilHint * 20).getTaskId();
		}
	}
	private static String scrambleString(String input) {
		char[] letters = input.toCharArray();
		List<Character> lettersList = new ArrayList<>();
		for (char letter : letters) {
			lettersList.add(letter);
		}
		String output = null;
		int escape = 0; // Emergency variable - Extremely low chance that shuffling 100 times in a row always has two+ spac
		// However, if a word has n+2 spaces where n is the number of non-space characters, it will need an escape variable.
		do {
			escape++;
			Collections.shuffle(lettersList);
			int index = 0;
			for (char letter : lettersList) {
				letters[index] = letter;
				index++;
			}
			output = new String(letters);
		} while (output.contains("  ") && escape < 100); // Keep scrambling until there are no repeated spaces.
		
		// if escape is 100 here, it was likely impossible to make the string have no consecutive spaces, just return the string
		
		return output;
	}
	private static String formatScramble(String word) {
		int length = word.length();
		if (length > 8 && length < 12) {
			word = WbsStrings.capitalize(word);
			if (chance(50)) {
				currentPoints--;
			}
		} else if (length >= 12) {
			word = WbsStrings.capitalizeAll(word);
		}
		
		return word;
	}
	// Auto capitalizes based on how long the word is.
	// Also decides how many points a word should be worth
	private static void scrambleCost(String word) {
		int length = word.length();
		
		currentPoints = Math.max(1, (length/4));
		if (word.contains(" ")) {
			currentPoints++;
		}
	}

	/************** Math *************/
	
	private static void math() {
		
		MathType type = getMathType();
		
		int length;
		if (type.max < type.min) {
			logger.warning("A math type was configured incorrectly; please ensure that max-numbers is greater than or equal to min-numbers");
			length = type.max;
			return;
		} else if (type.max == type.min) {
			length = type.min;
		} else {
			length = randomInt(type.max - type.min)+type.min;
		}
		
		String[] numberStrings = new String[length];
		int[] numbers = new int[length];
		
		if ((boolean) currentSettings.getOption("allow-zero")) {
			for (int i = 0; i < length; i++) {
				numbers[i] = randomInt(type.maxValue+1);
				numberStrings[i] = Integer.toString(numbers[i]);
			}
		} else {
			for (int i = 0; i < length; i++) {
				numbers[i] = randomInt(type.maxValue) + 1;
				numberStrings[i] = Integer.toString(numbers[i]);
			}
		}
		
		Set<Character> operations = new HashSet<>();
		if (type.addition) {
			operations.add('+');
		}
		if (type.subtraction) {
			operations.add('-');
		}
		if (type.multiplication) {
			operations.add('*');
		}
		if (type.division) {
			operations.add('/');
		}
		
		String questionString = String.join(" _ ", numberStrings); // Use _ as a placeholder 	
		String operationsPrompt = ""; // Either "(left to right) " or "(using PEDMAS) " - Or invisible if no * or /

		boolean usePedmas = false;
		if (type.multiplication || type.division) {
			if ((boolean) currentSettings.getOption("use-both")) { // Choose randomly
				usePedmas = chance(50);
			} else { // Choose users choice
				usePedmas = (boolean) currentSettings.getOption("order-of-operations");
			}
		} else { // Doesn't matter: do left to right because faster to calculate
			usePedmas = false;
		}
		
		currentPoints = 1;
		double total = numbers[0];
		if (usePedmas) {
			
		} else { // Calculate left to right
			for (int i = 1; i < length; i++) { // Starting at 1 since the first number is total already.
				char operation = getOperation(operations);
				switch (operation) {
				case '+': 
					total += numbers[i];
					currentPoints += Math.max(0, (Math.round(Math.log10(Math.abs(total)))-2));
					break;
				case '-':
					total -= numbers[i];
					break;
				case '*':
					total *= numbers[i];
					if (total != 0) {
						currentPoints += Math.max(0, (Math.round(Math.log10(Math.abs(total)))-1));
					}
					break;
				case '/':
					if (chance(200/currentPoints)) {
						currentPoints++;
					}
					total /= numbers[i];
					break;
				}
				questionString = questionString.replaceFirst("_", Character.toString(operation));
			}
		}
		
		if (questionString.contains("*") || questionString.contains("/")) {
			if (usePedmas) {
				operationsPrompt = "(using PEDMAS) ";
			} else {
				operationsPrompt = "(left to right) ";
			}
		}
		
		total = roundTo(total, 2);
		
		String answerString = Double.toString(total);
		if (answerString.endsWith(".0")) {
			answerString = answerString.substring(0, answerString.length()-2);
		}
		answers.add(answerString);
		
		currentQuestion = "Solve \"&h" + questionString + "&r\" " + operationsPrompt + "for " + pointsDisplay(currentPoints) + "!";
		ChatGame.broadcast(currentQuestion);
		
		currentTimerID = new BukkitRunnable() {
            public void run() {
    			ChatGame.broadcast("Nobody answered in time :(");
    			endRound();
            }
        }.runTaskLater(pl, currentSettings.duration * 20).getTaskId();
	}
	
	private static MathType getMathType() {

		// Line segment weighted selector - See nextRound() for explanation
		double point = Math.random();
		
		double pool = 0;
		for (MathType type : math) {
			pool += type.chance;
		}
		double scaleFactor = pool;
		pool = 0;
		for (MathType type : math) {
			pool += type.chance/scaleFactor;
			if (pool > point) {
				return type;
			}
		}
		return null;
	}
	
	private static char getOperation(Set<Character> valid) {
		int chosen = randomInt(valid.size());
		int index = 0;
		for (char operation : valid) {
			if (chosen == index) {
				return operation;
			}
			index++;
		}
		return '?';
	}
	
	/************** Trivia *************/
	private static void trivia() {
		trivia(0);
	}
	
	private static void trivia(int challengeFailCount) {
		String challenge = currentSettings.useChallenge();
		
		if (challenge != null) { // Use a challenge
			switch (challenge.toUpperCase()) {
			case "ANSWER-LAST-ROUND":
				answers.addAll(lastAnswers);
				currentPoints = 2;
				currentQuestion = "What was the answer to the previous question? (2 points)";
				ChatGame.broadcast(currentQuestion);
				break;
			case "WON-LAST-ROUND":
				if (lastWinner.equals("Nobody")) {
					answers.add("Noone");
					answers.add("Nobody");
					answers.add("No one");
					answers.add("None");
					answers.add("No-one");
				} else {
					answers.add(lastWinner);
				}
				currentPoints = 2;
				currentQuestion = "Who won last round? (2 points)";
				ChatGame.broadcast(currentQuestion);
				break;
			case "PLAYERS-ONLINE":
				answers.add(Integer.toString(Bukkit.getOnlinePlayers().size()));
				currentPoints = 1;
				currentQuestion = "How many players are online right now? (1 points)";
				ChatGame.broadcast(currentQuestion);
				break;
			default:
				logger.warning("An unknown challenge was parsed into the " + currentType + " game (" + challenge + ")");
				if (challengeFailCount > 15) {
					ChatGame.broadcast("&wAn error occurred; please inform an operator.");
				} else {
					trivia(++challengeFailCount); // Retry
				}
				return;
			}
		} else { // Normal gamemode
			TriviaQuestion chosenQuestion = null;
			Set<TriviaQuestion> questions = trivia.keySet();
			int chosen = randomInt(questions.size()); // Random random index for a set element
			int index = 0;
			for (TriviaQuestion question : questions) {
				if (chosen == index) {
				    chosenQuestion = question;
				}
				index++;
			}
			
			for (String answer : chosenQuestion.getAnswers()) {
			    answers.add(answer);
			}
		
			int points = trivia.get(chosenQuestion);
			currentPoints = points;
			currentQuestion = chosenQuestion.getQuestion() + " (" + pointsDisplay(points) + ")";
			ChatGame.broadcast(currentQuestion);
		}
		
		currentTimerID = new BukkitRunnable() {
            public void run() {
    			ChatGame.broadcast("Nobody answered in time :(");
    			endRound();
            }
        }.runTaskLater(pl, currentSettings.duration * 20).getTaskId();
	}
	
	/************** QuickType *************/
	
	private static void quicktype() {
		String challenge = currentSettings.useChallenge();
		String phrase = getRandomWord(quicktype);
		
		int points = quicktype.get(phrase); // Have to get points before possibly changing the order
		currentPoints = points;
		
		String challengeString = "";
		
		phrase = scrambleString(phrase);
		
		answers.add(phrase); // Add after scrambling for quicktypes as they need to type the scrambled version.
		
		if (challenge != null) {
			switch (challenge.toUpperCase()) {
			case "BACKWARDS":
				phrase = reverseString(phrase);
				challengeString = "&o&nbackwards&r ";
				points++;
			}
		}
		
		currentQuestion = "Quick! Type \"&h" + phrase + "&r\" " + challengeString + "for " + pointsDisplay(points) + "!";
		ChatGame.broadcast(currentQuestion);
		
		currentTimerID = new BukkitRunnable() {
            public void run() {
    			ChatGame.broadcast("Nobody reacted in time :(");
    			endRound();
            }
        }.runTaskLater(pl, currentSettings.duration * 20).getTaskId();
	}
	
	private static String reverseString(String input) {
		Stack<Character> letters = new Stack<>();
		for (char letter : input.toCharArray()) {
			letters.push(letter);
		}
		char[] newLetters = new char[input.length()];
		for (int i = 0; i < newLetters.length; i++) {
			newLetters[i] = letters.pop();
		}
		
		return new String(newLetters);
	}
	
	/************** Reveal *************/
	
	private static void reveal() {
		String phrase = null;
		String challenge = currentSettings.useChallenge();
		
		String challengeString = "";
		
		if (challenge != null) { // Use a challenge
			
			switch (challenge.toLowerCase()) {
			default: // Don't need to cancel as a normal game will occur.
				logger.warning("An unknown challenge was parsed into the " + currentType + " game (" + challenge + ")");
			}
		}

		if (phrase == null) {
			phrase = getRandomWord(reveal);
			if (autoGeneratedWord) {
				phrase = WbsStrings.capitalizeAll(phrase);
				revealCost(phrase);
			} else {
				currentPoints = reveal.get(phrase);
			}
		}

		answers.add(phrase); // Only one answer

		int amount = Math.max(1, Math.round(phrase.length()/3));
		String original = reveal(conceal(phrase), phrase, amount);
		currentQuestion = "Guess the word! \"&h" + original + "&r\" (" + pointsDisplay(currentPoints) + ")" + challengeString;
		ChatGame.broadcast(currentQuestion);
		
		final String finalAnswer = phrase;
		
		long delay = currentSettings.duration * 20;
		currentTimerID = new BukkitRunnable() {
			String currentString = original;
			String answer = finalAnswer;
			int amount;
			int maxAmount = Math.max(1, Math.round(answer.length()/4));
            public void run() {	
        		amount = randomInt(maxAmount)+1;
        		String amountDisplay;
        		if (amount == 1) {
        			amountDisplay = amount + " more letter";
        		} else {
        			amountDisplay = amount + " more letters";
        		}
            	currentString = reveal(currentString, answer, amount);
    			
            	if (currentString.equals(answer)) {
        			ChatGame.broadcast("Nobody answered in time :( The word was: &h" + answer);
        			endRound();
        			cancel(); // Will be canceled by endRound remotely, but -v('-')v-
            	} else {
            		if (currentPoints > 1 && amount > 1) {
                		currentPoints--;
            		}
            		currentQuestion = amountDisplay + "! \"&h" + currentString + "&r\" (" + pointsDisplay(currentPoints) + ")";
        			ChatGame.broadcast(currentQuestion);
            	}
            }
        }.runTaskTimer(pl, delay, delay).getTaskId();
	}
	
	private static void revealCost(String phrase) {
		int length = phrase.length();
		currentPoints = Math.max(1, (length/5));
	}
	
	private static String conceal(String answer) {
		String concealed = answer.replaceAll("[^ ]", "_");
		
		return concealed;
	}
	
	private static String reveal(String current, String answer, int reveal) {
		char[] letters = current.toCharArray();
		char[] answerLetters = answer.toCharArray();
		
		for (int i = 0; i < reveal; i++) { // Replace #reveal underscores if found
			boolean found = false; // Whether or not the current string (char array) contains an underscore
			int index = 0;
			while (!found && index < letters.length) {
				if (letters[index] == '_') {
					found = true;
				}
				index++;
			}
			if (found) {
				index = 0;
				do {
					index = (int) Math.ceil(Math.random() * letters.length)-1;
				} while (letters[index] != '_'); // Find an underscore
			
				letters[index] = answerLetters[index];
			}
		}
		
		String output = new String(letters);
		
		return output;
	}
}
