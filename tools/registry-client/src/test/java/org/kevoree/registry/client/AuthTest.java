package org.kevoree.registry.client;

import com.mashape.unirest.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.kevoree.registry.client.domain.RAuth;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class AuthTest {

	private KevoreeRegistryClient client;

	@Before
	public void setUp() {
		this.client = new KevoreeRegistryClient(TestUtils.BASE_URL);
	}

	@Test
	public void auth() throws Exception {
		HttpResponse<RAuth> authRes = this.client
				.auth("kevoree", "kevoree", TestUtils.CLIENT_ID, TestUtils.CLIENT_SECRET);
		RAuth auth = authRes.getBody();

		assertNotNull(auth.getAccessToken());
		assertNotNull(auth.getRefreshToken());
		assertThat(auth.getExpiresAt(), lessThan(System.currentTimeMillis()));
		assertThat(auth.getExpiresIn(), greaterThan(3000L));
		// XXX I know those tests are stateful and it sucks but I don't have time right now
		TestUtils.setAccessToken("kevoree", "kevoree", auth.getAccessToken());
	}
}
