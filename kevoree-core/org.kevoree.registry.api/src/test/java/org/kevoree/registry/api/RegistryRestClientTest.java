package org.kevoree.registry.api;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.kevoree.registry.api.RegistryRestClient;
import org.kevoree.registry.api.model.TypeDef;

public class RegistryRestClientTest {
	@Test
	@Ignore
	public void test() throws Exception {
		RegistryRestClient registryRestClient = new RegistryRestClient("http://localhost:8080", null);
		Set<TypeDef> res = registryRestClient.getTypeDefs(null, "JavaNode", null);
		System.out.println(res);
	}
}
