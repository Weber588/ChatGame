package wbs.chatgame;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import wbs.chatgame.game.Game;

public class GuessCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!(sender instanceof Player)) {
			ChatGame.sendMessage("&wThis command is only usable by players.", sender);
			return true;
		}
		
		if (!Game.isRunning) {
			ChatGame.sendMessage("&wThe game is not currently running!", sender);
			return true;
		}
		if (!Game.inRound) {
			ChatGame.sendMessage("&wNo question pending! Wait for the next round to start!", sender);
			return true;
		}
		if (args.length == 0) {
			ChatGame.sendMessage("Use &h/g <answer>&r to guess!", sender);
			return true;
		}
		
		Player player = (Player) sender;
		
		String guess = String.join(" ", args);
		
		Game.guess(player, guess);
		
		return true;
	}
}
