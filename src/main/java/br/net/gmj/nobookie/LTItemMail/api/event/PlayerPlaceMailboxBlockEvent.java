package br.net.gmj.nobookie.LTItemMail.api.event;

import java.util.Objects;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
/**
 * 
 * Event called when a mailbox block is placed by a player.
 * 
 * @author Nobookie
 * 
 */
public class PlayerPlaceMailboxBlockEvent extends PlaceMailboxBlockEvent implements Cancellable {
	private final LTPlayer player;
	private Boolean cancelled = false;
	public PlayerPlaceMailboxBlockEvent(final MailboxBlock block, final PlaceMailboxBlockEvent.Reason reason, final LTPlayer player) {
		super(block, reason);
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
	 * Cancels the event. If cancelled, the block will not be placed.
	 * 
	 */
	@Override
	public final void setCancelled(@NotNull final boolean cancel) throws NullPointerException {
		Objects.requireNonNull(cancel);
		cancelled = cancel;
	}
}