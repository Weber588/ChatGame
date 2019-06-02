package wbs.chatgame;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import wbs.chatgame.game.Game;
import wbs.chatgame.game.GameType;
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
		return true;
	}
	
	private final String usage = "Use &h/cg help&r for more information.";
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		int length = args.length;
		
		if (length == 0) {
			sendMessage(usage, sender);
			return true;
		}
		
		switch (args[0].toUpperCase()) {
		case "START":
			if (checkPermission(sender, "chatgame.admin.start")) {
				if (Game.isRunning) {
					sendMessage("&wThe game is already running!", sender);
					return true;
				}

				Game.start();
			}
			break;
		case "STOP":
			if (checkPermission(sender, "chatgame.admin.stop")) {
				if (!Game.isRunning) {
					sendMessage("&wThe game is not running!", sender);
					return true;
				}

				Game.stop();
			}
			break;
		case "RESTART":
			if (checkPermission(sender, "chatgame.admin.restart")) {
				if (!Game.isRunning) {
					sendMessage("&wThe game is not running! Use &h/cg start&w to start.", sender);
					return true;
				}

				Game.stop();
				Game.start();
			}
			break;
		case "RELOAD":
			if (checkPermission(sender, "chatgame.admin.reload")) {
				sendMessage("Reloading...", sender);
				ChatGame.reload();
				sendMessage("&hComplete.", sender);
			}
			break;
		case "SKIP":
			if (checkPermission(sender, "chatgame.admin.skip")) {
				
			}
			break;
		case "NEXT":
			if (checkPermission(sender, "chatgame.admin.next")) {
				if (length > 1) {
					String typeString = String.join("_", args);
					typeString = typeString.substring(6).toUpperCase(); // Omit "next "
					GameType next = getGameType(typeString); // Method to handle parsing String to Enum
					
					if (next != null) {
						Game.nextType(next);
					} else {
						sendMessage("Invalid game type; please choose from the following: &h" + allTypes, sender);
					}
				}
			}
			break;
		default:
			sendMessage(usage, sender);
		}
		return true;
	}
	
	private final GameType[] types = GameType.values(); // Doesn't need to be static since only one commandlistener object is created per instance - might as well be static
	
	private final String allTypes = WbsStrings.asList(types);
	
	private GameType getGameType(String typeString) { 
		for (GameType type : types) {
			if (type.toString().equals(typeString)) {
				return type;
			}
		}
		return null;
	}
}