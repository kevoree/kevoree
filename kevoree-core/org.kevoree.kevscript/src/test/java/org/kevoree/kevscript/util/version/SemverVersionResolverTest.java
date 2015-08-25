package org.kevoree.kevscript.util.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.junit.Test;
import org.kevoree.TypeDefinition;
import org.kevoree.kevscript.version.IVersionResolver;
import org.kevoree.kevscript.version.SemverVersionResolver;

public class SemverVersionResolverTest {

	private IVersionResolver resolver = new SemverVersionResolver();

	@Test
	public void testFindBestVersion() {
		/*
		 * We ask for one version, only one is available, it is the one we ask
		 * for
		 */
		String typeDefName = "test";
		String version = "1.0.0";
		ArrayList<TypeDefinition> selected = new ArrayList<TypeDefinition>();
		selected.add(new TypeDefinitionVersionMock("1.0.0"));
		TypeDefinition result = resolver.findBestVersion(typeDefName, version, selected);
		assertEquals("1.0.0", result.getVersion());
	}

	@Test
	public void testFindBestVersion2() {
		/*
		 * We ask for one version, only one is available, it is not the one we
		 * ask for
		 */
		String typeDefName = "test";
		String version = "2.0.0";
		ArrayList<TypeDefinition> selected = new ArrayList<TypeDefinition>();
		selected.add(new TypeDefinitionVersionMock("1.0.0"));
		TypeDefinition result = resolver.findBestVersion(typeDefName, version, selected);
		assertNull(result);
	}

	@Test
	public void testFindBestVersion3() {
		String typeDefName = "test";
		String version = null;
		ArrayList<TypeDefinition> selected = new ArrayList<TypeDefinition>();
		selected.add(new TypeDefinitionVersionMock("1.0.0"));
		selected.add(new TypeDefinitionVersionMock("4.0.0"));
		selected.add(new TypeDefinitionVersionMock("2.0.5-snapshot"));
		TypeDefinition result = resolver.findBestVersion(typeDefName, version, selected);
		assertEquals("4.0.0", result.getVersion());
	}

	@Test
	public void testFindBestVersion4() {
		String typeDefName = "test";
		String version = null;
		ArrayList<TypeDefinition> selected = new ArrayList<TypeDefinition>();
		selected.add(new TypeDefinitionVersionMock("1.0.0"));
		selected.add(new TypeDefinitionVersionMock("4.0.0"));
		selected.add(new TypeDefinitionVersionMock("5.0.5-snapshot"));
		TypeDefinition result = resolver.findBestVersion(typeDefName, version, selected);
		assertEquals("4.0.0", result.getVersion());
	}

	@Test
	public void testFindBestVersion5() {
		String typeDefName = "test";
		String version = null;
		ArrayList<TypeDefinition> selected = new ArrayList<TypeDefinition>();
		selected.add(new TypeDefinitionVersionMock("5.0.5-snapshot"));
		selected.add(new TypeDefinitionVersionMock("4.0.0-snapshot"));
		TypeDefinition result = resolver.findBestVersion(typeDefName, version, selected);
		assertEquals("5.0.5-snapshot", result.getVersion());
	}

	@Test
	public void testFindBestVersion6() {
		String typeDefName = "test";
		String version = "4.0.0-snapshot";
		ArrayList<TypeDefinition> selected = new ArrayList<TypeDefinition>();
		selected.add(new TypeDefinitionVersionMock("5.0.5-snapshot"));
		selected.add(new TypeDefinitionVersionMock("4.0.0-snapshot"));
		TypeDefinition result = resolver.findBestVersion(typeDefName, version, selected);
		assertEquals("4.0.0-snapshot", result.getVersion());
	}

	@Test
	public void testFindBestVersion7() {
		String typeDefName = "test";
		ArrayList<TypeDefinition> selected = new ArrayList<TypeDefinition>();
		TypeDefinition result = resolver.findBestVersion(typeDefName, null, selected);
		assertNull(result);
	}

}
