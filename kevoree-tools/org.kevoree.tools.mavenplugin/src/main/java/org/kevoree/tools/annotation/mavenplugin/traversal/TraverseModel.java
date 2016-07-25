package org.kevoree.tools.annotation.mavenplugin.traversal;

import org.apache.maven.plugin.MojoFailureException;
import org.json.JSONException;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.Package;
import org.kevoree.TypeDefinition;

import com.mashape.unirest.http.exceptions.UnirestException;

public abstract class TraverseModel {

	public final void recPackages(final ContainerRoot model) throws UnirestException, JSONException, MojoFailureException {
		for (final Package p : model.getPackages()) {
			innerLoop(p);
		}

	}

	private final void innerLoop(final Package p) throws UnirestException, JSONException, MojoFailureException {
		for (final TypeDefinition typeDefinition : p.getTypeDefinitions()) {
			visitTypeDefinition(typeDefinition);
			for (final DeployUnit du : typeDefinition.getDeployUnits()) {
				visitDeployUnit(du, typeDefinition.getName(), typeDefinition.getVersion());
			}

		}
		recPackages(p);
	}

	public abstract void handlerTypeDefError(TypeDefinitionException e);

	private final void recPackages(final Package currentPackage) throws UnirestException, JSONException, MojoFailureException {
		for (final Package p : currentPackage.getPackages()) {
			innerLoop(p);
		}

	}

	public abstract void visitDeployUnit(DeployUnit du, String name, String version) throws UnirestException;

	public abstract void visitTypeDefinition(TypeDefinition typeDefinition) throws JSONException, UnirestException, MojoFailureException;

}
