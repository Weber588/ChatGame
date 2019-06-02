package wbs.chatgame;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import wbs.chatgame.game.Game;
import wbs.chatgame.game.GameSettings;
import wbs.chatgame.game.GameType;
import wbs.chatgame.game.MathType;
import wbs.chatgame.game.TriviaQuestion;

public class ChatGame extends JavaPlugin {
	
	public final Logger logger = this.getLogger();
	
	private static ChatColor colour = ChatColor.GREEN;
	private static ChatColor highlight = ChatColor.BLUE;
	private static ChatColor errorColour = ChatColor.RED;
	public static String prefix;
	
	private static void setDisplays(String newPrefix, ChatColor newColour, ChatColor newHighlight, ChatColor newErrorColour) {
		prefix = ChatColor.translateAlternateColorCodes('&', newPrefix);
		colour = newColour;
		highlight = newHighlight;
		errorColour = newErrorColour;
	}
	
	public static void broadcast(String message) {
		for (Player receiver : Bukkit.getOnlinePlayers()) {
			sendMessage(message, receiver);
		}
	}
	
	public static void sendMessage(String message, CommandSender sender) {
		message = message.replaceAll("&r", "" + colour); // Replace default with the main colour
		message = message.replaceAll("&h", "" + highlight); // Replace &h with the highlight colour
		message = message.replaceAll("&w", "" + errorColour); // Replace &h with the highlight colour
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(prefix + ' ' +  colour + message);
	}
	
	public static void sendMessageNoPrefix(String message, CommandSender sender) {
		message = message.replaceAll("&r", "" + colour); // Replace default with the main colour
		message = message.replaceAll("&h", "" + highlight); // Replace &h with the highlight colour
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(message);
	}
	
	private static Map<String, FileConfiguration> configs;

	
	private static void loadConfigs() {
		final Plugin pl = Bukkit.getPluginManager().getPlugin("ChatGame");
		configs = new HashMap<>();
		
		File configFile = new File(pl.getDataFolder(), "config.yml");
        if (!configFile.exists()) { 
        	pl.saveResource("config.yml", false);
        }
        configs.put("main", YamlConfiguration.loadConfiguration(configFile));
		
        configs.put("unscramble", YamlConfiguration.loadConfiguration(genConfig("games" + File.separator + "unscramble.yml")));
        configs.put("math", YamlConfiguration.loadConfiguration(genConfig("games" + File.separator + "math.yml")));
        configs.put("trivia", YamlConfiguration.loadConfiguration(genConfig("games" + File.separator + "trivia.yml")));
        configs.put("quicktype", YamlConfiguration.loadConfiguration(genConfig("games" + File.separator + "quicktype.yml")));
        configs.put("reveal", YamlConfiguration.loadConfiguration(genConfig("games" + File.separator + "reveal.yml")));
		
	}
	
	private static File genConfig(String path) {
		final Plugin pl = Bukkit.getPluginManager().getPlugin("ChatGame");
		File configFile = new File(pl.getDataFolder(), path);
        if (!configFile.exists()) { 
        	configFile.getParentFile().mkdirs();
            pl.saveResource(path, false);
        }
        
        return configFile;
	}
	
	public static void reload() {
		loadConfigs();
		
		FileConfiguration main = configs.get("main");
        String newPrefix = main.getString("general.message-prefix");
        ChatColor newColour = ChatColor.getByChar(main.getString("general.message-colour"));
        ChatColor newHighlight = ChatColor.getByChar(main.getString("general.highlight-colour"));
        ChatColor newErrorColour = ChatColor.getByChar(main.getString("general.error-colour"));
        setDisplays(newPrefix, newColour, newHighlight, newErrorColour);

        for (GameType type : GameType.values()) {
        	// New settings instance for every game type
        	GameSettings settings = new GameSettings();
        	String name = type.name().toLowerCase();
        	
        	// Get global settings from the main config
        	settings.setDuration(main.getInt("general.durations." + name));
        	settings.setChance(main.getInt("general.rates." + name));
        	
        	FileConfiguration config = configs.get(name);
        	
        	ConfigurationSection options = config.getConfigurationSection(name + ".options");
    		if (options != null) {
	            for (String key : options.getKeys(false)) {
	            	switch (key.toLowerCase()) {
	            	case "challenges":
	            		ConfigurationSection challenges = options.getConfigurationSection(key);
	            		for (String challenge : challenges.getKeys(false)) {
	            			settings.setChallenge(challenge, challenges.getDouble(challenge));
	            		}
	            		break;
	            	case "generation":
	            		ConfigurationSection generation = options.getConfigurationSection(key);
	            		for (String wordSet : generation.getKeys(false)) {
	            			settings.setGeneration(wordSet, generation.getDouble(wordSet));
	            		}
	            		break;
	            	default:
            			settings.setOption(key, options.get(key)); // Stores as an Object; options are cast when used
	            	}
	            }
    		}
            Game.setTypeSettings(type, settings);
        }

        boolean guessCommand = main.getBoolean("general.guess-command");
        int betweenGames = main.getInt("general.seconds-between-rounds");
        boolean challenges = main.getBoolean("general.enable-challenges");
        boolean rewards = main.getBoolean("general.enable-rewards");
    	Game.setDurationBetweenGames(betweenGames);
    	Game.toggleRewards(rewards);
    	Game.toggleChallenges(challenges);
    	Game.toggleGuessCommand(guessCommand);
    	
    	Game.clearLists(); // Remove any existing words, questions etc
        
    	List<String> words = configs.get("unscramble").getStringList("unscramble.words");
    	for (String word : words) {
    		int pointsIndex = word.lastIndexOf(':');
    		String addWord = word.substring(0, pointsIndex);
    		int points = Integer.parseInt(word.substring(pointsIndex+1));
    		Game.addUnscramble(addWord, points);
    	}

    	ConfigurationSection mathTypes = configs.get("math").getConfigurationSection("math.types");
    	for (String type : mathTypes.getKeys(false)) {
    		ConfigurationSection newType = mathTypes.getConfigurationSection(type);
    		double chance = newType.getDouble("chance");
    		int maxValue = newType.getInt("max-value");
    		int min = newType.getInt("min-numbers");
    		int max = newType.getInt("max-numbers");
    		boolean addition = newType.getBoolean("include-addition", true);
    		boolean subtraction= newType.getBoolean("include-subtraction", true);
    		boolean multiplication= newType.getBoolean("include-multiplication", false);
    		boolean division= newType.getBoolean("include-division", false);
    		
    		MathType question = new MathType(chance, maxValue, min, max, addition, subtraction, multiplication, division);
    		Game.addMath(question);
    	}
    	
    	ConfigurationSection questionConfig = configs.get("trivia").getConfigurationSection("trivia.questions");
    	for (String questionName : questionConfig.getKeys(false)) {
    		ConfigurationSection question = questionConfig.getConfigurationSection(questionName);
    		TriviaQuestion triviaQuestion = new TriviaQuestion(question.getString("question"));
    		for (String answer : question.getStringList("answers")) {
    			triviaQuestion.addAnswer(answer);
    		}
    				
    		
    		Game.addTrivia(triviaQuestion, question.getInt("points"));
    	}

    	List<String> quickTypeChars = configs.get("quicktype").getStringList("quicktype.character-sets");
    	for (String sets : quickTypeChars) {
    		int pointsIndex = sets.lastIndexOf(':');
    		String addSet = sets.substring(0, pointsIndex);
    		int points = Integer.parseInt(sets.substring(pointsIndex+1));
    		Game.addQuickType(addSet, points);
    	}
    	words = configs.get("reveal").getStringList("reveal.words");	
    	for (String word : words) {
    		int pointsIndex = word.lastIndexOf(':');
    		String addWord = word.substring(0, pointsIndex);
    		int points = Integer.parseInt(word.substring(pointsIndex+1));
    		Game.addReveal(addWord, points);
    	}
	}
	
	@Override	
    public void onEnable() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

		reload();
        
	    PluginManager pm = Bukkit.getServer().getPluginManager();
	    pm.registerEvents(new GuessController(), this);

		getCommand("chatgame").setExecutor(new ChatGameCommand());
		getCommand("guess").setExecutor(new GuessCommand());

        Game.start();
    }
	
	
   
    @Override
    public void onDisable() {
    	Game.stop();
    	/* TODO:
    	 * Save player data
    	 */
    }
}
