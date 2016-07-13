package org.kevoree.tools.annotation.mavenplugin.traversal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.Package;
import org.kevoree.TypeDefinition;

import com.mashape.unirest.http.exceptions.UnirestException;

public abstract class TraverseModel {

	public final void recPackages(final ContainerRoot model) throws UnirestException {
		for (final Package p : model.getPackages()) {
			innerLoop(new ArrayList<String>(), p);
		}

	}

	private final void innerLoop(final List<String> packages, final Package p) throws UnirestException {
		final List<String> npackages = new ArrayList<>(packages);
		npackages.add(p.getName());
		for (final TypeDefinition typeDefinition : p.getTypeDefinitions()) {
			this.visitPackage(npackages);
			final String namespace = StringUtils.join(npackages, '.');
			visitTypeDefinition(namespace, typeDefinition);
			for (final DeployUnit du : typeDefinition.getDeployUnits()) {
				visitDeployUnit(namespace, du, typeDefinition.getName(), typeDefinition.getVersion());
			}
			
		}
		recPackages(p, npackages);
	}

	public abstract void handlerTypeDefError(TypeDefinitionException e);

	private final void recPackages(final Package currentPackage, final List<String> packages) throws UnirestException {
		for (final Package p : currentPackage.getPackages()) {
			innerLoop(packages, p);
		}

	}

	public abstract void visitDeployUnit(String namespace, DeployUnit du, String name, String version)
			throws UnirestException;

	public abstract void visitTypeDefinition(String namespace, TypeDefinition typeDefinition)
			throws JSONException, UnirestException;

	public abstract void visitPackage(List<String> npackages) throws UnirestException;

}
