package org.kevoree.registry.client;

import com.mashape.unirest.http.HttpResponse;
import org.junit.Test;
import org.kevoree.registry.client.domain.RAuth;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class AuthTest extends AbstractTest {

	@Test
	public void auth() throws Exception {
		HttpResponse<RAuth> authRes = this.client.auth("kevoree", "kevoree");
		RAuth auth = authRes.getBody();

		assertNotNull(auth.getAccessToken());
		assertNotNull(auth.getRefreshToken());
		assertThat(auth.getExpiresAt(), lessThan(System.currentTimeMillis()));
		assertThat(auth.getExpiresIn(), greaterThan(3000L));
	}
}
