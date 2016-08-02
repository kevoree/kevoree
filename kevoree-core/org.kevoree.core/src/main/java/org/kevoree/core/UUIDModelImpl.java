package org.kevoree.core;

import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.UUIDModel;

import java.util.UUID;

public class UUIDModelImpl implements UUIDModel {

	private UUID uuid;
	private ContainerRoot model;
	
	public UUIDModelImpl(UUID uuid, ContainerRoot model) {
		this.uuid = uuid;
		this.model = model;
	}
	
	@Override
	public UUID getUUID() {
		return uuid;
	}
	
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}
	
	@Override
	public ContainerRoot getModel() {
		return model;
	}
	
	public void setModel(ContainerRoot model) {
		this.model = model;
	}
}