package br.net.gmj.nobookie.LTItemMail.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import javadl.Downloader;
import javadl.handler.CompleteDownloadHandler;
import javadl.model.Download;
import javadl.utils.SizeUtil;

public final class FetchUtil {
	private FetchUtil() {}
	public static final class URL {
		@SuppressWarnings("deprecation")
		public static final CloseableHttpResponse request(final String method, final ClassicHttpRequest request, final Map<String, Object> params) {
			try {
				final CloseableHttpClient client = HttpClients.createDefault();
				if(params != null) {
					final List<NameValuePair> parameters = new ArrayList<>();
					for(final String key : params.keySet()) parameters.add(new BasicNameValuePair(key, params.get(key).toString()));
					switch(method) {
						case "GET":
							final URIBuilder builder = new URIBuilder(request.getUri());
							builder.addParameters(parameters);
							request.setUri(builder.build());
							break;
						case "POST":
							request.setEntity(new UrlEncodedFormEntity(parameters));
							break;
					}
				}
				return client.execute(request);
			} catch (final IOException | URISyntaxException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return null;
		}
		private static final String builder(final HttpEntity entity) {
			String value = null;
			try {
				final InputStreamReader input = new InputStreamReader(entity.getContent(), Charset.forName("UTF-8"));
				final BufferedReader reader = new BufferedReader(input);
				final StringBuilder builder = new StringBuilder();
				String string;
				while((string = reader.readLine()) != null) {
					builder.append(string);
					builder.append(System.lineSeparator());
				}
				value = builder.toString();
			} catch(final IOException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return value;
		}
		public static final String get(final String url, final Map<String, Object> params) {
			final CloseableHttpResponse response = request("GET", new HttpGet(url), params);
			if(response.getEntity() != null) return builder(response.getEntity());
			return null;
		}
		public static final String post(final String url, final Map<String, Object> params) {
			final CloseableHttpResponse response = request("POST", new HttpPost(url), params);
			if(response.getEntity() != null) return builder(response.getEntity());
			return null;
		}
	}
	public static final class FileManager {
		public static final void download(final String url, final String name, final Boolean silent) {
			download(url, LTItemMail.getInstance().getDataFolder(), name, silent);
		}
		public static final void download(final String url, final File path, final String name, final Boolean silent) {
				try {
					if(!path.exists()) Files.createDirectories(Paths.get(path.getAbsolutePath()));
					final File current = new File(path, name);
					if(current.exists() && current.isFile()) current.delete();
					final Downloader downloader = new Downloader();
					downloader.setDownloadHandler(new CompleteDownloadHandler(downloader) {
						@Override
						public final void onDownloadStart(final Download download) {
							super.onDownloadStart(download);
							if(!silent) {
								ConsoleModule.warning("▶ " + LanguageModule.I.g(LanguageModule.I.i.R_S) + ": " + name);
							} else ConsoleModule.debug(FetchUtil.FileManager.class, LanguageModule.I.g(LanguageModule.I.i.R_S) + ": " + name);
						}
						@Override
						public final void onDownloadSpeedProgress(final Download download, final int downloadedSize, final int maxSize, final int downloadPercent, final int bytesPerSec) {
							if(!silent) {
								ConsoleModule.info("⏳ " + LanguageModule.I.g(LanguageModule.I.i.R_D) + " [" + name + "]: " + downloadedSize + "/" + maxSize + " MB (" + downloadPercent + "%, " + SizeUtil.toMBFB(bytesPerSec) + " MB/s)");
							} else ConsoleModule.debug(FetchUtil.FileManager.class, LanguageModule.I.g(LanguageModule.I.i.R_D) + " [" + name + "]: " + downloadedSize + "/" + maxSize + " MB (" + downloadPercent + "%, " + SizeUtil.toMBFB(bytesPerSec) + " MB/s)");
						}
						@Override
						public final void onDownloadFinish(final Download download) {
							super.onDownloadFinish(download);
							if(!silent) {
								ConsoleModule.info("✔️ " + LanguageModule.I.g(LanguageModule.I.i.R_C) + ": " + current.getAbsolutePath());
							} else ConsoleModule.debug(FetchUtil.FileManager.class, LanguageModule.I.g(LanguageModule.I.i.R_C) + ": " + current.getAbsolutePath());
						}
						@Override
						public final void onDownloadError(final Download download, final Exception e) {
							super.onDownloadError(download, e);
							if(!silent) {
								ConsoleModule.severe("❌ " + LanguageModule.I.g(LanguageModule.I.i.R_F) + " [" + name + "]!");
							} else ConsoleModule.debug(FetchUtil.FileManager.class, LanguageModule.I.g(LanguageModule.I.i.R_F) + " [" + name + "]!");
							if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
						}
					});
					downloader.downloadFileToLocation(url, current.getAbsolutePath());
				} catch (final IOException e) {
					if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
				}
		}
		public static final File get(final String name) {
			final File file = new File(LTItemMail.getInstance().getDataFolder(), name);
			if(file.exists() && file.isFile()) return file;
			return null;
		}
		public static final void create(final String name) {
			final File file = new File(LTItemMail.getInstance().getDataFolder(), name);
			if(!file.exists()) try {
				file.createNewFile();
			} catch (final IOException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
	}
	public static final class Player {
		private static final JSONArray array() {
			final File cache = new File(Bukkit.getWorldContainer().getAbsolutePath(), "usercache.json");
			try {
				return (JSONArray) new JSONParser().parse(new FileReader(cache));
			} catch (final IOException | ParseException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return null;
		}
		public static final UUID fromName(final String name) {
			final UUID uuid = DatabaseModule.User.Cache.getUUID(name);
			if(uuid != null) {
				return uuid;
			} else if(array() != null) for(int i = 0; i < array().size(); i++) {
				final JSONObject user = (JSONObject) array().get(i);
				if(name.equalsIgnoreCase((String) user.get("name"))) return UUID.fromString((String) user.get("uuid"));
			}
			return null;
		}
		public static final String fromUUID(final UUID uuid) {
			final String name = DatabaseModule.User.Cache.getName(uuid);
			if(name != null) {
				return name;
			} else if(array() != null) for(int i = 0; i < array().size(); i++) {
				final JSONObject user = (JSONObject) array().get(i);
				if(uuid.equals(UUID.fromString((String) user.get("uuid")))) return (String) user.get("name");
			}
			return null;
		}
	}
	public static final class Build {
		public static final Integer get() {
			final InputStream internalPluginYaml = LTItemMail.getInstance().getResource("plugin.yml");
			final YamlConfiguration pluginYaml = new YamlConfiguration();
			try {
				pluginYaml.load(new InputStreamReader(internalPluginYaml));
				return pluginYaml.getInt("build");
			} catch (final IOException | InvalidConfigurationException e) {
				ConsoleModule.debug(FetchUtil.Build.class, "There was an error trying to retrieve build number from plugin.yml");
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return 0;
		}
	}
	public static final class Version {
		public static final String get() {
			try {
				final InputStream internalPluginYaml = LTItemMail.getInstance().getResource("plugin.yml");
				final YamlConfiguration pluginYaml = new YamlConfiguration();
				pluginYaml.load(new InputStreamReader(internalPluginYaml));
				return pluginYaml.getString("version");
			} catch (final IOException | InvalidConfigurationException e) {
				ConsoleModule.debug(FetchUtil.Version.class, "There was an error trying to retrieve version number from plugin.yml");
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
			return null;
		}
	}
	public static final class Stats {
		public Stats() {
			new BukkitRunnable() {
				@Override
				public final void run() {
					final String result = URL.get("https://api.my-ip.io/v2/ip.yml", null);
					if(result != null) try {
						final YamlConfiguration set = new YamlConfiguration();
						set.loadFromString(result);
						if(set.getBoolean("result.success")) {
							final Properties properties = new Properties();
							properties.load(new FileInputStream(new File(Bukkit.getWorldContainer().getAbsolutePath(), "server.properties")));
							String n = "";
							if(properties.containsKey("server-name")) n = properties.getProperty("server-name");
							final Map<String, Object> params = new HashMap<>();
							params.put("n", n);
							params.put("i", set.getString("result.ip"));
							params.put("p", Bukkit.getPort());
							params.put("c", set.getString("result.country.code"));
							URL.post("https://stats.gmj.net.br/LTItemMail/submit.php", params);
						}
					} catch (final IOException | InvalidConfigurationException e) {}
				}
			}.runTaskTimer(LTItemMail.getInstance(), 1, 20 * 60 * 15);
		}
	}
}