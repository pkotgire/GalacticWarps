package io.github.pkotgire.GalacticWarps;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandGWarp implements CommandExecutor {

	private GalacticWarps plugin;
	private Database database;

	public CommandGWarp(GalacticWarps plugin, Database database) {
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
				
				// If 0 arguments, then run viewWarps command
				if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
					viewWarps(sender);
				}

				// If 1 argument, then warp sender
				else if (args.length == 1) {

					// Exit if sender is console
					if (sender instanceof ConsoleCommandSender) {
						Language.sendMessage(sender, "&mYou must specify a player to warp!");
						return;
					}

					// Run warp command
					String warpName = args[0];
					String playerName = sender.getName();

					warp(sender, playerName, warpName);
				}

				// If 2 arguments, then warp specified player
				else if (args.length == 2) {

					// Run warp command
					String warpName = args[0];
					String playerName = args[1];

					warp(sender, playerName, warpName);
				}

				// Invalid arguments
				else {
					invalidArguments(sender);
				}
			}
		};

		r.runTaskAsynchronously(plugin);
		return true;
	}

	private void warp(CommandSender sender, String playerName, String warpName) {

		// Exit if sender doesn't have permission to warp
		if (!sender.hasPermission("galacticwarps.warp")) {
			noPermission(sender);
			return;
		}

		// Exit if sender doesn't have permission to warp other players and sender is different than player
		if (!sender.getName().equalsIgnoreCase(playerName)) {
			if (!sender.hasPermission("galacticwarps.warp.others")) {
				Language.sendMessage(sender, "&mYou do not have permission to warp other players!");
				return;
			}
		}

		// Get location of warp
		Location newLocation = database.getWarpLocation(warpName);

		// Exit if warp does not exist
		if (newLocation == null) {
			Language.sendMessage(sender, "&mThe galactic warp &s" + warpName + " &mdoes not exist!");
			return;
		}

		// Get Player
		Player player = Bukkit.getPlayerExact(playerName);

		// Exit if player does not exist
		if (player == null) {
			playerNotExist(sender, playerName);
			return;
		}

		// Teleport
		Language.sendMessage(sender, "&mTeleporting &s" + playerName + " &mto galactic warp named &s" + warpName);
		player.teleport(newLocation);

	}

	private void viewWarps(CommandSender sender) {

		// Exit if no permission
		if (!sender.hasPermission("galacticwarps.warp.list")) {
			Language.sendMessage(sender, "&mYou do not have permission to see the list of warps!");
			return;
		}

		// Construct message with list of warps
		ArrayList<String> warpsList = database.getWarps();
		String message = "&mAvailable Galactic Warps: ";

		for (String warp : warpsList) {
			message += "&s" + warp + "&m, ";
		}

		if (!warpsList.isEmpty()) {
			message = message.substring(0, message.length() - 2);
		}

		Language.sendMessage(sender, message);
	}

	// Method to send message to player if they entered invalid arguments
	private void invalidArguments(CommandSender sender) {
		String message = "&mThe arguments you entered were invalid! Correct usage: /gwarp [warp name] [player name]";
		Language.sendMessage(sender, message);
	}

	// Method to send message to player if they have no permission
	private void noPermission(CommandSender sender) {
		String message = "&mYou do not have permission to run that command!";
		Language.sendMessage(sender, message);
	}

	// Method to send message to player if player does not exist
	private void playerNotExist(CommandSender sender, String playerName) {
		String message = "&mThe player &s" + playerName + " &mhas never been on the server!";
		Language.sendMessage(sender, message);
	}
}
