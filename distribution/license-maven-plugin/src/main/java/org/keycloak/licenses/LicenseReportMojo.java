package org.keycloak.licenses;

import freemarker.template.TemplateException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.List;


@Mojo(name = "license-report", defaultPhase = LifecyclePhase.INSTALL)
public class LicenseReportMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "additionalConfig", required = true)
    private File additionalConfig;

    @Parameter(property = "licenses", required = true)
    private File licenses;

    @Parameter(property = "mvnReports")
    private List<File> mvnReports;

    @Parameter(property = "pnpmReports")
    private List<File> pnpmReports;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File outputDir = project.getBasedir().toPath().resolve("target").resolve("licenses").toFile();

        getLog().info("Additional config: " + additionalConfig.getAbsolutePath());

        LicensesReport licensesReport = new LicensesReport(additionalConfig);

        for (File f : mvnReports) {
            try {
                getLog().info("Parsing Maven licenses from: " + f.getCanonicalFile().getAbsolutePath());
                licensesReport.parseMaven(f);
            } catch (Exception e) {
                throw new MojoFailureException("Failed to parse Maven license file", e);
            }
        }
        for (File f : pnpmReports) {
            try {
                getLog().info("Parsing PNPM licenses from: " + f.getCanonicalFile().getAbsolutePath());
                licensesReport.parsePnpm(f);
            } catch (Exception e) {
                throw new MojoFailureException("Failed to parse PNPM license file", e);
            }
        }

        try {
            licensesReport.createReport(outputDir, licenses, false);
            
            getLog().info("Report: " + new File(outputDir, "report.html").toURI().toURL());
            getLog().info("CNCF Report: " + new File(outputDir, "cncf-report.html").toURI().toURL());
        } catch (Exception e) {
            throw new MojoFailureException("Failed to generate report", e);
        }


    }
}
