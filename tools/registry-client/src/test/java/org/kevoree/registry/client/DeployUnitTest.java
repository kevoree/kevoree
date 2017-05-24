package org.kevoree.registry.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.kevoree.registry.client.domain.RDeployUnit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class DeployUnitTest {

	private KevoreeRegistryClient client;

	@Before
	public void setUp() {
		this.client = new KevoreeRegistryClient(TestUtils.BASE_URL);
	}

	@Test
	public void getAllDeployUnits() throws Exception {
		HttpResponse<RDeployUnit[]> res = this.client.getAllDus();
		assertEquals(17, res.getBody().length);
	}

	@Test
	public void getAllDeployUnitsByNamespace() throws Exception {
		HttpResponse<RDeployUnit[]> tdefsRes = this.client.getAllDus("kevoree");
		assertEquals(15, tdefsRes.getBody().length);
	}

	@Test
	public void getAllDeployUnitsByNamespaceAndTdefName() throws Exception {
		HttpResponse<RDeployUnit[]> res = this.client.getAllDus("kevoree", "Ticker");
		assertEquals(9, res.getBody().length);
	}

	@Test
	public void getAllDeployUnitsByNamespaceAndTdefNameAndTdefVersion() throws Exception {
		HttpResponse<RDeployUnit[]> res = this.client.getAllDus("kevoree", "Ticker", 3);
		assertEquals(6, res.getBody().length);
	}

	@Test
	public void getLatestDeployUnitsByNamespaceAndTdefNameAndTdefVersion() throws Exception {
		HttpResponse<RDeployUnit[]> res = this.client.getLatestsDus("kevoree", "Ticker", 3);
		assertEquals(3, res.getBody().length);
	}

	@Test
	public void getReleaseDeployUnitsByNamespaceAndTdefNameAndTdefVersion() throws Exception {
		HttpResponse<RDeployUnit[]> res = this.client.getReleasesDus("kevoree", "Ticker", 3);
		assertEquals(2, res.getBody().length);
	}

	@Test
	public void getSpecificDeployUnitsByNamespaceAndTdefNameAndTdefVersion() throws Exception {
		Map<String, Object> filters = new HashMap<String, Object>() {{
			put("js", "3.1.0-alpha");
			put("dotnet", "latest");
		}};
		HttpResponse<RDeployUnit[]> res = this.client.getSpecificDus("kevoree", "Ticker", 3, filters);

		RDeployUnit[] dus = res.getBody();
		assertEquals(3, dus.length);

		Optional<RDeployUnit> jsDu = Arrays.stream(dus).filter(du -> du.getPlatform().equals("js")).findFirst();
		assertTrue(jsDu.isPresent());
		assertEquals("3.1.0-alpha", jsDu.get().getVersion());

		Optional<RDeployUnit> javaDu = Arrays.stream(dus).filter(du -> du.getPlatform().equals("java")).findFirst();
		assertTrue(javaDu.isPresent());
		assertEquals("3.1.0-alpha", jsDu.get().getVersion());

		Optional<RDeployUnit> dotnetDu = Arrays.stream(dus).filter(du -> du.getPlatform().equals("dotnet")).findFirst();
		assertTrue(dotnetDu.isPresent());
		assertEquals("3.1.0-alpha", jsDu.get().getVersion());
	}

	@Test
	public void createDuWithoutCredentials() throws Exception {
		RDeployUnit du = new RDeployUnit();
		du.setName("atari-ticker");
		du.setVersion("1.2.3-alpha");
		du.setModel("{\"foo\": \"baz\"}");
		du.setPlatform("atari");

		HttpResponse<RDeployUnit> res = this.client.createDu("kevoree", "Ticker", 3, du);
		assertEquals(401, res.getStatus());
	}

	@Test
	public void createDuUpdateItAndDeleteIt() throws Exception {
		RDeployUnit du = new RDeployUnit();
		du.setName("atari-ticker");
		du.setVersion("1.2.3-alpha");
		du.setModel("{\"foo\": \"baz\"}");
		du.setPlatform("atari");

		this.client.setAccessToken(TestUtils.accessToken(this.client, "kevoree", "kevoree"));
		HttpResponse<RDeployUnit> res = this.client.createDu("kevoree", "Ticker", 3, du);
		assertEquals(201, res.getStatus());
		RDeployUnit newDu = res.getBody();

		assertNotNull("an id should be defined on creation", newDu.getId());
		assertEquals("kevoree", newDu.getNamespace());
		assertEquals(du.getName(), newDu.getName());
		assertEquals(du.getVersion(), newDu.getVersion());
		assertEquals(du.getPlatform(), newDu.getPlatform());
		assertEquals("Ticker", newDu.getTdefName());
		assertEquals(Long.valueOf(3), newDu.getTdefVersion());
		assertEquals(du.getModel(), newDu.getModel());

		newDu.setModel("{\"foo\": \"new value\"}");
		HttpResponse<RDeployUnit> updateRes = this.client.updateDu(newDu);
		assertEquals(200, updateRes.getStatus());
		assertEquals(newDu.getModel(), updateRes.getBody().getModel());

		HttpResponse<JsonNode> delRes = this.client.deleteDu("kevoree", "Ticker", 3, "atari-ticker", "1.2.3-alpha", "atari");
		assertEquals(200, delRes.getStatus());
	}
}
