package br.net.gmj.nobookie.LTItemMail.api.block;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

/**
 * 
 * Interface to represent all types of LT Item Mail blocks.
 * 
 * @author Nobookie
 * 
 */
public interface Block {
	/**
	 * 
	 * Gets the block id.
	 * 
	 */
	@NotNull
	public Integer getId();
	/**
	 * 
	 * Gets the block type.
	 * 
	 */
	@NotNull
	public Block.Type getType();
	/**
	 * 
	 * Gets in which server the block was created.
	 * 
	 */
	@NotNull
	public String getServer();
	/**
	 * 
	 * Gets the block current location.
	 * 
	 */
	@NotNull
	public Location getLocation();
	/**
	 * 
	 * Converts from LT Item Mail block to Bukkit block.
	 * 
	 */
	@NotNull
	public org.bukkit.block.Block getBukkitBlock();
	/**
	 * 
	 * Used internally. Do not mess with it.
	 * 
	 */
	@NotNull
	public List<Listener> getListeners();
	/**
	 * 
	 * Used internally. Do not mess with it.
	 * 
	 */
	public void runTasks();
	/**
	 * 
	 * Used internally. Do not mess with it.
	 * 
	 */
	@NotNull
	public List<BukkitTask> getTasks();
	/**
	 * 
	 * All types of blocks.
	 * 
	 */
	public enum Type {
		MAILBOX_BLOCK
	}
}