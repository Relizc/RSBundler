package net.itsrelizc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.itsrelizc.warp.ServerCategory;
import net.itsrelizc.warp.WarpUtils;

public class CommandLobby implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		Player player = (Player) sender;
		
		WarpUtils.send(player, ServerCategory.LOBBY_MAIN);
		
		return true;
	}

}
