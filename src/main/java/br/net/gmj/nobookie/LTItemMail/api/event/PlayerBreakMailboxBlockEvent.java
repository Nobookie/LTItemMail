package br.net.gmj.nobookie.LTItemMail.api.event;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.bukkit.event.Cancellable;

import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
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
	/**
	 * 
	 * The constructor of the event.
	 * 
	 * @param block The {@link MailboxBlock} object
	 * @param reason The reason of the event
	 * @param player The player involved on the event
	 * 
	 */
	public PlayerBreakMailboxBlockEvent(final MailboxBlock block, final BreakMailboxBlockEvent.Reason reason, final LTPlayer player) {
		super(block, reason, false, null, null);
		this.player = player;
	}
	/**
	 * 
	 * Gets who is involved on this event.
	 * 
	 * @return {@link LTPlayer} object representing a player.
	 * 
	 */
	@Nonnull
	public final LTPlayer getPlayer() {
		return player;
	}
	@Override
	public final boolean isCancelled() {
		return cancelled;
	}
	@Override
	public final void setCancelled(@Nonnull final boolean cancel) throws NullPointerException {
		Objects.requireNonNull(cancel);
		cancelled = cancel;
	}
}