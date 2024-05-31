package org.keycloak.licenses.parsers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import org.keycloak.licenses.Dependency;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PnpmReportParser {

    private final File file;

    public PnpmReportParser(File file) {
        this.file = file;
    }

    public List<Dependency> parse() throws IOException {
        List<Dependency> list = new LinkedList<>();

        ObjectMapper om = new ObjectMapper();

        MapLikeType mapLikeType = om.getTypeFactory().constructMapLikeType(Map.class, String.class, PnpmDependency[].class);

        Map<String, PnpmDependency[]> report = om.readValue(file, mapLikeType);


        for (Map.Entry<String, PnpmDependency[]> e : report.entrySet()) {
            String license = e.getKey();
            for (PnpmDependency d : e.getValue()) {
                Dependency dependency = new Dependency(d.getName() + ":" + d.getVersion());
                dependency.addLicenseInfo(license, null);
                list.add(dependency);
            }
        }

        return list;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class PnpmDependency {

        private String name;
        private String version;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

    }


}
