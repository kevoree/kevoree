package org.kevoree.registry.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.junit.Ignore;
import org.junit.Test;
import org.kevoree.registry.client.domain.RDeployUnit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

public class DeployUnitTest extends AbstractTest {

	@Test
	public void getAllDeployUnits() throws Exception {
		HttpResponse<RDeployUnit[]> res = this.client.getAllDus();
		assertEquals(20, res.getBody().length);
	}

	@Test
	public void getAllDeployUnitsByNamespace() throws Exception {
		HttpResponse<RDeployUnit[]> tdefsRes = this.client.getAllDus("kevoree");
		assertEquals(55, tdefsRes.getBody().length);
	}

	@Test
	public void getAllDeployUnitsByNamespaceAndTdefName() throws Exception {
		HttpResponse<RDeployUnit[]> res = this.client.getAllDus("kevoree", "Ticker");
		assertEquals(3, res.getBody().length);
	}

	@Test
	public void getAllDeployUnitsByNamespaceAndTdefNameAndTdefVersion() throws Exception {
		HttpResponse<RDeployUnit[]> res = this.client.getAllDus("kevoree", "Ticker", 1);
		assertEquals(3, res.getBody().length);
	}

	@Test
	public void getLatestDeployUnitsByNamespaceAndTdefNameAndTdefVersion() throws Exception {
		HttpResponse<RDeployUnit[]> res = this.client.getLatestsDus("kevoree", "Ticker", 1);
		assertEquals(2, res.getBody().length);
	}

	@Test
	public void getReleaseDeployUnitsByNamespaceAndTdefNameAndTdefVersion() throws Exception {
		HttpResponse<RDeployUnit[]> res = this.client.getReleasesDus("kevoree", "Ticker", 1);
		assertEquals(0, res.getBody().length);
	}

	@Test
	public void getSpecificDeployUnitsByNamespaceAndTdefNameAndTdefVersion() throws Exception {
		Map<String, Object> filters = new HashMap<String, Object>() {{
			put("js", "5.3.3-beta.3");
		}};
		HttpResponse<RDeployUnit[]> res = this.client.getSpecificDus("kevoree", "Ticker", 1, filters);

		RDeployUnit[] dus = res.getBody();
		assertEquals(1, dus.length);

		Optional<RDeployUnit> jsDu = Arrays.stream(dus).filter(du -> du.getPlatform().equals("js")).findFirst();
		assertTrue(jsDu.isPresent());
		assertEquals("5.3.3-beta.3", jsDu.get().getVersion());
	}

	@Test
	@Ignore
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
	@Ignore
	public void createDuUpdateItAndDeleteIt() throws Exception {
		RDeployUnit du = new RDeployUnit();
		du.setName("atari-ticker");
		du.setVersion("1.2.3-alpha");
		du.setModel("{\"foo\": \"baz\"}");
		du.setPlatform("atari");

		stubFor(post(urlEqualTo("/api/namespaces/kevoree/tdefs/Ticker/1/dus"))
				.withHeader("Content-Type", equalTo("application/json"))
				.withHeader("Authorization", equalTo("Bearer 123"))
				.withRequestBody(equalToJson(toJson(du)))
				.willReturn(aResponse()
						.withStatus(201)
						.withBodyFile("dus/atari-ticker_1.2.3-alpha_atari.json")));

		this.client.setAccessToken(accessToken(this.client, "kevoree", "kevoree"));
		HttpResponse<RDeployUnit> res = this.client.createDu("kevoree", "Ticker", 1, du);
		assertEquals(201, res.getStatus());
		RDeployUnit newDu = res.getBody();

		assertNotNull("an id should be defined on creation", newDu.getId());
		assertEquals("kevoree", newDu.getNamespace());
		assertEquals(du.getName(), newDu.getName());
		assertEquals(du.getVersion(), newDu.getVersion());
		assertEquals(du.getPlatform(), newDu.getPlatform());
		assertEquals("Ticker", newDu.getTdefName());
		assertEquals(Long.valueOf(1), newDu.getTdefVersion());
		assertEquals(du.getModel(), newDu.getModel());

		// update deployUnit to test PUT request
		newDu.setModel("{\"foo\": \"new value\"}");
		stubFor(put(urlEqualTo("/api/namespaces/kevoree/tdefs/Ticker/1/dus/atari-ticker/1.2.3-alpha/atari"))
				.withHeader("Content-Type", equalTo("application/json"))
				.withHeader("Authorization", equalTo("Bearer 123"))
				.withRequestBody(equalToJson(toJson(newDu)))
				.willReturn(aResponse()
						.withStatus(201)
						.withBodyFile("dus/atari-ticker_1.2.3-alpha_atari-updated.json")));

		HttpResponse<RDeployUnit> updateRes = this.client.updateDu(newDu);
		assertEquals(200, updateRes.getStatus());
		assertEquals(newDu.getModel(), updateRes.getBody().getModel());

		HttpResponse<JsonNode> delRes = this.client.deleteDu("kevoree", "Ticker", 3, "atari-ticker", "1.2.3-alpha", "atari");
		assertEquals(200, delRes.getStatus());
	}
}
