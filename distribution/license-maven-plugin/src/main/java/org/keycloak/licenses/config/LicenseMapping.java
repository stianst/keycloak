package org.keycloak.licenses.config;

import java.util.Set;

public class LicenseMapping {

    private String licenseId;

    private Set<String> nameMappings;
    private Set<String> urlMappings;
    private Set<String> dependencyMappings;

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }

    public Set<String> getNameMappings() {
        return nameMappings;
    }

    public void setNameMappings(Set<String> nameMappings) {
        this.nameMappings = nameMappings;
    }

    public Set<String> getUrlMappings() {
        return urlMappings;
    }

    public void setUrlMappings(Set<String> urlMappings) {
        this.urlMappings = urlMappings;
    }

    public Set<String> getDependencyMappings() {
        return dependencyMappings;
    }

    public void setDependencyMappings(Set<String> dependencyMappings) {
        this.dependencyMappings = dependencyMappings;
    }
}
