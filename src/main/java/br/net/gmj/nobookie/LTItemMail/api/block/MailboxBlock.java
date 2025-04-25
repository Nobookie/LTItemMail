package br.net.gmj.nobookie.LTItemMail.api.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.LTItemMailAPI;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.api.event.BreakMailboxBlockEvent;
import br.net.gmj.nobookie.LTItemMail.api.event.PlaceMailboxBlockEvent;
import br.net.gmj.nobookie.LTItemMail.block.listener.MailboxBlockListener;
import br.net.gmj.nobookie.LTItemMail.block.task.MailboxBlockTask;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;

/**
 * 
 * The object representing the block of the physical mailbox.
 * 
 * @author Nobookie
 * 
 */
public final class MailboxBlock {
	private final Integer id;
	private LTPlayer owner;
	private final String server;
	private final World world;
	private final Integer x;
	private final Integer y;
	private final Integer z;
	/**
	 * 
	 * Use {@link LTItemMailAPI#getMailboxBlock(Location)} instead.
	 * 
	 * 
	 */
	public MailboxBlock(final Integer id, final LTPlayer owner, final String server, final World world, final Integer x, final Integer y, final Integer z) {
		this.id = id;
		this.owner = owner;
		this.server = server;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	/**
	 * 
	 * Gets the block id.
	 * 
	 */
	public final Integer getId() {
		return id;
	}
	/**
	 * 
	 * Gets in which server the block was created.
	 * 
	 */
	public final String getServer() {
		return server;
	}
	/**
	 * 
	 * Gets the block current location.
	 * 
	 */
	public final Location getLocation() {
		return new Location(world, x, y, z);
	}
	/**
	 * 
	 * Converts from LT Item Mail block to Bukkit block.
	 * 
	 */
	public final org.bukkit.block.Block getBukkitBlock(){
		return getLocation().getBlock();
	}
	/**
	 * 
	 * Used internally. Do not mess with it.
	 * 
	 */
	public final List<Listener> getListeners(){
		return Arrays.asList(new MailboxBlockListener());
	}
	private final List<BukkitTask> tasks = new ArrayList<>();
	/**
	 * 
	 * Used internally. Do not mess with it.
	 * 
	 */
	public final void runTasks() {
		if(LTItemMail.getInstance().connection != null) tasks.add(Bukkit.getScheduler().runTaskTimer(LTItemMail.getInstance(), new MailboxBlockTask(), 20, 20));
	}
	/**
	 * 
	 * Used internally. Do not mess with it.
	 * 
	 */
	public final List<BukkitTask> getTasks(){
		return tasks;
	}
	/**
	 * 
	 * Gets the owner of the mailbox block.
	 * 
	 */
	@NotNull
	public final LTPlayer getOwner() {
		return owner;
	}
	/**
	 * 
	 * Removes the mailbox block.
	 * Unregisters the block from the database.
	 * The mailbox block will not drop.
	 * 
	 * @param virtual If set to true, the current block will be set to air.
	 * 
	 */
	public final void remove(@NotNull final Boolean virtual) throws NullPointerException {
		Objects.requireNonNull(virtual);
		Bukkit.getPluginManager().callEvent(new BreakMailboxBlockEvent(this, BreakMailboxBlockEvent.Reason.BY_SERVER, virtual, null));
		DatabaseModule.Block.breakMailbox(getLocation());
		if(!virtual) getBukkitBlock().setType(Material.AIR);
	}
	/**
	 * 
	 * Replaces the mailbox block.
	 * Unregisters the block and register again on the database.
	 * 
	 */
	public final void replace() {
		Bukkit.getPluginManager().callEvent(new BreakMailboxBlockEvent(this, BreakMailboxBlockEvent.Reason.BY_SERVER, false, null));
		DatabaseModule.Block.breakMailbox(getLocation());
		Bukkit.getPluginManager().callEvent(new PlaceMailboxBlockEvent(this, PlaceMailboxBlockEvent.Reason.BY_SERVER));
		DatabaseModule.Block.placeMailbox(owner.getUniqueId(), getLocation());
	}
	/**
	 * 
	 * Transfers the mailbox block to a new owner.
	 * @param newOwner The new owner's unique id.
	 * 
	 */
	public final Boolean transferOwnership(@NotNull final LTPlayer newOwner) throws NullPointerException {
		Objects.requireNonNull(newOwner);
		if(newOwner.getUniqueId() == owner.getUniqueId()) return false;
		DatabaseModule.Block.breakMailbox(getLocation());
		DatabaseModule.Block.placeMailbox(newOwner.getUniqueId(), getLocation());
		owner = newOwner;
		return true;
	}
}