package br.net.gmj.nobookie.LTItemMail.module;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;

public final class DataModule {
	private DataModule() {}
	private static final String JENKINS = "https://jenkins.nobookie.net.br";
	private static final String DATE = JENKINS + "/job/" + LTItemMail.getInstance().getDescription().getName() + "/$build/buildTimestamp";
	private static final String LOG = JENKINS + "/job/" + LTItemMail.getInstance().getDescription().getName() + "/$build/api/json";
	public static final String UPDATE = JENKINS + "/job/" + LTItemMail.getInstance().getDescription().getName() + "/lastSuccessfulBuild/buildNumber";
	public static final String ARTIFACT = JENKINS + "/job/" + LTItemMail.getInstance().getDescription().getName() + "/lastSuccessfulBuild/artifact/target/" + LTItemMail.getInstance().getDescription().getName() + ".jar";
	public static final String RESOURCE_ARTIFACT = JENKINS + "/job/" + LTItemMail.getInstance().getDescription().getName() + "-ResourcePack/lastSuccessfulBuild/artifact/" + LTItemMail.getInstance().getDescription().getName() + "-ResourcePack.zip";
	private static final String PLUGIN = "https://request.nobookie.net.br/" + LTItemMail.getInstance().getDescription().getName() + "/$version/manifest.yml";
	public static final String STATS = "https://stats.nobookie.net.br";
	public static final String DISCORD = "https://discord.gg/Nvnrv3P";
	public static final Integer STABLE = 100;
	public static final String getDateURL(final Integer build) {
		return DATE.replace("$build", String.valueOf(build));
	}
	public static final String getLogURL(final Integer build) {
		return LOG.replace("$build", String.valueOf(build));
	}
	public static final String getManifestURL(final String version) {
		return PLUGIN.replace("$version", version);
	}
	public enum Version {
		CONFIG_YML(22),
		ITEM_MODELS_YML(1),
		HEADDATABASE_YML(1),
		SKULLS_YML(1),
		ENGLISH_YML(14),
		PORTUGUESE_YML(14),
		VIETNAMESE_YML(14),
		DATABASE(5);
		private final Integer value;
		Version(final Integer value){
			this.value = value;
		}
		public final Integer value() {
			return value;
		}
	}
}
