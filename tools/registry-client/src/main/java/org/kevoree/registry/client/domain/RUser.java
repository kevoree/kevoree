package org.kevoree.registry.client.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 *
 * Created by leiko on 8/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RUser {

    private String login;
    private List<String> namespaces;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
    }
}
