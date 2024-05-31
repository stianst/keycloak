package org.keycloak.licenses.spdx;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class License {

    private String reference;

    @JsonProperty("isDeprecatedLicenseId")
    private boolean deprecatedLicenseId;

    private String detailsUrl;

    private int referenceNumber;

    private String name;

    private String licenseId;

    private Set<String> seeAlso;

    @JsonProperty("isSpdxListed")
    private boolean spdxListed = true;

    @JsonProperty("isCncfApproved")
    private boolean cncfApproved;

    @JsonProperty("isOsiApproved")
    private boolean osiApproved;

    @JsonProperty("isFsfLibre")
    private boolean fsfLibre;

    private String licenseReference;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isDeprecatedLicenseId() {
        return deprecatedLicenseId;
    }

    public void setDeprecatedLicenseId(boolean deprecatedLicenseId) {
        this.deprecatedLicenseId = deprecatedLicenseId;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }

    public int getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(int referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }

    public Set<String> getSeeAlso() {
        return seeAlso;
    }

    public void setSeeAlso(Set<String> seeAlso) {
        this.seeAlso = seeAlso;
    }

    public boolean isSpdxListed() {
        return spdxListed;
    }

    public void setSpdxListed(boolean spdxListed) {
        this.spdxListed = spdxListed;
    }

    public boolean isCncfApproved() {
        return cncfApproved;
    }

    public void setCncfApproved(boolean cncfApproved) {
        this.cncfApproved = cncfApproved;
    }

    public boolean isOsiApproved() {
        return osiApproved;
    }

    public void setOsiApproved(boolean osiApproved) {
        this.osiApproved = osiApproved;
    }

    public boolean isFsfLibre() {
        return fsfLibre;
    }

    public void setFsfLibre(boolean fsfLibre) {
        this.fsfLibre = fsfLibre;
    }

    public String getLicenseReference() {
        return licenseReference;
    }

    public void setLicenseReference(String licenseReference) {
        this.licenseReference = licenseReference;
    }
}
