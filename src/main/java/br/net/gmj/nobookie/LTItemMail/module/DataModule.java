package br.net.gmj.nobookie.LTItemMail.module;

public final class DataModule {
	private DataModule() {}
	private static final String DATE = "https://jenkins.gmj.net.br/job/LTItemMail/$build/buildTimestamp";
	private static final String LOG = "https://jenkins.gmj.net.br/job/LTItemMail/$build/api/json";
	public static final String UPDATE = "https://jenkins.gmj.net.br/job/LTItemMail/lastSuccessfulBuild/buildNumber";
	public static final String ARTIFACT = "https://jenkins.gmj.net.br/job/LTItemMail/lastSuccessfulBuild/artifact/target/LTItemMail.jar";
	private static final String PLUGIN = "https://request.gmj.net.br/LTItemMail/$version/manifest.yml";
	public static final String RESOURCE_ARTIFACT = "https://jenkins.gmj.net.br/job/LTItemMail-ResourcePack/lastSuccessfulBuild/artifact/LTItemMail-ResourcePack.zip";
	public static final Integer STABLE = 99;
	public static final String DISCORD = "https://discord.gg/Nvnrv3P";
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
		CONFIG_YML(20),
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
	public enum ProjectType {
		BUKKIT_DEV,
		SPIGOT_MC
	}
}
