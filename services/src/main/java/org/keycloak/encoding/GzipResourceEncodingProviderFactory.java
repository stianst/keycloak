package org.keycloak.encoding;

import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.common.Version;
import org.keycloak.models.KeycloakSession;
import org.keycloak.platform.Platform;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GzipResourceEncodingProviderFactory implements ResourceEncodingProviderFactory {

    private static final Logger logger = Logger.getLogger(GzipResourceEncodingProviderFactory.class);

    private Set<String> excludedContentTypes = new HashSet<>();

    private File cacheDir;

    @Override
    public ResourceEncodingProvider create(KeycloakSession session) {
        return new GzipResourceEncodingProvider(session, cacheDir);
    }

    @Override
    public void init(Config.Scope config) {
        String e = config.get("excludedContentTypes", "image/png image/jpeg");
        for (String s : e.split(" ")) {
            excludedContentTypes.add(s);
        }
        cacheDir = initCacheDir();
    }

    @Override
    public boolean encodeContentType(String contentType) {
        return !excludedContentTypes.contains(contentType);
    }

    @Override
    public String getId() {
        return "gzip";
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("excludedContentTypes")
                .type("string")
                .helpText("A space separated list of content-types to exclude from encoding.")
                .defaultValue("image/png image/jpeg")
                .add()
                .build();
    }

    private File initCacheDir() {
        File cacheDir = new File(Platform.getPlatform().getTmpDirectory(), "kc-gzip-cache");
        try {
            FileUtils.deleteDirectory(cacheDir);
        } catch (IOException e) {
            logger.warn("Failed to delete old gzip cache directory", e);
        }

        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            logger.warn("Failed to create gzip cache directory");
            return null;
        }
        return cacheDir;
    }
}
