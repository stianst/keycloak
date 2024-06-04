package org.keycloak.licenses;

import org.keycloak.licenses.spdx.License;

import java.util.Comparator;

public class DependencyLicenseInfoComparator implements Comparator<Dependency.DependencyLicenseInfo> {

    @Override
    public int compare(Dependency.DependencyLicenseInfo o1, Dependency.DependencyLicenseInfo o2) {
        int nameOrder = o1.getName().compareTo(o2.getName());

        License m1 = o1.getMatchedLicense();
        License m2 = o2.getMatchedLicense();

        if (m1 == null && m2 == null) {
            return nameOrder;
        } else if (m2 == null) {
            return 1;
        } else if (m1 == null) {
            return -1;
        } else if (m1.isCncfApproved() || m2.isCncfApproved()) {
            int v = Boolean.compare(m1.isCncfApproved(), m2.isCncfApproved());
            return v != 0 ? v : nameOrder;
        } else if (m1.isOsiApproved() || m2.isOsiApproved()) {
            int v = Boolean.compare(m1.isOsiApproved(), m2.isOsiApproved());
            return v != 0 ? v : nameOrder;
        }

        return nameOrder;
    }

}
