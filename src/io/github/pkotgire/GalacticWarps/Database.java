package io.github.pkotgire.GalacticWarps;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Database implements Listener {

	// Instance variables
	private GalacticWarps plugin;
	private Connection connection;
	private Statement statement;
	private String tablePrefix, host, database, username, password;
	private int port;
	private boolean useMySQL = false;

	// Constructor for Database, requires config file to exist
	public Database(GalacticWarps pl) {
		// Set plugin
		plugin = pl;

		// Get SQL credentials
		tablePrefix = plugin.getConfig().getString("table-prefix");
		host = plugin.getConfig().getString("host");
		port = Integer.parseInt(plugin.getConfig().getString("port"));
		database = plugin.getConfig().getString("database");
		username = plugin.getConfig().getString("username");
		password = plugin.getConfig().getString("password");

	}

	// Method that creates a connection with the database, throws an exception
	// on failure
	public void openConnection() throws SQLException, ClassNotFoundException {
		synchronized (this) {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}

			// Get useMySQL value
			useMySQL = plugin.getConfig().getBoolean("use-mysql");

			// Open connection with MySQL server
			if (useMySQL) {
				try {
					Class.forName("com.mysql.jdbc.Driver");
					connection = DriverManager.getConnection(
							"jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username,
							this.password);
					initializeTables();
				} catch (Exception ex) {
					// If connection failed, open connection with SQLite
					plugin.println("Connection with MySQL failed, check your settings in the config!");
					plugin.println("Attempting to connect with local SQLite database file...");
					useMySQL = false;
				}
			}

			// Open connection with SQLite file
			if (!useMySQL) {
				Class.forName("org.sqlite.JDBC");
				connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/database.db");
				initializeTables();
			}

			statement = connection.createStatement();
		} 

		String databaseType = (useMySQL) ? "MySQL" : "SQLite";
		plugin.println("Succsesfully connected to " + databaseType + " database!");
	}

	// Method to close a connection with the database
	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			plugin.println("An error occured when closing the connection with the database!");
		}
	}

	// Method that generates appropriate MySQL tables if they do not exist
	private void initializeTables() throws SQLException {

		// Create the Player table if it does not exist
		if (!tableExists(tablePrefix + "player")) {
			plugin.println("Player table does not exist, creating it...");
			String query = "CREATE TABLE " + tablePrefix + "Player(Name VARCHAR(16) NOT NULL, Tokens INT NOT NULL, "
					+ "PRIMARY KEY (Name));";
			statement.executeUpdate(query);
		}

		// Create the Warp table if it does not exist
		if (!tableExists(tablePrefix + "warp")) {
			plugin.println("Warp table does not exist, creating it...");
			String query = "CREATE TABLE " + tablePrefix
					+ "Warp(Name VARCHAR(16) NOT NULL, World VARCHAR(32) NOT NULL, X DOUBLE NOT NULL, Y DOUBLE NOT NULL, "
					+ "Z DOUBLE NOT NULL, Yaw FLOAT NOT NULL, Pitch FLOAT NOT NULL, PRIMARY KEY (Name));";
			statement.executeUpdate(query);
		}

		// Create the Has_Warp table if it does not exist
		if (!tableExists(tablePrefix + "has_warp")) {
			plugin.println("Has_Warp table does not exist, creating it...");
			String query = "CREATE TABLE " + tablePrefix
					+ "Has_Warp(pName CHAR(16) NOT NULL, wName VARCHAR(16) NOT NULL, PRIMARY KEY (wName), "
					+ "FOREIGN KEY (pName) REFERENCES " + tablePrefix + "Player(Name), FOREIGN KEY (wName) REFERENCES "
					+ tablePrefix + "Warp(Name));";
			statement.executeUpdate(query);
		}

	}

	// Method that returns if the connection is open
	public boolean connectionIsOpen() {
		try {
			return (connection != null) ? !connection.isClosed() : false;
		} catch (SQLException e) {
			return false;
		}
	}

	// Adds new player to database if they do not exist
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		BukkitRunnable r = new BukkitRunnable() {
			@Override
			public void run() {
				Player player = event.getPlayer();

				// Add player to database if they are not already in it
				try {
					if (!playerExists(player.getName())) {
						String query = "INSERT INTO " + tablePrefix + "Player (Name, Tokens) VALUES (" + "\""
								+ player.getName() + "\"" + ", 0);";

						// plugin.println(query);
						statement.executeUpdate(query);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		};

		// Runs the query on a new thread
		r.runTaskAsynchronously(plugin);
	}

	// Method to check if the specified table exists in the database
	private boolean tableExists(String tableName) throws SQLException {
		DatabaseMetaData dbm = connection.getMetaData();
		ResultSet rs = dbm.getTables(null, null, tableName, null);
		return rs.next();
	}

	// Method to check if players exists in Player table
	public boolean playerExists(String playerName) {
		boolean result = false;
		String query = "SELECT Name FROM " + tablePrefix + "Player WHERE LOWER(Name) = " + "\""
				+ playerName.toLowerCase() + "\";";
		try {
			result = statement.executeQuery(query).next();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	// Method to check if a warp exists in the database
	public boolean warpExists(String warpName) {
		boolean warpExists = false;
		String query = "SELECT Name FROM " + tablePrefix + "Warp WHERE LOWER(Name) = " + "\"" + warpName.toLowerCase()
				+ "\";";
		try {
			warpExists = statement.executeQuery(query).next();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return warpExists;
	}

	// Method to get exact name of player
	public String getPlayerNameExact(String playerName) {
		String result = null;

		String query = "SELECT Name FROM " + tablePrefix + "Player WHERE LOWER(Name) = " + "\""
				+ playerName.toLowerCase() + "\";";

		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				result = resultSet.getString("Name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	// Method to get exact name of player
	public String getWarpNameExact(String warpName) {
		String result = null;

		String query = "SELECT Name FROM " + tablePrefix + "Warp WHERE LOWER(Name) = " + "\"" + warpName.toLowerCase()
				+ "\";";

		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				result = resultSet.getString("Name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	// Method to add tokens to a player's account
	public void giveTokens(String playerName, int numTokens) {

		String query = "UPDATE " + tablePrefix + "Player SET Tokens = Tokens + " + numTokens + " "
				+ "WHERE LOWER(Name) = " + "\"" + playerName.toLowerCase() + "\";";
		try {
			statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// Method to subtract tokens from a player's account
	public void takeTokens(String playerName, int numTokens) {

		int oldTokens = getTokens(playerName);
		int newTokens = (oldTokens - numTokens) < 0 ? 0 : oldTokens - numTokens;
		String query = "UPDATE " + tablePrefix + "Player SET Tokens = " + newTokens + " " + "WHERE LOWER(Name) = "
				+ "\"" + playerName.toLowerCase() + "\";";
		try {
			statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Method to set amount of tokens in a player's account
	public void setTokens(String playerName, int numTokens) {

		String query = "UPDATE " + tablePrefix + "Player SET Tokens = " + numTokens + " " + "WHERE LOWER(Name) = "
				+ "\"" + playerName.toLowerCase() + "\";";
		try {
			statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// Method to get the number of tokens currently in a player's account
	public int getTokens(String playerName) {
		int tokens = 0;
		ResultSet result;
		String query = "SELECT TOKENS FROM " + tablePrefix + "Player WHERE LOWER(Name) = " + "\""
				+ playerName.toLowerCase() + "\";";
		try {
			result = statement.executeQuery(query);

			if (result.next()) {
				tokens = result.getInt("Tokens");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return tokens;
	}

	// Method to add a warp to the database
	public void setWarp(String warpName, Location location) {

		// Store attributes of warp into variables
		String worldName = location.getWorld().getName();
		double X = location.getX();
		double Y = location.getY();
		double Z = location.getZ();
		float Yaw = location.getYaw();
		float Pitch = location.getPitch();

		String query = "";

		// Update warp if it already exists
		if (warpExists(warpName)) {
			query = "UPDATE " + tablePrefix + "Warp SET " + "World = " + "\"" + worldName + "\"" + ", " + "X = " + X
					+ ", " + "Y = " + Y + ", " + "Z = " + Z + ", " + "Yaw = " + Yaw + ", " + "Pitch = " + Pitch
					+ " WHERE LOWER(Name) = " + "\"" + warpName.toLowerCase() + "\"" + ";";
		}

		// Create a new warp if it does not exist
		else {
			query = "INSERT INTO " + tablePrefix + "Warp (Name, World, X, Y, Z, Yaw, Pitch) VALUES (" + "\"" + warpName
					+ "\"" + ", " + "\"" + worldName + "\"" + ", " + X + ", " + Y + ", " + Z + ", " + Yaw + ", " + Pitch
					+ ");";
		}

		// Execute the query
		try {
			statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// Method to set the owner of a warp
	public void setWarpOwner(String warpName, String playerName) {
		String query = "INSERT INTO " + tablePrefix + "Has_Warp (pName, wName) VALUES (" + "\"" + playerName + "\","
				+ "\"" + warpName + "\");";

		// Execute the query
		try {
			statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Method to get owner of a warp
	public String getWarpOwner(String warpName) {
		String result = null;

		String query = "SELECT pName FROM " + tablePrefix + "Has_Warp WHERE LOWER(wName) = " + "\""
				+ warpName.toLowerCase() + "\";";

		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				result = resultSet.getString("pName");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	// Method to get a ResultSet with the names of all the warps
	public ArrayList<String> getWarps() {
		ArrayList<String> result = new ArrayList<String>();
		String query = "SELECT Name FROM " + tablePrefix + "Warp;";

		try {
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				result.add(resultSet.getString("Name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	// Method to get the location of a warp
	public Location getWarpLocation(String warpName) {
		Location location = null;
		World world = null;
		String query = "SELECT * FROM " + tablePrefix + "Warp WHERE LOWER(Name) = " + "\"" + warpName.toLowerCase()
				+ "\";";

		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				world = plugin.getServer().getWorld(resultSet.getString("World"));
				if (world != null) {
					double X = resultSet.getDouble("X");
					double Y = resultSet.getDouble("Y");
					double Z = resultSet.getDouble("Z");
					float Yaw = resultSet.getFloat("Yaw");
					float Pitch = resultSet.getFloat("Pitch");
					location = new Location(world, X, Y, Z, Yaw, Pitch);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return location;
	}

	// Method to delete a warp
	public void deleteWarp(String warpName) {
		try {

			// Delete from Has_Warp table
			String query = "DELETE FROM " + tablePrefix + "Has_Warp WHERE LOWER(wName) = " + "\""
					+ warpName.toLowerCase() + "\";";
			statement.executeUpdate(query);

			// Delete from Warp table
			query = "DELETE FROM " + tablePrefix + "Warp WHERE LOWER(Name) = " + "\"" + warpName.toLowerCase() + "\";";
			statement.executeUpdate(query);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
