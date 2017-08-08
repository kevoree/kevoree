package org.kevoree.registry.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.typesafe.config.Config;
import org.kevoree.tools.KevoreeConfig;

import java.io.IOException;

/**
 *
 * Created by leiko on 5/23/17.
 */
public class KevoreeRegistryClient implements AuthRegistryClient, AccountRegistryClient, TypeDefRegistryClient,
                                              DeployUnitRegistryClient {

    private KevoreeConfig config;

    public KevoreeRegistryClient(KevoreeConfig config) {
        this.config = config;
    }

    static {
        // serializing JSON from/to Object using Jackson ObjectMapper
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return value.isEmpty() ? null : jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return value == null ? null : jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public KevoreeConfig config() {
        return config;
    }
}
