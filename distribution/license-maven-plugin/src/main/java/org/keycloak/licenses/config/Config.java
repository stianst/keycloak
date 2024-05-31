package org.keycloak.licenses.config;

import org.keycloak.licenses.spdx.License;

import java.util.List;
import java.util.Set;

public class Config {

    private Set<String> cncfApproved;

    private List<License> additionalLicenses;

    private List<LicenseMapping> mappings;

    public Set<String> getCncfApproved() {
        return cncfApproved;
    }

    private List<String> excludedDependencies;

    public void setCncfApproved(Set<String> cncfApproved) {
        this.cncfApproved = cncfApproved;
    }

    public List<License> getAdditionalLicenses() {
        return additionalLicenses;
    }

    public void setAdditionalLicenses(List<License> additionalLicenses) {
        this.additionalLicenses = additionalLicenses;
    }

    public List<LicenseMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<LicenseMapping> mappings) {
        this.mappings = mappings;
    }

    public List<String> getExcludedDependencies() {
        return excludedDependencies;
    }

    public void setExcludedDependencies(List<String> excludedDependencies) {
        this.excludedDependencies = excludedDependencies;
    }
}
