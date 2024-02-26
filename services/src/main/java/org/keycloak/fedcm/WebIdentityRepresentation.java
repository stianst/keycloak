package org.keycloak.fedcm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebIdentityRepresentation {

    @JsonProperty("provider_urls")
    private String[] providerUrls;

    public String[] getProviderUrls() {
        return providerUrls;
    }

    public void setProviderUrls(String[] providerUrls) {
        this.providerUrls = providerUrls;
    }
}
