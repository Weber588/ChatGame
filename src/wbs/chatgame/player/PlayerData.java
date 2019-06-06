package wbs.chatgame.player;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.entity.Player;

import wbs.chatgame.game.GameType;

public class PlayerData implements Serializable {
	public static enum RankType {
		TOTAL, WEEK;
	}
	
	private static final long serialVersionUID = 735312392172726885L;

	// Map of username to all associated data
	/* TODO:
	 * Implement a transfer command to remap this
	 */
	private static Map<String, PlayerData> allPlayerData = new HashMap<>();
	private static GameType[] gameTypes = GameType.values();

	private static ArrayList<PlayerData> ranking = calculateRanks(RankType.TOTAL);
	private static ArrayList<PlayerData> weekRanking = calculateRanks(RankType.WEEK);
	private static LocalDateTime lastRefresh = LocalDateTime.now();
	private static LocalDateTime lastWeekRefresh = LocalDateTime.now();
	private static int playerSetSize = allPlayerData.size();
	
	private final static Duration refreshRate = Duration.parse("PT2S");
	
	private String username;
	private Map<GameType, Integer> totalPoints = new HashMap<>();
	private Map<GameType, Integer> weekPoints = new HashMap<>();
	
	private Map<GameType, Integer> correct = new HashMap<>();
	private Map<GameType, Integer> incorrect = new HashMap<>();
	// Map of GameType to average seconds to answer that game type
	private Map<GameType, Double> speed = new HashMap<>();
	
	private PlayerData(String username) {
		
		for (GameType type : GameType.values()) {
			totalPoints.put(type, 0);
			weekPoints.put(type, 0);
			
			correct.put(type, 0);
			incorrect.put(type, 0);
			speed.put(type, 0.0);
		}
		this.username = username;
		playerSetSize++;
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
		totalPoints.put(type, totalPoints.get(type)+points);
		weekPoints.put(type, weekPoints.get(type)+points);
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
		int total = 0;
		for (GameType checkType : gameTypes) {
			total += totalPoints.get(checkType);
		}
		return total;
	}
	public int getTotalPoints(GameType type) {
		if (type == null) {
			return getTotalPoints();
		}
		return totalPoints.get(type);
	}

	public int getWeekPoints() {
		int total = 0;
		for (GameType checkType : gameTypes) {
			total += weekPoints.get(checkType);
		}
		return total;
	}
	public int getWeekPoints(GameType type) {
		return weekPoints.get(type);
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
	public String getUsername() {
		return username;
	}
	
	private static ArrayList<PlayerData> calculateRanks(RankType type) {
		ArrayList<PlayerData> newDataSet = new ArrayList<>();
		for (String username : allPlayerData.keySet()) {
			newDataSet.add(allPlayerData.get(username));
		}
		lastRefresh = LocalDateTime.now();
		
		switch (type) {
		case TOTAL:
			newDataSet.sort(new Comparator<PlayerData>() {
				@Override
				public int compare(PlayerData first, PlayerData second) {
					return second.getTotalPoints() - first.getTotalPoints();
				}
			});
			break;
		case WEEK:
			newDataSet.sort(new Comparator<PlayerData>() {
				@Override
				public int compare(PlayerData first, PlayerData second) {
					return second.getWeekPoints() - first.getWeekPoints();
				}
			});
			break;
		}
		
		return newDataSet;
	}
	
	public static PlayerData[] getTopN(int n, RankType type) {
		int rank = 0;
		PlayerData[] topN = new PlayerData[n];
		switch (type) {
		case TOTAL:
			if (Duration.between(lastRefresh, LocalDateTime.now()).compareTo(refreshRate) > 0) {
				ranking = calculateRanks(type);
			}
			
			for (PlayerData data : ranking) {
				if (rank == n) {
					break;
				}
				topN[rank] = data;
				rank++;
			}
			break;
		case WEEK:
			if (Duration.between(lastWeekRefresh, LocalDateTime.now()).compareTo(refreshRate) > 0) {
				weekRanking = calculateRanks(type);
			}
	
			for (PlayerData data : weekRanking) {
				if (rank == n) {
					break;
				}
				topN[rank] = data;
				rank++;
			}
		}
		if (rank < n) {
			for (int i = rank; i < n; i++) {
				topN[i] = null;
			}
		}
		return topN;
	}
	
	public static int getRank(Player player, RankType type) {
		return getRank(player.getName(), type);
	}
	public static int getRank(String username, RankType type) {
		int rank = 0;
		switch (type) {
		case TOTAL:
			if (Duration.between(lastRefresh, LocalDateTime.now()).compareTo(refreshRate) > 0) {
				ranking = calculateRanks(type);
			}
			
			for (PlayerData data : ranking) {
				String user = data.getUsername();
				if (user.equalsIgnoreCase(username)) {
					break;
				}
				rank++;
			}
			break;
		case WEEK:
			if (Duration.between(lastWeekRefresh, LocalDateTime.now()).compareTo(refreshRate) > 0) {
				weekRanking = calculateRanks(type);
			}
			
			for (PlayerData data : weekRanking) {
				if (data.getUsername().equalsIgnoreCase(username)) {
					break;
				}
				rank++;
			}
		}
		return ++rank;
	}
}


