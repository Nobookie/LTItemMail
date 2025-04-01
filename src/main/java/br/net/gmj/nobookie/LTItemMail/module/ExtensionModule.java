package br.net.gmj.nobookie.LTItemMail.module;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
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
import br.net.gmj.nobookie.LTItemMail.module.ext.LTSkulls;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTTownyAdvanced;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTUltimateAdvancementAPI;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTVault;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTWorldGuard;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;
import net.milkbowl.vault.permission.Permission;

public final class ExtensionModule {
	private static ExtensionModule instance = null;
	public final Map<EXT, LTExtension> REG = new HashMap<>();
	public final void warn(final Plugin source, final Plugin plugin) {
		String sourceEXT = "";
		if(source != null && source.isEnabled()) sourceEXT = " through " + source.getDescription().getName();
		if(plugin != null && plugin.isEnabled()) plugin.getLogger().info("Hooked into " + LTItemMail.getInstance().getDescription().getName() + sourceEXT);
	}
	private final boolean register(final EXT plugin) {
		switch(plugin) {
			case VAULT:
				final RegisteredServiceProvider<Permission> permission = Bukkit.getServicesManager().getRegistration(Permission.class);
				if(permission != null) REG.putIfAbsent(plugin, new LTVault(permission.getPlugin(), permission.getProvider()));
				break;
			case GRIEFPREVENTION:
				REG.putIfAbsent(plugin, new LTGriefPrevention());
				break;
			case REDPROTECT:
				REG.putIfAbsent(plugin, new LTRedProtect());
				break;
			case TOWNYADVANCED:
				REG.putIfAbsent(plugin, new LTTownyAdvanced());
				break;
			case WORLDGUARD:
				REG.putIfAbsent(plugin, new LTWorldGuard());
				break;
			case DYNMAP:
				REG.putIfAbsent(plugin, new LTDynmap());
				break;
			case BLUEMAP:
				REG.putIfAbsent(plugin, new LTBlueMap());
				break;
			case DECENTHOLOGRAMS:
				REG.putIfAbsent(plugin, new LTDecentHolograms());
				break;
			case PLACEHOLDERAPI:
				REG.putIfAbsent(plugin, new LTPlaceholderAPI());
				break;
			case ULTIMATEADVANCEMENTAPI:
				REG.putIfAbsent(plugin, new LTUltimateAdvancementAPI());
				break;
			case HEADDATABASE:
				REG.putIfAbsent(plugin, new LTHeadDatabase());
				break;
			case SKULLS:
				REG.putIfAbsent(plugin, new LTSkulls());
				break;
		}
		return isRegistered(plugin);
	}
	private Listener plugMan = null;
	public final void unload() {
		ConsoleModule.info("Unloading extensions...");
		for(final EXT plugin : REG.keySet()) {
			final LTExtension extension = (LTExtension) REG.get(plugin);
			extension.unload();
		}
		if(plugMan != null) HandlerList.unregisterAll(plugMan);
	}
	private final boolean isInstalled(final Plugin plugin) {
		if(plugin != null) return plugin.isEnabled();
		return false;
	}
	public final boolean isRegistered(final EXT plugin) {
		return REG.containsKey(plugin);
	}
	public final LTExtension get(final EXT plugin) {
		if(isRegistered(plugin)) return REG.get(plugin);
		return null;
	}
	public final void load() {
		ConsoleModule.info("Loading extensions...");
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_ECONOMY_ENABLE)) EconomyModule.init();
		if(isInstalled(EXT.VAULT.plugin()) && !isRegistered(EXT.VAULT)) if(register(EXT.VAULT)) warn(EXT.VAULT.plugin(), ((LTVault) get(EXT.VAULT)).getPermissionPlugin());
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_GRIEFPREVENTION)) if(isInstalled(EXT.GRIEFPREVENTION.plugin()) && !isRegistered(EXT.GRIEFPREVENTION)) {
			warn(null, EXT.GRIEFPREVENTION.plugin());
			register(EXT.GRIEFPREVENTION);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_REDPROTECT)) if(isInstalled(EXT.REDPROTECT.plugin()) && !isRegistered(EXT.REDPROTECT)) {
			warn(null, EXT.REDPROTECT.plugin());
			register(EXT.REDPROTECT);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_TOWNYADVANCED)) if(isInstalled(EXT.TOWNYADVANCED.plugin()) && !isRegistered(EXT.TOWNYADVANCED)) {
			warn(null, EXT.TOWNYADVANCED.plugin());
			register(EXT.TOWNYADVANCED);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_WORLDGUARD)) if(isInstalled(EXT.WORLDGUARD.plugin()) && !isRegistered(EXT.WORLDGUARD)) {
			warn(null, EXT.WORLDGUARD.plugin());
			register(EXT.WORLDGUARD);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_DYNMAP)) if(isInstalled(EXT.DYNMAP.plugin()) && !isRegistered(EXT.DYNMAP)) {
			warn(null, EXT.DYNMAP.plugin());
			register(EXT.DYNMAP);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_BLUEMAP)) if(isInstalled(EXT.BLUEMAP.plugin()) && !isRegistered(EXT.BLUEMAP)) {
			warn(null, EXT.BLUEMAP.plugin());
			register(EXT.BLUEMAP);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_DECENTHOLOGRAMS)) if(isInstalled(EXT.DECENTHOLOGRAMS.plugin()) && !isRegistered(EXT.DECENTHOLOGRAMS)) {
			warn(null, EXT.DECENTHOLOGRAMS.plugin());
			register(EXT.DECENTHOLOGRAMS);
			if(isRegistered(EXT.DECENTHOLOGRAMS)) ((LTDecentHolograms) get(EXT.DECENTHOLOGRAMS)).cleanup();
		}
		if(isInstalled(EXT.PLACEHOLDERAPI.plugin()) && !isRegistered(EXT.PLACEHOLDERAPI)) {
			warn(null, EXT.PLACEHOLDERAPI.plugin());
			register(EXT.PLACEHOLDERAPI);
		}
		Boolean toastFallback = false;
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_ULTIMATEADVANCEMENTAPI)) {
			if(isInstalled(EXT.ULTIMATEADVANCEMENTAPI.plugin())) {
				if(!isRegistered(EXT.ULTIMATEADVANCEMENTAPI)) {
					warn(null, EXT.ULTIMATEADVANCEMENTAPI.plugin());
					register(EXT.ULTIMATEADVANCEMENTAPI);
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
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_HEADDATABASE)) if(isInstalled(EXT.HEADDATABASE.plugin()) && !isRegistered(EXT.HEADDATABASE)) {
			warn(null, EXT.HEADDATABASE.plugin());
			register(EXT.HEADDATABASE);
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_SKULLS)) if(isInstalled(EXT.SKULLS.plugin()) && !isRegistered(EXT.SKULLS)) {
			warn(null, EXT.SKULLS.plugin());
			register(EXT.SKULLS);
		}
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
	public static final ExtensionModule getInstance() {
		if(instance == null) instance = new ExtensionModule();
		return instance;
	}
	public enum EXT {
		VAULT(Bukkit.getPluginManager().getPlugin("Vault")),
		GRIEFPREVENTION(Bukkit.getPluginManager().getPlugin("GriefPrevention")),
		REDPROTECT(Bukkit.getPluginManager().getPlugin("RedProtect")),
		TOWNYADVANCED(Bukkit.getPluginManager().getPlugin("Towny")),
		WORLDGUARD(Bukkit.getPluginManager().getPlugin("WorldGuard")),
		DYNMAP(Bukkit.getPluginManager().getPlugin("dynmap")),
		BLUEMAP(Bukkit.getPluginManager().getPlugin("BlueMap")),
		DECENTHOLOGRAMS(Bukkit.getPluginManager().getPlugin("DecentHolograms")),
		PLACEHOLDERAPI(Bukkit.getPluginManager().getPlugin("PlaceholderAPI")),
		ULTIMATEADVANCEMENTAPI(Bukkit.getPluginManager().getPlugin("UltimateAdvancementAPI")),
		HEADDATABASE(Bukkit.getPluginManager().getPlugin("HeadDatabase")),
		SKULLS(Bukkit.getPluginManager().getPlugin("Skulls"));
		private final Plugin plugin;
		EXT(final Plugin plugin){
			this.plugin = plugin;
		}
		public final Plugin plugin() {
			return plugin;
		}
	}
}