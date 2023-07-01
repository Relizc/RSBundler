package net.itsrelizc.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.itsrelizc.global.ChatUtils;
import net.itsrelizc.server.ShutdownManager;
import net.itsrelizc.server.ShutdownManager.ShutdownType;
import net.itsrelizc.warp.ServerCategory;
import net.itsrelizc.warp.WarpUtils;

public class CommandShutdown implements CommandExecutor {
	
	

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (ShutdownManager.timeleft != -1) {
			ChatUtils.systemMessage(sender, "§b§lSERVER", "§cThe universe is already about to collapse!");
			return true;
		}
		
		ShutdownManager.shutdown(ShutdownType.OTHER);
		
		return true;
		
	}

}
