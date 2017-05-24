package org.kevoree.registry.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;

import java.io.IOException;

/**
 *
 * Created by leiko on 5/23/17.
 */
public class KevoreeRegistryClient implements AuthRegistryClient, TypeDefRegistryClient, DeployUnitRegistryClient {

    private String baseUrl;
    private String accessToken;

    public KevoreeRegistryClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String baseUrl() {
        return this.baseUrl;
    }

    @Override
    public String accessToken() {
        return this.accessToken;
    }

    static {
        // serializing JSON from/to Object using Jackson ObjectMapper
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
