package org.keycloak.licenses.report;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.keycloak.licenses.spdx.License;
import org.keycloak.licenses.spdx.SpdxLicenses;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FreeMarkerReport {

    public void render(File templateFile, Map<License, Set<String>> licenseMap, SpdxLicenses spdxLicenses, Map<String, String> licenseContent, Writer writer) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setTemplateLoader(new FileTemplateLoader(templateFile.getParentFile()));
        cfg.setDefaultEncoding("UTF-8");

        Map<String, Object> data = new HashMap<>();
        data.put("licenseMap", licenseMap);
        data.put("spdxLicenses", spdxLicenses);
        data.put("licenseContent", licenseContent);

        Template template = cfg.getTemplate(templateFile.getName());
        template.process(data, writer);
    }

}
