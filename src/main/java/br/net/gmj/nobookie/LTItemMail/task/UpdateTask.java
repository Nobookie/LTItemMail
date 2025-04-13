package br.net.gmj.nobookie.LTItemMail.task;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;

public class UpdateTask {
	private final BukkitTask task;
	public UpdateTask() {
		task = new BukkitRunnable() {
			@Override
			public final void run() {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ltitemmail:itemmailadmin update -s");
				if(!(Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_UPDATE_PERIODIC_NOTIFICATION)) task.cancel();
			}
		}.runTaskTimer(LTItemMail.getInstance(), 1, 20 * 60 * 60 * 24);
	}
}