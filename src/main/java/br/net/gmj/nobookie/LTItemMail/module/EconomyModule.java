package br.net.gmj.nobookie.LTItemMail.module;

import java.math.BigDecimal;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.tnemc.core.TNECore;
import net.tnemc.core.api.TNEAPI;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;

public final class EconomyModule {
	private static EconomyModule instance = null;
	private static boolean available = false;
	private Type type = null;
	private Object currency = null;
	private Object api = null;
	private Plugin plugin = null;
	private EconomyModule() {
		final String[] config = ((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_ECONOMY_TYPE)).split("\\:");
		String coin = null;
		try {
			if(config.length > 1) {
				type = Type.valueOf(config[0].toUpperCase());
				coin = config[1];
			} else type = Type.valueOf(((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_ECONOMY_TYPE)).toUpperCase());
		} catch(final IllegalArgumentException e) {
			ConsoleModule.severe("Economy provider not found. Disabling.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		if(type == null) return;
		if(type.plugin() == null || (type.plugin() != null && !type.plugin().isEnabled())) return;
		switch(type) {
			case VAULT:
				final RegisteredServiceProvider<Economy> vault = Bukkit.getServicesManager().getRegistration(Economy.class);
				if(vault != null) {
					api = vault.getProvider();
					plugin = vault.getPlugin();
				} else return;
				break;
			case COINSENGINE:
				if(CoinsEngineAPI.class != null && coin != null) {
					for(final su.nightexpress.coinsengine.api.currency.Currency c : CoinsEngineAPI.getCurrencyManager().getCurrencies()) if(c.getName().equalsIgnoreCase(coin)) {
						currency = c;
						break;
					}
					if(currency == null) return;
				} else return;
				break;
			case THENEWECONOMY:
				if(TNECore.api() != null && coin != null) {
					api = TNECore.api();
					for(final net.tnemc.core.currency.Currency c : TNECore.api().getCurrencies()) if(c.getIdentifier().equalsIgnoreCase(coin)) {
						currency = c;
						break;
					}
					if(currency == null) return;
				} else return;
				break;
		}
		if(plugin != null) {
			ExtensionModule.getInstance().warn(type.plugin(), plugin);
		} else ExtensionModule.getInstance().warn(null, type.plugin());
		available = true;
	}
	public final boolean deposit(final OfflinePlayer player, final Integer amount) {
		return deposit(player, Double.parseDouble(String.valueOf(amount)));
	}
	public final boolean deposit(final OfflinePlayer player, final Double amount) {
		switch(type) {
			case VAULT:
				final EconomyResponse response = ((Economy) api).depositPlayer(player, amount);
				return response.transactionSuccess();
			case COINSENGINE:
				return CoinsEngineAPI.addBalance(player.getUniqueId(), (su.nightexpress.coinsengine.api.currency.Currency) currency, amount);
			case THENEWECONOMY:
				return ((TNEAPI) api).setHoldings(player.getName(), ((TNEAPI) api).getPlayerAccount(player.getUniqueId()).get().location().get().getWorld(), ((net.tnemc.core.currency.Currency) currency).getIdentifier(), ((TNEAPI) api).getHoldings(player.getName(), ((TNEAPI) api).getPlayerAccount(player.getUniqueId()).get().location().get().getWorld(), ((net.tnemc.core.currency.Currency) currency).getIdentifier()).add(BigDecimal.valueOf(amount)));
		}
		return false;
	}
	public final boolean withdraw(final OfflinePlayer player, final Integer amount) {
		return withdraw(player, Double.parseDouble(String.valueOf(amount)));
	}
	public final boolean withdraw(final OfflinePlayer player, final Double amount) {
		switch(type) {
			case VAULT:
				final EconomyResponse response = ((Economy) api).withdrawPlayer(player, amount);
				return response.transactionSuccess();
			case COINSENGINE:
				return CoinsEngineAPI.removeBalance(player.getUniqueId(), (su.nightexpress.coinsengine.api.currency.Currency) currency, amount);
			case THENEWECONOMY:
				return ((TNEAPI) api).setHoldings(player.getName(), ((TNEAPI) api).getPlayerAccount(player.getUniqueId()).get().location().get().getWorld(), ((net.tnemc.core.currency.Currency) currency).getIdentifier(), ((TNEAPI) api).getHoldings(player.getName(), ((TNEAPI) api).getPlayerAccount(player.getUniqueId()).get().location().get().getWorld(), ((net.tnemc.core.currency.Currency) currency).getIdentifier()).subtract(BigDecimal.valueOf(amount)));
		}
		return false;
	}
	public final boolean has(final OfflinePlayer player, final Integer amount) {
		return has(player, Double.parseDouble(String.valueOf(amount)));
	}
	public final boolean has(final OfflinePlayer player, final Double amount) {
		switch(type) {
			case VAULT:
				return ((Economy) api).has(player, amount);
			case COINSENGINE:
				final Double balance = CoinsEngineAPI.getBalance(player.getUniqueId(), (su.nightexpress.coinsengine.api.currency.Currency) currency);
				return (balance >= amount);
			case THENEWECONOMY:
				return ((TNEAPI) api).hasHoldings(player.getName(), ((TNEAPI) api).getPlayerAccount(player.getUniqueId()).get().location().get().getWorld(), ((net.tnemc.core.currency.Currency) currency).getIdentifier(), BigDecimal.valueOf(amount));
		}
		return false;
	}
	public final Type getEconomy() {
		if((api != null || currency != null) && type != null) return type;
		return null;
	}
	public static final EconomyModule getInstance() {
		if(instance == null) {
			instance = new EconomyModule();
		} else if(!available) instance = null;
		return instance;
	}
	public enum Type {
		VAULT(ExtensionModule.EXT.VAULT.plugin()),
		COINSENGINE(Bukkit.getPluginManager().getPlugin("CoinsEngine")),
		THENEWECONOMY(Bukkit.getPluginManager().getPlugin("TheNewEconomy"));
		private final Plugin plugin;
		Type(final Plugin plugin){
			this.plugin = plugin;
		}
		public final Plugin plugin() {
			return plugin;
		}
	}
}