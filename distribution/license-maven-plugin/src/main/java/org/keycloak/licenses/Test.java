package org.keycloak.licenses;

import org.keycloak.licenses.spdx.License;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        LicensesReport licensesReport = new LicensesReport(new File("/home/st/dev/keycloak/distribution/licenses/src/main/resources/config.json"));
        licensesReport.parseMaven(new File("/home/st/dev/keycloak/distribution/licenses/target/generated-resources/licenses.xml"));
        licensesReport.findMatches();

        for (Dependency d : licensesReport.getDependencies()) {

            License bestMatch = licensesReport.findBestMatch(d);


            if (bestMatch != null) {
//                System.out.println("BEST: " + bestMatch.getLicenseId());
            } else {
                System.out.println(d.getIdentifier());
                for (Dependency.DependencyLicenseInfo i : d.getLicenseInfo()) {
                    System.out.println("--> " + i.getName() + " --> " + i.getMatchedLicense());
                }
            }

//            for (Dependency.DependencyLicenseInfo i : d.getLicenseInfo()) {
//                if (i.getMatchedLicense() == null) {
//                    System.out.println(d.getIdentifier() + " --> " + i.getName());
//                }
//            }
        }


    }

}
