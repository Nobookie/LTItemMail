package br.net.gmj.nobookie.LTItemMail.module;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule.Type;
import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;

public final class ConsoleModule {
	private ConsoleModule() {}
	private static final ConsoleCommandSender sender() {
		return Bukkit.getConsoleSender();
	}
	public static final void hello() {
		String buildDate = FetchUtil.URL.get(DataModule.getDateURL((Integer) ConfigurationModule.get(Type.BUILD_NUMBER)), null);
		if(buildDate == null) buildDate = ChatColor.DARK_RED + "Server down!";
		sender().sendMessage(ChatColor.DARK_AQUA + " _   _______ _____ __  __ ");
		sender().sendMessage(ChatColor.DARK_AQUA + "| | |__   __|_   _|  \\/  |");
		sender().sendMessage(ChatColor.DARK_AQUA + "| |    | |    | | | \\  / |" + ChatColor.WHITE + "  Build number: #" + ConfigurationModule.get(Type.BUILD_NUMBER));
		sender().sendMessage(ChatColor.DARK_AQUA + "| |____| |   _| |_| |\\/| |" + ChatColor.WHITE + "  Build date: " + buildDate.replaceAll(System.lineSeparator(), ""));
		sender().sendMessage(ChatColor.DARK_AQUA + "|______|_|  |_____|_|  |_|" + ChatColor.WHITE + "  Need support or have questions? https://discord.gg/Nvnrv3P");
	}
	public static final void info(final String message) {
		sender().sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "LTIM " + ChatColor.WHITE + "I] " + message);
	}
	public static final void warning(final String message) {
		sender().sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "LTIM " + ChatColor.YELLOW + "W" + ChatColor.WHITE + "] " + ChatColor.YELLOW + message);
	}
	public static final void severe(final String message) {
		sender().sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "LTIM " + ChatColor.RED + "E" + ChatColor.WHITE + "] " + ChatColor.RED + message);
	}
	public static final void raw(final String message) {
		sender().sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "LTIM" + ChatColor.WHITE + "] " + ChatColor.RESET + message);
	}
	public static final void debug(final Class<?> clazz, final String message) {
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) sender().sendMessage(ChatColor.GRAY + "[" + clazz.getName() + "] " + message);
	}
	public static final void server(final List<String> warnings) {
		sender().sendMessage(ChatColor.WHITE + "<!-- " + ChatColor.DARK_AQUA + "LT Item Mail " + ChatColor.RED + "Version Control");
		for(final String warning : warnings) sender().sendMessage(ChatColor.WHITE + "#   " + BukkitUtil.Text.Color.format(warning));
		sender().sendMessage(ChatColor.WHITE + "-->");
	}
	public static final void board(final CommandSender receiver, final Map<Integer, Map<String, Map<String, List<String>>>> messages) {
		receiver.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "<!-- " + ChatColor.DARK_AQUA + ChatColor.BOLD + "LT Item Mail " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Messages Board");
		receiver.sendMessage("");
		receiver.sendMessage("");
		for(final Integer id : messages.keySet()) {
			final Map<String, Map<String, List<String>>> contents1 = messages.get(id);
			for(final String title : contents1.keySet()) {
				final Map<String, List<String>> contents2 = contents1.get(title);
				for(final String datetime : contents2.keySet()) {
					receiver.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + title + " (" + datetime + ")");
					for(final String message : contents2.get(datetime)) receiver.sendMessage(ChatColor.WHITE + "#   " + BukkitUtil.Text.Color.format(message));
				}
				receiver.sendMessage("");
				receiver.sendMessage("");
			}
		}
		receiver.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "-->");
	}
	public static final void br() {
		sender().sendMessage("⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣴⣾⣷⣦⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
		sender().sendMessage("⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣴⣾⡿⠿⠟⠻⠿⢿⣷⣦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
		sender().sendMessage("⠀⠀⠀⠀⠀⠀⣀⣤⣾⣿⠟⠁⠀⠀⠀⠀⠀⠀⠈⠻⣿⣷⣤⣀⠀⠀⠀⠀⠀⠀");
		sender().sendMessage("⠀⠀⠀⣀⣴⣾⣿⣿⣿⡿⠿⠿⠿⠶⢶⣦⣤⣀⠀⠀⢹⣿⣿⣿⣷⣦⣀⠀⠀⠀");
		sender().sendMessage("⠀⠰⢾⣿⣿⣿⣿⣿⣿⡁⠀⠀⠀⠀⠀⠀⠈⠙⠳⣦⣈⣿⣿⣿⣿⣿⣿⡷⠆⠀");
		sender().sendMessage("⠀⠀⠀⠉⠻⢿⣿⣿⣿⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣹⣿⣿⣿⡿⠟⠉⠀⠀⠀");
		sender().sendMessage("⠀⠀⠀⠀⠀⠀⠉⠛⢿⣿⣦⡀⠀⠀⠀⠀⠀⠀⢀⣴⣿⡿⠛⠉⠀⠀⠀⠀⠀⠀");
		sender().sendMessage("⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠻⢿⣷⣶⣦⣴⣶⣾⡿⠟⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀");
		sender().sendMessage("⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠻⢿⡿⠟⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
		sender().sendMessage("");
		final List<ChatColor> cores = Arrays.asList(ChatColor.GREEN,
				ChatColor.YELLOW);
		int n = 0;
		String obrigado = "";
		for(char letra : new String("Obrigado por instalar o meu plugin! Aproveite =D").toCharArray()) {
			obrigado = obrigado + cores.get(n) + letra;
			n++;
			if(n >= cores.size()) n = 0;
		}
		sender().sendMessage(obrigado);
	}
}