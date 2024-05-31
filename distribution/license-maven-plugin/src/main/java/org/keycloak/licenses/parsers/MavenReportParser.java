package org.keycloak.licenses.parsers;

import org.keycloak.licenses.Dependency;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MavenReportParser {

    private final File file;

    public MavenReportParser(File file) {
        this.file = file;
    }

    public List<Dependency> parse() throws ParserConfigurationException, IOException, SAXException {
        List<Dependency> list = new LinkedList<>();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        document.getDocumentElement().normalize();

        NodeList dependencyNodeList = document.getElementsByTagName("dependency");

        for (int i = 0; i < dependencyNodeList.getLength(); i++) {
            Element dependencyElement = (Element) dependencyNodeList.item(i);

            String groupId = getTextContent(dependencyElement, "groupId");
            String artifactId = getTextContent(dependencyElement, "artifactId");
            String version = getTextContent(dependencyElement, "version");
            String gav = groupId + ":" + artifactId + ":" + version;

            Dependency dependency = new Dependency(gav);
            list.add(dependency);

            NodeList licensesNodeList = dependencyElement.getElementsByTagName("licenses");
            for (int j = 0; j < licensesNodeList.getLength(); j++) {
                Element licensesElement = (Element) licensesNodeList.item(j);
                NodeList licenseNodeList = licensesElement.getElementsByTagName("license");

                for (int k = 0; k < licenseNodeList.getLength(); k++) {
                    Element licenseElement = (Element) licenseNodeList.item(k);

                    String name = getTextContent(licenseElement, "name");
                    String url = getTextContent(licenseElement, "url");

                    dependency.addLicenseInfo(name, url);
                }
            }
        }

        return list;
    }

    private String getTextContent(Element e, String name) {
        NodeList list = e.getElementsByTagName(name);
        if (list.getLength() == 0) {
            return null;
        } else if (list.getLength() > 1) {
            throw new RuntimeException("multiple " + name + " found in " + e.getLocalName());
        } else {
            return list.item(0).getTextContent();
        }
    }


}
