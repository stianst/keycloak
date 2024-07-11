package org.keycloak.test.provider;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.common.util.QuarkusMultivaluedHashMap;
import org.keycloak.http.FormPartValue;
import org.keycloak.http.HttpRequest;

import java.security.cert.X509Certificate;

public class DummyHttpRequest implements HttpRequest {
    @Override
    public String getHttpMethod() {
        return "GET";
    }

    @Override
    public MultivaluedMap<String, String> getDecodedFormParameters() {
        return new QuarkusMultivaluedHashMap<>();
    }

    @Override
    public MultivaluedMap<String, FormPartValue> getMultiPartFormParameters() {
        return new QuarkusMultivaluedHashMap<>();
    }

    @Override
    public HttpHeaders getHttpHeaders() {
        return new DummyHttpHeaders();
    }

    @Override
    public X509Certificate[] getClientCertificateChain() {
        return new X509Certificate[0];
    }

    @Override
    public UriInfo getUri() {
        return null;
    }
}
