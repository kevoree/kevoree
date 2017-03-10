package org.kevoree.registry.api;

import org.junit.Ignore;
import org.junit.Test;

public class RegistryRestClientTest {

	@Test
	@Ignore
	public void test() throws Exception {
		RegistryRestClient registryRestClient = new RegistryRestClient("https://registry.kevoree.org", null);
		System.out.println(registryRestClient.getLatestTypeDef("kevoree", "JavaNode"));
	}
}
