package org.keycloak.quarkus.runtime.benchmark.tasks;

import jakarta.ws.rs.core.MediaType;
import org.keycloak.common.util.Base64Url;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.keys.DefaultKeyManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.nio.charset.StandardCharsets;

public abstract class AbstractSign implements BenchmarkTask {

    private DefaultKeyManager defaultKeyManager;
    private KeyWrapper key;
    private KeycloakSession session;

    private static final byte[] message = "Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world! Hello world!".getBytes(StandardCharsets.UTF_8);

    @Override
    public void init(KeycloakSession session) {
        this.session = session;

        RealmModel realm = session.getContext().getRealm();
        defaultKeyManager = new DefaultKeyManager(session);
        key = defaultKeyManager.getActiveKey(realm, KeyUse.SIG, getAlgorithm());
    }

    @Override
    public Object run() {
        SignatureProvider sign = session.getProvider(SignatureProvider.class, getAlgorithm());
        SignatureSignerContext signCtx = sign.signer(key);
        byte[] signed = signCtx.sign(message);
        return Base64Url.encode(signed);
    }

    @Override
    public MediaType mediaType() {
        return MediaType.TEXT_PLAIN_TYPE;
    }

    public abstract String getAlgorithm();

}
