package br.net.gmj.nobookie.LTItemMail.api.event;

import java.util.LinkedList;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

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
	private String cancelReason = null;
	/**
	 * 
	 * The constructor of the event.
	 * 
	 * @param from The player who sent it
	 * @param to The player who received it
	 * @param contents The delivery contents
	 * @param hasCost Whether the delivery was paid or not
	 * @param cost The delivery cost if there is any
	 * @param label The delivery label if there is any
	 * 
	 */
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
	 * Gets who is involved on this event.
	 * 
	 * @return {@link LTPlayer} object representing a player.
	 * 
	 */
	@Nonnull
	public final LTPlayer getFrom() {
		return from;
	}
	/**
	 * 
	 * Gets who received the mailbox.
	 * 
	 * @return {@link LTPlayer} object representing a player.
	 * 
	 */
	@Nonnull
	public final LTPlayer getTo() {
		return to;
	}
	/**
	 * 
	 * Gets the mailbox contents.
	 * 
	 * @return {@link LinkedList<ItemStack>}
	 * 
	 */
	@Nonnull
	public final LinkedList<ItemStack> getContents(){
		return contents;
	}
	/**
	 * 
	 * Whether the delivery has a cost or not.
	 * 
	 * @return {@link Boolean#TRUE} if the delivery was paid
	 * 
	 */
	@Nonnull
	public final Boolean hasCost() {
		return hasCost;
	}
	/**
	 * 
	 * The amount paid by the player who sent it
	 * 
	 * @return The amount paid or 0.0 if {@link PlayerSendMailEvent#hasCost()} is {@link Boolean#FALSE}
	 * 
	 */
	@Nonnull
	public final Double getCost() {
		return cost;
	}
	/**
	 * 
	 * Gets the label of the mail.
	 * 
	 * @return {@link String}
	 * 
	 */
	@Nonnull
	public final String getLabel() {
		return label;
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
	@Override
	public final boolean isCancelled() {
		return cancelled;
	}
	@Override
	public final void setCancelled(@Nonnull final boolean cancel) throws NullPointerException {
		Objects.requireNonNull(cancel);
		cancelled = cancel;
	}
	/**
	 * 
	 * If the event is cancelled, gets the cancel reason.
	 * 
	 * @return {@link String} or null
	 * 
	 */
	@Nullable
	public final String getCancelReason() {
		return cancelReason;
	}
	/**
	 * 
	 * Sets the event cancel reason.
	 * 
	 * @param reason The cancel reason
	 * 
	 */
	public final void setCancelReason(final String reason) {
		cancelReason = reason;
	}
}