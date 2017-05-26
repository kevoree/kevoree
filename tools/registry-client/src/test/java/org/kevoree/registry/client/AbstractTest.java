package org.kevoree.registry.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 *
 * Created by leiko on 5/24/17.
 */
public abstract class AbstractTest {

    static final String CLIENT_ID = "kevoree_registryapp";
    static final String CLIENT_SECRET = "kevoree_registryapp_secret";

    private static int PORT = 8899;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private WireMockConfiguration conf = options().port(PORT);
    private ObjectMapper oMapper = new ObjectMapper();

    protected KevoreeRegistryClient client;
    private Map<String, String> accessTokens = new HashMap<>();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(conf, false);

    @Before
    public void setUp() {
        this.client = new KevoreeRegistryClient(BASE_URL);
    }

    @After
    public void tearDown() {
        this.client.setAccessToken(null);
    }

    protected String accessToken(KevoreeRegistryClient client, String username, String password)
            throws UnirestException {
        final String key = username + ":" + password;
        if (!accessTokens.containsKey(key)) {
            accessTokens.put(key, client.auth(username, password, CLIENT_ID, CLIENT_SECRET).getBody().getAccessToken());
        }
        return accessTokens.get(key);
    }

    protected void setAccessToken(String username, String password, String accessToken) {
        accessTokens.put(username + ":" + password, accessToken);
    }

    protected String toJson(Object o) throws JsonProcessingException {
        return oMapper.writeValueAsString(o);
    }
}
