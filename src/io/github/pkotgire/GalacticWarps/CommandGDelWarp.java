package io.github.pkotgire.GalacticWarps;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandGDelWarp implements CommandExecutor {

	private GalacticWarps plugin;
	private Database database;

	public CommandGDelWarp(GalacticWarps plugin, Database database) {
		this.database = database;
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		BukkitRunnable r = new BukkitRunnable() {
			@Override
			public void run() {

				// Exit if connection to database is closed
				if (!database.connectionIsOpen()) {
					return;
				}

				// Invalid Arguments
				if (args.length != 1) {
					invalidArguments(sender);
					return;
				}

				// If 1 argument, attempt to delete warp
				else {

					String warpName = args[0];
					deleteWarp(sender, warpName);
				}
			}

		};

		r.runTaskAsynchronously(plugin);
		return true;
	}

	// Method to delete a warp
	private void deleteWarp(CommandSender sender, String wName) {

		// Exit if sender does not have permission
		if (!sender.hasPermission("galacticwarps.delete")) {
			noPermission(sender);
			return;
		}

		// Get warp owner
		String warpOwner = database.getWarpOwner(wName);

		// Exit if warp does not exist
		if (warpOwner == null) {
			Language.sendMessage(sender, "&mThe warp &s" + wName + " &mdoes not exist!");
			return;
		}

		// Delete warp if sender is owner
		else if (warpOwner.equalsIgnoreCase(sender.getName())) {
			database.deleteWarp(wName);
			Language.sendMessage(sender, "&mDeleted the warp named &s" + wName + " &msuccessfully!");

			// Refund token if sender doesn't have "free" permission and refund-tokens is true
			boolean refundTokens = plugin.getConfig().getBoolean("refund-tokens");
			if (!sender.hasPermission("galacticwarps.tokens.free") && refundTokens) {
				database.giveTokens(sender.getName(), 1);
				Language.sendMessage(sender, "&s1 &mwarp token was added to your account!");
			}
		}

		// Delete warp if sender is not owner
		else {

			// Exit if player does not have permission to delete other players warps
			if (!sender.hasPermission("galacticwarps.delete.others")) {
				Language.sendMessage(sender, "&mYou do not have permission to delete warps owned by other players!");
				return;
			}

			// Delete warp, token is not refunded when deleting other player's warp
			database.deleteWarp(wName);
			Language.sendMessage(sender,
					"&mDeleted warp named &s" + wName + "&m, owned by player &s" + warpOwner + "&m, successfully!");
		}

	}

	// Method to send message to player if arguments were invalid
	private void invalidArguments(CommandSender sender) {
		String message = "&mThe arguments you entered were invalid! Correct usage: /gdelwarp [warp name]";
		Language.sendMessage(sender, message);
	}

	// Method to send message to player if they have no permission
	private void noPermission(CommandSender sender) {
		String message = "&mYou do not have permission to run that command!";
		Language.sendMessage(sender, message);
	}
}
