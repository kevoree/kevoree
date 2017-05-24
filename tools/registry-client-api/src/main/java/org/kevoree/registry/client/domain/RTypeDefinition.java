package org.kevoree.registry.client.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mleduc
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RTypeDefinition {
	
	private Long id;
	private String name;
	private Long version;
	private String model;
	private String namespace;
	private List<Long> deployUnits = new ArrayList<>();

	public List<Long> getDeployUnits() {
		return deployUnits;
	}

	public String getModel() {
		return model;
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	public Long getVersion() {
		return version;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "RTypeDefinition [id=" + id + ", deployUnits=" + deployUnits + ", model=" + model + ", name=" + name + ", namespace="
				+ namespace + ", version=" + version + "]";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
}
