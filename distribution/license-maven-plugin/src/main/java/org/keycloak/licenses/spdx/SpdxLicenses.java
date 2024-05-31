package org.keycloak.licenses.spdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.licenses.config.Config;
import org.keycloak.licenses.config.LicenseMapping;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class SpdxLicenses {

    private static final String SPDX_LICENSES_URL = "https://raw.githubusercontent.com/spdx/license-list-data/main/json/licenses.json";

    private final Licenses licenses;
    private final List<LicenseMapping> licenseMappings;
    private final List<String> excludedDependencies;

    public SpdxLicenses(File configFile) {
        ObjectMapper om = new ObjectMapper();
        try {
            licenses = om.readValue(new URL(SPDX_LICENSES_URL), Licenses.class);

            Config config = om.readValue(configFile, Config.class);
            licenses.getLicenses().addAll(config.getAdditionalLicenses());

            for (String cncfApprovedLicense : config.getCncfApproved()) {
                License l = findByLicenseId(cncfApprovedLicense);
                if (l != null) {
                    l.setCncfApproved(true);
                }
            }

            licenseMappings = config.getMappings();

            excludedDependencies = config.getExcludedDependencies().stream().map(r -> r.replace(".", "\\.").replace("*", ".*")).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public License findByNameOrLicenseId(String name) {
        License license = licenses.getLicenses().stream()
                .filter(l -> l.getName().equals(name) || l.getLicenseId().equals(name)).findFirst().orElse(null);
        if (license != null) {
            return license;
        }

        LicenseMapping licenseMapping = licenseMappings.stream().filter(m -> m.getNameMappings() != null && m.getNameMappings().contains(name)).findFirst().orElse(null);
        if (licenseMapping != null) {
            return findByLicenseId(licenseMapping.getLicenseId());
        } else {
            return null;
        }
    }

    public License findByLicenseId(String licenseId) {
        return licenses.getLicenses().stream().filter(l -> l.getLicenseId().equals(licenseId)).findFirst().orElse(null);
    }

    public License findByUrl(String url) {
        License license = licenses.getLicenses().stream().filter(l ->
                l.getReference().equals(url) || (l.getSeeAlso() != null && l.getSeeAlso().contains(url))).findFirst().orElse(null);
        if (license != null) {
            return license;
        }

        LicenseMapping licenseMapping = licenseMappings.stream().filter(m -> m.getUrlMappings() != null && m.getUrlMappings().contains(url)).findFirst().orElse(null);
        if (licenseMapping != null) {
            return findByLicenseId(licenseMapping.getLicenseId());
        } else {
            return licenses.getLicenses().stream().filter(l ->
                    l.getReference().equals(url) || (l.getSeeAlso() != null && l.getSeeAlso().contains(url))).findFirst().orElse(null);
        }
    }

    public List<License> findByDependency(String dependency) {
        List<LicenseMapping> licenseMapping = licenseMappings.stream().filter(m -> m.getDependencyMappings() != null && m.getDependencyMappings().contains(dependency)).toList();
        if (licenseMapping.isEmpty()) {
            return null;
        } else {
            return licenseMapping.stream().map(m -> findByLicenseId(m.getLicenseId())).toList();
        }
    }

    public boolean isExcluced(String dependency) {
        Optional<String> m = excludedDependencies.stream().filter(dependency::matches).findFirst();
        return m.isPresent();
    }

    public String getSourceUrl(String dependency) {
        String[] split = dependency.split(":");
        if (split.length == 3) {
            return "https://repo1.maven.org/maven2/" + split[0].replace(".", "/") + "/" + split[1] + "/" + split[2] + "/" + split[1] + "-" + split[2] + "-sources.jar";
        } else {
            return "https://www.npmjs.com/package/" + split[0] + "/v/" + split[1] + "?activeTab=code";
        }
    }

}
