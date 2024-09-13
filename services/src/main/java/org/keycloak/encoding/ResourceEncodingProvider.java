package org.keycloak.encoding;

import org.keycloak.provider.Provider;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceEncodingProvider extends Provider {

    InputStream getEncodedStream(InputStream unencodedStream, String... path);

    String getEncoding();

    @Override
    default void close() {
    }

}
