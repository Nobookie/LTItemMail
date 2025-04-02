package br.net.gmj.nobookie.LTItemMail.module;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import br.net.gmj.nobookie.LTItemMail.module.ext.LTVault;

public class PermissionModule {
	private PermissionModule() {}
	public static final void load() {
		for(final Type perm : Type.values()) {
			final Permission permission = new Permission(perm.node(), perm.permissionDefault());
			try {
				Bukkit.getPluginManager().addPermission(permission);
			} catch(final IllegalArgumentException e) {
				ConsoleModule.debug(PermissionModule.class, "Permission node " + permission.getName() + " already registered.");
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
	}
	public static final void unload() {
		for(final Type perm : Type.values()) {
			final Permission permission = new Permission(perm.node(), perm.permissionDefault());
			Bukkit.getPluginManager().removePermission(permission);
		}
	}
	public static final boolean hasPermission(final CommandSender sender, final Type permission) {
		final String node = permission.node();
		if(ExtensionModule.getInstance().isRegistered(ExtensionModule.EXT.VAULT)) return ((LTVault) ExtensionModule.getInstance().get(ExtensionModule.EXT.VAULT)).getAPI().has(sender, node);
		return sender.hasPermission(node);
	}
	public enum Type {
		CMD_PLAYER_MAIN("ltitemmail.player", "TRUE"),
		CMD_PLAYER_VERSION("ltitemmail.player.version", "TRUE"),
		CMD_PLAYER_LIST("ltitemmail.player.list", "TRUE"),
		CMD_PLAYER_COLOR("ltitemmail.player.color", "TRUE"),
		CMD_PLAYER_OPEN("ltitemmail.player.open", "TRUE"),
		CMD_PLAYER_DELETE("ltitemmail.player.delete", "TRUE"),
		CMD_PLAYER_SEND("ltitemmail.player.send", "TRUE"),
		CMD_PLAYER_PRICE("ltitemmail.player.price", "TRUE"),
		CMD_PLAYER_INFO("ltitemmail.player.info", "TRUE"),
		CMD_PLAYER_BLOCKS("ltitemmail.player.blocks", "TRUE"),
		CMD_PLAYER_NOTIFY("ltitemmail.player.notify", "TRUE"),
		CMD_ADMIN_MAIN("ltitemmail.admin", "OP"),
		CMD_ADMIN_UPDATE("ltitemmail.admin.update", "OP"),
		CMD_ADMIN_LIST("ltitemmail.admin.list", "OP"),
		CMD_ADMIN_RECOVER("ltitemmail.admin.recover", "OP"),
		CMD_ADMIN_BAN("ltitemmail.admin.ban", "OP"),
		CMD_ADMIN_UNBAN("ltitemmail.admin.unban", "OP"),
		CMD_ADMIN_BANLIST("ltitemmail.admin.banlist", "OP"),
		CMD_ADMIN_INFO("ltitemmail.admin.info", "OP"),
		CMD_ADMIN_RELOAD("ltitemmail.admin.reload", "OP"),
		CMD_ADMIN_BLOCKS("ltitemmail.admin.blocks", "OP"),
		CMD_ADMIN_NOTIFY("ltitemmail.admin.notify", "OP"),
		CMD_ADMIN_BYPASS("ltitemmail.admin.bypass", "OP"),
		CMD_ADMIN_PURGE("ltitemmail.admin.purge", "OP"),
		CMD_ADMIN_DUMP("ltitemmail.admin.dump", "OP"),
		CMD_ADMIN_CHANGELOG("ltitemmail.admin.changelog", "OP"),
		BLOCK_PLAYER_PLACE("ltitemmail.block.place", "TRUE"),
		BLOCK_PLAYER_BREAK("ltitemmail.block.break", "TRUE"),
		BLOCK_PLAYER_USE("ltitemmail.block.use", "TRUE"),
		BLOCK_ADMIN_BREAK("ltitemmail.block.break.bypass", "OP");
		private final String node;
		private final PermissionDefault permissionDefault;
		Type(final String node, final String permissionDefault){
			this.node = node;
			this.permissionDefault = PermissionDefault.getByName(permissionDefault);
		}
		public final String node() {
			return node;
		}
		public final PermissionDefault permissionDefault() {
			return permissionDefault;
		}
	}
}