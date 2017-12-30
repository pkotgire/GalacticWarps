package io.github.pkotgire.GalacticWarps;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandGSetWarp implements CommandExecutor {

	private GalacticWarps plugin;
	private Database database;

	public CommandGSetWarp(GalacticWarps plugin, Database database) {
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
				
				// Give error message if gsetwarp command is run from the console
				if (sender instanceof ConsoleCommandSender) {
					Language.sendMessage(sender, "&4Console cannot set warps! Use this command in game!");
				}

				// Run command if sender is Player
				if (sender instanceof Player) {

					// If 1 argument, attempt to set warp
					if (args.length == 1) {
						Player player = (Player) sender;
						Location location = player.getLocation();
						String warpName = args[0];

						setWarp(sender, sender.getName(), warpName, location);
					}

					// If 2 arguments, attempt to set warp for specific player
					else if (args.length == 2) {

						// Exit if sender does not have permission to create warps for others
						if (!sender.hasPermission("galacticwarps.set.others")) {
							Language.sendMessage(sender,
									"&mYou do not have permission to create warps for other players!");
							return;
						}

						Player player = (Player) sender;
						Location location = player.getLocation();
						String warpName = args[0];
						String playerName = args[1];

						// Exit if player does not exist
						if (!database.playerExists(playerName)) {
							playerNotExist(sender, playerName);
							return;
						}

						setWarp(sender, database.getPlayerNameExact(playerName), warpName, location);
					}

					// Invalid arguments
					else {
						invalidArguments(sender);
					}
				}
			}
		};

		r.runTaskAsynchronously(plugin);
		return true;
	}

	// Method to set a warp
	private void setWarp(CommandSender sender, String pName, String wName, Location location) {

		// Exit if sender does not have permission
		if (!sender.hasPermission("galacticwarps.set")) {
			noPermission(sender);
			return;
		}

		// Get warp owner
		String warpOwner = database.getWarpOwner(wName);

		// Set warp if warp does not already exist
		if (warpOwner == null) {

			// Exit if player doesn't have free permission or any tokens
			int tokens = database.getTokens(sender.getName());
			if (!sender.hasPermission("galacticwarps.tokens.free") && tokens < 1) {
				Language.sendMessage(sender, "&mYou do not have any warp tokens to create this warp!");
				return;
			}

			// Exit if warp name is too long
			if (wName.length() > 16) {
				Language.sendMessage(sender, "&mThe name of that warp is too long!");
				return;
			}

			// Exit if warp name is "list"
			if (wName.equalsIgnoreCase("list")) {
				Language.sendMessage(sender, "&mYou cannot name your warp &s\"" + wName + "\"&m!");
				return;
			}
			
			// Add the warp to the database and set the warp owner
			database.setWarp(wName, location);
			database.setWarpOwner(wName, pName);
			Language.sendMessage(sender, "&mWarp named &s" + wName + "&m, owned by player &s"
					+ pName + "&m, was created successfully!");

			// Deduct token if player doesn't have free permission
			if (!sender.hasPermission("galacticwarps.tokens.free")) {
				database.takeTokens(sender.getName(), 1);
				Language.sendMessage(sender, "&s1 &mwarp token was taken from your account!");
			}
		}

		// Set warp if warp already exists and warp owner is current player
		else if (warpOwner.equalsIgnoreCase(sender.getName())) {
			database.setWarp(wName, location);
			Language.sendMessage(sender, "&mChanged location of warp named &s" + wName + " &msuccessfully!");
		}

		// Set warp if warp already exists and warp owner is not current player
		else {

			// Exit if player does not have permission to overwrite other players warps
			if (!sender.hasPermission("galacticwarps.set.others")) {
				Language.sendMessage(sender, "&mYou do not have permission to change warps owned by other players!");
				return;
			}

			database.setWarp(wName, location);
			Language.sendMessage(sender, "&mChanged location of warp named &s" + wName + "&m, owned by player &s"
					+ warpOwner + "&m, successfully!");
		}

	}

	// Method to send message to player if arguments were invalid
	private void invalidArguments(CommandSender sender) {
		String message = "&mThe arguments you entered were invalid! Correct usage: /gsetwarp [warp name]";
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
