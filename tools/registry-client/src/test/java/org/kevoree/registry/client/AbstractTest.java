package org.kevoree.registry.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.kevoree.tools.KevoreeConfig;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 *
 * Created by leiko on 5/24/17.
 */
public abstract class AbstractTest {

    private static final int PORT = 3000;

    private WireMockConfiguration conf = options().port(PORT);
    private ObjectMapper oMapper = new ObjectMapper();

    protected KevoreeRegistryClient client;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(conf, false);

    @Before
    public void setUp() {
        KevoreeConfig config = new KevoreeConfig.Builder().useDefault().build();
        config.set("registry.host", "localhost");
        config.set("registry.port", PORT);
        config.set("registry.ssl", false);
        config.set("user.access_token", "my_token");
        config.set("user.refresh_token", "my_refresh_token");
        config.set("user.expires_at", System.currentTimeMillis() + 1000 * 60 * 5); // expires in 5 minutes
        this.client = new KevoreeRegistryClient(config);
    }

    protected String toJson(Object o) throws JsonProcessingException {
        return oMapper.writeValueAsString(o);
    }
}
