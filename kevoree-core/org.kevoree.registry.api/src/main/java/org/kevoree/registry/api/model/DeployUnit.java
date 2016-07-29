package org.kevoree.registry.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author mleduc
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeployUnit {
	
	private Long id;
	private String model;
	private String name;
	private String platform;
	private String version;
	private TypeDef typeDefinition;

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

	@Override
	public String toString() {
		return "DeployUnit [id="+id+", model=" + model + ", name=" + name + ", platform=" + platform + ", version=" + version
				+ "]";
	}

	public TypeDef getTypeDefinition() {
		return typeDefinition;
	}

	public void setTypeDefinition(TypeDef typeDefinition) {
		this.typeDefinition = typeDefinition;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
}
