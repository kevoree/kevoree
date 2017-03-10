package org.kevoree.registry.api;

import org.junit.Ignore;
import org.junit.Test;

public class OAuthRegistryClientTest {
	@Test
	@Ignore
	public void test() throws Exception {
		final OAuthRegistryClient oAuthRegistryClient = new OAuthRegistryClient("http://localhost:8080");
		final String token = oAuthRegistryClient.getToken("admin", "admin");
		System.out.println(token);
	}
}
