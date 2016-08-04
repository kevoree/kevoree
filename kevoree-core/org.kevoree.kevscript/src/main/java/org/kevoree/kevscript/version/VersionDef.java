package org.kevoree.kevscript.version;

public class VersionDef {
	public final Boolean isDURelease;
	public final Long version;

	private VersionDef(final Long version, final Boolean isRelease) {
		this.isDURelease = isRelease;
		this.version = version;
	}

	public static VersionDef version(final Long version, boolean isRelease) {
		return new VersionDef(version, isRelease);
	}

	public static VersionDef defaultVersion() {
		return new VersionDef(null, true);
	}

	public static VersionDef latestDu(final boolean isRelease) {
		return new VersionDef(null, isRelease);
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("VersionDef [");
		stringBuilder.append("type def version=");
		stringBuilder.append((version == null ? "latest" : version));
		stringBuilder.append(", ");
		stringBuilder.append("du version=");
		stringBuilder.append((isDURelease ? "release" : "latest"));
		stringBuilder.append("]");
		return stringBuilder.toString();
	}

}
