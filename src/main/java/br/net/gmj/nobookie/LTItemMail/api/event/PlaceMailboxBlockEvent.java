package br.net.gmj.nobookie.LTItemMail.api.event;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
/**
 * 
 * Event called when a mailbox block is placed.
 * 
 * @author Nobookie
 * 
 */
public class PlaceMailboxBlockEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final MailboxBlock block;
	private final Reason reason;
	/**
	 * 
	 * The constructor of the event.
	 * 
	 * @param block The {@link MailboxBlock} object
	 * @param reason The reason of the event
	 * 
	 */
	public PlaceMailboxBlockEvent(final MailboxBlock block, final Reason reason) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.block = block;
		this.reason = reason;
	}
	/**
	 * 
	 * Gets the block affected by this event.
	 * 
	 * @return {@link MailboxBlock}
	 * 
	 */
	@Nonnull
	public final MailboxBlock getBlock() {
		return block;
	}
	@Override
	public final HandlerList getHandlers() {
		return handlers;
	}
	/**
	 * 
	 * See {@link HandlerList}
	 * 
	 * @return {@link HandlerList}
	 * 
	 */
	@Nonnull
	public static final HandlerList getHandlerList() {
		return handlers;
	}
	/**
	 * 
	 * Gets what triggered the event.
	 * 
	 * @return {@link Reason}
	 * 
	 */
	@Nonnull
	public final Reason getReason() {
		return reason;
	}
	/**
	 * 
	 * What triggered the event.
	 * 
	 * @author Nobookie
	 * 
	 */
	public enum Reason {
		/**
		 * 
		 * Called by player.
		 * 
		 */
		BY_PLAYER,
		/**
		 * 
		 * Called by server.
		 * 
		 */
		BY_SERVER
	}
}
