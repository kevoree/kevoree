package org.kevoree.registry.client;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.registry.client.domain.RUser;

public class AccountTest extends AbstractTest {

	@Test
	public void account() throws Exception {
		RUser user = this.client.getAccount();
		Assert.assertEquals("john", user.getLogin());
	}
}
