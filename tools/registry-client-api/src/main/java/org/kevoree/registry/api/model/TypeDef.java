package org.kevoree.registry.api.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author mleduc
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TypeDef {
	
	private Long id;
	private String name;
	private String version;
	private String model;
	private Namespace namespace;
	private List<DeployUnit> deployUnits = new ArrayList<>();

	public List<DeployUnit> getDeployUnits() {
		return deployUnits;
	}

	public String getModel() {
		return model;
	}

	public String getName() {
		return name;
	}

	public Namespace getNamespace() {
		return namespace;
	}

	public String getVersion() {
		return version;
	}

	public void setDeployUnits(List<DeployUnit> deployUnits) {
		this.deployUnits = deployUnits;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNamespace(Namespace namespace) {
		this.namespace = namespace;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "TypeDef [id=" + id + ", deployUnits=" + deployUnits + ", model=" + model + ", name=" + name + ", namespace="
				+ namespace + ", version=" + version + "]";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
}
