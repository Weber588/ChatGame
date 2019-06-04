package wbs.chatgame.player;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import wbs.chatgame.game.GameType;

public class PlayerData implements Serializable {
	private static final long serialVersionUID = 735312392172726885L;

	// Map of username to all associated data
	/* TODO:
	 * Implement a transfer command to remap this
	 */
	private static Map<String, PlayerData> allPlayerData = new HashMap<>();
	
	private int totalPoints;
	private int weekPoints;
	private Map<GameType, Integer> correct = new HashMap<>();
	private Map<GameType, Integer> incorrect = new HashMap<>();
	// Map of GameType to average seconds to answer that game type
	private Map<GameType, Double> speed = new HashMap<>();
	
	private PlayerData(String username) {
		totalPoints = 0;
		weekPoints = 0;
		
		for (GameType type : GameType.values()) {
			correct.put(type, 0);
			incorrect.put(type, 0);
			speed.put(type, 0.0);
		}
		
		allPlayerData.put(username, this);
	}

	public static boolean exists(Player player) {
		return exists(player.getName());
	}
	
	public static boolean exists(String username) {
		return (allPlayerData.containsKey(username));
	}
	
	public static PlayerData getPlayerData(Player player) {
		return getPlayerData(player.getName());
	}
	
	public static PlayerData getPlayerData(String username) {
		if (allPlayerData.containsKey(username)) {
			return allPlayerData.get(username);
		}
		return new PlayerData(username);
	}
	
	public void addWin(GameType type, int points, double seconds) {
		totalPoints += points;
		weekPoints += points;
		int correctTotal = correct.get(type);
		correct.put(type, correctTotal+1);
		
		double newSpeed = ((speed.get(type) * correctTotal) + seconds) / (correctTotal + 1);
		speed.put(type, newSpeed);
	}
	public void addLoss(GameType type) {
		int incorrectTotal = incorrect.get(type);
		incorrect.put(type, incorrectTotal+1);
	}
	
	public int getTotalPoints() {
		return totalPoints;
	}
	public int getWeekPoints() {
		return weekPoints;
	}
	public int getCorrect(GameType type) {
		return correct.get(type);
	}
	public int getIncorrect(GameType type) {
		return incorrect.get(type);
	}
	public double getAverageSpeed(GameType type) {
		return speed.get(type);
	}
}


