package org.keycloak.licenses;

import freemarker.template.TemplateException;
import org.keycloak.licenses.parsers.MavenReportParser;
import org.keycloak.licenses.parsers.PnpmReportParser;
import org.keycloak.licenses.report.FreeMarkerReport;
import org.keycloak.licenses.spdx.License;
import org.keycloak.licenses.spdx.SpdxLicenses;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class LicensesReport {

    private final SpdxLicenses spdxLicenses;
    private final List<Dependency> dependencies;

    public LicensesReport(File config) {
        spdxLicenses = new SpdxLicenses(config);
        dependencies = new LinkedList<>();
    }

    public void parseMaven(File file) throws ParserConfigurationException, IOException, SAXException {
        MavenReportParser mavenReportParser = new MavenReportParser(file);
        dependencies.addAll(mavenReportParser.parse());
    }

    public void parsePnpm(File file) throws ParserConfigurationException, IOException, SAXException {
        PnpmReportParser pnpmReportParser = new PnpmReportParser(file);
        dependencies.addAll(pnpmReportParser.parse());
    }

    public void createReport(File outputDirectory, File licensesDirectory, boolean excludeCncfApproved, File... reports) throws IOException, TemplateException {
        if (!outputDirectory.isDirectory()) {
            outputDirectory.mkdirs();
        }

        Map<License, Set<String>> licenseMap = new TreeMap<>(Comparator.comparing(License::getName));
        List<License> unknownLicenses = new LinkedList<>();

        for (Dependency dependency : dependencies) {
            if (spdxLicenses.isExcluced(dependency.getIdentifier())) {
                continue;
            }

            List<License> licenses = new LinkedList<>();
            List<License> dependencyOverride = spdxLicenses.findByDependency(dependency.getIdentifier());
            if (dependencyOverride != null) {
                licenses.addAll(dependencyOverride);
            } else {
                for (Dependency.DependencyLicenseInfo licenseInfo : dependency.getLicenseInfo()) {
                    License license = spdxLicenses.findByNameOrLicenseId(licenseInfo.getName());
                    if (license == null && licenseInfo.getUrl() != null) {
                        license = spdxLicenses.findByUrl(licenseInfo.getUrl());
                    }

                    // Uncomment to show license and reference
//                    if (license != null && license.getLicenseReference() != null) {
//                        license = spdxLicenses.findByLicenseId(license.getLicenseReference());
//                    }

                    if (license != null) {
                        licenses.add(license);
                    } else {
                        License unknownLicense = unknownLicenses.stream().filter(l -> l.getName().equals(licenseInfo.getName()) || l.getReference() != null && l.getReference().equals(licenseInfo.getUrl())).findFirst().orElse(null);
                        if (unknownLicense == null) {
                            unknownLicense = new License();
                            unknownLicense.setName(licenseInfo.getName());
                            unknownLicense.setReference(licenseInfo.getUrl());
                            unknownLicense.setLicenseId("???");
                            unknownLicenses.add(unknownLicense);
                        }
                        licenses.add(unknownLicense);
                    }
                }
            }
            if (licenses.isEmpty()) {
                throw new RuntimeException("No license found for " + dependency.getIdentifier());
            } else {
                List<License> preferredLicenses = licenses.stream().filter(l -> l.isCncfApproved() || l.isOsiApproved()).collect(Collectors.toList());
                if (!preferredLicenses.isEmpty()) {
                    licenses = preferredLicenses;
                }

                for (License l : licenses) {
                    if (!licenseMap.containsKey(l)) {
                        licenseMap.put(l, new TreeSet<>());
                    }

                    licenseMap.get(l).add(dependency.getIdentifier());
                }
            }
        }

        if (excludeCncfApproved) {
            licenseMap.keySet().removeIf(License::isCncfApproved);
        }

        Map<String, String> licenseContent = new HashMap<>();

        for (Map.Entry<License, Set<String>> e : licenseMap.entrySet()) {
            License l = e.getKey();
            File licenseFile = new File(licensesDirectory, l.getLicenseId() + ".txt");
            try {
                licenseContent.put(l.getLicenseId(), new String(licenseFile.toURI().toURL().openStream().readAllBytes(), StandardCharsets.UTF_8));
            } catch (Exception ex) {
                licenseContent.put(l.getLicenseId(), "MISSING");
//                throw new RuntimeException("License file not found: " + licenseFile.getAbsolutePath() + " (" + l.getName() + ") " + "(" + String.join(", ", e.getValue()));
            }
        }

        for (File report : reports) {
            new FreeMarkerReport().render(report, licenseMap, spdxLicenses, licenseContent, new FileWriter(new File(outputDirectory, report.getName().replace(".ftl", ".html"))));
        }
    }

}
