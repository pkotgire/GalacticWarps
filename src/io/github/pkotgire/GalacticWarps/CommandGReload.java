package io.github.pkotgire.GalacticWarps;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandGReload implements CommandExecutor {

	private GalacticWarps plugin;

	public CommandGReload(GalacticWarps plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		// Reload if sender has permission
		if (sender.hasPermission("galacticwarps.reload")) {
			BukkitRunnable r = new BukkitRunnable() {
				@Override
				public void run() {
					plugin.reload();
					Language.sendMessage(sender, "&mGalacticWarps reloaded successfully!");
				}
			};

			r.runTaskAsynchronously(plugin);
		}

		// Otherwise, give no permission message
		else {
			noPermission(sender);
		}

		return true;
	}

	// Method to send message to player if they have no permission
	private void noPermission(CommandSender sender) {
		String message = "&mYou do not have permission to run that command!";
		Language.sendMessage(sender, message);
	}
}
