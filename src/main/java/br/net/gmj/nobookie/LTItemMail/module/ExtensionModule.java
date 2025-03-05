package br.net.gmj.nobookie.LTItemMail.module;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTBlueMap;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTDecentHolograms;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTDynmap;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTExtension;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTGriefPrevention;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTHeadDatabase;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTPlaceholderAPI;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTPlugMan;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTRedProtect;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTTownyAdvanced;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTUltimateAdvancementAPI;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTVaultPermission;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTWorldGuard;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;
import net.milkbowl.vault.permission.Permission;

public final class ExtensionModule {
	private static ExtensionModule instance = null;
	private final Map<Name, Plugin> plugins = new HashMap<>();
	private final Map<Function, Object> register = new HashMap<>();
	private ExtensionModule() {
		final PluginManager manager = Bukkit.getPluginManager();
		plugins.putIfAbsent(Name.VAULT, manager.getPlugin("Vault"));
		plugins.putIfAbsent(Name.COINSENGINE, manager.getPlugin("CoinsEngine"));
		plugins.putIfAbsent(Name.THENEWECONOMY, manager.getPlugin("TheNewEconomy"));
		plugins.putIfAbsent(Name.GRIEFPREVENTION, manager.getPlugin("GriefPrevention"));
		plugins.putIfAbsent(Name.REDPROTECT, manager.getPlugin("RedProtect"));
		plugins.putIfAbsent(Name.TOWNYADVANCED, manager.getPlugin("Towny"));
		plugins.putIfAbsent(Name.WORLDGUARD, manager.getPlugin("WorldGuard"));
		plugins.putIfAbsent(Name.DYNMAP, manager.getPlugin("dynmap"));
		plugins.putIfAbsent(Name.BLUEMAP, manager.getPlugin("BlueMap"));
		plugins.putIfAbsent(Name.DECENTHOLOGRAMS, manager.getPlugin("DecentHolograms"));
		plugins.putIfAbsent(Name.PLACEHOLDERAPI, manager.getPlugin("PlaceholderAPI"));
		plugins.putIfAbsent(Name.ULTIMATEADVANCEMENTAPI, manager.getPlugin("UltimateAdvancementAPI"));
		plugins.putIfAbsent(Name.HEADDATABASE, manager.getPlugin("HeadDatabase"));
	}
	public final void warn(final Name sourceName, final Name pluginName) {
		Plugin source = null;
		Plugin plugin = null;
		if(getPlugin(sourceName) != null) source = getPlugin(sourceName);
		if(getPlugin(pluginName) != null) plugin = getPlugin(pluginName);
		warn(source, plugin);
	}
	public final void warn(final Name sourceName, final Plugin plugin) {
		Plugin source = null;
		if(getPlugin(sourceName) != null) source = getPlugin(sourceName);
		warn(source, plugin);
	}
	private final void warn(final Plugin source, final Plugin plugin) {
		String sourceName = "";
		if(source != null && source.isEnabled()) sourceName = " through " + source.getDescription().getName();
		if(plugin != null && plugin.isEnabled()) plugin.getLogger().info("Hooked into " + LTItemMail.getInstance().getDescription().getName() + sourceName);
	}
	private final boolean register(final Function function) {
		switch(function) {
			case VAULT_PERMISSION:
				final RegisteredServiceProvider<Permission> permission = Bukkit.getServicesManager().getRegistration(Permission.class);
				if(permission != null) register.putIfAbsent(function, new LTVaultPermission(getPlugin(Name.VAULT), permission.getPlugin(), permission.getProvider()));
				break;
			case GRIEFPREVENTION:
				register.putIfAbsent(function, new LTGriefPrevention(getPlugin(Name.GRIEFPREVENTION)));
				break;
			case REDPROTECT:
				register.putIfAbsent(function, new LTRedProtect(getPlugin(Name.REDPROTECT)));
				break;
			case TOWNYADVANCED:
				register.putIfAbsent(function, new LTTownyAdvanced(getPlugin(Name.TOWNYADVANCED)));
				break;
			case WORLDGUARD:
				register.putIfAbsent(function, new LTWorldGuard(getPlugin(Name.WORLDGUARD)));
				break;
			case DYNMAP:
				register.putIfAbsent(function, new LTDynmap(getPlugin(Name.DYNMAP)));
				break;
			case BLUEMAP:
				register.putIfAbsent(function, new LTBlueMap(getPlugin(Name.BLUEMAP)));
				break;
			case DECENTHOLOGRAMS:
				register.putIfAbsent(function, new LTDecentHolograms(getPlugin(Name.DECENTHOLOGRAMS)));
				break;
			case PLACEHOLDERAPI:
				register.putIfAbsent(function, new LTPlaceholderAPI(getPlugin(Name.PLACEHOLDERAPI)));
				break;
			case ULTIMATEADVANCEMENTAPI:
				register.putIfAbsent(function, new LTUltimateAdvancementAPI(getPlugin(Name.ULTIMATEADVANCEMENTAPI)));
				break;
			case HEADDATABASE:
				register.putIfAbsent(function, new LTHeadDatabase(getPlugin(Name.HEADDATABASE)));
				break;
		}
		return isRegistered(function);
	}
	private Listener plugMan = null;
	public final void unload() {
		for(final Function function : reg().keySet()) {
			final LTExtension extension = (LTExtension) reg().get(function);
			extension.unload();
		}
		if(plugMan != null) HandlerList.unregisterAll(plugMan);
	}
	public final boolean isInstalled(final Name name) {
		Plugin plugin = null;
		if(getPlugin(name) != null) plugin = getPlugin(name);
		return (plugin != null && plugin.isEnabled());
	}
	public final boolean isRegistered(final Function function) {
		return register.containsKey(function);
	}
	public final Object get(final Function function) {
		if(isRegistered(function)) return register.get(function);
		return null;
	}
	private final Plugin getPlugin(final Name name) {
		if(plugins.containsKey(name)) return plugins.get(name);
		return null;
	}
	public final void load() {
		Boolean detected = false;
		for(final Name name : plugins.keySet()) if(isInstalled(name)) {
			detected = true;
			break;
		}
		if(detected) ConsoleModule.info("Loading extensions...");
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_ECONOMY_ENABLE)) EconomyModule.init();
		if(isInstalled(Name.VAULT) && !isRegistered(Function.VAULT_PERMISSION)) if(register(Function.VAULT_PERMISSION)) warn(Name.VAULT, ((LTVaultPermission) get(Function.VAULT_PERMISSION)).getPermissionPlugin());
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_GRIEFPREVENTION)) if(isInstalled(Name.GRIEFPREVENTION) && !isRegistered(Function.GRIEFPREVENTION)) {
			warn(null, Name.GRIEFPREVENTION);
			register(Function.GRIEFPREVENTION);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_REDPROTECT)) if(isInstalled(Name.REDPROTECT) && !isRegistered(Function.REDPROTECT)) {
			warn(null, Name.REDPROTECT);
			register(Function.REDPROTECT);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_TOWNYADVANCED)) if(isInstalled(Name.TOWNYADVANCED) && !isRegistered(Function.TOWNYADVANCED)) {
			warn(null, Name.TOWNYADVANCED);
			register(Function.TOWNYADVANCED);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_WORLDGUARD)) if(isInstalled(Name.WORLDGUARD) && !isRegistered(Function.WORLDGUARD)) {
			warn(null, Name.WORLDGUARD);
			register(Function.WORLDGUARD);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_DYNMAP)) if(isInstalled(Name.DYNMAP) && !isRegistered(Function.DYNMAP)) {
			warn(null, Name.DYNMAP);
			register(Function.DYNMAP);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_BLUEMAP)) if(isInstalled(Name.BLUEMAP) && !isRegistered(Function.BLUEMAP)) {
			warn(null, Name.BLUEMAP);
			register(Function.BLUEMAP);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_DECENTHOLOGRAMS)) if(isInstalled(Name.DECENTHOLOGRAMS) && !isRegistered(Function.DECENTHOLOGRAMS)) {
			warn(null, Name.DECENTHOLOGRAMS);
			register(Function.DECENTHOLOGRAMS);
			if(isRegistered(Function.DECENTHOLOGRAMS)) ((LTDecentHolograms) get(Function.DECENTHOLOGRAMS)).cleanup();
		}
		if(isInstalled(Name.PLACEHOLDERAPI) && !isRegistered(Function.PLACEHOLDERAPI)) {
			warn(null, Name.PLACEHOLDERAPI);
			register(Function.PLACEHOLDERAPI);
		}
		Boolean toastFallback = false;
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_ULTIMATEADVANCEMENTAPI)) {
			if(isInstalled(Name.ULTIMATEADVANCEMENTAPI)) {
				if(!isRegistered(Function.ULTIMATEADVANCEMENTAPI)) {
					warn(null, Name.ULTIMATEADVANCEMENTAPI);
					register(Function.ULTIMATEADVANCEMENTAPI);
				}
			} else toastFallback = true;
		} else toastFallback = true;
		if(toastFallback && ((String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_DISPLAY)).equalsIgnoreCase("TOAST")) {
			ConsoleModule.warning("You must install and enable UltimateAdvancementAPI in config.yml to use TOAST notifications. Falling back to CHAT notifications.");
			LTItemMail.getInstance().configuration.set(ConfigurationModule.Type.MAILBOX_DISPLAY.path(), "CHAT");
			try {
				LTItemMail.getInstance().configuration.save(FetchUtil.FileManager.get("config.yml"));
			} catch (final IOException e) {
				ConsoleModule.severe("Error while saving config.yml.");
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_HEADDATABASE)) if(isInstalled(Name.HEADDATABASE) && !isRegistered(Function.HEADDATABASE)) {
			warn(null, Name.HEADDATABASE);
			register(Function.HEADDATABASE);
		}
		if(detected) {
			ConsoleModule.info("Extensions loaded.");
		} else ConsoleModule.info("No extensions detected.");
		plugMan = new LTPlugMan();
	}
	public static final ExtensionModule reload() {
		if(instance != null) {
			instance.unload();
			instance = null;
			instance = new ExtensionModule();
			return instance;
		}
		return getInstance();
	}
	public final Map<Function, Object> reg(){
		return register;
	}
	public static final ExtensionModule getInstance() {
		if(instance == null) instance = new ExtensionModule();
		return instance;
	}
	public enum Name {
		VAULT,
		COINSENGINE,
		THENEWECONOMY,
		GRIEFPREVENTION,
		REDPROTECT,
		TOWNYADVANCED,
		WORLDGUARD,
		DYNMAP,
		BLUEMAP,
		DECENTHOLOGRAMS,
		PLACEHOLDERAPI,
		ULTIMATEADVANCEMENTAPI,
		HEADDATABASE
	}
	public enum Function {
		VAULT_PERMISSION,
		GRIEFPREVENTION,
		REDPROTECT,
		TOWNYADVANCED,
		WORLDGUARD,
		DYNMAP,
		BLUEMAP,
		DECENTHOLOGRAMS,
		PLACEHOLDERAPI,
		ULTIMATEADVANCEMENTAPI,
		HEADDATABASE
	}
}