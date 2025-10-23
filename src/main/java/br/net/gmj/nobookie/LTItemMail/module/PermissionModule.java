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
		return sender.hasPermission(Bukkit.getPluginManager().getPermission(node));
	}
	public enum Type {
		CMD_PLAYER_MAIN("ltitemmail.player", PermissionDefault.TRUE),
		CMD_PLAYER_VERSION("ltitemmail.player.version", PermissionDefault.TRUE),
		CMD_PLAYER_LIST("ltitemmail.player.list", PermissionDefault.TRUE),
		CMD_PLAYER_COLOR("ltitemmail.player.color", PermissionDefault.TRUE),
		CMD_PLAYER_OPEN("ltitemmail.player.open", PermissionDefault.TRUE),
		CMD_PLAYER_DELETE("ltitemmail.player.delete", PermissionDefault.TRUE),
		CMD_PLAYER_SEND("ltitemmail.player.send", PermissionDefault.TRUE),
		CMD_PLAYER_PRICE("ltitemmail.player.price", PermissionDefault.TRUE),
		CMD_PLAYER_INFO("ltitemmail.player.info", PermissionDefault.TRUE),
		CMD_PLAYER_BLOCKS("ltitemmail.player.blocks", PermissionDefault.TRUE),
		CMD_PLAYER_NOTIFY("ltitemmail.player.notify", PermissionDefault.TRUE),
		CMD_ADMIN_MAIN("ltitemmail.admin", PermissionDefault.OP),
		CMD_ADMIN_UPDATE("ltitemmail.admin.update", PermissionDefault.OP),
		CMD_ADMIN_LIST("ltitemmail.admin.list", PermissionDefault.OP),
		CMD_ADMIN_RECOVER("ltitemmail.admin.recover", PermissionDefault.OP),
		CMD_ADMIN_BAN("ltitemmail.admin.ban", PermissionDefault.OP),
		CMD_ADMIN_UNBAN("ltitemmail.admin.unban", PermissionDefault.OP),
		CMD_ADMIN_BANLIST("ltitemmail.admin.banlist", PermissionDefault.OP),
		CMD_ADMIN_INFO("ltitemmail.admin.info", PermissionDefault.OP),
		CMD_ADMIN_RELOAD("ltitemmail.admin.reload", PermissionDefault.OP),
		CMD_ADMIN_BLOCKS("ltitemmail.admin.blocks", PermissionDefault.OP),
		CMD_ADMIN_NOTIFY("ltitemmail.admin.notify", PermissionDefault.OP),
		CMD_ADMIN_BYPASS("ltitemmail.admin.bypass", PermissionDefault.OP),
		CMD_ADMIN_WIPE("ltitemmail.admin.wipe", PermissionDefault.OP),
		CMD_ADMIN_DUMP("ltitemmail.admin.dump", PermissionDefault.OP),
		CMD_ADMIN_CHANGELOG("ltitemmail.admin.changelog", PermissionDefault.OP),
		CMD_ADMIN_GIVE("ltitemmail.admin.give", PermissionDefault.OP),
		BLOCK_PLAYER_PLACE("ltitemmail.block.place", PermissionDefault.TRUE),
		BLOCK_PLAYER_BREAK("ltitemmail.block.break", PermissionDefault.TRUE),
		BLOCK_PLAYER_USE("ltitemmail.block.use", PermissionDefault.TRUE),
		BLOCK_ADMIN_BREAK("ltitemmail.block.break.bypass", PermissionDefault.OP);
		private final String node;
		private final PermissionDefault permissionDefault;
		Type(final String node, final PermissionDefault permissionDefault){
			this.node = node;
			this.permissionDefault = permissionDefault;
		}
		public final String node() {
			return node;
		}
		public final PermissionDefault permissionDefault() {
			return permissionDefault;
		}
	}
}