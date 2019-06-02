package wbs.chatgame;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import wbs.chatgame.game.Game;

public class GuessController implements Listener {

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		if (Game.isRunning) {
			String message = event.getMessage();
			Player player = event.getPlayer();
			if (!Game.useGuessCommand) {
				Game.guess(player, message);
			} else {
				if (Game.checkAnswer(message) != null) {
					ChatGame.sendMessage("&wUse the guess command to guess!", player);
				}
			}
		}
	}
}
