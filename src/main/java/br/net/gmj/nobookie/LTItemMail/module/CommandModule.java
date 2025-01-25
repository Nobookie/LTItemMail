package br.net.gmj.nobookie.LTItemMail.module;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.command.LTCommandExecutor;
import br.net.gmj.nobookie.LTItemMail.command.LTCommandInfo;
import br.net.gmj.nobookie.LTItemMail.util.ReflectionsUtil;

public final class CommandModule {
	public CommandModule() {
		CommandMap map = null;
		try {
		   final Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
		   f.setAccessible(true);
		   map = (CommandMap) f.get(Bukkit.getServer());
		} catch(final Exception e) {
			ConsoleModule.debug("Bukkit CommandMap not found.");
            if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		if(map != null) {
			final List<Class<? extends LTCommandExecutor>> commands = new ArrayList<>();
			try {
	            for (final Class<? extends LTCommandExecutor> clazz : ReflectionsUtil.getSubtypesOf(LTCommandExecutor.class, LTCommandExecutor.class.getPackage().getName(), LTItemMail.getInstance().getLTClassLoader(), LTCommandExecutor.class)) commands.add(clazz);
	        } catch (final Exception e) {
	        	ConsoleModule.debug("Could not load command class with Reflections.");
	            if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
	        }
			if(commands.size() > 0) for(final Class<? extends LTCommandExecutor> clazz : commands) {
				if (!clazz.isAnnotationPresent(LTCommandInfo.class)) {
					ConsoleModule.debug("Missing annotation (" + LTCommandInfo.class.getName() + ") in class " + clazz.getName() + ".");
		            continue;
		        }
		        final LTCommandInfo cmdInfo = clazz.getAnnotation(LTCommandInfo.class);
		        final List<String> aliases = new ArrayList<>();
		        if(cmdInfo.aliases().split(",").length > 1) {
		        	for(final String alias : cmdInfo.aliases().split(",")) aliases.add(alias);
		        } else aliases.add(cmdInfo.aliases());
		        final LTCommand command = new LTCommand(cmdInfo.name(), cmdInfo.description(), cmdInfo.usage(), aliases);
		        String permission = cmdInfo.permission();
		        if(permission.equals("")) permission = "ltitemmail." + cmdInfo.name();
		        command.setPermission(permission);
		        command.setPermissionMessage(LanguageModule.get(LanguageModule.Type.PLAYER_PERMISSIONERROR));
		        try {
		        	command.setExecutor(clazz.getConstructor().newInstance());
		        } catch (final Exception e) {
		        	ConsoleModule.debug("Could not set command executor of class " + clazz.getName() + ".");
		            if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		            continue;
		        }
		        map.register("", command);
			}
		} else ConsoleModule.severe("Could not register commands: Bukkit CommandMap not found.");
	}
	private static class LTCommand extends Command {
		private LTCommandExecutor executor;
	    public LTCommand(final String name, final String description, final String usageMessage, final List<String> aliases) {
	        super(name, description, usageMessage, aliases);
	    }
		@Override
	    public final boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
	        return executor.onCommand(sender, this, commandLabel, args);
	    }
	    @Override
	    public final List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
	        return executor.onTabComplete(sender, this, alias, args);
	    }
	    public final void setExecutor(final LTCommandExecutor executor) {
	        this.executor = executor;
	    }
	}
}