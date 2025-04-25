package br.net.gmj.nobookie.LTItemMail.api.event;

import java.util.LinkedList;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
/**
 * 
 * Event called when a mailbox is sent by a player.
 * 
 * @author Nobookie
 * 
 */
public final class PlayerSendMailEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private final LTPlayer from;
	private final LTPlayer to;
	private final LinkedList<ItemStack> contents;
	private final Boolean hasCost;
	private final Double cost;
	private final String label;
	private Boolean cancelled = false;
	private String cancelReason = "";
	public PlayerSendMailEvent(final LTPlayer from, final LTPlayer to, final LinkedList<ItemStack> contents, final Boolean hasCost, final Double cost, final String label) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.from = from;
		this.to = to;
		this.contents = contents;
		this.hasCost = hasCost;
		this.cost = cost;
		this.label = label;
	}
	/**
	 * 
	 * Gets who sent the mailbox.
	 * 
	 */
	@NotNull
	public final LTPlayer getFrom() {
		return from;
	}
	/**
	 * 
	 * Gets who received the mailbox.
	 * 
	 */
	@NotNull
	public final LTPlayer getTo() {
		return to;
	}
	/**
	 * 
	 * Gets the mailbox contents.
	 * 
	 */
	@NotNull
	public final LinkedList<ItemStack> getContents(){
		return contents;
	}
	/**
	 * 
	 * Returns "true" if the mailbox was paid to be sent.
	 * 
	 */
	@NotNull
	public final Boolean hasCost() {
		return hasCost;
	}
	/**
	 * 
	 * If the mailbox was paid, returns the paid value, otherwise it will return "0.0".
	 * 
	 */
	@NotNull
	public final Double getCost() {
		return cost;
	}
	/**
	 * 
	 * Gets the label of the mail.
	 * 
	 */
	@NotNull
	public final String getLabel() {
		return label;
	}
	@Override
	public final HandlerList getHandlers() {
		return handlers;
	}
	@NotNull
	public static final HandlerList getHandlerList() {
		return handlers;
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
	 * Cancels the event. If cancelled, mailbox deliver is stopped before it reaches the addressee and returns to the sender.
	 * 
	 */
	@Override
	public final void setCancelled(@NotNull final boolean cancel) throws NullPointerException {
		Objects.requireNonNull(cancel);
		cancelled = cancel;
	}
	/**
	 * 
	 * If the event is cancelled, gets the cancel reason.
	 * 
	 */
	@NotNull
	public final String getCancelReason() {
		return cancelReason;
	}
	/**
	 * 
	 * Sets the event cancel reason.
	 * 
	 */
	public final void setCancelReason(@NotNull final String reason) throws NullPointerException {
		Objects.requireNonNull(reason);
		cancelReason = reason;
	}
}