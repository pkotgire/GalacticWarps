package io.github.pkotgire.GalacticWarps;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Language {

	// Send message in chat to sender
	public static void sendMessage(CommandSender sender, String message) {
		message = message.replaceAll("&m", mainColorCode);
		message = message.replaceAll("&s", specialColorCode);
		sender.sendMessage(translateColor(prefix + " " + message));
	}

	// Method to translate color codes into color
	public static String translateColor(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	// Chat Prefix
	public static String prefix = "";

	// Color Codes
	public static String mainColorCode = "&m";
	public static String specialColorCode = "&s";

	private static String regex = "&[0-9a-f]";

	// Sets value of prefix from value obtained from config
	public static void getChatSettings(GalacticWarps plugin) {

		// Get the prefix from the config
		prefix = plugin.getConfig().getString("chat-prefix");
		prefix = (prefix == null) ? "&8[&3Gala&cctic&8]" : prefix;

		// Get mainColorCode
		mainColorCode = plugin.getConfig().getString("main-color-code");
		mainColorCode = (mainColorCode != null) ? mainColorCode : "&m";
		mainColorCode = (mainColorCode.matches(regex)) ? mainColorCode : "&3";

		// Get specialColorCode
		specialColorCode = plugin.getConfig().getString("special-color-code");
		specialColorCode = (specialColorCode != null) ? specialColorCode : "&s";
		specialColorCode = (specialColorCode.matches(regex)) ? specialColorCode : "&c";

	}
}
