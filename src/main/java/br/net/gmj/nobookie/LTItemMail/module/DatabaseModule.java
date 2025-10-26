/*
 * Copyright (C) 2025  Murilo Amaral Nappi
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package br.net.gmj.nobookie.LTItemMail.module;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;

public final class DatabaseModule {
	private DatabaseModule() {}
	private static final class SQLite {
		private static final File file = new File(LTItemMail.getInstance().getDataFolder(), (String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_SQLITE_FILE));
		private static final void check() {
			if(!file.exists()) {
				ConsoleModule.warning("Extracting " + (String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_SQLITE_FILE) + "...");
				LTItemMail.getInstance().saveResource((String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_SQLITE_FILE), false);
				ConsoleModule.info("Done.");
			}
		}
		private static final HikariDataSource connect() {
			if(file.exists()) {
				final HikariConfig config = new HikariConfig();
				config.setPoolName("LTItemMail SQLite");
				config.setJdbcUrl("jdbc:sqlite:" + LTItemMail.getInstance().getDataFolder() + File.separator + (String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_SQLITE_FILE));
				final HikariDataSource data = new HikariDataSource(config);
				if(data != null && data.isRunning()) {
					ConsoleModule.info("Loaded SQLite database.");
					return data;
				}
				data.close();
			}
			ConsoleModule.severe("Could not load SQLite database.");
			return null;
		}
	}
	private static final class MySQL {
		private static final HikariDataSource connect() {
			try {
				ConsoleModule.info("MySQL flags: [" + String.join(", ", ((String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_MYSQL_FLAGS)).replace("?", "").split("&")) + "]");
				final HikariConfig config = new HikariConfig();
				config.setPoolName("LTItemMail MySQL");
				config.setJdbcUrl("jdbc:mysql://" + (String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_MYSQL_HOST) + "/" + (String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_MYSQL_NAME) + (String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_MYSQL_FLAGS));
				config.setUsername((String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_MYSQL_USER));
				config.setPassword((String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_MYSQL_PASSWORD));
				config.addDataSourceProperty("cachePrepStmts", "true");
				config.addDataSourceProperty("prepStmtCacheSize", "250");
				config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
				config.addDataSourceProperty("useServerPrepStmts", "true");
				config.addDataSourceProperty("useLocalSessionState", "true");
				config.addDataSourceProperty("rewriteBatchedStatements", "true");
				config.addDataSourceProperty("cacheResultSetMetadata", "true");
				config.addDataSourceProperty("cacheServerConfiguration", "true");
				config.addDataSourceProperty("elideSetAutoCommits", "true");
				config.addDataSourceProperty("maintainTimeStats", "false");
				config.addDataSourceProperty("cacheCallableStmts", "true");
				config.setMaximumPoolSize((Integer) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_MYSQL_MAX_POOL_SIZE));
				config.setMaxLifetime((Integer) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_MYSQL_MAX_LIFETIME));
				config.setConnectionTimeout((Integer) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_MYSQL_CONNECTION_TIMEOUT));
				try {
					Driver driver;
					try {
						driver = (Driver) Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
					} catch (final ClassNotFoundException e) {
						// The non deprecated driver was not found, fall back to the deprecated one.
						driver = (Driver) Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
					}
					DriverManager.registerDriver(driver);
				} catch (final Exception e) {
					ConsoleModule.debug(DatabaseModule.MySQL.class, "Driver error.");
					if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
				}
				final HikariDataSource data = new HikariDataSource(config);
				if(data != null && data.isRunning()) {
					ConsoleModule.info("Opened MySQL connection.");
					return data;
				}
				data.close();
			} catch(final PoolInitializationException e) {
				ConsoleModule.severe("Could not open MySQL connection.");
				ConsoleModule.severe("Check the MySQL login information in config.yml and restart your Minecraft server.");
				ConsoleModule.severe("Is the MySQL server set up correctly?");
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
				Bukkit.getPluginManager().disablePlugin(LTItemMail.getInstance());
			}
			return null;
		}
	}
	public static final HikariDataSource connect() {
		switch(((String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_TYPE)).toLowerCase()) {
			case "sqlite":
				SQLite.check();
				return SQLite.connect();
			case "mysql":
				return MySQL.connect();
		}
		return null;
	}
	public static final void convert() {
		HikariDataSource data = null;
		switch(((String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_TYPE)).toLowerCase()) {
		case "sqlite":
			ConsoleModule.info("Starting database conversion... (MySQL => SQLite)");
			data = MySQL.connect();
			break;
		case "mysql":
			ConsoleModule.info("Starting database conversion... (SQLite => MySQL)");
			data = SQLite.connect();
			break;
		}
		Statement statement = null;
		ResultSet results = null;
		PreparedStatement prepared = null;
		if(data != null) {
			try {
				final Connection connection = data.getConnection();
				LTItemMail.getInstance().connection.createStatement().executeLargeUpdate("DELETE FROM mailbox;");
				statement = connection.createStatement();
				results = statement.executeQuery("SELECT * FROM mailbox;");
				while(results.next()) {
					prepared = LTItemMail.getInstance().connection.prepareStatement("INSERT INTO mailbox(id, uuid_from, uuid_to, sent_date, items, deleted, label, status) VALUES(?, ?, ?, ?, ?, ?, ?, ?);");
					prepared.setInt(1, results.getInt("id"));
					prepared.setString(2, results.getString("uuid_from"));
					prepared.setString(3, results.getString("uuid_to"));
					prepared.setString(4, results.getString("sent_date"));
					prepared.setString(5, results.getString("items"));
					prepared.setInt(6, results.getInt("deleted"));
					prepared.setString(7, results.getString("label"));
					prepared.setString(8, results.getString("status"));
					prepared.executeUpdate();
				}
				LTItemMail.getInstance().connection.createStatement().executeLargeUpdate("DELETE FROM mailbox_block;");
				statement = connection.createStatement();
				results = statement.executeQuery("SELECT * FROM mailbox_block;");
				while(results.next()) {
					prepared = LTItemMail.getInstance().connection.prepareStatement("INSERT INTO mailbox_block(id, owner_uuid, mailbox_server, mailbox_world, mailbox_x, mailbox_y, mailbox_z) VALUES(?, ?, ?, ?, ?, ?, ?);");
					prepared.setInt(1, results.getInt("id"));
					prepared.setString(2, results.getString("owner_uuid"));
					prepared.setString(3, results.getString("mailbox_server"));
					prepared.setString(4, results.getString("mailbox_world"));
					prepared.setInt(5, results.getInt("mailbox_x"));
					prepared.setInt(6, results.getInt("mailbox_y"));
					prepared.setInt(7, results.getInt("mailbox_z"));
					prepared.executeUpdate();
				}
				LTItemMail.getInstance().connection.createStatement().executeLargeUpdate("DELETE FROM users;");
				statement = connection.createStatement();
				results = statement.executeQuery("SELECT * FROM users;");
				while(results.next()) {
					prepared = LTItemMail.getInstance().connection.prepareStatement("INSERT INTO users(uuid, name, sent_count, received_count, ban, ban_reason, registered_date) VALUES(?, ?, ?, ?, ?, ?, ?);");
					prepared.setString(1, results.getString("uuid"));
					prepared.setString(2, results.getString("name"));
					prepared.setInt(3, results.getInt("sent_count"));
					prepared.setInt(4, results.getInt("received_count"));
					prepared.setInt(5, results.getInt("ban"));
					prepared.setString(6, results.getString("ban_reason"));
					prepared.setString(7, results.getString("registered_date"));
					prepared.executeUpdate();
				}
				ConfigurationModule.disableDatabaseConversion();
				ConsoleModule.info("Database conversion: Done!");
			} catch (final SQLException | NullPointerException e) {
				ConsoleModule.severe("Database conversion error: Could not convert");
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			data.close();
		} else ConsoleModule.warning("Database conversion error: SQLite or MySQL not reachable");
	}
	public static final void disconnect() {
		try {
			LTItemMail.getInstance().dataSource.close();
		} catch (final NullPointerException e) {
			ConsoleModule.debug(DatabaseModule.class, "Unable to disconnect from database.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
	}
	public static final boolean purge() {
		try {
			switch(((String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_TYPE)).toLowerCase()) {
				case "sqlite":
					LTItemMail.getInstance().connection.createStatement().executeLargeUpdate("DELETE FROM mailbox;");
					LTItemMail.getInstance().connection.createStatement().executeLargeUpdate("DELETE FROM mailbox_block;");
					LTItemMail.getInstance().connection.createStatement().executeLargeUpdate("DELETE FROM users;");
					break;
				case "mysql":
					LTItemMail.getInstance().connection.createStatement().executeLargeUpdate("TRUNCATE TABLE mailbox;");
					LTItemMail.getInstance().connection.createStatement().executeLargeUpdate("TRUNCATE TABLE mailbox_block;");
					LTItemMail.getInstance().connection.createStatement().executeLargeUpdate("TRUNCATE TABLE users;");
					break;
			}
			return true;
		} catch (final SQLException e) {
			ConsoleModule.debug(DatabaseModule.class, "Unable to purge database.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return false;
	}
	private static final int getCurrentVersion() {
		try {
			ResultSet results = null;
			Boolean exists = false;
			switch(((String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_TYPE)).toLowerCase()) {
				case "sqlite":
					final DatabaseMetaData meta = LTItemMail.getInstance().connection.getMetaData();
					results = meta.getTables(null, null, "config", new String[] {"TABLE"});
					exists = results.next();
					break;
				case "mysql":
					final Statement statement = LTItemMail.getInstance().connection.createStatement();
					results = statement.executeQuery("SHOW TABLES;");
					while(results.next()) if(results.getString(1).equals("config")) {
						exists = true;
						break;
					}
					break;
			}
			if(exists) {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				results = statement.executeQuery("SELECT version FROM config;");
				if(results.next()) return results.getInt("version");
			}
		} catch (final SQLException | NullPointerException e) {
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return 0;
	}
	public static final void checkForUpdates() {
		final Integer dbVer = getCurrentVersion();
		if(dbVer < DataModule.Version.DATABASE.value()) {
			for(Integer i = dbVer; i < DataModule.Version.DATABASE.value(); i++) {
				if(((String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_TYPE)).toLowerCase().equals("mysql") && i > 0 && i < 4) continue;
				ConsoleModule.info("Updating database... (" + i + " -> " + (i + 1) + ")");
				if(DatabaseModule.runSQL(i)) {
					ConsoleModule.info("Database updated! (" + i + " -> " + (i + 1) + ")");
				} else ConsoleModule.severe("Database update failed! (" + i + " -> " + (i + 1) + ")");
			}
		} else ConsoleModule.info("Database is up to date! (" + dbVer + ")");
	}
	private static final boolean runSQL(final int version) {
		final LinkedList<String> sql = new LinkedList<>();
		switch(((String) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_TYPE)).toLowerCase()) {
			case "sqlite":
				switch(version) {
					case 0:
						sql.add("CREATE TABLE config(version INTEGER NOT NULL);");
						sql.add("INSERT INTO config(version) VALUES('1');");
						sql.add("ALTER TABLE mailbox DROP COLUMN items_lost;");
						break;
					case 1:
						sql.add("CREATE TABLE mailbox_block ("
								+ "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
								+ "owner_uuid TEXT NOT NULL,"
								+ "mailbox_x  INTEGER NOT NULL,"
								+ "mailbox_y  INTEGER NOT NULL,"
								+ "mailbox_z  INTEGER NOT NULL"
								+ ");");
						sql.add("UPDATE config SET version = '2';");
						break;
					case 2:
						sql.add("ALTER TABLE mailbox ADD label TEXT;");
						sql.add("UPDATE config SET version = '3';");
						break;
					case 3:
						sql.add("CREATE TABLE users ("
							+ "uuid TEXT PRIMARY KEY NOT NULL UNIQUE,"
							+ "name TEXT NOT NULL,"
							+ "sent_count INTEGER NOT NULL,"
							+ "received_count INTEGER NOT NULL,"
							+ "ban INTEGER NOT NULL,"
							+ "ban_reason TEXT,"
							+ "registered_date TEXT NOT NULL"
							+ ");");
						sql.add("ALTER TABLE mailbox ADD status TEXT NOT NULL DEFAULT 'PENDING';");
						sql.add("ALTER TABLE mailbox RENAME COLUMN opened TO deleted;");
						sql.add("ALTER TABLE mailbox_block ADD mailbox_world TEXT NOT NULL;");
						sql.add("UPDATE config SET version = '4';");
						break;
					case 4:
						sql.add("ALTER TABLE mailbox_block ADD mailbox_server TEXT NOT NULL;");
						sql.add("UPDATE mailbox_block SET mailbox_server = '" + (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID) + "';");
						sql.add("UPDATE config SET version = '5';");
						break;
				}
				break;
			case "mysql":
				switch(version) {
					case 0:
						sql.add("DROP TABLE IF EXISTS config;");
						sql.add("CREATE TABLE config ("
								+ "version int NOT NULL"
								+ ") ENGINE=MyISAM DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;");
						sql.add("INSERT INTO config VALUES(4);");
						sql.add("DROP TABLE IF EXISTS mailbox;");
						sql.add("CREATE TABLE mailbox ("
								+ "id int NOT NULL AUTO_INCREMENT,"
								+ "uuid_from longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci,"
								+ "uuid_to longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,"
								+ "sent_date longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,"
								+ "items longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,"
								+ "deleted int NOT NULL DEFAULT '0',"
								+ "label longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci,"
								+ "status tinytext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci,"
								+ "PRIMARY KEY (id)"
								+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;");
						sql.add("DROP TABLE IF EXISTS mailbox_block;");
						sql.add("CREATE TABLE mailbox_block ("
								+ "id int NOT NULL AUTO_INCREMENT,"
								+ "owner_uuid longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,"
								+ "mailbox_world longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,"
								+ "mailbox_x int NOT NULL,"
								+ "mailbox_y int NOT NULL,"
								+ "mailbox_z int NOT NULL,"
								+ "PRIMARY KEY (id)"
								+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;");
						sql.add("DROP TABLE IF EXISTS users;");
						sql.add("CREATE TABLE users ("
								+ "uuid varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,"
								+ "name varchar(25) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,"
								+ "sent_count int NOT NULL DEFAULT '0',"
								+ "received_count int NOT NULL DEFAULT '0',"
								+ "ban int NOT NULL DEFAULT '0',"
								+ "ban_reason longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci,"
								+ "registered_date longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,"
								+ "PRIMARY KEY (uuid),"
								+ "UNIQUE KEY name_UNIQUE (name)"
								+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;");
						break;
					case 4:
						sql.add("ALTER TABLE mailbox_block ADD COLUMN mailbox_server VARCHAR(45) NOT NULL AFTER owner_uuid;");
						sql.add("UPDATE mailbox_block SET mailbox_server = '" + (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID) + "';");
						sql.add("UPDATE config SET version = '5';");
						break;
				}
				break;
		}
		if(sql.size() > 0) try {
			for(final String lines : sql) {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				statement.execute(lines);
			}
			return true;
		} catch (SQLException | NullPointerException e) {
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return false;
	}
	public static final class Virtual {
		public static final int saveMailbox(final UUID playerFrom, UUID playerTo, final LinkedList<ItemStack> items, final String label) {
			final String time = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now());
			try {
				final PreparedStatement insert = LTItemMail.getInstance().connection.prepareStatement("INSERT INTO mailbox(uuid_from, uuid_to, sent_date, items, label, status) VALUES(?, ?, ?, ?, ?, ?);");
				if(playerFrom != null) {
					insert.setString(1, playerFrom.toString());
				} else insert.setString(1, "");
				insert.setString(2, playerTo.toString());
				insert.setString(3, time);
				final YamlConfiguration itemString = new YamlConfiguration();
				for(Integer i = 0; i < items.size(); i++) itemString.set("i_" + String.valueOf(i), items.get(i));
				insert.setString(4, itemString.saveToString());
				insert.setString(5, label);
				insert.setString(6, Status.PENDING.toString());
				insert.executeUpdate();
				final Statement get = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = get.executeQuery("SELECT id FROM mailbox WHERE uuid_to = '" + playerTo.toString() + "' AND sent_date = '" + time + "' ORDER BY id DESC LIMIT 1;");
				if(results.next()) return results.getInt("id");
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return 0;
		}
		public static final boolean updateMailbox(final Integer mailboxID, final LinkedList<ItemStack> items) {
			try {
				final PreparedStatement statement = LTItemMail.getInstance().connection.prepareStatement("UPDATE mailbox SET items = ?, deleted = ? WHERE id = ?;");
				if(items.size() > 0) {
					final YamlConfiguration itemString = new YamlConfiguration();
					for(Integer i = 0; i < items.size(); i++) itemString.set("i_" + String.valueOf(i), items.get(i));
					statement.setString(1, itemString.saveToString());
					statement.setInt(2, 0);
				} else {
					statement.setString(1, "");
					statement.setInt(2, 1);
				}
				statement.setInt(3, mailboxID);
				statement.executeUpdate();
				return true;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final LinkedList<ItemStack> getMailbox(final Integer mailboxID) {
			final LinkedList<ItemStack> items = new LinkedList<>();
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT items FROM mailbox WHERE id = '" + mailboxID + "';");
				final YamlConfiguration itemString = new YamlConfiguration();
				Boolean empty = false;
				if(results.next()) if(!results.getString("items").equals("")) {
					itemString.loadFromString(results.getString("items"));
				} else empty = true;
				if(!empty) for(Integer i = 0; i < 27; i++) items.add(i, itemString.getItemStack("i_" + String.valueOf(i)));
			} catch (final SQLException | NullPointerException | InvalidConfigurationException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return items;
		}
		public static final boolean setMailboxDeleted(final Integer mailboxID) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				statement.executeUpdate("UPDATE mailbox SET deleted = '1' WHERE id = '" + mailboxID + "';");
				return true;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final boolean isMaiboxOwner(final UUID owner, final Integer mailboxID) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT uuid_to FROM mailbox WHERE id = '" + mailboxID + "';");
				if(results.next()) if(UUID.fromString(results.getString("uuid_to")).equals(owner)) return true;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final UUID getMailboxOwner(final Integer mailboxID) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT uuid_to FROM mailbox WHERE id = '" + mailboxID + "';");
				if(results.next()) return UUID.fromString(results.getString("uuid_to"));
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return null;
		}
		public static final UUID getMailboxFrom(final Integer mailboxID) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT uuid_from FROM mailbox WHERE id = '" + mailboxID + "';");
				if(results.next()) {
					if(!results.getString("uuid_from").isEmpty()) try {
						final UUID from = UUID.fromString(results.getString("uuid_from"));
						return from;
					} catch(final IllegalArgumentException e) {
						if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
					}
					return null;
				}
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return null;
		}
		public static final String getMailboxLabel(final Integer mailboxID) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT label FROM mailbox WHERE id = '" + mailboxID + "';");
				if(results.next()) {
					String label = results.getString("label");
					if(label == null) label = "";
					return label;
				}
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return "";
		}
		public static final boolean isMailboxDeleted(final Integer mailboxID) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT deleted FROM mailbox WHERE id = '" + mailboxID + "';");
				if(results.next()) if(results.getInt("deleted") == 1) return true;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final HashMap<Integer, String> getMailboxesList(final UUID owner, final Status status){
			String sts = " AND status = '" + status.toString() + "'";
			Integer deleted = 0;
			if(status.equals(Status.DENIED)) deleted = 1;
			if(status.equals(Status.ALL)) sts = "";
			final HashMap<Integer, String> mailboxes = new HashMap<>();
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT * FROM mailbox WHERE uuid_to = '" + owner.toString() + "' AND deleted = '" + deleted + "'" + sts + " ORDER BY id ASC;");
				while(results.next()) mailboxes.putIfAbsent(results.getInt("id"), results.getString("sent_date"));
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return mailboxes;
		}
		public static final HashMap<Integer, String> getDeletedMailboxesList(final UUID owner){
			final HashMap<Integer, String> mailboxes = new HashMap<>();
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT id,sent_date FROM mailbox WHERE uuid_to = '" + owner.toString() + "' AND deleted = '1' ORDER BY id ASC;");
				while(results.next()) mailboxes.putIfAbsent(results.getInt("id"), results.getString("sent_date"));
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return mailboxes;
		}
		public static final void setStatus(final Integer mailboxID, final Status status) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				statement.executeUpdate("UPDATE mailbox SET status = '" + status.toString() + "' WHERE id = '" + mailboxID + "';");
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
		public static final Status getStatus(final Integer mailboxID) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT status FROM mailbox WHERE id = '" + mailboxID + "';");
				if(results.next()) return Status.valueOf(results.getString("status"));
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return Status.UNDEFINED;
		}
		public enum Status {
			PENDING,
			DENIED,
			ACCEPTED,
			ALL,
			UNDEFINED
		}
	}
	public static final class Block {
		public static final boolean isMailboxBlock(final Location block) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT * FROM mailbox_block WHERE mailbox_server = '" + (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID) + "' AND mailbox_world = '" + block.getWorld().getName() + "' AND mailbox_x = '" + block.getBlockX() + "' AND mailbox_y = '" + block.getBlockY() + "' AND mailbox_z = '" + block.getBlockZ() + "';");
				return results.next();
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final boolean isMailboxOwner(final UUID owner, final Location block) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT owner_uuid FROM mailbox_block WHERE mailbox_server = '" + (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID) + "' AND mailbox_world = '" + block.getWorld().getName() + "' AND mailbox_x = '" + block.getBlockX() + "' AND mailbox_y = '" + block.getBlockY() + "' AND mailbox_z = '" + block.getBlockZ() + "';");
				if(results.next()) if(UUID.fromString(results.getString("owner_uuid")).equals(owner)) return true;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final UUID getMailboxOwner(final Location block) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT owner_uuid FROM mailbox_block WHERE mailbox_server = '" + (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID) + "' AND mailbox_world = '" + block.getWorld().getName() + "' AND mailbox_x = '" + block.getBlockX() + "' AND mailbox_y = '" + block.getBlockY() + "' AND mailbox_z = '" + block.getBlockZ() + "';");
				if(results.next()) return UUID.fromString(results.getString("owner_uuid"));
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return null;
		}
		public static final Integer getMailboxID(final Location block) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT id FROM mailbox_block WHERE mailbox_server = '" + (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID) + "' AND mailbox_world = '" + block.getWorld().getName() + "' AND mailbox_x = '" + block.getBlockX() + "' AND mailbox_y = '" + block.getBlockY() + "' AND mailbox_z = '" + block.getBlockZ() + "';");
				if(results.next()) return results.getInt("id");
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return null;
		}
		public static final List<MailboxBlock> getMailboxBlocks(){
			final List<MailboxBlock> blocks = new ArrayList<>();
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT * FROM mailbox_block ORDER BY id ASC;");
				while(results.next()) blocks.add(new MailboxBlock(results.getInt("id"), LTPlayer.fromUUID(UUID.fromString(results.getString("owner_uuid"))), results.getString("mailbox_server"), Bukkit.getWorld(results.getString("mailbox_world")), results.getInt("mailbox_x"), results.getInt("mailbox_y"), results.getInt("mailbox_z")));
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return blocks;
		}
		public static final List<MailboxBlock> getMailboxBlocks(final UUID owner){
			final List<MailboxBlock> blocks = new ArrayList<>();
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT * FROM mailbox_block WHERE owner_uuid = '" + owner.toString() + "' ORDER BY id ASC;");
				while(results.next()) blocks.add(new MailboxBlock(results.getInt("id"), LTPlayer.fromUUID(owner), results.getString("mailbox_server"), Bukkit.getWorld(results.getString("mailbox_world")), results.getInt("mailbox_x"), results.getInt("mailbox_y"), results.getInt("mailbox_z")));
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return blocks;
		}
		public static final boolean placeMailbox(final UUID owner, Location block) {
			try {
				final PreparedStatement statement = LTItemMail.getInstance().connection.prepareStatement("INSERT INTO mailbox_block(owner_uuid, mailbox_server, mailbox_world, mailbox_x, mailbox_y, mailbox_z) VALUES(?, ?, ?, ?, ?, ?);");
				statement.setString(1, owner.toString());
				statement.setString(2, (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID));
				statement.setString(3, block.getWorld().getName());
				statement.setInt(4, block.getBlockX());
				statement.setInt(5, block.getBlockY());
				statement.setInt(6, block.getBlockZ());
				statement.executeUpdate();
				return true;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final boolean breakMailbox(final Location block) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				statement.executeUpdate("DELETE FROM mailbox_block WHERE mailbox_server = '" + (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID) + "' AND mailbox_world = '" + block.getWorld().getName() + "' AND mailbox_x = '" + block.getBlockX() + "' AND mailbox_y = '" + block.getBlockY() + "' AND mailbox_z = '" + block.getBlockZ() + "';");
				return true;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
	}
	public static final class User {
		public static final boolean ban(final UUID user, String reason) {
			try {
				final PreparedStatement statement = LTItemMail.getInstance().connection.prepareStatement("UPDATE users SET ban = ?, ban_reason = ? WHERE uuid = ?;");
				statement.setInt(1, 1);
				statement.setString(2, reason);
				statement.setString(3, user.toString());
				statement.executeUpdate();
				return true;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final boolean unban(final UUID user) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				statement.executeUpdate("UPDATE users SET ban = '0', ban_reason = '' WHERE uuid = '" + user + "';");
				return true;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final boolean setSentCount(final UUID user, final Integer sent) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				statement.executeUpdate("UPDATE users SET sent_count = '" + sent + "' WHERE uuid = '" + user + "';");
				return true;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final boolean setReceivedCount(final UUID user, final Integer received) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				statement.executeUpdate("UPDATE users SET received_count = '" + received + "' WHERE uuid = '" + user + "';");
				return true;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final boolean isBanned(final UUID user) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT ban FROM users WHERE uuid = '" + user + "';");
				if(results.next()) return results.getInt("ban") == 1;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final LinkedList<String> getBansList(){
			final LinkedList<String> banlist = new LinkedList<>();
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT uuid FROM users WHERE ban = '1';");
				while(results.next()) banlist.add(LTPlayer.fromUUID(UUID.fromString(results.getString("uuid"))).getName());
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return banlist;
		}
		public static final String getBanReason(final UUID user) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT ban_reason FROM users WHERE uuid = '" + user + "';");
				if(results.next()) return results.getString("ban_reason");
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return null;
		}
		public static final boolean register(final LTPlayer player) {
			final String time = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now());
			try {
				final PreparedStatement insert = LTItemMail.getInstance().connection.prepareStatement("INSERT INTO users(uuid, name, sent_count, received_count, ban, registered_date) VALUES(?, ?, ?, ?, ?, ?);");
				insert.setString(1, player.getUniqueId().toString());
				insert.setString(2, player.getName());
				insert.setInt(3, 0);
				insert.setInt(4, 0);
				insert.setInt(5, 0);
				insert.setString(6, time);
				insert.executeUpdate();
				return true;
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final boolean isRegistered(final UUID user) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT name FROM users WHERE uuid = '" + user + "';");
				return results.next();
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return false;
		}
		public static final int getSentCount(final UUID user) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT sent_count FROM users WHERE uuid = '" + user + "';");
				if(results.next()) return results.getInt("sent_count");
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return 0;
		}
		public static final int getReceivedCount(final UUID user) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT received_count FROM users WHERE uuid = '" + user + "';");
				if(results.next()) return results.getInt("received_count");
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return 0;
		}
		public static final String getRegistryDate(final UUID user) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				final ResultSet results = statement.executeQuery("SELECT registered_date FROM users WHERE uuid = '" + user + "';");
				if(results.next()) return results.getString("registered_date");
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return null;
		}
		public static final void updateUUID(final Player player) {
			try {
				final Statement statement = LTItemMail.getInstance().connection.createStatement();
				statement.executeUpdate("UPDATE users SET uuid = '" + player.getUniqueId().toString() + "' WHERE name = '" + player.getName() + "';");
			} catch (final SQLException | NullPointerException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
		public static final class Cache {
			public static final String getName(final UUID uuid) {
				try {
					final Statement statement = LTItemMail.getInstance().connection.createStatement();
					final ResultSet results = statement.executeQuery("SELECT name FROM users WHERE uuid = '" + uuid + "';");
					if(results.next()) return results.getString("name");
				} catch (final SQLException | NullPointerException e) {
					if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
				}
				return null;
			}
			public static final UUID getUUID(final String name) {
				try {
					final Statement statement = LTItemMail.getInstance().connection.createStatement();
					final ResultSet results = statement.executeQuery("SELECT uuid FROM users WHERE name = '" + name + "';");
					if(results.next()) return UUID.fromString(results.getString("uuid"));
				} catch (final SQLException | NullPointerException e) {
					if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
				}
				return null;
			}
		}
	}
}