package org.kevoree.kevscript.util.version;

import java.util.List;

import org.kevoree.DeployUnit;
import org.kevoree.DictionaryType;
import org.kevoree.NamedElement;
import org.kevoree.TypeDefinition;
import org.kevoree.Value;
import org.kevoree.pmodeling.api.KMFContainer;
import org.kevoree.pmodeling.api.events.ModelElementListener;
import org.kevoree.pmodeling.api.trace.ModelTrace;
import org.kevoree.pmodeling.api.util.ActionType;
import org.kevoree.pmodeling.api.util.ModelAttributeVisitor;
import org.kevoree.pmodeling.api.util.ModelVisitor;

final class TypeDefinitionVersionMock implements TypeDefinition {
	private String version;

	public TypeDefinitionVersionMock(String version) {
		this.version = version;
	}

	@Override
	public NamedElement withName(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void visitReferences(ModelVisitor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitNotContained(ModelVisitor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitContained(ModelVisitor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitAttributes(ModelAttributeVisitor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ModelVisitor arg0, boolean arg1, boolean arg2, boolean arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ModelTrace> toTraces(boolean arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRecursiveReadOnly() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInternalReadOnly() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<KMFContainer> select(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeModelTreeListener(ModelElementListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeModelElementListener(ModelElementListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAllModelTreeListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAllModelElementListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reflexiveMutator(ActionType arg0, String arg1, Object arg2, boolean arg3, boolean arg4) {
		// TODO Auto-generated method stub

	}

	@Override
	public String path() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean modelEquals(KMFContainer arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String metaClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRoot() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRecursiveReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String internalGetKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRefInParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KMFContainer findByPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KMFContainer findByID(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KMFContainer eContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deepVisitReferences(ModelVisitor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deepVisitNotContained(ModelVisitor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deepVisitContained(ModelVisitor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean deepModelEquals(KMFContainer arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<ModelTrace> createTraces(KMFContainer arg0, boolean arg1, boolean arg2, boolean arg3,
			boolean arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addModelTreeListener(ModelElementListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addModelElementListener(ModelElementListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public TypeDefinition withVersion(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition withDictionaryType(DictionaryType arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition withAbstract(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVersion(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSuperTypes(List<? extends TypeDefinition> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMetaData(List<? extends Value> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDictionaryType(DictionaryType arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDeployUnits(List<? extends DeployUnit> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAbstract(Boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public TypeDefinition removeSuperTypes(TypeDefinition arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition removeMetaData(Value arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition removeDeployUnits(DeployUnit arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition removeAllSuperTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition removeAllMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition removeAllDeployUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		return this.version;
	}

	@Override
	public List<TypeDefinition> getSuperTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Value> getMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DictionaryType getDictionaryType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DeployUnit> getDeployUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean getAbstract() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition findSuperTypesByNameVersion(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition findSuperTypesByID(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value findMetaDataByID(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeployUnit findDeployUnitsByID(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeployUnit findDeployUnitsByHashcodeNameVersion(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition addSuperTypes(TypeDefinition arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition addMetaData(Value arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition addDeployUnits(DeployUnit arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition addAllSuperTypes(List<? extends TypeDefinition> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition addAllMetaData(List<? extends Value> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition addAllDeployUnits(List<? extends DeployUnit> arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}