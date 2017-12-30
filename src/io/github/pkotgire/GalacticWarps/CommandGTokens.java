package io.github.pkotgire.GalacticWarps;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandGTokens implements CommandExecutor {

	private GalacticWarps plugin;
	private Database database;

	public CommandGTokens(GalacticWarps plugin, Database database) {
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

				// If command has 0 or 1 arguments, act as a "view" command as long as sender is a player
				if (args.length == 0 || args.length == 1) {

					// Exit if there is 1 argument and it is not a view command
					if (args.length == 1) {
						String command = args[0];
						if (!command.equalsIgnoreCase("view")) {
							invalidArguments(sender);
							return;
						}
					}

					// Run onCommand again with 2 arguments
					String[] args = new String[2];
					args[0] = "view";
					args[1] = sender.getName();

					onCommand(sender, cmd, label, args);
				}

				// If command has 2 arguments, it must be a view command for a specific player
				else if (args.length == 2) {
					// Save arguments into variables
					String command = args[0];
					String playerName = args[1];

					// Execute view command
					if (command.equalsIgnoreCase("view")) {
						viewTokens(sender, playerName);
					}

					// Invalid command
					else {
						invalidArguments(sender);
					}
				}

				// If command has 3 arguments, it must be a give/take/set command
				else if (args.length == 3) {

					// Save arguments into variables
					String command = args[0];
					String playerName = args[1];
					String numTokens = args[2];

					// Execute give command
					if (command.equalsIgnoreCase("give")) {
						giveTokens(sender, playerName, numTokens);
					}

					// Execute take command
					else if (command.equalsIgnoreCase("take")) {
						takeTokens(sender, playerName, numTokens);
					}

					// Execute set command
					else if (command.equalsIgnoreCase("set")) {
						setTokens(sender, playerName, numTokens);
					}

					// Invalid command
					else {
						invalidArguments(sender);
					}

				}

				// Invalid command (args cannot have more than 3 arguments)
				else {
					invalidArguments(sender);
				}
			}
		};

		r.runTaskAsynchronously(plugin);
		return true;
	}

	private boolean giveTokens(CommandSender sender, String playerName, String numTokens) {

		// Exit if sender does not have permission
		if (!sender.hasPermission("galacticwarps.tokens.give")) {
			noPermission(sender);
			return false;
		}

		// Convert numTokens to an int, if unable to, then exit
		int tokensToGive = 0;
		try {
			tokensToGive = Math.abs(Integer.parseInt(numTokens));
		} catch (Exception e) {
			invalidArguments(sender);
			return false;
		}

		// Exit if player does not exist
		if (!database.playerExists(playerName)) {
			playerNotExist(sender, playerName);
			return false;
		}

		// Execute give command
		database.giveTokens(playerName, tokensToGive);

		// Send success message
		String message = "&mGave &s" + tokensToGive + " &mwarp tokens to &s"
				+ Bukkit.getPlayerExact(playerName).getName();
		Language.sendMessage(sender, message);

		return true;
	}

	private boolean takeTokens(CommandSender sender, String playerName, String numTokens) {

		// Exit if sender does not have permission
		if (!sender.hasPermission("galacticwarps.tokens.take")) {
			noPermission(sender);
			return false;
		}

		// Convert numTokens to an int, if unable to, then exit
		int tokensToTake = 0;
		try {
			tokensToTake = Math.abs(Integer.parseInt(numTokens));
		} catch (Exception e) {
			invalidArguments(sender);
			return false;
		}

		// Exit if player does not exist
		if (!database.playerExists(playerName)) {
			playerNotExist(sender, playerName);
			return false;
		}

		// Execute take command
		database.takeTokens(playerName, tokensToTake);

		// Send success message
		String message = "&mTook &s" + tokensToTake + " &mwarp tokens from &s"
				+ Bukkit.getPlayerExact(playerName).getName();
		Language.sendMessage(sender, message);

		return true;
	}

	private boolean setTokens(CommandSender sender, String playerName, String numTokens) {

		// Exit if sender does not have permission
		if (!sender.hasPermission("galacticwarps.tokens.set")) {
			noPermission(sender);
			return false;
		}

		// Convert numTokens to an int, if unable to, then exit
		int tokensToSet = 0;
		try {
			tokensToSet = Math.abs(Integer.parseInt(numTokens));
		} catch (Exception e) {
			invalidArguments(sender);
			return false;
		}

		// Exit if player does not exist
		if (!database.playerExists(playerName)) {
			playerNotExist(sender, playerName);
			return false;
		}

		// Execute set command
		database.setTokens(playerName, tokensToSet);

		// Send success message
		String message = "&s" + Bukkit.getPlayerExact(playerName).getName() + " &mnow has &s" + tokensToSet
				+ " &mwarp tokens";
		Language.sendMessage(sender, message);

		return true;
	}

	private boolean viewTokens(CommandSender sender, String playerName) {

		// Exit if player has no permissions for this command
		if (!sender.hasPermission("galacticwarps.tokens.view.others")
				&& !sender.hasPermission("galacticwarps.tokens.view")) {
			noPermission(sender);
			return false;
		}

		// Exit if player does not exist
		if (!database.playerExists(playerName)) {
			playerNotExist(sender, playerName);
			return false;
		}

		// View other player's tokens if sender has permission
		if (sender.hasPermission("galacticwarps.tokens.view.others")) {
			int numTokens = database.getTokens(playerName);
			String message = "&s" + Bukkit.getPlayerExact(playerName).getName() + " &mhas &s" + numTokens
					+ " &mwarp tokens";
			Language.sendMessage(sender, message);
		}

		// View own player's tokens
		else {
			int numTokens = database.getTokens(sender.getName());
			String message = "&s" + sender.getName() + " &mhas &s" + numTokens + " &mwarp tokens";
			Language.sendMessage(sender, message);
		}

		return true;
	}

	// Method to send message to player if arguments were invalid
	private void invalidArguments(CommandSender sender) {
		String message = "&mThe arguments you entered were invalid! Correct usage: /gtokens [view/set/give/take] "
				+ "[player name] {tokens}";
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
