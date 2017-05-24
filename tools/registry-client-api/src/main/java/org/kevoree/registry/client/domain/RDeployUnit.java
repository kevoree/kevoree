package org.kevoree.registry.client.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author mleduc
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RDeployUnit {
	
	private Long id;
	private String model;
	private String name;
	private String platform;
	private String version;
	private Long tdefId;
	private String tdefName;
	private Long tdefVersion;
	private String namespace;

	public Long getTdefId() {
		return tdefId;
	}

	public void setTdefId(Long tdefId) {
		this.tdefId = tdefId;
	}

	public String getTdefName() {
		return tdefName;
	}

	public void setTdefName(String tdefName) {
		this.tdefName = tdefName;
	}

	public Long getTdefVersion() {
		return tdefVersion;
	}

	public void setTdefVersion(Long tdefVersion) {
		this.tdefVersion = tdefVersion;
	}

	public String getModel() {
		return model;
	}

	public String getName() {
		return name;
	}

	public String getPlatform() {
		return platform;
	}

	public String getVersion() {
		return version;
	}

	public void setModel(final String model) {
		this.model = model;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setPlatform(final String platform) {
		this.platform = platform;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public String toString() {
		return "RDeployUnit [" +
				"id="+id+"" +
				", namespace=" + namespace +
				", tdefId="+ tdefId +
				", tdefName="+ tdefName +
				", tdefVersion="+ tdefVersion +
				", name=" + name +
				", platform=" + platform +
				", version=" + version +
				", model=" + model +
				"]";
	}
}
