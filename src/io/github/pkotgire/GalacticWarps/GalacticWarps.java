package io.github.pkotgire.GalacticWarps;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class GalacticWarps extends JavaPlugin {

	private Logger logger = Logger.getLogger("GalacticWarps");
	private Database database = new Database(this);

	@Override
	public void onEnable() {

		// Create config if it does not exist
		registerConfig();

		// Load everything else
		reload();
	}

	@Override
	public void onDisable() {
		database.closeConnection();
		println("GalacticWarps is disabling, if this is a reload and you experience issues, consider rebooting.");
	}

	// Method to open connection with database and initialize tables
	public void reload() {

		// Reload config
		reloadConfig();

		// Set the chat prefix
		Language.getChatSettings(this);

		// Open the connection with the database
		try {
			database.openConnection();

			// Register all commands
			registerCommands();

			// Register all listeners
			registerListeners();
		} catch (Exception e) {
			println("Unable to connect to a Database, GalacticWarps will not work properly!");
		}
	}

	// Method to log to console
	public void println(String str) {
		logger.info("[GalacticWarps] " + str);
	}

	// Generate the config file if it does not exist
	private void registerConfig() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
	}

	// Register all commands
	private void registerCommands() {
		this.getCommand("gtokens").setExecutor(new CommandGTokens(this, database));
		this.getCommand("gsetwarp").setExecutor(new CommandGSetWarp(this, database));
		this.getCommand("gdelwarp").setExecutor(new CommandGDelWarp(this, database));
		this.getCommand("gwarp").setExecutor(new CommandGWarp(this, database));
		this.getCommand("greload").setExecutor(new CommandGReload(this));
	}

	// Registers all event listeners
	private void registerListeners() {
		getServer().getPluginManager().registerEvents(database, this);
	}

}
