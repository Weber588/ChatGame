package wbs.chatgame;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;

import wbs.chatgame.game.Game;
import wbs.chatgame.game.GameType;
import wbs.chatgame.game.TriviaQuestion;
import wbs.chatgame.player.PlayerData;
import wbs.chatgame.player.PlayerData.RankType;
import wbs.util.WbsStrings;

public class ChatGameCommand implements CommandExecutor {

	private void sendMessage(String message, CommandSender sender) {
		ChatGame.sendMessage(message, sender);
	}
	private void sendMessageNoPrefix(String message, CommandSender sender) {
		ChatGame.sendMessageNoPrefix(message, sender);
	}
	
	private boolean checkPermission(CommandSender sender, String permission) {
		if (!sender.hasPermission(permission)) {
			sendMessage("&wYou are lacking the permission node: &h" + permission, sender);
			return false;
		}
		if (sender.isOp()) {
			return true;
		}
		return true;
	}
	
	private boolean checkAllPermissions(CommandSender sender, String permissionStartsWith) {
		for (PermissionAttachmentInfo perm : sender.getEffectivePermissions()) {
			if (perm.getPermission().startsWith(permissionStartsWith)) {
				return true;
			}
		}
		if (sender.isOp()) {
			return true;
		}
		sendMessage("&wYou are lacking a child permission of: &h" + permissionStartsWith, sender);
		return false;
	}
	
	private final String usage = "Use &h/cg help&r for more information.";
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		int length = args.length;
		
		if (length == 0) {
			sendMessage(usage, sender);
			return true;
		}
		
		// Only check admin commands if they have one of them
		if (checkAllPermissions(sender, "chatgame.admin")) {
			boolean runStaffCommand = true; // Assume a command was run - default case changes to false. Then return if true.
			switch (args[0].toUpperCase()) {
			case "START":
				if (checkPermission(sender, "chatgame.admin.start")) {
					if (Game.isRunning) {
						sendMessage("&wThe game is already running!", sender);
						return true;
					}
	
					Game.start();
				}
				return true;
			case "STOP":
				if (checkPermission(sender, "chatgame.admin.stop")) {
					if (!Game.isRunning) {
						sendMessage("&wThe game is not running!", sender);
						return true;
					}
	
					Game.stop();
				}
				return true;
			case "RESTART":
				if (checkPermission(sender, "chatgame.admin.restart")) {
					if (!Game.isRunning) {
						sendMessage("&wThe game is not running! Use &h/cg start&w to start.", sender);
						return true;
					}
	
					Game.stop();
					Game.start();
				}
				return true;
			case "RELOAD":
				if (checkPermission(sender, "chatgame.admin.reload")) {
					sendMessage("Reloading...", sender);
					ChatGame.reload();
					sendMessage("&hComplete.", sender);
				}
				return true;
			case "SKIP":
				if (checkPermission(sender, "chatgame.admin.skip")) {
					Game.skipRound();
				}
				return true;
			case "NEXT":
				if (checkPermission(sender, "chatgame.admin.next")) {
					switch (length) {
					case 1:
						sendMessage("Usage: &h/cg next <GameType>.&r Please use one of the following: &h" + getTypesList(), sender);
						return true;
					default:
						GameType next = getGameType(args[1]); // Method to handle parsing String to Enum
						if (next != null) {
							Game.nextType(next);
							sendMessage("The next game type will be " + WbsStrings.capitalize(args[1]), sender);
						} else {
							sendMessage("Invalid game type; please choose from the following: &h" + getTypesList(), sender);
						}
					}
				}
				return true;
			case "CUSTOM":
				if (checkPermission(sender, "chatgame.admin.custom")) {
					switch (length) {
					case 1:
						sendMessage("Usage: &h/cg custom <points> <question> -a <answer>", sender);
						sendMessage("(You can list answers with \", \")", sender);
						return true;
					default:
						// Ignore first 
						String[] newArgs = new String[args.length - 2]; // Not keeping "custom" or points
						for (int i = 0; i < args.length - 2; i++) {
							newArgs[i] = args[i+2];
						}
						// custom == null if no -a was present.
						TriviaQuestion custom = parseCustom(String.join(" ", newArgs));
						
						if (custom == null) {
							sendMessage("Usage: &h/cg custom <points> <question> -a <answer>", sender);
							return true;
						}
						int points;
						try {
							points = Integer.parseInt(args[1]);
						} catch (NumberFormatException e) {
							sendMessage("&wPoints must be an integer.", sender);
							return true;
						}
						Game.setCustom(custom, points);
						sendMessage("Question will be: " + custom.getQuestion(), sender);
						sendMessage("Answers can be: " + custom.getAnswers().toString(), sender);
					}
					
				}
				return true;
			}
			
			// Normal user commands
			switch (args[0].toUpperCase()) {
			case "STATS":
			case "POINTS":
				switch (length) {
				case 1:
					showStats(sender, sender.getName());
					break;
				case 2:
					showStats(sender, args[1]);
					break;
				case 3:
					GameType type = getGameType(args[2]);
					if (getGameType(args[2]) != null) {
						showStats(sender, args[1], type);
					} else {
						sendMessage("Invalid game type; please choose from the following: &h" + getTypesList(), sender);
					}
					break;
				}
				break;
			case "LEADERBOARD":
			case "TOP":
				int amount;
				switch (length) {
				case 1:
					amount = 5;
					break;
				default:
					try {
						amount = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						sendMessage("Usage: &b/cg top [int]", sender);
						return true;
					}
				}
				PlayerData[] top = PlayerData.getTopN(amount, RankType.TOTAL);
				sendMessageNoPrefix("--== &hTop " + amount + "&r ==--", sender);
				for (int i = 0; i < amount; i++) {
					if (top[i] != null) {
						sendMessageNoPrefix((i+1) + ". &h" + top[i].getUsername() + "&r: " + top[i].getTotalPoints(), sender);
					}
				}
			}
		}
		return true;
	}
	
	private TriviaQuestion parseCustom(String phrase) {
		int index = phrase.indexOf(" -a ");
		if (index == -1) {
			return null;
		}
		String question = phrase.substring(0, index);
		TriviaQuestion custom = new TriviaQuestion(question);
		
		String answersString = phrase.substring(index+4);
		for (String answer : answersString.split(", ")) {
			custom.addAnswer(answer);
		}
		
		return custom;
	}

	private void showStats(CommandSender sender, String lookup) {
		if (!PlayerData.exists(lookup)) {
			sendMessage("&wThat player has not interacted with ChatGame yet.", sender);
			return;
		}
		PlayerData data = PlayerData.getPlayerData(lookup);
		
		sendMessageNoPrefix("--== &h" + lookup + "&r ==--", sender);
		sendMessageNoPrefix("Total points: &h" + data.getTotalPoints(), sender);
		sendMessageNoPrefix("Weekly points: &h" + data.getWeekPoints(), sender);
		sendMessageNoPrefix("Rank: &h" + PlayerData.getRank(lookup, PlayerData.RankType.TOTAL), sender);
		sendMessageNoPrefix("Weekly Rank: &h" + PlayerData.getRank(lookup, PlayerData.RankType.WEEK), sender);
	}
	private void showStats(CommandSender sender, String lookup, GameType type) {
		if (!PlayerData.exists(lookup)) {
			sendMessage("&wThat player has not interacted with ChatGame yet.", sender);
			return;
		}
		PlayerData data = PlayerData.getPlayerData(lookup);
		
		switch (type) {
		case UNSCRAMBLE:
			break;
		case MATH:
			break;
		case TRIVIA:
			break;
		case QUICKTYPE:
			break;
		case REVEAL:
			break;
			
		}
	}
	
	private final GameType[] types = GameType.values(); // Doesn't need to be static since only one commandlistener object is created per instance - might as well be static
	
	private String allTypes = null;
	
	private String getTypesList() {
		if (allTypes == null) { // Only do this when the first time since the listener was registered
			String[] typeStringArray = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				typeStringArray[i] = WbsStrings.capitalize(types[i].name());
			}
			allTypes = String.join(", ", typeStringArray);
		}
		return allTypes;
	}
	
	private GameType getGameType(String typeString) {
		for (GameType type : types) {
			if (type.toString().equalsIgnoreCase(typeString)) {
				return type;
			}
		}
		return null;
	}
}
