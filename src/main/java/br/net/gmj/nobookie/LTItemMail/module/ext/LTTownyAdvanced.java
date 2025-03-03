package br.net.gmj.nobookie.LTItemMail.module.ext;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;

import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;

public final class LTTownyAdvanced implements LTExtension {
	private final Plugin plugin;
	public LTTownyAdvanced(final Plugin plugin) {
		this.plugin = plugin;
	}
	@Override
	public final Plugin getBasePlugin() {
		return plugin;
	}
	public final boolean canBuild(final Player player, final Location location) {
		final TownBlock block = TownyAPI.getInstance().getTownBlock(location);
		Boolean result = true;
		if(block != null) result = PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.BUILD);
		ConsoleModule.debug(getClass(), "#canBuild: " + player.getName() + " " + result);
		return result;
	}
	public final boolean canBreak(final Player player, final Location location) {
		final TownBlock block = TownyAPI.getInstance().getTownBlock(location);
		Boolean result = true;
		if(block != null) result = PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.DESTROY);
		ConsoleModule.debug(getClass(), "#canBreak: " + player.getName() + " " + result);
		return result;
	}
	public final boolean canInteract(final Player player, final Location location) {
		final TownBlock block = TownyAPI.getInstance().getTownBlock(location);
		Boolean result = true;
		if(block != null) result = PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.ITEM_USE);
		ConsoleModule.debug(getClass(), "#canInteract: " + player.getName() + " " + result);
		return result;
	}
}