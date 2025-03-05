package br.net.gmj.nobookie.LTItemMail.module.ext;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;

public final class LTWorldGuard implements LTExtension {
	private final Plugin plugin;
	public LTWorldGuard(final Plugin plugin) {
		this.plugin = plugin;
	}
	@Override
	public final Plugin getBasePlugin() {
		return plugin;
	}
	@Override
	public final void unload() {}
	public final boolean canBuild(final Player player, final Location location) {
		final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		final RegionQuery query = container.createQuery();
		final Boolean result = query.testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.BLOCK_PLACE);
		ConsoleModule.debug(getClass(), "#canBuild: " + player.getName() + " " + result);
		return result;
	}
	public final boolean canBreak(final Player player, final Location location) {
		final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		final RegionQuery query = container.createQuery();
		final Boolean result = query.testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.BLOCK_BREAK);
		ConsoleModule.debug(getClass(), "#canBreak: " + player.getName() + " " + result);
		return result;
	}
	public final boolean canInteract(final Player player, final Location location) {
		final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		final RegionQuery query = container.createQuery();
		final Boolean result = query.testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.INTERACT);
		ConsoleModule.debug(getClass(), "#canInteract: " + player.getName() + " " + result);
		return result;
	}
}