package wbs.chatgame;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import wbs.chatgame.game.Game;

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
		default:
			sendMessage(usage, sender);
		}
		
		
		
		return true;
	}

}
