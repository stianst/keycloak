package org.keycloak.licenses;

import org.keycloak.licenses.spdx.License;

import java.util.LinkedList;
import java.util.List;

public class Dependency {

    private String identifier;
    private List<DependencyLicenseInfo> licenseInfo = new LinkedList<>();

    public Dependency(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void addLicenseInfo(String name, String url) {
        licenseInfo.add(new DependencyLicenseInfo(name, url));
    }

    public List<DependencyLicenseInfo> getLicenseInfo() {
        return licenseInfo;
    }

    public static final class DependencyLicenseInfo {

        private String name;
        private String url;
        private License matchedLicense;

        public DependencyLicenseInfo(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public License getMatchedLicense() {
            return matchedLicense;
        }

        public void setMatchedLicense(License matchedLicense) {
            this.matchedLicense = matchedLicense;
        }
    }

}
