package org.kevoree.registry.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.kevoree.registry.client.domain.RTypeDefinition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TypeDefTest {

	private KevoreeRegistryClient client;

	@Before
	public void setUp() {
		this.client = new KevoreeRegistryClient(TestUtils.BASE_URL);
	}

	@Test
	public void getAllTypeDefs() throws Exception {
		HttpResponse<RTypeDefinition[]> tdefsRes = this.client.getAllTdefs();
		assertEquals(8, tdefsRes.getBody().length);
	}

	@Test
	public void getAllTypeDefsByNamespace() throws Exception {
		HttpResponse<RTypeDefinition[]> tdefsRes = this.client.getAllTdefs("kevoree");
		assertEquals(7, tdefsRes.getBody().length);
	}

	@Test
	public void getAllTypeDefsByNamespaceAndName() throws Exception {
		HttpResponse<RTypeDefinition[]> tdefsRes = this.client.getAllTdefs("kevoree", "Ticker");
		assertEquals(3, tdefsRes.getBody().length);
	}

	@Test
	public void getTypeDefByNamespaceAndNameAndVersion() throws Exception {
		HttpResponse<RTypeDefinition> tdefRes = this.client.getTdef("kevoree", "Ticker", 3);
		RTypeDefinition tdef = tdefRes.getBody();
		assertNotNull(tdef.getId());
		assertEquals("Ticker", tdef.getName());
		assertEquals(Long.valueOf(3), tdef.getVersion());
		assertEquals("kevoree", tdef.getNamespace());
	}

	@Test
	public void getUnknownTypeDefByNamespaceAndNameAndVersion() throws Exception {
		HttpResponse<RTypeDefinition> tdefRes = this.client.getTdef("kevoree", "Unknown", 3);
		assertEquals(404, tdefRes.getStatus());
	}

	@Test
	public void getLatestTypeDefByNamespaceAndName() throws Exception {
		HttpResponse<RTypeDefinition> tdefRes = this.client.getLatestTdef("kevoree", "Ticker");
		RTypeDefinition tdef = tdefRes.getBody();
		assertNotNull(tdef.getId());
		assertEquals("Ticker", tdef.getName());
		assertEquals(Long.valueOf(3), tdef.getVersion());
		assertEquals("kevoree", tdef.getNamespace());
	}

	@Test
	public void createTdefWithoutCredentials() throws Exception {
		RTypeDefinition tdef = new RTypeDefinition();
		tdef.setName("Foo");
		tdef.setVersion(1L);
		tdef.setModel("{\"foo\": \"bar\"}");

		HttpResponse<RTypeDefinition> res = this.client.createTdef("kevoree", tdef);
		assertEquals(401, res.getStatus());
	}

	@Test
	public void createTdefAndDeleteIt() throws Exception {
		RTypeDefinition tdef = new RTypeDefinition();
		tdef.setName("Foo");
		tdef.setVersion(1L);
		tdef.setModel("{\"foo\": \"bar\"}");

		this.client.setAccessToken(TestUtils.accessToken(this.client, "kevoree", "kevoree"));
		HttpResponse<RTypeDefinition> res = this.client.createTdef("kevoree", tdef);
		assertEquals(201, res.getStatus());
		RTypeDefinition newTdef = res.getBody();
		assertNotNull("an id should be defined on creation", newTdef.getId());
		assertEquals("kevoree", newTdef.getNamespace());
		assertEquals(tdef.getName(), newTdef.getName());
		assertEquals(tdef.getVersion(), newTdef.getVersion());
		assertEquals(tdef.getModel(), newTdef.getModel());

		HttpResponse<JsonNode> delRes = this.client.deleteTdef("kevoree", "Foo", 1);
		assertEquals(200, delRes.getStatus());
	}
}
