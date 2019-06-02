package wbs.chatgame.game;

import java.util.HashMap;
import java.util.Map;

public class GameSettings {

	public int duration;
	public double chance;
	private Map<String, Object> options;
	private Map<String, Double> challenges;
	private Map<String, Double> generation;
	
	public GameSettings() {
		options = new HashMap<>();
		challenges = new HashMap<>();
		generation = new HashMap<>();
	}
	public Object getOption(String option) {
		return getOption(option, null);
	}

	public Object getOption(String option, Object defaultValue) {
		if (options.containsKey(option)) {
			return options.get(option);
		}
		return defaultValue;
	}
	public double getChallenge(String option) {
		return challenges.get(option);
	}
	// Returns null if the normal game should be used
	public String useChallenge() {
		// If challenges are disabled, always use normal game (i.e. return null)
		if (!(boolean) getOption("include-challenges", false)) {
			return null;
		}
		
		double point = Math.random();
		
		double pool = chance;
		for (String challenge: challenges.keySet()) {
			pool += challenges.get(challenge);
		}
		double scaleFactor = pool;
		pool = chance/scaleFactor;
		if (pool > point) { // Use normal gametype
			return null;
		}
		for (String challenge: challenges.keySet()) {
			pool += challenges.get(challenge)/scaleFactor;
			if (pool > point) {
				return challenge;
			}
		}
		return null;
	}

	public void setChance(double chance) {
		this.chance = chance;
	}
	public void setDuration(int seconds) {
		duration = seconds;
	}
	public void setOption(String option, Object value) {
		options.put(option, value);
	}
	public void setChallenge(String challenge, double chance) {
		challenges.put(challenge, chance);
	}
	public void setGeneration(String generationType, double chance) {
		generation.put(generationType, chance);
	}
	private static boolean chance(double percentage) {
		return (Math.random() < percentage/100);
	}
	public String useGeneration() {
		// Chance to use a provided word
		double genChance = (double) (int) getOption("generation-chance", 0); // getOption returns Integer, so cast to int then to double
		if (!chance(genChance)) {
			return null;
		}
		
		double point = Math.random();
		
		double pool = 0;
		for (String genType: generation.keySet()) {
			pool += generation.get(genType);
		}
		double scaleFactor = pool;
		pool = 0;
		for (String genType: generation.keySet()) {
			pool += generation.get(genType)/scaleFactor;
			if (pool > point) {
				return genType;
			}
		}
		return null;
	}
}
