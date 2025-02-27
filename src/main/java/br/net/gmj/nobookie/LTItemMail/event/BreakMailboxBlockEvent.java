package br.net.gmj.nobookie.LTItemMail.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import br.net.gmj.nobookie.LTItemMail.block.Block;
/**
 * 
 * Event called when a mailbox block breaks.
 * 
 * @author Nobookie
 * 
 */
public class BreakMailboxBlockEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final Block block;
	private Reason reason;
	private final Boolean virtual;
	public BreakMailboxBlockEvent(final Block block, final Reason reason, Boolean virtual) {
		this.block = block;
		this.reason = reason;
		this.virtual = virtual;
	}
	/**
	 * 
	 * Gets the block affected by this event.
	 * 
	 */
	public final Block getBlock() {
		return block;
	}
	/**
	 * 
	 * "true" means that the block was removed from the database and it remains on the game as a vanilla shulker box.
	 * 
	 */
	public final Boolean isVirtual() {
		return virtual;
	}
	@Override
	public final HandlerList getHandlers() {
		return handlers;
	}
	public static final HandlerList getHandlerList() {
		return handlers;
	}
	/**
	 * 
	 * Gets what triggered the event.
	 * 
	 */
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
		ON_UNCLAIM,
		BY_PLAYER_OWNER,
		BY_PLAYER_ADMIN,
		BY_SERVER
	}
}
