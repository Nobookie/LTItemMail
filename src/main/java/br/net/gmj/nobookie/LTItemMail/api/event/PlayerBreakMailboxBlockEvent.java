package br.net.gmj.nobookie.LTItemMail.api.event;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

import br.net.gmj.nobookie.LTItemMail.api.block.Block;
import br.net.gmj.nobookie.LTItemMail.api.event.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
/**
 * 
 * Event called when a mailbox block is broken by a player.
 * 
 * @author Nobookie
 * 
 */
public class PlayerBreakMailboxBlockEvent extends BreakMailboxBlockEvent implements Cancellable {
	private final LTPlayer player;
	private Boolean cancelled = false;
	public PlayerBreakMailboxBlockEvent(final Block block, final BreakMailboxBlockEvent.Reason reason, final LTPlayer player) {
		super(block, reason, false, null);
		this.player = player;
	}
	/**
	 * 
	 * Gets who is involved on this event.
	 * 
	 */
	@NotNull
	public final LTPlayer getPlayer() {
		return player;
	}
	/**
	 * 
	 * Gets if the event was cancelled.
	 * 
	 */
	@Override
	public final boolean isCancelled() {
		return cancelled;
	}
	/**
	 * 
	 * Cancels the event. If cancelled, the block will not be broken.
	 * 
	 */
	@Override
	public final void setCancelled(@NotNull final boolean cancel) {
		try {
			cancelled = cancel;
		} catch(final IllegalArgumentException e) {
			ConsoleModule.debug(getClass(), "Argument cannot be null.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
	}
}